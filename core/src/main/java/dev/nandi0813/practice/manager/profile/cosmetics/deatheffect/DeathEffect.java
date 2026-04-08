package dev.nandi0813.practice.manager.profile.cosmetics.deatheffect;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.util.EntityHiderListener;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Represents a kill effect cosmetic that plays at the victim's location on death.
 * Every effect is fully configurable via guis.yml under GUIS.COSMETICS.DEATH-EFFECTS.
 */
@Getter
public enum DeathEffect {

    NONE(
            "none",
            "None",
            Material.BARRIER
    ),
    FLAME(
            "flame",
            "Flame",
            Material.BLAZE_POWDER
    ),
    LIGHTNING(
            "lightning",
            "Lightning",
            Material.LIGHTNING_ROD
    ),
    FIREWORK(
            "firework",
            "Firework",
            Material.FIREWORK_ROCKET
    ),
    EXPLOSION(
            "explosion",
            "Explosion",
            Material.TNT
    ),
    BLOOD(
            "blood",
            "Blood",
            Material.REDSTONE
    ),
    ENCHANT(
            "enchant",
            "Enchant",
            Material.ENCHANTING_TABLE
    ),
    ENDER(
            "ender",
            "Ender",
            Material.ENDER_PEARL
    ),
    HEARTS(
            "hearts",
            "Hearts",
            Material.PINK_DYE
    ),
    ICE(
            "ice",
            "Ice",
            Material.PACKED_ICE
    ),
    SUPERNOVA(
            "supernova",
            "Supernova",
            Material.NETHER_STAR
    ),
    VOIDSTORM(
            "voidstorm",
            "Voidstorm",
            Material.ENDER_EYE
    ),
    PHOENIX(
            "phoenix",
            "Phoenix",
            Material.TOTEM_OF_UNDYING
    ),
    COMET(
            "comet",
            "Comet",
            Material.FIRE_CHARGE
    );

    private final String id;
    private final String defaultDisplayName;
    private final Material icon;

    DeathEffect(String id, String defaultDisplayName, Material icon) {
        this.id = id;
        this.defaultDisplayName = defaultDisplayName;
        this.icon = icon;
    }

    /** Display name read from guis.yml with fallback to default. */
    public String getDisplayName() {
        String key = "GUIS.COSMETICS.DEATH-EFFECTS.ENTRIES." + this.name() + ".DISPLAY-NAME";
        String val = GUIFile.getConfig().getString(key);
        return (val != null && !val.isBlank()) ? val : defaultDisplayName;
    }

