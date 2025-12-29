package dev.nandi0813.practice.manager.inventory.inventoryitem.lobbyitems;

import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.util.RematchRequest;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import org.bukkit.entity.Player;

public class RematchInvItem extends InvItem {

    public RematchInvItem() {
        super(getItemStack("LOBBY-BASIC.NORMAL.REMATCH.ITEM"), getInt("LOBBY-BASIC.NORMAL.REMATCH.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        RematchRequest rematchRequest = MatchManager.getInstance().getRematchRequest(player);

        if (rematchRequest != null)
            rematchRequest.sendRematchRequest(player);
    }

}
