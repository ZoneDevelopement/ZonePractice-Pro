package dev.nandi0813.practice.manager.inventory.inventoryitem.partyitems;

import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import org.bukkit.entity.Player;

public class LeavePartyInvItem extends InvItem {

    public LeavePartyInvItem() {
        super(getItemStack("PARTY.NORMAL.LEAVE-PARTY.ITEM"), getInt("PARTY.NORMAL.LEAVE-PARTY.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        Party party = PartyManager.getInstance().getParty(player);
        if (party == null) return;

        party.removeMember(player, false);
    }

}
