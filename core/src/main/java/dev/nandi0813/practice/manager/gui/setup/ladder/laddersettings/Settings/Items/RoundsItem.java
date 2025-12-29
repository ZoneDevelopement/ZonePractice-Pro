package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RoundsItem extends SettingItem {

    public RoundsItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.ROUNDS, ladder);
    }

    @Override
    public void updateItemStack() {
        guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.ROUNDS")
                .replaceAll("%rounds%", String.valueOf(ladder.getRounds()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        int round = ladder.getRounds();

        if (e.getClick().isLeftClick() && round > 1)
            ladder.setRounds(round - 1);
        else if (e.getClick().isRightClick() && round < 10)
            ladder.setRounds(round + 1);

        this.settingsGui.build();
    }

}
