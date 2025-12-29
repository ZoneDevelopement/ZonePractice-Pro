package dev.nandi0813.practice.manager.inventory.inventoryitem.lobbyitems;

import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import org.bukkit.entity.Player;

public class StaffMode extends InvItem {

    public StaffMode() {
        super(getItemStack("LOBBY-BASIC.NORMAL.STAFF-MODE.ITEM"), getInt("LOBBY-BASIC.NORMAL.STAFF-MODE.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        ProfileManager.getInstance().getProfile(player).setStaffMode(true);
        InventoryManager.getInstance().setLobbyInventory(player, false);
    }

}
