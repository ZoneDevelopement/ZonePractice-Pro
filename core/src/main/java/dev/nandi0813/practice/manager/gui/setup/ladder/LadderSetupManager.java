package dev.nandi0813.practice.manager.gui.setup.ladder;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.*;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.type.SkyWars;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Getter
public class LadderSetupManager implements Listener {

    private static LadderSetupManager instance;

    public static LadderSetupManager getInstance() {
        if (instance == null)
            instance = new LadderSetupManager();
        return instance;
    }

    private final Map<Ladder, Map<GUIType, GUI>> ladderSetupGUIs = new HashMap<>();

    public LadderSetupManager() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    public void buildLadderSetupGUIs(NormalLadder ladder) {
        Map<GUIType, GUI> guis = new EnumMap<>(GUIType.class);

        guis.put(GUIType.Ladder_Main, new LadderMainGui(ladder));
        guis.put(GUIType.Ladder_Inventory, new InventoryGui(ladder));
        guis.put(GUIType.Ladder_CustomKitExtra_unRanked, new CustomKitGui(ladder, false));
        guis.put(GUIType.Ladder_CustomKitExtra_Ranked, new CustomKitGui(ladder, true));
        guis.put(GUIType.Ladder_Settings, new SettingsGui(ladder));
        guis.put(GUIType.Ladder_MatchType, new MatchTypeGui(ladder));

        if (ladder.isBuild())
            guis.put(GUIType.Ladder_DestroyableBlock, new DestroyableBlocksGui(ladder));

        if (ladder instanceof SkyWars)
            guis.put(GUIType.Ladder_SkyWarsLoot, new SkyWarsLootGui((SkyWars) ladder));

        ladderSetupGUIs.put(ladder, guis);
    }

    public void loadGUIs() {
        GUIManager.getInstance().addGUI(new LadderSummaryGui());

        for (NormalLadder ladder : LadderManager.getInstance().getLadders())
            buildLadderSetupGUIs(ladder);
    }

    public void removeLadderGUIs(Ladder ladder) {
        for (GUI gui : ladderSetupGUIs.get(ladder).values()) {
            for (Player player : gui.getInGuiPlayers().keySet()) {
                GUIManager.getInstance().searchGUI(GUIType.Setup_Hub).open(player);
            }
        }

        ladderSetupGUIs.remove(ladder);
        GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).update();
    }

}
