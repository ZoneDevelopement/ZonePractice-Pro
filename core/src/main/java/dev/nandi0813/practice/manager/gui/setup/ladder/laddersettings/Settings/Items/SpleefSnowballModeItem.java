package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.type.Spleef;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SpleefSnowballModeItem extends SettingItem {

    private final Spleef spleef;

    public SpleefSnowballModeItem(SettingsGui settingsGui, Spleef spleef) {
        super(settingsGui, SettingType.SPLEEF_SNOWBALL_MODE, spleef);
        this.spleef = spleef;
    }

    @Override
    public void updateItemStack() {
        if (spleef.isSnowballMode()) {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.SPLEEF-SNOWBALL-MODE.ENABLED").setGlowing(true);
        } else {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.SPLEEF-SNOWBALL-MODE.DISABLED");
        }
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        spleef.setSnowballMode(!spleef.isSnowballMode());
        this.build(true);
    }
}

