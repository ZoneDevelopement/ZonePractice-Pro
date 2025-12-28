package dev.nandi0813.practice.Manager.Fight.Util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import dev.nandi0813.practice.Manager.Fight.Event.EventManager;
import dev.nandi0813.practice.Manager.Fight.Event.Events.Duel.Brackets.Brackets;
import dev.nandi0813.practice.Manager.Fight.Event.Events.Duel.Interface.DuelFight;
import dev.nandi0813.practice.Manager.Fight.Event.Interface.Event;
import dev.nandi0813.practice.Manager.Fight.FFA.FFA.FFA;
import dev.nandi0813.practice.Manager.Fight.FFA.FFAManager;
import dev.nandi0813.practice.Manager.Fight.Match.Match;
import dev.nandi0813.practice.Manager.Fight.Match.MatchManager;
import dev.nandi0813.practice.Manager.Profile.Enum.ProfileStatus;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileManager;
import dev.nandi0813.practice.Manager.Server.ServerManager;
import dev.nandi0813.practice.Manager.Server.WorldEnum;
import dev.nandi0813.practice.ZonePractice;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    private final ConcurrentHashMap<Integer, Location> entityLocations = new ConcurrentHashMap<>();

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
        Player player = e.getPlayer();

        if (!this.checkPlayer(player)) {
            effectTo.remove(player);
            entityLocations.remove(player.getEntityId());
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
            e.setCancelled(true);
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
                    if (!match.getPlayers().contains((Player) entity)) {
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
