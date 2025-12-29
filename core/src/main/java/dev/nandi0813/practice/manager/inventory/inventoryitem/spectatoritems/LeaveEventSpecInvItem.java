package dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems;

import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import org.bukkit.entity.Player;

public class LeaveEventSpecInvItem extends InvItem {

    public LeaveEventSpecInvItem() {
        super(getItemStack("SPECTATOR.EVENT.NORMAL.LEAVE.ITEM"), getInt("SPECTATOR.EVENT.NORMAL.LEAVE.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        Event event = EventManager.getInstance().getEventBySpectator(player);
        if (event != null)
            event.removeSpectator(player);
    }

}
