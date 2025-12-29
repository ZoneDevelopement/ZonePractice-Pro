package dev.nandi0813.practice.manager.inventory.inventories;

import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.staffitems.*;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class StaffInventory extends Inventory {

    public StaffInventory() {
        super(InventoryType.STAFF_MODE);

        this.invItems.add(new CheckInventoryInvItem());
        this.invItems.add(new HideOnInvItem());
        this.invItems.add(new HideOffInvItem());
        this.invItems.add(new LeaveSpecInvItem());
        this.invItems.add(new ModeOffInvItem());
        this.invItems.add(new RandomMatchInvItem());
    }

    @Override
    protected void set(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        PlayerInventory playerInventory = player.getInventory();

        for (InvItem invItem : invItems) {
            int slot = invItem.getSlot();
            if (slot == -1)
                continue;

            if (invItem instanceof HideOffInvItem) {
                if (profile.isHideFromPlayers())
                    continue;
            } else if (invItem instanceof HideOnInvItem) {
                if (!profile.isHideFromPlayers())
                    continue;
            } else if (invItem instanceof LeaveSpecInvItem) {
                if (!profile.getStatus().equals(ProfileStatus.SPECTATE))
                    continue;
            }

            playerInventory.setItem(slot, invItem.getItem());
        }
    }

}
