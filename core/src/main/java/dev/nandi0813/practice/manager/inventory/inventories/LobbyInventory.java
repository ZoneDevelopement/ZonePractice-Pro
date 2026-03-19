package dev.nandi0813.practice.manager.inventory.inventories;

import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.lobbyitems.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class LobbyInventory extends Inventory {

    public LobbyInventory() {
        super(InventoryType.LOBBY);

        this.invItems.add(new KitEditorInvItem());
        this.invItems.add(new PartyCreateInvItem());
        this.invItems.add(new RankedInvItem());
        this.invItems.add(new RematchInvItem());
        this.invItems.add(new SettingsInvItem());
        this.invItems.add(new CosmeticsInvItem());
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

            switch (invItem) {
                case SpectateModeInvItem spectateModeInvItem -> {
                    if (!InventoryManager.SPECTATOR_MODE_ENABLED)
                        continue;
                }
                case SetupInvItem setupInvItem -> {
                    if (!player.hasPermission("zpp.setup"))
                        continue;
                    setupItemSet = true;
                }
                case StaffMode staffMode -> {
                    if (!player.hasPermission("zpp.staffmode"))
                        continue;
                    if (setupItemSet)
                        continue;
                }
                case RematchInvItem rematchInvItem -> {
                    continue;
                }
                case CosmeticsInvItem cosmeticsInvItem -> {
                    if (!player.hasPermission("zpp.cosmetics.main")) {
                        continue;
                    }
                }
                default -> {
                }
            }

            playerInventory.setItem(slot, invItem.getItem());
        }
    }

    public void addRematchItem(Player player) {
        InvItem invItem = getRematchInvItem();
        if (invItem == null) return;

        player.getInventory().setItem(invItem.getSlot(), invItem.getItem());
    }

    public void removeRematchItem(Player player) {
        InvItem invItem = getRematchInvItem();
        if (invItem == null) return;

        int slot = invItem.getSlot();
        if (slot < 0) return;

        ItemStack current = player.getInventory().getItem(slot);
        if (current == null || current.getType() == Material.AIR) return;

        // Only clear the slot if the current item is still the rematch item.
        if (current.isSimilar(invItem.getItem())) {
            player.getInventory().setItem(slot, null);
            player.updateInventory();
        }
    }

    private InvItem getRematchInvItem() {
        return this.getInvItem();
    }

}
