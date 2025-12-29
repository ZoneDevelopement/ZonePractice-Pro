package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.WeightClassType;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class RankedItem extends SettingItem {

    public RankedItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.WEIGHT_CLASS, ladder);
    }

    @Override
    public void updateItemStack() {
        List<String> extension = new ArrayList<>();
        for (WeightClassType weightClassType : WeightClassType.values()) {
            String ktName = StringUtils.capitalize(weightClassType.getName());

            if (ladder.getWeightClass().equals(weightClassType)) extension.add(" &a» " + ktName);
            else extension.add(" &7» " + ktName);
        }

        GUIItem guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.WEIGHT-CLASS");

        List<String> lore = new ArrayList<>();
        for (String line : guiItem.getLore()) {
            if (line.contains("%weightClassTypes%"))
                lore.addAll(extension);
            else
                lore.add(line);
        }

        guiItem.setLore(lore);
        this.guiItem = guiItem;
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        switch (ladder.getWeightClass()) {
            case UNRANKED:
                ladder.setWeightClass(WeightClassType.RANKED);

                ladder.getMatchTypes().clear();
                ladder.getMatchTypes().add(MatchType.DUEL);

                LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_MatchType).update();
                break;
            case RANKED:
                ladder.setWeightClass(WeightClassType.UNRANKED_AND_RANKED);
                break;
            case UNRANKED_AND_RANKED:
                ladder.setWeightClass(WeightClassType.UNRANKED);
                break;
        }

        GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).update();
        LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_CustomKitExtra_unRanked).update();
        LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_CustomKitExtra_Ranked).update();

        build(true);
    }

}
