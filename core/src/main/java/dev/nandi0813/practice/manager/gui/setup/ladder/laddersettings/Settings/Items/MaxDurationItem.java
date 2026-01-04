package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MaxDurationItem extends SettingItem {

    public MaxDurationItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.MAX_DURATION, ladder);
    }

    @Override
    public void updateItemStack() {
        this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.MAX-DURATION")
                .replace("%maxDuration%", String.valueOf(ladder.getMaxDuration()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        int duration = ladder.getMaxDuration();

        if (e.getClick().isLeftClick() && duration > 60)
            ladder.setMaxDuration(duration - 30);
        else if (e.getClick().isRightClick() && duration < 6000)
            ladder.setMaxDuration(duration + 30);

        this.settingsGui.build();
    }

}
