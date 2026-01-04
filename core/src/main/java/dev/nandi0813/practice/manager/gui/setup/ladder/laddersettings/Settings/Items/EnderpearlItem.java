package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EnderpearlItem extends SettingItem {

    public EnderpearlItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.ENDER_PEARL_COOLDOWN, ladder);
    }

    @Override
    public void updateItemStack() {
        guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.ENDERPEARL-COOLDOWN")
                .replace("%epCooldown%", String.valueOf(ladder.getEnderPearlCooldown()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        int epCooldown = ladder.getEnderPearlCooldown();

        if (e.getClick().isLeftClick() && epCooldown > 0)
            ladder.setEnderPearlCooldown(epCooldown - 1);
        else if (e.getClick().isRightClick() && epCooldown < 60)
            ladder.setEnderPearlCooldown(epCooldown + 1);

        build(true);
    }

}
