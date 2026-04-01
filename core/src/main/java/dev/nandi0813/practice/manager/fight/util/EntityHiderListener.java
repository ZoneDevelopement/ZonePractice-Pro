package dev.nandi0813.practice.manager.fight.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.Brackets;
import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelFight;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.manager.server.WorldEnum;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityHiderListener implements PacketListener, Listener {

    private static EntityHiderListener instance;

    public static EntityHiderListener getInstance() {
        if (instance == null)
            instance = new EntityHiderListener();
        return instance;
    }

    protected EntityHiderListener() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
        PacketEvents.getAPI().getEventManager().registerListener(
                this, PacketListenerPriority.NORMAL);
    }

    protected final Set<Player> effectTo = new HashSet<>();
    private final ConcurrentHashMap<java.util.UUID, AtomicInteger> allowedParticlePackets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<java.util.UUID, CopyOnWriteArrayList<ParticleBurstPermit>> allowedParticleBursts = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Integer, Location> entityLocations = new ConcurrentHashMap<>();

    public void allowNextParticlePackets(Player player, int packetCount) {
        if (player == null || packetCount <= 0) {
            return;
        }

        allowedParticlePackets
                .computeIfAbsent(player.getUniqueId(), ignored -> new AtomicInteger())
                .addAndGet(packetCount);
    }

    public void allowNextParticleBurst(Player player, double x, double y, double z, int packetCount, long ttlMillis, double radius) {
        if (player == null || packetCount <= 0 || ttlMillis <= 0L || radius <= 0.0D) {
            return;
        }

        allowedParticleBursts
                .computeIfAbsent(player.getUniqueId(), ignored -> new CopyOnWriteArrayList<>())
                .add(new ParticleBurstPermit(x, y, z, radius * radius, System.currentTimeMillis() + ttlMillis, packetCount));
    }

    private boolean consumeAllowedParticlePacket(Player player) {
        AtomicInteger allowance = allowedParticlePackets.get(player.getUniqueId());
        if (allowance == null) {
            return false;
        }

        int left = allowance.decrementAndGet();
        if (left <= 0) {
            allowedParticlePackets.remove(player.getUniqueId());
        }
        return true;
    }

    private boolean consumeAllowedParticleBurst(Player player, WrapperPlayServerParticle particleWrapper) {
        CopyOnWriteArrayList<ParticleBurstPermit> permits = allowedParticleBursts.get(player.getUniqueId());
        if (permits == null || permits.isEmpty()) {
            return false;
        }

        Vector3d position = particleWrapper.getPosition();
        long now = System.currentTimeMillis();
        boolean consumed = false;

        for (ParticleBurstPermit permit : permits) {
            if (permit.isExpired(now)) {
                permits.remove(permit);
                continue;
            }

            if (!permit.matches(position)) {
                continue;
            }

            if (permit.consume()) {
                consumed = true;
            }

            if (permit.isDepleted()) {
                permits.remove(permit);
            }

            if (consumed) {
                break;
            }
        }

        if (permits.isEmpty()) {
            allowedParticleBursts.remove(player.getUniqueId());
        }

        return consumed;
    }

    private boolean checkPlayer(Player player) {
        if (!ServerManager.getInstance().getInWorld().containsKey(player)) {
            return false;
        }

        WorldEnum worldEnum = ServerManager.getInstance().getInWorld().get(player);
        if (worldEnum == null) {
            return false;
        }

        switch (worldEnum) {
            case LOBBY:
            case OTHER:
                return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        switch (profile.getStatus()) {
            case MATCH:
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                if (match != null && !match.getLadder().isBuild()) {
                    return true;
                }
            case EVENT:
                Event event = EventManager.getInstance().getEventByPlayer(player);
                if (event instanceof Brackets) {
                    return true;
                }
            case SPECTATE:
                Match spectateMatch = MatchManager.getInstance().getLiveMatchBySpectator(player);
                if (spectateMatch != null && !spectateMatch.getLadder().isBuild()) {
                    return true;
                }
                Event spectateEvent = EventManager.getInstance().getEventBySpectator(player);
                if (spectateEvent instanceof Brackets) {
                    return true;
                }
            default:
                return false;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        int entityID = player.getEntityId();

        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () -> {
            if (this.checkPlayer(player)) {
                entityLocations.put(entityID, player.getLocation());
            } else {
                entityLocations.remove(entityID);
            }
        });
    }

    @Override
    public void onPacketSend(PacketSendEvent e) {
        @Nullable Player player = e.getPlayer();

        //noinspection
        if (player == null) {
            return;
        }

        if (!this.checkPlayer(player)) {
            effectTo.remove(player);
            entityLocations.remove(player.getEntityId());
            allowedParticlePackets.remove(player.getUniqueId());
            allowedParticleBursts.remove(player.getUniqueId());
            return;
        }

        if (e.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
            WrapperPlayServerSpawnEntity event = new WrapperPlayServerSpawnEntity(e);

            Entity entity = SpigotConversionUtil.getEntityById(player.getWorld(), event.getEntityId());
            if (entity instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player shooter) {
                    if (!player.canSee(shooter)) {
                        e.setCancelled(true);
                    }
                }
            }
        } else if (e.getPacketType() == PacketType.Play.Server.EFFECT) {
            if (!effectTo.contains(player)) {
                e.setCancelled(true);
                return;
            }

            effectTo.remove(player);
        } else if (e.getPacketType() == PacketType.Play.Server.PARTICLE) {
            WrapperPlayServerParticle particleWrapper = new WrapperPlayServerParticle(e);
            if (!consumeAllowedParticleBurst(player, particleWrapper) && !consumeAllowedParticlePacket(player)) {
                e.setCancelled(true);
            }
        } else if (e.getPacketType() == PacketType.Play.Server.SOUND_EFFECT) {
            WrapperPlayServerSoundEffect soundWrapper = new WrapperPlayServerSoundEffect(e);
            Vector3i pos = soundWrapper.getEffectPosition();
            Location location = new Location(player.getWorld(), (double) pos.getX() / 8, (double) pos.getY() / 8, (double) pos.getZ() / 8);

            int closestEntityID = -1;
            double closestDistanceSquared = Double.MAX_VALUE;

            for (Map.Entry<Integer, Location> entry : entityLocations.entrySet()) {
                if (location.getWorld() != entry.getValue().getWorld()) {
                    continue;
                }

                double distanceSquared = entry.getValue().distanceSquared(location);

                if (distanceSquared < closestDistanceSquared) {
                    closestDistanceSquared = distanceSquared;
                    closestEntityID = entry.getKey();
                }
            }

            if (closestEntityID != -1) {
                Entity closestEntity = SpigotConversionUtil.getEntityById(player.getWorld(), closestEntityID);
                if (closestEntity instanceof Player target) {
                    if (!player.canSee(target)) {
                        e.setCancelled(true);
                    }
                }
            }
        } else if (e.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
            WrapperPlayServerEntityEffect event = new WrapperPlayServerEntityEffect(e);
            Entity entity = SpigotConversionUtil.getEntityById(player.getWorld(), event.getEntityId());
            if (entity instanceof Player target) {
                if (!player.canSee(target)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    private static final class ParticleBurstPermit {
        private final double x;
        private final double y;
        private final double z;
        private final double radiusSquared;
        private final long expiresAtMillis;
        private final AtomicInteger remaining;

        private ParticleBurstPermit(double x, double y, double z, double radiusSquared, long expiresAtMillis, int packetCount) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radiusSquared = radiusSquared;
            this.expiresAtMillis = expiresAtMillis;
            this.remaining = new AtomicInteger(packetCount);
        }

        private boolean isExpired(long now) {
            return now > expiresAtMillis;
        }

        private boolean matches(Vector3d position) {
            double dx = position.getX() - x;
            double dy = position.getY() - y;
            double dz = position.getZ() - z;
            return (dx * dx) + (dy * dy) + (dz * dz) <= radiusSquared;
        }

        private boolean consume() {
            return remaining.decrementAndGet() >= 0;
        }

        private boolean isDepleted() {
            return remaining.get() <= 0;
        }
    }

    @EventHandler ( ignoreCancelled = true, priority = EventPriority.MONITOR )
    public void onPotionSplash(PotionSplashEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();
        switch (profileStatus) {
            case MATCH:
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                if (match == null) return;

                for (LivingEntity entity : e.getAffectedEntities()) {
                    if (!(entity instanceof Player livingPlayer)) continue;

                    if (!match.getPlayers().contains(livingPlayer)) {
                        e.setIntensity(entity, 0);
                    }
                }

                if (!match.getLadder().isBuild()) {
                    effectTo.addAll(match.getPlayers());
                    effectTo.addAll(match.getSpectators());
                }
                break;
            case FFA:
                FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
                if (ffa == null) return;

                for (LivingEntity entity : e.getAffectedEntities()) {
                    if (!ffa.getPlayers().containsKey((Player) entity)) {
                        e.setIntensity(entity, 0);
                    }
                }
                break;
            case EVENT:
                Event event = EventManager.getInstance().getEventByPlayer(player);
                if (!(event instanceof Brackets brackets)) {
                    return;
                }

                DuelFight duelFight = brackets.getFight(player);
                if (duelFight == null)
                    return;

                for (LivingEntity entity : e.getAffectedEntities()) {
                    if (!duelFight.getPlayers().contains((Player) entity)) {
                        e.setIntensity(entity, 0);
                    }

                    effectTo.addAll(duelFight.getPlayers());
                    effectTo.addAll(duelFight.getSpectators());
                }
                break;
        }
    }

}
