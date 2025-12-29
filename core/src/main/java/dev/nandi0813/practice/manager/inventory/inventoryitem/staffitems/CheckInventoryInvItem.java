package dev.nandi0813.practice.manager.inventory.inventoryitem.staffitems;

import dev.nandi0813.practice.manager.gui.guis.PlayerInvGui;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import org.bukkit.entity.Player;

public class CheckInventoryInvItem extends InvItem {

    public CheckInventoryInvItem() {
        super(getItemStack("STAFF-MODE.NORMAL.PLAYER-INVENTORY.ITEM"), getInt("STAFF-MODE.NORMAL.PLAYER-INVENTORY.SLOT"));
    }

    public void handleClickEvent(Player player, Player target) {
        if (target != null)
            new PlayerInvGui(target).open(player);
    }

    @Override
    public void handleClickEvent(Player player) {
    }

}
