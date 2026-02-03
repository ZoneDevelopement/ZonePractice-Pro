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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class HologramManager {

    private static HologramManager instance;

    public static HologramManager getInstance() {
        if (instance == null)
            instance = new HologramManager();
        return instance;
    }

    private HologramManager() {
    }

    private final List<Hologram> holograms = new ArrayList<>();


    public Hologram getHologram(String name) {
        for (Hologram hologram : holograms)
            if (hologram.getName().equalsIgnoreCase(name))
                return hologram;
        return null;
    }

    public void createHologram(Hologram hologram) {
        holograms.add(hologram);
        hologram.setSetupHologram(SetupHologramType.SETUP);

        HologramSetupManager.getInstance().buildHologramSetupGUIs(hologram);
        GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).update();
    }

    public void loadHolograms() {
        if (BackendManager.getConfig().isConfigurationSection("holograms")) {
            for (String name : BackendManager.getConfig().getConfigurationSection("holograms").getKeys(false)) {
                try {
                    Hologram hologram = null;
                    HologramType hologramType = HologramType.valueOf(BackendManager.getConfig().getString("holograms." + name + ".type"));

                    hologram = switch (hologramType) {
                        case GLOBAL -> new GlobalHologram(name);
                        case LADDER_STATIC -> new LadderStaticHologram(name);
                        case LADDER_DYNAMIC -> new LadderDynamicHologram(name);
                    };

                    if (hologram != null) {
                        if (hologram.getBaseLocation() != null && isDuplicateLocation(hologram)) {
                            Common.sendConsoleMMMessage("<red>Warning: Hologram '" + name + "' has the same location as another hologram! Disabling it.");
                            hologram.setEnabled(false);
                        }

                        holograms.add(hologram);
                    }
                } catch (Exception e) {
                    Common.sendConsoleMMMessage("<red>Error loading hologram " + name + "!");
                    e.printStackTrace();
                }
            }
        }

        HologramSetupManager.getInstance().loadGUIs();

        startHologramsWithDelay();
    }

    /**
     * Start holograms with staggered delays to prevent FPS drops and race conditions
     */
    private void startHologramsWithDelay() {
        int delay = 0;
        for (Hologram hologram : holograms) {
            final int currentDelay = delay;
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                    ZonePractice.getInstance(),
                    () -> {
                        if (hologram.isEnabled()) {
                            hologram.getHologramRunnable().begin();
                        } else {
                            hologram.setSetupHologram(SetupHologramType.SETUP);
                        }
                    },
                    20L + (currentDelay * 10L)
            );
            delay++;
        }
    }

    /**
     * Check if a hologram's location conflicts with any existing hologram
     */
    private boolean isDuplicateLocation(Hologram newHologram) {
        for (Hologram existing : holograms) {
            if (existing.getBaseLocation() != null && newHologram.getBaseLocation() != null) {
                if (existing.getBaseLocation().getWorld() != null &&
                        existing.getBaseLocation().getWorld().equals(newHologram.getBaseLocation().getWorld())) {
                    double distance = existing.getBaseLocation().distance(newHologram.getBaseLocation());
                    if (distance < 2.0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void saveHolograms() {
        for (Hologram hologram : holograms) {
            hologram.setData();
            hologram.deleteHologram(false);
        }
    }

    public void removeLadder(NormalLadder ladder) {
        for (Hologram hologram : new ArrayList<>(holograms)) {
            if (hologram instanceof LadderStaticHologram ladderStaticHologram) {
                if (ladderStaticHologram.getLadder() == ladder) {
                    ladderStaticHologram.setLadder(null);
                    hologram.setEnabled(false);
                }
            } else if (hologram instanceof LadderDynamicHologram ladderDynamicHologram) {
                if (ladderDynamicHologram.getLadders().contains(ladder)) {
                    ladderDynamicHologram.getLadders().remove(ladder);

                    if (ladderDynamicHologram.getLadders().isEmpty()) {
                        hologram.setEnabled(false);
                    }
                }
            }
        }
    }

    private final List<LbSecondaryType> lbSecondaryTypes = new ArrayList<>(Arrays.asList(LbSecondaryType.values()));

    public LbSecondaryType getNextType(LbSecondaryType hologramType) {
        if (hologramType != null) {
            int c = lbSecondaryTypes.indexOf(hologramType);

            if (lbSecondaryTypes.size() - 1 == c)
                return lbSecondaryTypes.get(0);
            else
                return lbSecondaryTypes.get(c + 1);
        } else
            return lbSecondaryTypes.get(0);
    }

}
