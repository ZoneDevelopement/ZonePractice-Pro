package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RoundEndDelayItem extends SettingItem {

    public RoundEndDelayItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.ROUND_END_DELAY, ladder);
    }

    @Override
    public void updateItemStack() {
        this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.ROUND-END-DELAY")
                .replace("%roundEndDelay%", String.valueOf(ladder.getRoundEndDelay()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        int delay = ladder.getRoundEndDelay();

        if (e.getClick().isLeftClick() && delay > 0)
            ladder.setRoundEndDelay(delay - 1);
        else if (e.getClick().isRightClick() && delay < 30)
            ladder.setRoundEndDelay(delay + 1);

        this.build(true);
    }

}

