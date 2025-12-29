package dev.nandi0813.practice.manager.gui.setup.event;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EventSummaryGui extends GUI {

    private final Map<Integer, EventData> eventSlots = new HashMap<>();

    public EventSummaryGui() {
        super(GUIType.Event_Summary);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.EVENT.EVENT-MANAGER.TITLE"), 3));

        build();
    }

    @Override
    public void build() {
        // Frame
        for (int i = 0; i < gui.get(1).getSize(); i++)
            gui.get(1).setItem(i, GUIManager.getFILLER_ITEM());

        // Back to Manager Icon
        gui.get(1).setItem(18, GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-MANAGER.ICONS.BACK-TO").get());

        update();
    }

    @Override
    public void update() {
        for (EventData eventData : EventManager.getInstance().getEventData().values()) {
            ItemStack icon = GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-MANAGER.ICONS.EVENT-ICON")
                    .replaceAll("%eventName%", eventData.getType().getName())
                    .replaceAll("%state%", eventData.isEnabled() ?
                            GUIFile.getString("GUIS.SETUP.EVENT.EVENT-MANAGER.ICONS.EVENT-ICON.STATUS-NAMES.ENABLED") :
                            GUIFile.getString("GUIS.SETUP.EVENT.EVENT-MANAGER.ICONS.EVENT-ICON.STATUS-NAMES.DISABLED"))
                    .setMaterial(eventData.getIcon().getMaterial())
                    .setDamage(eventData.getIcon().getDamage())
                    .get();

            int slot = eventData.getType().getGuiSlot();
            gui.get(1).setItem(slot, icon);
            eventSlots.put(slot, eventData);
        }

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack currentItem = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() > slot && currentItem != null && !currentItem.equals(GUIManager.getFILLER_ITEM())) {
            if (slot == 18)
                GUIManager.getInstance().searchGUI(GUIType.Setup_Hub).open(player);
            else if (eventSlots.containsKey(slot)) {
                EventData eventData = eventSlots.get(slot);

                EventSetupManager.getInstance().getEventSetupGUIs().get(eventData).get(GUIType.Event_Main).open(player);
            }
        }
    }

}
