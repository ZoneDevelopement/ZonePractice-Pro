package dev.nandi0813.practice.manager.leaderboard.hologram;

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

                    switch (hologramType) {
                        case GLOBAL:
                            hologram = new GlobalHologram(name);
                            break;
                        case LADDER_STATIC:
                            hologram = new LadderStaticHologram(name);
                            break;
                        case LADDER_DYNAMIC:
                            hologram = new LadderDynamicHologram(name);
                            break;
                    }

                    if (hologram != null) {
                        holograms.add(hologram);
                    }
                } catch (Exception e) {
                    Common.sendConsoleMMMessage("<red>Error loading hologram " + name + "!");
                }
            }
        }

        HologramSetupManager.getInstance().loadGUIs();
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
