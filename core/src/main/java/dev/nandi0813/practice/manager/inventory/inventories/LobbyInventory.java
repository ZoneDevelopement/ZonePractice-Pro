package dev.nandi0813.practice.manager.inventory.inventories;

import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.lobbyitems.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class LobbyInventory extends Inventory {

    public LobbyInventory() {
        super(InventoryType.LOBBY);

        this.invItems.add(new KitEditorInvItem());
        this.invItems.add(new PartyCreateInvItem());
        this.invItems.add(new RankedInvItem());
        this.invItems.add(new RematchInvItem());
        this.invItems.add(new SettingsInvItem());
        this.invItems.add(new SpectateModeInvItem());
        this.invItems.add(new StaffMode());
        this.invItems.add(new SetupInvItem());
        this.invItems.add(new StatisticsInvItem());
        this.invItems.add(new UnrankedInvItem());
    }

    @Override
    protected void set(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        boolean setupItemSet = false;

        for (InvItem invItem : invItems) {
            int slot = invItem.getSlot();
            if (slot == -1)
                continue;

            if (invItem instanceof SpectateModeInvItem) {
                if (!InventoryManager.SPECTATOR_MODE_ENABLED)
                    continue;
            } else if (invItem instanceof SetupInvItem) {
                if (!player.hasPermission("zpp.setup"))
                    continue;
                setupItemSet = true;
            } else if (invItem instanceof StaffMode) {
                if (!player.hasPermission("zpp.staffmode"))
                    continue;
                if (setupItemSet)
                    continue;
            } else if (invItem instanceof RematchInvItem) {
                continue;
            }

            playerInventory.setItem(slot, invItem.getItem());
        }
    }

    public void addRematchItem(Player player) {
        InvItem invItem = this.getInvItem(RematchInvItem.class);
        if (invItem == null) return;

        player.getInventory().setItem(invItem.getSlot(), invItem.getItem());
    }

}
