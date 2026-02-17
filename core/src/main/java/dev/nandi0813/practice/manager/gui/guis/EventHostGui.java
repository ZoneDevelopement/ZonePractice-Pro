package dev.nandi0813.practice.manager.gui.guis;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.module.interfaces.ItemCreateUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;

@Getter
public class EventHostGui extends GUI {

    private final Map<Integer, EventType> eventSlots = new HashMap<>();

    public EventHostGui() {
        super(GUIType.Event_Host);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.EVENT-HOST.TITLE"), 1));

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        eventSlots.clear();
        gui.get(1).clear();

        for (EventType eventType : EventType.values()) {
            if (EventManager.getInstance().getEventData().get(eventType).isEnabled()) {
                int slot = gui.get(1).firstEmpty();
                eventSlots.put(slot, eventType);

                gui.get(1).setItem(slot, ItemCreateUtil.hideItemFlags(EventManager.getInstance().getEventData().get(eventType).getIcon().get()));
            }
        }
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (e.getView().getTopInventory().getSize() > slot) {
            if (eventSlots.containsKey(slot)) {
                EventType eventType = eventSlots.get(slot);

                if (!EventManager.getInstance().getEvents().isEmpty() && !ConfigManager.getBoolean("EVENT.MULTIPLE")) {
                    Common.sendMMMessage(player, LanguageManager.getString("EVENT.ONLY-ONE-EVENT"));
                    return;
                }

                if (!player.hasPermission("zpp.event.host." + eventType.name().toLowerCase()) && !player.hasPermission("zpp.event.host.all")) {
                    Common.sendMMMessage(player, LanguageManager.getString("EVENT.CANT-HOST-EVENT").replace("%event%", eventType.getName()));
                    return;
                }

                if (profile.getEventStartLeft() <= 0) {
                    Common.sendMMMessage(player, LanguageManager.getString("EVENT.CANT-HOST-EVENT-TODAY"));
                    return;
                }

                EventManager.getInstance().startEvent(player, eventType);
                player.closeInventory();
            }
        }
    }

}
