package dev.nandi0813.practice.manager.inventory.inventoryitem.partyitems;

import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import org.bukkit.entity.Player;

public class PartyInfoInvItem extends InvItem {

    public PartyInfoInvItem() {
        super(getItemStack("PARTY.NORMAL.PARTY-INFO.ITEM"), getInt("PARTY.NORMAL.PARTY-INFO.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        player.performCommand("party info");
    }

}
