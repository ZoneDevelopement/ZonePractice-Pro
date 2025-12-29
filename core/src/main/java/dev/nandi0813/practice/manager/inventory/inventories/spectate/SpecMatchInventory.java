package dev.nandi0813.practice.manager.inventory.inventories.spectate;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.LeaveMatchSpecInvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Match.HideSpectatorsInvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Match.RandomMatchInvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Match.ShowSpectatorsInvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Match.SpecMenuInvItem;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class SpecMatchInventory extends Inventory {

    private static final double SPECTATOR_SPEED = ConfigManager.getDouble("SPECTATOR-SETTINGS.SPECTATOR-SPEED");

    public SpecMatchInventory() {
        super(InventoryType.SPECTATE_MATCH);

        this.invItems.add(new HideSpectatorsInvItem());
        this.invItems.add(new RandomMatchInvItem());
        this.invItems.add(new ShowSpectatorsInvItem());
        this.invItems.add(new SpecMenuInvItem());
        this.invItems.add(new LeaveMatchSpecInvItem());
    }

    @Override
    protected void set(Player player) {
        PlayerUtil.clearPlayer(player, true, true, false);
        player.setFlySpeed((float) SPECTATOR_SPEED / 10);

        Profile profile = ProfileManager.getInstance().getProfile(player);
        profile.setStatus(ProfileStatus.SPECTATE);

        PlayerInventory playerInventory = player.getInventory();

        for (InvItem invItem : this.invItems) {
            int slot = invItem.getSlot();
            if (slot == -1)
                continue;

            if (invItem instanceof HideSpectatorsInvItem) {
                if (!InventoryManager.SPECTATOR_MODE_ENABLED)
                    continue;

                if (profile.isHideSpectators())
                    continue;
            } else if (invItem instanceof ShowSpectatorsInvItem) {
                if (!InventoryManager.SPECTATOR_MODE_ENABLED)
                    continue;

                if (!profile.isHideSpectators())
                    continue;
            } else if (invItem instanceof RandomMatchInvItem) {
                if (!InventoryManager.SPECTATOR_MODE_ENABLED)
                    continue;
            } else if (invItem instanceof SpecMenuInvItem) {
                if (!InventoryManager.SPECTATOR_MODE_ENABLED)
                    continue;

                if (!InventoryManager.SPECTATOR_MENU_ENABLED)
                    continue;
            }

            playerInventory.setItem(slot, invItem.getItem());
        }
    }

}
