package dev.nandi0813.practice.manager.inventory.inventories.spectate;

import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Lobby.DisableSpecMode;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Lobby.RandomMatchInvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Lobby.SpecMenuInvItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class SpecModeLobbyInventory extends Inventory {

    public SpecModeLobbyInventory() {
        super(InventoryType.SPEC_MODE_LOBBY);

        this.invItems.add(new DisableSpecMode());
        this.invItems.add(new RandomMatchInvItem());
        this.invItems.add(new SpecMenuInvItem());
    }

    @Override
    protected void set(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        for (InvItem invItem : invItems) {
            int slot = invItem.getSlot();
            if (slot == -1)
                continue;

            playerInventory.setItem(slot, invItem.getItem());
        }
    }

}
