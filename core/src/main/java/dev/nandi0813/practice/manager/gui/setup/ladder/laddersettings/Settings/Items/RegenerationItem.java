package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RegenerationItem extends SettingItem {

    public RegenerationItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.REGENERATION, ladder);
    }

    @Override
    public void updateItemStack() {
        if (ladder.isRegen())
            guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.REGENERATION.ENABLED").setGlowing(true);
        else
            guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.REGENERATION.DISABLED");
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        ladder.setRegen(!ladder.isRegen());

        build(true);
    }

}
