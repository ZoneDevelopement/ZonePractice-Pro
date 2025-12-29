package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempDead;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TempRespawnTimeItem extends SettingItem {

    private final TempDead tempDead;

    public TempRespawnTimeItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.RESPAWN_TIME, ladder);
        this.tempDead = (TempDead) ladder;
    }

    @Override
    public void updateItemStack() {
        guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.RESPAWN")
                .replaceAll("%respawnTime%", String.valueOf(tempDead.getRespawnTime()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        ClickType click = e.getClick();

        int respawnTime = tempDead.getRespawnTime();

        if (click.isLeftClick() && respawnTime > 3)
            tempDead.setRespawnTime(respawnTime - 1);
        else if (click.isRightClick() && respawnTime < 10)
            tempDead.setRespawnTime(respawnTime + 1);

        build(true);
    }

}
