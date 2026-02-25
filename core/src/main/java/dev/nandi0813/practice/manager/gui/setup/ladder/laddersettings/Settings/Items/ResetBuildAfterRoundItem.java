package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ResetBuildAfterRoundItem extends SettingItem {

    public ResetBuildAfterRoundItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.RESET_BUILD_AFTER_ROUND, ladder);
    }

    @Override
    public void updateItemStack() {
        if (this.ladder.isResetBuildAfterRound()) {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.RESET-BUILD-AFTER-ROUND.ENABLED").setGlowing(true);
        } else {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.RESET-BUILD-AFTER-ROUND.DISABLED");
        }
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        this.ladder.setResetBuildAfterRound(!this.ladder.isResetBuildAfterRound());
        this.build(true);
    }
}
