package dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Match;

import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import org.bukkit.entity.Player;

public class RandomMatchInvItem extends InvItem {

    public RandomMatchInvItem() {
        super(getItemStack("SPECTATOR.MATCH.NORMAL.RANDOM.ITEM"), getInt("SPECTATOR.MATCH.NORMAL.RANDOM.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        SpectatorManager.spectateRandomMatchItemUse(player);
    }

}
