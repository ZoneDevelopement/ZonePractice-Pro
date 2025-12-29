package dev.nandi0813.practice.manager.inventory.inventoryitem.staffitems;

import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import org.bukkit.entity.Player;

public class RandomMatchInvItem extends InvItem {

    public RandomMatchInvItem() {
        super(getItemStack("STAFF-MODE.NORMAL.RANDOM-GAME-SPECTATE.ITEM"), getInt("STAFF-MODE.NORMAL.RANDOM-GAME-SPECTATE.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        SpectatorManager.spectateRandomMatch(player);
    }

}
