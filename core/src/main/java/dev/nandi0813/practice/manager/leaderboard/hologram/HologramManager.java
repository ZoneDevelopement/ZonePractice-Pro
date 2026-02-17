package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.BackendManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.GlobalHologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.LadderDynamicHologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.LadderStaticHologram;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.*;

/**
 * Manages all leaderboard holograms in the plugin.
 * Handles loading, saving, creation, and lifecycle management.
 */
@Getter
public class HologramManager {

    private static final int STARTUP_DELAY_TICKS = 20;
    private static final int STARTUP_STAGGER_TICKS = 10;
    private static final double DUPLICATE_DISTANCE_THRESHOLD = 2.0;

    private static HologramManager instance;

    private final List<Hologram> holograms = new ArrayList<>();
    private final List<LbSecondaryType> lbSecondaryTypes = List.of(LbSecondaryType.values());

    private HologramManager() {}

    public static HologramManager getInstance() {
        if (instance == null) {
            instance = new HologramManager();
        }
        return instance;
    }

    /**
     * Finds a hologram by name (case-insensitive).
     */
    public Optional<Hologram> findHologram(String name) {
        return holograms.stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * @deprecated Use {@link #findHologram(String)} instead
     */
    @Deprecated
    public Hologram getHologram(String name) {
        return findHologram(name).orElse(null);
    }

    /**
     * Creates and registers a new hologram.
     */
    public void createHologram(Hologram hologram) {
        holograms.add(hologram);
        hologram.setSetupHologram(SetupHologramType.SETUP);

        HologramSetupManager.getInstance().buildHologramSetupGUIs(hologram);
        GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).update();
    }

    /**
     * Loads all holograms from configuration.
     */
    public void loadHolograms() {
        var config = BackendManager.getConfig();
        if (!config.isConfigurationSection("holograms")) {
            HologramSetupManager.getInstance().loadGUIs();
            startHologramsWithDelay();
            return;
        }

        config.getConfigurationSection("holograms").getKeys(false).forEach(this::loadHologram);

        HologramSetupManager.getInstance().loadGUIs();
        startHologramsWithDelay();
    }

    /**
     * Loads a single hologram from configuration.
     */
    private void loadHologram(String name) {
        try {
            var config = BackendManager.getConfig();
            HologramType type = HologramType.valueOf(config.getString("holograms." + name + ".type"));

            Hologram hologram = createHologramByType(name, type);
            if (hologram == null) return;

            if (hologram.getBaseLocation() != null && isDuplicateLocation(hologram)) {
                Common.sendConsoleMMMessage("<red>Warning: Hologram '" + name + "' has duplicate location! Disabling.");
                hologram.setEnabled(false);
            }

            holograms.add(hologram);
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Error loading hologram " + name + "!");
            e.printStackTrace();
        }
    }

    /**
     * Creates a hologram instance based on type.
     */
    private Hologram createHologramByType(String name, HologramType type) {
        return switch (type) {
            case GLOBAL -> new GlobalHologram(name);
            case LADDER_STATIC -> new LadderStaticHologram(name);
            case LADDER_DYNAMIC -> new LadderDynamicHologram(name);
        };
    }

    /**
     * Starts holograms with staggered delays to prevent lag spikes.
     */
    private void startHologramsWithDelay() {
        for (int i = 0; i < holograms.size(); i++) {
            Hologram hologram = holograms.get(i);
            long delay = STARTUP_DELAY_TICKS + ((long) i * STARTUP_STAGGER_TICKS);

            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
                if (hologram.isEnabled()) {
                    hologram.getHologramRunnable().begin();
                } else {
                    hologram.setSetupHologram(SetupHologramType.SETUP);
                }
            }, delay);
        }
    }

    /**
     * Checks if a hologram's location conflicts with existing holograms.
     */
    private boolean isDuplicateLocation(Hologram newHologram) {
        if (newHologram.getBaseLocation() == null || newHologram.getBaseLocation().getWorld() == null) {
            return false;
        }

        return holograms.stream()
                .filter(h -> h.getBaseLocation() != null)
                .filter(h -> h.getBaseLocation().getWorld() != null)
                .filter(h -> h.getBaseLocation().getWorld().equals(newHologram.getBaseLocation().getWorld()))
                .anyMatch(h -> h.getBaseLocation().distance(newHologram.getBaseLocation()) < DUPLICATE_DISTANCE_THRESHOLD);
    }

    /**
     * Saves all hologram data without despawning.
     */
    public void saveHolograms() {
        holograms.forEach(Hologram::setData);
    }

    /**
     * Saves and despawns all holograms (for shutdown).
     */
    public void saveAndDespawnHolograms() {
        holograms.forEach(hologram -> {
            hologram.setData();
            hologram.deleteHologram(false);
        });
    }

    /**
     * Removes a ladder from all holograms that reference it.
     */
    public void removeLadder(NormalLadder ladder) {
        holograms.forEach(hologram -> {
            if (hologram instanceof LadderStaticHologram staticHolo && staticHolo.getLadder() == ladder) {
                staticHolo.setLadder(null);
                hologram.setEnabled(false);
            } else if (hologram instanceof LadderDynamicHologram dynamicHolo && dynamicHolo.getLadders().contains(ladder)) {
                dynamicHolo.getLadders().remove(ladder);
                if (dynamicHolo.getLadders().isEmpty()) {
                    hologram.setEnabled(false);
                }
            }
        });
    }

    /**
     * Gets the next leaderboard type in the cycle.
     */
    public LbSecondaryType getNextType(LbSecondaryType current) {
        if (current == null) {
            return lbSecondaryTypes.get(0);
        }

        int index = lbSecondaryTypes.indexOf(current);
        return lbSecondaryTypes.get((index + 1) % lbSecondaryTypes.size());
    }
}
