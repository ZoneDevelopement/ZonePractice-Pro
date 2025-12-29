package dev.nandi0813.practice.manager.inventory.inventoryitem.partyitems;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public class PartySettingsInvItem extends InvItem {

    public PartySettingsInvItem() {
        super(getItemStack("PARTY.NORMAL.PARTY-SETTINGS.ITEM"), getInt("PARTY.NORMAL.PARTY-SETTINGS.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        Party party = PartyManager.getInstance().getParty(player);
        if (party == null) return;

        if (!party.getLeader().equals(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("PARTY.NOT-LEADER"));
            return;
        }

        party.getPartySettingsGui().open(player);
    }
}
