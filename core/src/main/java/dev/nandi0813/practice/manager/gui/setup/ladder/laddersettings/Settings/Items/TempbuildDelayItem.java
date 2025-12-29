package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempBuild;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TempbuildDelayItem extends SettingItem {

    private final TempBuild tempBuild;

    public TempbuildDelayItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.TEMP_BUILD_DELAY, ladder);
        this.tempBuild = (TempBuild) ladder;
    }

    @Override
    public void updateItemStack() {
        this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.TEMP-BUILD")
                .replaceAll("%tempBuildDelay%", String.valueOf(tempBuild.getBuildDelay()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        ClickType click = e.getClick();

        int tempBuildDelay = tempBuild.getBuildDelay();

        if (click.isLeftClick() && tempBuildDelay > 3)
            tempBuild.setBuildDelay(tempBuildDelay - 1);
        else if (click.isRightClick() && tempBuildDelay < 30)
            tempBuild.setBuildDelay(tempBuildDelay + 1);

        build(true);
    }

}
