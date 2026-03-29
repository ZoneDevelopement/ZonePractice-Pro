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
        guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.ATTACKCOOLDOWN")
                .replace("%attackcooldown%", String.format("%.1f", ladder.getAttackCooldownModifier()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        double hitDelay = ladder.getAttackCooldownModifier();

        if (e.getClick().isLeftClick() && hitDelay > 0)
            ladder.setAttackCooldownModifier(Math.clamp(Math.round((hitDelay - 0.1) * 10) / 10.0, 0, 3.0));
        else if (e.getClick().isRightClick() && hitDelay < 3.0)
            ladder.setAttackCooldownModifier(Math.clamp(Math.round((hitDelay + 0.1) * 10) / 10.0, 0, 3.0));

        build(true);
    }

}
