package dev.nandi0813.practice.manager.gui.setup.server;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ServerEventsGui extends GUI {

    private final GUI backTo;
    private final Map<Integer, Event> eventSlots = new HashMap<>();

    public ServerEventsGui(GUI backTo) {
        super(GUIType.Server_Events);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.SERVER.EVENTS.TITLE"), 2));
        this.backTo = backTo;
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);
            inventory.clear();
            eventSlots.clear();

            for (int i : new int[]{10, 11, 12, 13, 14, 15, 16})
                inventory.setItem(i, GUIManager.getFILLER_ITEM());

            inventory.setItem(9, GUIFile.getGuiItem("GUIS.SETUP.SERVER.EVENTS.ICONS.BACK-TO").get());
            inventory.setItem(17, GUIFile.getGuiItem("GUIS.SETUP.SERVER.EVENTS.ICONS.REFRESH-PAGE").get());

            for (Event event : EventManager.getInstance().getEvents()) {
                int slot = inventory.firstEmpty();

                eventSlots.put(slot, event);
                inventory.setItem(slot, getEventItem(event));
            }

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        Inventory inventory = e.getClickedInventory();
        ClickType click = e.getClick();
        ItemStack item = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (item == GUIManager.getFILLER_ITEM()) return;

        switch (slot) {
            case 9:
                backTo.open(player);
                break;
            case 17:
                if (PlayerCooldown.isActive(player, CooldownObject.SERVER_SETUP_EVENT_REFRESH)) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.SERVER.WAIT-BEFORE"));
                    return;
                }

                update();
                PlayerCooldown.addCooldown(player, CooldownObject.SERVER_SETUP_EVENT_REFRESH, 5);
                break;
            default:
                if (eventSlots.containsKey(slot)) {
                    Event event = eventSlots.get(slot);

                    if (click.isLeftClick())
                        event.addSpectator(player, null, true, false);
                    else if (click.isRightClick()) {
                        event.forceEnd(player);
                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.SERVER.ENDED-EVENT").replaceAll("%event%", event.getType().getName()));
                    }
                }
                break;
        }
    }

    private ItemStack getEventItem(Event event) {
        GUIItem guiItem = event.getEventData().getIcon();
        guiItem.setName(GUIFile.getString("GUIS.SETUP.SERVER.EVENTS.ICONS.EVENT-ITEM.NAME"));
        guiItem.setLore(GUIFile.getStringList("GUIS.SETUP.SERVER.EVENTS.ICONS.EVENT-ITEM.LORE"));

        guiItem
                .replaceAll("%eventName%", event.getEventData().getType().getName())
                .replaceAll("%type%", event.getType().getName())
                .replaceAll("%players%", String.valueOf(event.getPlayers().size()))
                .replaceAll("%spectators%", String.valueOf(event.getSpectators().size()));

        return guiItem.get();
    }

}
