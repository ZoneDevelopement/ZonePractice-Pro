package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempBuildReturnDelay;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TempBuildReturnDelayItem extends SettingItem {

    private static final int MIN_DELAY_SECONDS = -1;
    private static final int MAX_DELAY_SECONDS = 30;

    private final TempBuildReturnDelay tempBuildReturnDelay;

    public TempBuildReturnDelayItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.TEMP_BUILD_RETURN_DELAY, ladder);
        this.tempBuildReturnDelay = (TempBuildReturnDelay) ladder;
    }

    @Override
    public void updateItemStack() {
        int delay = tempBuildReturnDelay.getTempBuildReturnDelaySeconds();
        String delayText = delay < 0 ? "Disabled" : delay + "";

        this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.TEMP-BUILD-RETURN-DELAY")
                .replace("%tempBuildReturnDelay%", delayText);
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        int delay = tempBuildReturnDelay.getTempBuildReturnDelaySeconds();

        if (e.getClick().isLeftClick() && delay > MIN_DELAY_SECONDS) {
            tempBuildReturnDelay.setTempBuildReturnDelaySeconds(delay - 1);
        } else if (e.getClick().isRightClick() && delay < MAX_DELAY_SECONDS) {
            tempBuildReturnDelay.setTempBuildReturnDelaySeconds(delay + 1);
        }

        build(true);
    }

}

