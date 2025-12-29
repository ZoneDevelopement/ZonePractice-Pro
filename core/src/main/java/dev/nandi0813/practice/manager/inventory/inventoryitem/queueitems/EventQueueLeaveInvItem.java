package dev.nandi0813.practice.manager.inventory.inventoryitem.queueitems;

import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import org.bukkit.entity.Player;

public class EventQueueLeaveInvItem extends InvItem {

    public EventQueueLeaveInvItem() {
        super(getItemStack("QUEUE.EVENT.NORMAL.LEAVE-EVENT-QUEUE.ITEM"), getInt("QUEUE.EVENT.NORMAL.LEAVE-EVENT-QUEUE.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        Event event = EventManager.getInstance().getEventByPlayer(player);
        if (event != null)
            event.removePlayer(player, true);
    }
}
