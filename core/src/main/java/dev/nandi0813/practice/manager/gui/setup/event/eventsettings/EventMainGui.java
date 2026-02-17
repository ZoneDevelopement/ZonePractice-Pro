package dev.nandi0813.practice.manager.gui.setup.event.eventsettings;

import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.manager.fight.event.setup.EventWandSetupManager;
import dev.nandi0813.practice.manager.fight.event.util.EventUtil;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.event.EventSetupManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class EventMainGui extends GUI {

    private final EventData eventData;

    public EventMainGui(EventData eventData) {
        super(GUIType.Event_Main);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.EVENT.EVENT-MAIN.TITLE").replace("%eventName%", eventData.getType().getName()), 4));
        this.eventData = eventData;

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        inventory.setItem(27, GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-MAIN.ICONS.BACK-TO").get());

        for (int i : new int[]{28, 29, 30, 31, 32, 33, 34, 35})
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        inventory.setItem(15, GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-MAIN.ICONS.SETTINGS").get());

        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        inventory.setItem(10, GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-MAIN.ICONS.EVENT-NAME")
                .replace("%eventName%", eventData.getType().getName())
                .setMaterial(eventData.getIcon().getMaterial())
                .setDamage(eventData.getIcon().getDamage())
                .get());

        inventory.setItem(11, eventData.isEnabled() ?
                GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-MAIN.ICONS.STATUS.ENABLED").get()
                : GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-MAIN.ICONS.STATUS.DISABLED").get());

        inventory.setItem(16, GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-MAIN.ICONS.LOCATION")
                .replace("%eventName%", eventData.getType().getName())
                .replace("%corner1%", Common.mmToNormal(ArenaUtil.convertLocation(eventData.getCuboidLoc1())))
                .replace("%corner2%", Common.mmToNormal(ArenaUtil.convertLocation(eventData.getCuboidLoc2())))
                .replace("%spawnPositions%", String.valueOf(eventData.getSpawns().size()))
                .get());

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();
        ClickType click = e.getClick();

        int slot = e.getRawSlot();

        if (inventory.getSize() > slot && e.getCurrentItem() != null) {
            e.setCancelled(true);

            switch (slot) {
                case 10:
                    if (click.isLeftClick()) {
                        if (eventData.isEnabled()) {
                            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.CANT-EDIT-ENABLED"));
                            return;
                        }
                        ItemStack cursorItem = player.getItemOnCursor();
                        if (cursorItem == null || cursorItem.getType().equals(Material.AIR)) {
                            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.NO-ITEM-ON-CURSOR"));
                            return;
                        }

                        try {
                            eventData.setIcon(new GUIItem(cursorItem));
                        } catch (Exception ex) {
                            Common.sendMMMessage(player, "<red>" + ex.getMessage());
                            return;
                        }

                        update();
                        GUIManager.getInstance().searchGUI(GUIType.Event_Summary).update();
                        GUIManager.getInstance().searchGUI(GUIType.Event_Host).update();
                    } else if (click.isRightClick()) {
                        Location location = eventData.getAvailableLocation();
                        if (location == null) {
                            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.NO-LOCATION"));
                            return;
                        }

                        player.teleport(location);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    }
                    break;
                case 11:
                    EventUtil.changeStatus(eventData, player);
                    this.update();
                    break;
                case 15:
                    EventSetupManager.getInstance().getEventSetupGUIs().get(eventData).get(GUIType.Event_Settings).open(player);
                    break;
                case 16:
                    if (eventData.isEnabled()) {
                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.CANT-EDIT-ENABLED"));
                        return;
                    }

                    if (click.isLeftClick()) {
                        // Start wand-based setup mode
                        player.closeInventory();
                        EventWandSetupManager.getInstance().startSetup(player, eventData);
                    }
                    break;
                case 27:
                    GUIManager.getInstance().searchGUI(GUIType.Event_Summary).open(player, 1);
                    break;
            }
        }
    }

}
