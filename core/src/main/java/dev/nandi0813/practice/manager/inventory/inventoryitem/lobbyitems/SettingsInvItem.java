package dev.nandi0813.practice.manager.inventory.inventoryitem.lobbyitems;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public class SettingsInvItem extends InvItem {

    public SettingsInvItem() {
        super(getItemStack("LOBBY-BASIC.NORMAL.SETTINGS.ITEM"), getInt("LOBBY-BASIC.NORMAL.SETTINGS.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        if (player.hasPermission("zpp.settings.open"))
            ProfileManager.getInstance().getProfile(player).getSettingsGui().open(player);
        else
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETTINGS.NO-PERMISSION"));
    }

}
