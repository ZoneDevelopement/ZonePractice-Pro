package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GoldenAppleItem extends SettingItem {

    public GoldenAppleItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.GOLDEN_APPLE_COOLDOWN, ladder);
    }

    @Override
    public void updateItemStack() {
        guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.GOLDENAPPLE-COOLDOWN")
                .replace("%golden_apple_cooldown%", String.valueOf(ladder.getGoldenAppleCooldown()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        int goldenAppleCooldown = ladder.getGoldenAppleCooldown();

        if (e.getClick().isLeftClick() && goldenAppleCooldown > 0)
            ladder.setGoldenAppleCooldown(goldenAppleCooldown - 1);
        else if (e.getClick().isRightClick() && goldenAppleCooldown < 30)
            ladder.setGoldenAppleCooldown(goldenAppleCooldown + 1);

        build(true);
    }

}
