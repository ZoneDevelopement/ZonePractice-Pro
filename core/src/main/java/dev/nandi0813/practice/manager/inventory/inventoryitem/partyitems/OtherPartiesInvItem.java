package dev.nandi0813.practice.manager.inventory.inventoryitem.partyitems;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public class OtherPartiesInvItem extends InvItem {

    public OtherPartiesInvItem() {
        super(getItemStack("PARTY.NORMAL.OTHER-PARTIES.ITEM"), getInt("PARTY.NORMAL.OTHER-PARTIES.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        Party party = PartyManager.getInstance().getParty(player);
        if (party == null) return;

        if (!party.getLeader().equals(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("PARTY.NOT-LEADER"));
            return;
        }

        GUIManager.getInstance().searchGUI(GUIType.Party_OtherParties).open(player);
    }

}
