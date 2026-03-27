package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CountdownTitlesItem extends SettingItem {

    public CountdownTitlesItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.COUNTDOWN_TITLES, ladder);
    }

    @Override
    public void updateItemStack() {
        if (this.ladder.isCountdownTitles()) {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.COUNTDOWN-TITLES.ENABLED");
        } else {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.COUNTDOWN-TITLES.DISABLED");
        }
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        this.ladder.setCountdownTitles(!ladder.isCountdownTitles());
        this.build(true);
    }
}

