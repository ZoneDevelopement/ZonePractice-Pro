package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DropInventoryItem extends SettingItem {

    public DropInventoryItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.DROP_INVENTORY_TEAM, ladder);
    }

    @Override
    public void updateItemStack() {
        if (ladder.isDropInventoryPartyGames())
            guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.DROP-INVENTORY-PARTY-GAMES.ENABLED").setGlowing(true);
        else
            guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.DROP-INVENTORY-PARTY-GAMES.DISABLED");
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        ladder.setDropInventoryPartyGames(!ladder.isDropInventoryPartyGames());

        build(true);
    }

}
