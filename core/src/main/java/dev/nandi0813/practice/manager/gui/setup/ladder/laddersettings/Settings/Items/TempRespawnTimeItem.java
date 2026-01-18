package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.RespawnableLadder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TempRespawnTimeItem extends SettingItem {

    private final RespawnableLadder respawnableLadder;

    public TempRespawnTimeItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.RESPAWN_TIME, ladder);
        this.respawnableLadder = (RespawnableLadder) ladder;
    }

    @Override
    public void updateItemStack() {
        guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.RESPAWN")
                .replace("%respawnTime%", String.valueOf(respawnableLadder.getRespawnTime()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        ClickType click = e.getClick();

        int respawnTime = respawnableLadder.getRespawnTime();

        if (click.isLeftClick() && respawnTime > 0)
            respawnableLadder.setRespawnTime(respawnTime - 1);
        else if (click.isRightClick() && respawnTime < 10)
            respawnableLadder.setRespawnTime(respawnTime + 1);

        build(true);
    }

}
