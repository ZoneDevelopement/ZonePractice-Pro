package dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import org.bukkit.entity.Player;

public class LeaveMatchSpecInvItem extends InvItem {

    public LeaveMatchSpecInvItem() {
        super(getItemStack("SPECTATOR.MATCH.NORMAL.LEAVE.ITEM"), getInt("SPECTATOR.MATCH.NORMAL.LEAVE.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        Match match = MatchManager.getInstance().getLiveMatchBySpectator(player);
        if (match != null)
            match.removeSpectator(player);
    }
}
