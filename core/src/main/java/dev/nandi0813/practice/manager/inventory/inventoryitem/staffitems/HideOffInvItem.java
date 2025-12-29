package dev.nandi0813.practice.manager.inventory.inventoryitem.staffitems;

import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import org.bukkit.entity.Player;

public class HideOffInvItem extends InvItem {

    public HideOffInvItem() {
        super(getItemStack("STAFF-MODE.NORMAL.HIDE-FROM-PLAYERS-OFF.ITEM"), getInt("STAFF-MODE.NORMAL.HIDE-FROM-PLAYERS-OFF.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        profile.setHideFromPlayers(!profile.isHideFromPlayers());

        PlayerHider.getInstance().toggleStaffVisibility(player);
        InventoryManager.getInstance().setStaffModeInventory(player);
    }
}
