package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TntFuseTimeItem extends SettingItem {

    public TntFuseTimeItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.TNT_FUSE_TIME, ladder);
    }

    @Override
    public void updateItemStack() {
        this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.TNT-FUSE-TIME")
                .replace("%tntFuseTime%", String.valueOf(ladder.getTntFuseTime()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        int tntFuseTime = ladder.getTntFuseTime();

        if (e.getClick().isLeftClick() && tntFuseTime > 1)
            ladder.setTntFuseTime(tntFuseTime - 1);
        else if (e.getClick().isRightClick() && tntFuseTime < 10)
            ladder.setTntFuseTime(tntFuseTime + 1);

        build(true);
    }

}
