package dev.nandi0813.practice.manager.inventory.inventories.spectate;

import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.LeaveFfaSpecInvItem;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class SpecFfaInventory extends Inventory {

    public SpecFfaInventory() {
        super(InventoryType.SPECTATE_FFA);

        this.invItems.add(new LeaveFfaSpecInvItem());
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
