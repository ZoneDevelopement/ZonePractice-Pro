package dev.nandi0813.practice.manager.inventory.inventories.spectate;

import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.LeaveEventSpecInvItem;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class SpecEventInventory extends Inventory {

    public SpecEventInventory() {
        super(InventoryType.SPECTATE_EVENT);

        this.invItems.add(new LeaveEventSpecInvItem());
    }

    @Override
    protected void set(Player player) {
        PlayerUtil.clearPlayer(player, true, true, false);

        PlayerInventory playerInventory = player.getInventory();

        for (InvItem invItem : invItems) {
            int slot = invItem.getSlot();
            if (slot == -1)
                continue;

            playerInventory.setItem(slot, invItem.getItem());
        }
    }

}