    /** Icon material read from guis.yml with fallback. */
    public Material getConfiguredIcon() {
        String key = "GUIS.COSMETICS.DEATH-EFFECTS.ENTRIES." + this.name() + ".ICON";
        String val = GUIFile.getConfig().getString(key);
        if (val != null && !val.isBlank()) {
            try {
                return Material.valueOf(val.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {}
        }
        return icon;
    }

    public static String getPermissionNode(String id) {
        return "zpp.cosmetics.deatheffect." + id;
    }

    /**
     * Plays this kill effect at the given location.
     * Called from Match.killPlayer and FFA.killPlayer after a kill is confirmed.
     */
    public void play(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        play(location, location.getWorld().getPlayers());
    }

    public void play(Location location, Collection<Player> recipients) {
        if (location == null || location.getWorld() == null || recipients == null || recipients.isEmpty()) {
            return;
        }

        List<Player> viewers = filterViewers(location, recipients);

        if (viewers.isEmpty()) {
            return;
        }

        if (this == LIGHTNING) {
            playLightningSequence(location, recipients);
            return;
        }

        if (this == SUPERNOVA) {
            playSupernovaSequence(location, recipients);
            return;
        }

        if (this == VOIDSTORM) {
            playVoidstormSequence(location, recipients);
            return;
        }

        if (this == PHOENIX) {
            playPhoenixSequence(location, recipients);
            return;
        }

        if (this == COMET) {
            playCometSequence(location, recipients);
            return;
        }

        List<ParticleSpec> particles = buildParticles(location);
        sendParticles(particles, viewers);

        playScopedSounds(location, viewers);
    }

    private List<Player> filterViewers(Location location, Collection<Player> recipients) {
        return recipients.stream()
                .filter(player -> player != null && player.isOnline())
                .filter(player -> player.getWorld().equals(location.getWorld()))
                .collect(Collectors.toList());
    }

    private void playLightningSequence(Location baseLocation, Collection<Player> recipients) {
        Location center = baseLocation.clone().add(0.0D, 1.0D, 0.0D);
        List<Player> viewers = filterViewers(baseLocation, recipients);
        if (viewers.isEmpty()) {
            return;
        }

        sendParticles(List.of(
                spec(new Particle<>(ParticleTypes.ELECTRIC_SPARK), toVector3d(center), 140, 0.45f, 0.95f, 0.45f, 0.22f),
                spec(new Particle<>(ParticleTypes.EXPLOSION), toVector3d(center), 2, 0.12f, 0.12f, 0.12f, 0.0f),
                spec(dust(1.8f, Color.fromRGB(230, 245, 255)), toVector3d(center), 55, 0.35f, 0.85f, 0.35f, 0.0f)
        ), viewers);
        viewers.forEach(player -> {
            player.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.3f, 1.0f);
            player.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.4f);
        });

        if (!ZonePractice.getInstance().isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            List<Player> liveViewers = filterViewers(baseLocation, recipients);
            if (liveViewers.isEmpty()) {
                return;
            }

            sendParticles(List.of(
                    spec(new Particle<>(ParticleTypes.ELECTRIC_SPARK), toVector3d(center), 95, 0.30f, 1.20f, 0.30f, 0.18f),
                    spec(new Particle<>(ParticleTypes.LARGE_SMOKE), toVector3d(center), 24, 0.40f, 0.45f, 0.40f, 0.04f),
                    spec(dust(1.4f, Color.fromRGB(160, 210, 255)), toVector3d(center), 40, 0.30f, 0.70f, 0.30f, 0.0f)
            ), liveViewers);
            liveViewers.forEach(player -> player.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.75f, 1.15f));
        }, 2L);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            List<Player> liveViewers = filterViewers(baseLocation, recipients);
            if (liveViewers.isEmpty()) {
                return;
            }

            sendParticles(List.of(
                    spec(new Particle<>(ParticleTypes.ELECTRIC_SPARK), toVector3d(center), 60, 0.55f, 0.30f, 0.55f, 0.08f),
                    spec(new Particle<>(ParticleTypes.SMOKE), toVector3d(center), 30, 0.50f, 0.25f, 0.50f, 0.03f)
            ), liveViewers);
            liveViewers.forEach(player -> player.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.45f, 1.35f));
        }, 4L);
    }

    private void playSupernovaSequence(Location baseLocation, Collection<Player> recipients) {
        Location center = baseLocation.clone().add(0.0D, 0.9D, 0.0D);
        List<Player> viewers = filterViewers(baseLocation, recipients);
        if (viewers.isEmpty()) {
            return;
        }

        sendParticles(List.of(
                spec(dust(2.8f, Color.fromRGB(255, 235, 120)), toVector3d(center), 100, 0.45f, 0.45f, 0.45f, 0.0f),
                spec(new Particle<>(ParticleTypes.FIREWORK), toVector3d(center), 220, 1.05f, 1.20f, 1.05f, 0.28f),
                spec(new Particle<>(ParticleTypes.ELECTRIC_SPARK), toVector3d(center), 120, 0.75f, 0.95f, 0.75f, 0.18f)
        ), viewers);
        viewers.forEach(player -> {
            player.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 0.85f);
            player.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.45f);
        });

        runLater(3L, () -> {
            List<Player> liveViewers = filterViewers(baseLocation, recipients);
            if (liveViewers.isEmpty()) {
                return;
            }

            sendParticles(List.of(
                    spec(new Particle<>(ParticleTypes.EXPLOSION), toVector3d(center), 14, 0.55f, 0.55f, 0.55f, 0.0f),
                    spec(new Particle<>(ParticleTypes.FLAME), toVector3d(center), 180, 1.35f, 1.10f, 1.35f, 0.16f),
                    spec(new Particle<>(ParticleTypes.LARGE_SMOKE), toVector3d(center), 70, 0.95f, 0.85f, 0.95f, 0.07f)
            ), liveViewers);
            liveViewers.forEach(player -> {
                player.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.9f);
                player.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
            });
        });
    }

    private void playVoidstormSequence(Location baseLocation, Collection<Player> recipients) {
        Location center = baseLocation.clone().add(0.0D, 0.7D, 0.0D);
        List<Player> viewers = filterViewers(baseLocation, recipients);
        if (viewers.isEmpty()) {
            return;
        }

        sendParticles(List.of(
                spec(new Particle<>(ParticleTypes.PORTAL), toVector3d(center), 260, 1.25f, 1.30f, 1.25f, 1.45f),
                spec(new Particle<>(ParticleTypes.WITCH), toVector3d(center), 130, 0.95f, 1.10f, 0.95f, 0.0f),
                spec(dust(2.1f, Color.fromRGB(120, 40, 200)), toVector3d(center), 75, 0.85f, 0.90f, 0.85f, 0.0f)
        ), viewers);
        viewers.forEach(player -> {
            player.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.65f);
            player.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.85f, 1.25f);
        });

        runLater(4L, () -> {
            List<Player> liveViewers = filterViewers(baseLocation, recipients);
            if (liveViewers.isEmpty()) {
                return;
            }

            sendParticles(List.of(
                    spec(new Particle<>(ParticleTypes.PORTAL), toVector3d(center), 220, 1.15f, 0.80f, 1.15f, 1.35f),
                    spec(new Particle<>(ParticleTypes.SMOKE), toVector3d(center), 90, 0.80f, 0.70f, 0.80f, 0.03f),
                    spec(new Particle<>(ParticleTypes.EXPLOSION), toVector3d(center), 6, 0.35f, 0.35f, 0.35f, 0.0f)
            ), liveViewers);
            liveViewers.forEach(player -> player.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.45f, 1.45f));
        });
    }

    private void playPhoenixSequence(Location baseLocation, Collection<Player> recipients) {
        Location center = baseLocation.clone().add(0.0D, 0.85D, 0.0D);
        List<Player> viewers = filterViewers(baseLocation, recipients);
        if (viewers.isEmpty()) {
            return;
        }

        sendParticles(List.of(
                spec(new Particle<>(ParticleTypes.FLAME), toVector3d(center), 260, 1.20f, 1.25f, 1.20f, 0.15f),
                spec(dust(2.5f, Color.fromRGB(255, 120, 20)), toVector3d(center), 100, 0.95f, 0.95f, 0.95f, 0.0f),
                spec(dust(1.9f, Color.fromRGB(255, 215, 90)), toVector3d(center), 85, 0.85f, 1.05f, 0.85f, 0.0f)
        ), viewers);
        viewers.forEach(player -> {
            player.playSound(center, Sound.ITEM_TOTEM_USE, 1.0f, 1.05f);
            player.playSound(center, Sound.ENTITY_BLAZE_SHOOT, 0.9f, 1.2f);
        });

        runLater(3L, () -> {
            List<Player> liveViewers = filterViewers(baseLocation, recipients);
            if (liveViewers.isEmpty()) {
                return;
            }

            sendParticles(List.of(
                    spec(new Particle<>(ParticleTypes.FIREWORK), toVector3d(center), 130, 0.80f, 1.40f, 0.80f, 0.2f),
                    spec(new Particle<>(ParticleTypes.SOUL_FIRE_FLAME), toVector3d(center), 90, 0.70f, 0.85f, 0.70f, 0.04f),
                    spec(new Particle<>(ParticleTypes.EXPLOSION), toVector3d(center), 5, 0.25f, 0.25f, 0.25f, 0.0f)
            ), liveViewers);
            liveViewers.forEach(player -> player.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.95f, 1.35f));
        });
    }

    private void playCometSequence(Location baseLocation, Collection<Player> recipients) {
        Location center = baseLocation.clone().add(0.0D, 0.8D, 0.0D);
        List<Player> viewers = filterViewers(baseLocation, recipients);
        if (viewers.isEmpty()) {
            return;
        }

        sendParticles(List.of(
                spec(new Particle<>(ParticleTypes.ELECTRIC_SPARK), toVector3d(center), 150, 1.30f, 0.95f, 1.30f, 0.14f),
                spec(dust(2.2f, Color.fromRGB(140, 220, 255)), toVector3d(center), 90, 1.10f, 0.90f, 1.10f, 0.0f),
                spec(new Particle<>(ParticleTypes.FIREWORK), toVector3d(center), 150, 1.20f, 1.35f, 1.20f, 0.28f)
        ), viewers);
        viewers.forEach(player -> {
            player.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 0.9f);
            player.playSound(center, Sound.ENTITY_BREEZE_SHOOT, 1.0f, 0.6f);
        });

        runLater(3L, () -> {
            List<Player> liveViewers = filterViewers(baseLocation, recipients);
            if (liveViewers.isEmpty()) {
                return;
            }

            sendParticles(List.of(
                    spec(new Particle<>(ParticleTypes.EXPLOSION), toVector3d(center), 9, 0.45f, 0.45f, 0.45f, 0.0f),
                    spec(new Particle<>(ParticleTypes.LARGE_SMOKE), toVector3d(center), 80, 0.90f, 0.80f, 0.90f, 0.05f),
                    spec(new Particle<>(ParticleTypes.END_ROD), toVector3d(center), 95, 0.80f, 1.00f, 0.80f, 0.01f)
            ), liveViewers);
            liveViewers.forEach(player -> player.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.85f, 1.2f));
        });
    }

    private static void runLater(long delay, Runnable runnable) {
        if (!ZonePractice.getInstance().isEnabled()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), runnable, delay);
    }

    private void sendParticles(List<ParticleSpec> particles, List<Player> viewers) {
        if (particles == null || particles.isEmpty() || viewers == null || viewers.isEmpty()) {
            return;
        }

        EntityHiderListener listener = EntityHiderListener.getInstance();
        Vector3d center = particles.getFirst().position;
        for (Player viewer : viewers) {
            listener.allowNextParticlePackets(viewer, particles.size());
            listener.allowNextParticleBurst(viewer, center.getX(), center.getY(), center.getZ(), particles.size(), 500L, 4.5D);
        }

        for (ParticleSpec particleSpec : particles) {
            try {
                WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
                        particleSpec.particle,
                        false,
                        particleSpec.position,
                        particleSpec.offset,
                        particleSpec.speed,
                        particleSpec.count,
                        true
                );

                for (Player viewer : viewers) {
                    PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
                }
            } catch (Exception ex) {
                ZonePractice.getInstance().getLogger().warning("Skipped incompatible particle packet for death effect: " + this.name()
                        + " (particle=" + particleSpec.particle + ") due to: " + ex.getClass().getSimpleName());
            }
        }
    }


    private List<ParticleSpec> buildParticles(Location location) {
        Vector3d position = toVector3d(location.clone().add(0.0D, 0.6D, 0.0D));
        switch (this) {
            case NONE -> {
                return List.of();
            }

            case FLAME -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.FLAME), position, 120, 0.85f, 0.95f, 0.85f, 0.09f),
                        spec(new Particle<>(ParticleTypes.LAVA), position, 35, 0.60f, 0.55f, 0.60f, 0.0f),
                        spec(new Particle<>(ParticleTypes.SMOKE), position, 55, 0.80f, 0.80f, 0.80f, 0.06f)
                );
            }

            case LIGHTNING -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.ELECTRIC_SPARK), position, 150, 0.70f, 0.90f, 0.70f, 0.15f),
                        spec(dust(1.8f, Color.fromRGB(220, 245, 255)), position, 60, 0.50f, 0.70f, 0.50f, 0.0f),
                        spec(new Particle<>(ParticleTypes.EXPLOSION), position, 2, 0.20f, 0.20f, 0.20f, 0.0f)
                );
            }

            case FIREWORK -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.FIREWORK), position, 180, 0.95f, 1.05f, 0.95f, 0.32f),
                        spec(new Particle<>(ParticleTypes.EXPLOSION), position, 4, 0.35f, 0.35f, 0.35f, 0.0f),
                        spec(dust(1.5f, Color.fromRGB(255, 90, 210)), position, 55, 0.75f, 0.85f, 0.75f, 0.0f)
                );
            }

            case EXPLOSION -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.EXPLOSION), position, 8, 0.45f, 0.45f, 0.45f, 0.0f),
                        spec(new Particle<>(ParticleTypes.SMOKE), position, 85, 0.95f, 0.95f, 0.95f, 0.11f),
                        spec(new Particle<>(ParticleTypes.LARGE_SMOKE), position, 40, 0.80f, 0.80f, 0.80f, 0.06f)
                );
            }

            case BLOOD -> {
                return List.of(
                        spec(dust(1.8f, Color.RED), position, 130, 0.85f, 0.75f, 0.85f, 0.0f),
                        spec(dust(2.3f, Color.fromRGB(139, 0, 0)), position, 55, 0.60f, 0.60f, 0.60f, 0.0f),
                        spec(new Particle<>(ParticleTypes.SMOKE), position, 25, 0.45f, 0.40f, 0.45f, 0.02f)
                );
            }

            case ENCHANT -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.ENCHANT), position, 300, 0.95f, 1.05f, 0.95f, 0.70f),
                        spec(new Particle<>(ParticleTypes.ENCHANTED_HIT), position, 95, 0.80f, 0.80f, 0.80f, 0.38f),
                        spec(new Particle<>(ParticleTypes.WITCH), position, 55, 0.75f, 0.75f, 0.75f, 0.0f)
                );
            }

            case ENDER -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.PORTAL), position, 240, 0.95f, 1.05f, 0.95f, 1.25f),
                        spec(new Particle<>(ParticleTypes.SMOKE), position, 55, 0.75f, 0.75f, 0.75f, 0.08f),
                        spec(new Particle<>(ParticleTypes.WITCH), position, 45, 0.70f, 0.70f, 0.70f, 0.0f)
                );
            }

            case HEARTS -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.HEART), position, 55, 0.95f, 0.95f, 0.95f, 0.2f),
                        spec(dust(1.8f, Color.fromRGB(255, 105, 180)), position, 90, 0.80f, 0.80f, 0.80f, 0.0f),
                        spec(dust(1.4f, Color.fromRGB(255, 190, 220)), position, 45, 0.65f, 0.70f, 0.65f, 0.0f)
                );
            }

            case ICE -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.SNOWFLAKE), position, 130, 0.95f, 0.95f, 0.95f, 0.16f),
                        spec(dust(1.8f, Color.fromRGB(173, 216, 230)), position, 65, 0.80f, 0.80f, 0.80f, 0.0f),
                        spec(new Particle<>(ParticleTypes.ITEM_SNOWBALL), position, 45, 0.70f, 0.60f, 0.70f, 0.08f)
                );
            }

            case SUPERNOVA -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.FIREWORK), position, 220, 1.15f, 1.25f, 1.15f, 0.3f),
                        spec(new Particle<>(ParticleTypes.EXPLOSION), position, 12, 0.45f, 0.45f, 0.45f, 0.0f),
                        spec(dust(2.5f, Color.fromRGB(255, 220, 90)), position, 95, 0.90f, 0.95f, 0.90f, 0.0f)
                );
            }

            case VOIDSTORM -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.PORTAL), position, 260, 1.20f, 1.25f, 1.20f, 1.4f),
                        spec(new Particle<>(ParticleTypes.WITCH), position, 170, 1.05f, 0.80f, 1.05f, 0.0f),
                        spec(new Particle<>(ParticleTypes.WITCH), position, 120, 0.95f, 0.95f, 0.95f, 0.0f)
                );
            }

            case PHOENIX -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.FLAME), position, 250, 1.15f, 1.20f, 1.15f, 0.14f),
                        spec(new Particle<>(ParticleTypes.SOUL_FIRE_FLAME), position, 90, 0.75f, 0.85f, 0.75f, 0.04f),
                        spec(dust(2.2f, Color.fromRGB(255, 130, 40)), position, 90, 0.85f, 0.95f, 0.85f, 0.0f)
                );
            }

            case COMET -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.ELECTRIC_SPARK), position, 150, 1.30f, 0.95f, 1.30f, 0.15f),
                        spec(new Particle<>(ParticleTypes.END_ROD), position, 95, 0.85f, 0.95f, 0.85f, 0.01f),
                        spec(new Particle<>(ParticleTypes.EXPLOSION), position, 8, 0.40f, 0.40f, 0.40f, 0.0f)
                );
            }
        }

        return List.of();
    }

    private void playScopedSounds(Location location, List<Player> viewers) {
        if (viewers.isEmpty()) {
            return;
        }

        switch (this) {
            case LIGHTNING -> viewers.forEach(player -> player.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f));
            case FIREWORK -> viewers.forEach(player -> {
                player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.05f, 1.0f);
                player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.8f, 1.2f);
            });
            case EXPLOSION -> viewers.forEach(player -> {
                player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.55f, 0.7f);
            });
            case FLAME -> viewers.forEach(player -> player.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 0.9f, 1.1f));
            case ENDER -> viewers.forEach(player -> player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.85f, 1.15f));
            case ICE -> viewers.forEach(player -> player.playSound(location, Sound.BLOCK_GLASS_BREAK, 0.75f, 1.35f));
            case SUPERNOVA -> viewers.forEach(player -> {
                player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.95f);
                player.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.4f);
            });
            case VOIDSTORM -> viewers.forEach(player -> {
                player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.95f, 0.75f);
                player.playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.85f, 1.1f);
            });
            case PHOENIX -> viewers.forEach(player -> {
                player.playSound(location, Sound.ITEM_TOTEM_USE, 0.95f, 1.05f);
                player.playSound(location, Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.25f);
            });
            case COMET -> viewers.forEach(player -> {
                player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 0.95f, 0.9f);
                player.playSound(location, Sound.ENTITY_BREEZE_IDLE_AIR, 0.75f, 1.4f);
            });
            default -> {
            }
        }
    }

    private static Particle<?> dust(float scale, Color color) {
        return new Particle<>(ParticleTypes.DUST,
                new ParticleDustData(scale, color.getRed(), color.getGreen(), color.getBlue()));
    }

    private static Vector3d toVector3d(Location location) {
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    private static ParticleSpec spec(Particle<?> particle, Vector3d position, int count,
                                     float offsetX, float offsetY, float offsetZ, float speed) {
        return new ParticleSpec(particle, position, new Vector3f(offsetX, offsetY, offsetZ), speed, count);
    }

    private record ParticleSpec(Particle<?> particle, Vector3d position, Vector3f offset, float speed, int count) {
    }

    public static DeathEffect fromId(String id) {
        if (id == null || id.isBlank()) return NONE;
        String normalized = id.toLowerCase(Locale.ROOT);
        for (DeathEffect ke : values()) {
            if (ke.id.equals(normalized)) return ke;
        }
        return NONE;
    }
}