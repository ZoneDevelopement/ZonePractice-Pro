package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class HitdelayItem extends SettingItem {

    public HitdelayItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.HIT_DELAY, ladder);
    }

    @Override
    public void updateItemStack() {
        guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.HITDELAY")
                .replaceAll("%hitdelay%", String.valueOf(ladder.getHitDelay()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        int hitDelay = ladder.getHitDelay();

        if (e.getClick().isLeftClick() && hitDelay > 0)
            ladder.setHitDelay(hitDelay - 1);
        else if (e.getClick().isRightClick() && hitDelay < 100)
            ladder.setHitDelay(hitDelay + 1);

        build(true);
    }

}
