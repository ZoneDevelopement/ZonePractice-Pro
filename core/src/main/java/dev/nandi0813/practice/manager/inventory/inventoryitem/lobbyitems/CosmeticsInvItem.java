package dev.nandi0813.practice.manager.inventory.inventoryitem.lobbyitems;

import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import org.bukkit.entity.Player;

public class CosmeticsInvItem extends InvItem {

    public CosmeticsInvItem() {
        super(getItemStack("LOBBY-BASIC.NORMAL.COSMETICS.ITEM"), getInt("LOBBY-BASIC.NORMAL.COSMETICS.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        player.performCommand("cosmetics");
    }
}

