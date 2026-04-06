package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;

public class WindChargeCooldownItem extends SettingItem {

    public WindChargeCooldownItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.WIND_CHARGE_COOLDOWN, ladder);
    }

    @Override
    public void updateItemStack() {
        guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.WIND-CHARGE-COOLDOWN")
                .replace("%windChargeCooldown%", String.format(Locale.US, "%.1f", ladder.getWindChargeCooldown()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        double windChargeCooldown = ladder.getWindChargeCooldown();

        if (e.getClick().isLeftClick() && windChargeCooldown > 0)
            ladder.setWindChargeCooldown(Math.max(0, Math.round((windChargeCooldown - 0.5) * 10.0) / 10.0));
        else if (e.getClick().isRightClick() && windChargeCooldown < 30)
            ladder.setWindChargeCooldown(Math.min(30, Math.round((windChargeCooldown + 0.5) * 10.0) / 10.0));

        build(true);
    }
}

