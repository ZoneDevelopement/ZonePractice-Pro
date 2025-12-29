package dev.nandi0813.practice.manager.inventory.inventoryitem.staffitems;

import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.entity.Player;

public class LeaveSpecInvItem extends InvItem {

    public LeaveSpecInvItem() {
        super(getItemStack("STAFF-MODE.NORMAL.LEAVE-SPECTATE.ITEM"), getInt("STAFF-MODE.NORMAL.LEAVE-SPECTATE.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(player);
        if (spectatable != null)
            spectatable.removeSpectator(player);
    }
}
