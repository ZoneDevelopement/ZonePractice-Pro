package dev.nandi0813.practice.manager.inventory.inventories;

import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.partyitems.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class PartyInventory extends Inventory {

    public PartyInventory() {
        super(InventoryType.PARTY);

        this.invItems.add(new KitEditorInvItem());
        this.invItems.add(new LeavePartyInvItem());
        this.invItems.add(new OtherPartiesInvItem());
        this.invItems.add(new PartyGameInvItem());
        this.invItems.add(new PartyInfoInvItem());
        this.invItems.add(new PartySettingsInvItem());
    }

    @Override
    protected void set(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        /*
         * TODO
         * Party party = PartyManager.getInstance().getParty(player);
         */

        for (InvItem invItem : invItems) {
            int slot = invItem.getSlot();
            if (slot == -1)
                continue;

            playerInventory.setItem(slot, invItem.getItem());
        }
    }

}
