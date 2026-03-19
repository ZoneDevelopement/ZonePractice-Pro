package dev.nandi0813.practice.manager.profile.cosmetics.deatheffect;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.util.EntityHiderListener;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
    );

    private final String id;
    private final String defaultDisplayName;
    private final Material icon;

    DeathEffect(String id, String defaultDisplayName, Material icon) {
        this.id = id;
        this.defaultDisplayName = defaultDisplayName;
        this.icon = icon;
    }

    public String getDefaultDisplayName() { return defaultDisplayName; }

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

        List<Player> viewers = recipients.stream()
                .filter(player -> player != null && player.isOnline())
                .filter(player -> player.getWorld().equals(location.getWorld()))
                .collect(Collectors.toList());

        if (viewers.isEmpty()) {
            return;
        }

        List<ParticleSpec> particles = buildParticles(location);
        if (!particles.isEmpty()) {
            EntityHiderListener listener = EntityHiderListener.getInstance();
            for (Player viewer : viewers) {
                listener.allowNextParticlePackets(viewer, particles.size());
            }

            for (ParticleSpec particleSpec : particles) {
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
            }
        }

        playScopedSounds(location, viewers);
    }

    private List<ParticleSpec> buildParticles(Location location) {
        Vector3d position = toVector3d(location);
        switch (this) {
            case NONE -> {
                return List.of();
            }

            case FLAME -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.FLAME), position, 60, 0.4f, 0.4f, 0.4f, 0.05f),
                        spec(new Particle<>(ParticleTypes.LAVA), position, 15, 0.3f, 0.3f, 0.3f, 0.0f)
                );
            }

            case LIGHTNING -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.ELECTRIC_SPARK), position, 80, 0.5f, 0.5f, 0.5f, 0.1f)
                );
            }

            case FIREWORK -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.FIREWORK), position, 90, 0.4f, 0.4f, 0.4f, 0.2f),
                        spec(new Particle<>(ParticleTypes.EXPLOSION), position, 2, 0.2f, 0.2f, 0.2f, 0.0f)
                );
            }

            case EXPLOSION -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.EXPLOSION), position, 5, 0.3f, 0.3f, 0.3f, 0.0f),
                        spec(new Particle<>(ParticleTypes.SMOKE), position, 40, 0.5f, 0.5f, 0.5f, 0.08f),
                        spec(new Particle<>(ParticleTypes.LARGE_SMOKE), position, 20, 0.4f, 0.4f, 0.4f, 0.04f)
                );
            }

            case BLOOD -> {
                return List.of(
                        spec(dust(1.5f, Color.RED), position, 80, 0.4f, 0.4f, 0.4f, 0.0f),
                        spec(dust(2.0f, Color.fromRGB(139, 0, 0)), position, 30, 0.2f, 0.2f, 0.2f, 0.0f)
                );
            }

            case ENCHANT -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.ENCHANT), position, 200, 0.5f, 0.5f, 0.5f, 0.5f),
                        spec(new Particle<>(ParticleTypes.ENCHANTED_HIT), position, 60, 0.4f, 0.4f, 0.4f, 0.3f),
                        spec(new Particle<>(ParticleTypes.WITCH), position, 30, 0.4f, 0.4f, 0.4f, 0.0f)
                );
            }

            case ENDER -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.PORTAL), position, 150, 0.5f, 0.5f, 0.5f, 1.0f),
                        spec(new Particle<>(ParticleTypes.SMOKE), position, 30, 0.4f, 0.4f, 0.4f, 0.05f),
                        spec(new Particle<>(ParticleTypes.WITCH), position, 20, 0.4f, 0.4f, 0.4f, 0.0f)
                );
            }

            case HEARTS -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.HEART), position, 25, 0.5f, 0.5f, 0.5f, 0.1f),
                        spec(dust(1.5f, Color.fromRGB(255, 105, 180)), position, 40, 0.4f, 0.4f, 0.4f, 0.0f)
                );
            }

            case ICE -> {
                return List.of(
                        spec(new Particle<>(ParticleTypes.SNOWFLAKE), position, 60, 0.5f, 0.5f, 0.5f, 0.1f),
                        spec(dust(1.5f, Color.fromRGB(173, 216, 230)), position, 30, 0.4f, 0.4f, 0.4f, 0.0f),
                        spec(new Particle<>(ParticleTypes.ITEM_SNOWBALL), position, 20, 0.4f, 0.3f, 0.4f, 0.05f)
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
            case FIREWORK -> viewers.forEach(player -> player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f));
            case EXPLOSION -> viewers.forEach(player -> player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.0f));
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