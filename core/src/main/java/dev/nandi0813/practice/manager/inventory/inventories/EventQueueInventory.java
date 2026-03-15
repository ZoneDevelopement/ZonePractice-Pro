package dev.nandi0813.practice.manager.inventory.inventories;

import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.queueitems.EventQueueLeaveInvItem;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class EventQueueInventory extends Inventory {

    public EventQueueInventory() {
        super(InventoryType.EVENT_QUEUE);

        this.invItems.add(new EventQueueLeaveInvItem());
    }

    @Override
    protected void set(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        for (InvItem invItem : invItems) {
            int slot = invItem.getSlot();
            if (slot == -1)
                continue;

            if (invItem instanceof EventQueueLeaveInvItem) {
                Event event = EventManager.getInstance().getEventByPlayer(player);
                if (event == null)
                    continue;

                ItemStack item = invItem.getItem().clone();
                ItemMeta itemMeta = item.getItemMeta();
                String displayName = Common.serializeComponentToLegacyString(itemMeta.displayName());
                itemMeta.displayName(Common.legacyToComponent(StringUtil.CC(displayName.replace("%event%", event.getType().getName()))));
                item.setItemMeta(itemMeta);

                playerInventory.setItem(slot, item);
            }
        }
    }

}
