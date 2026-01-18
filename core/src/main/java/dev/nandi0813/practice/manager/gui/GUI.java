package dev.nandi0813.practice.manager.gui;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.confirmgui.ConfirmGUI;
import dev.nandi0813.practice.manager.gui.confirmgui.ConfirmGuiType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class GUI {

    protected final GUIType type;

    protected final Map<Integer, Inventory> gui = new HashMap<>();

    protected final Map<Player, Integer> inGuiPlayers = new HashMap<>();
    protected final Map<Player, ConfirmGUI> inConfirmationGui = new HashMap<>();

    public GUI(GUIType type) {
        this.type = type;
    }

    public abstract void build();

    public abstract void update();

    /**
     * Update with optional cache bypass.
     * For cacheable GUIs, this checks cache validity first.
     *
     * @param forceRefresh If true, bypass cache and rebuild
     */
    public boolean update(boolean forceRefresh) {
        // If this GUI type should be cached and we're not forcing refresh
        if (!forceRefresh && GUICache.shouldCache(type)) {
            // Check if we have valid cached data
            if (GUICache.isCacheValid(type)) {
                // Load from cache instead of rebuilding
                Map<Integer, Inventory> cached = GUICache.getCached(type);
                if (cached != null) {
                    gui.clear();
                    gui.putAll(cached);
                    updatePlayers();
                    return false;
                }
            }
        }

        // Either not cacheable, cache expired, or force refresh requested
        update();

        // After update, cache if this is a cacheable GUI type
        if (GUICache.shouldCache(type)) {
            GUICache.putCache(type, gui);
        }

        return true;
    }

    public void open(Player player, int page) {
        inConfirmationGui.remove(player);

        if (gui.containsKey(page)) {
            player.openInventory(gui.get(page));

            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
            {
                inGuiPlayers.put(player, page);
                GUIManager.getInstance().getOpenGUI().put(player, this);
            }, 2L);
        } else if (page > 1)
            open(player, page - 1);
        else
            player.closeInventory();
    }

    public void open(Player player) {
        open(player, 1);
    }

    protected void updatePlayers() {
        if (inGuiPlayers.isEmpty()) {
            return;
        }

        for (Player player : inGuiPlayers.keySet()) {
            if (player != null && player.isOnline() && player.getOpenInventory() != null && inGuiPlayers.get(player) != -1) {
                player.updateInventory();
            }
        }
    }

    public void close(Player player) {
        inGuiPlayers.remove(player);
        GUIManager.getInstance().getOpenGUI().remove(player);
    }

    public abstract void handleClickEvent(InventoryClickEvent e);

    public void handleCloseEvent(InventoryCloseEvent e) {
    }

    public void handleDragEvent(InventoryDragEvent e) {
    }

    public void openConfirmGUI(Player player, ConfirmGuiType confirmGuiType, GUI backToConfirm, GUI backToCancel) {
        ConfirmGUI confirmGUI = new ConfirmGUI(confirmGuiType, backToConfirm, backToCancel);
        confirmGUI.openInventory(player);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
        {
            inGuiPlayers.put(player, -1);
            inConfirmationGui.put(player, confirmGUI);
            GUIManager.getInstance().getOpenGUI().put(player, this);
        }, 2L);
    }

    public void handleConfirmGUIClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        if (!inConfirmationGui.containsKey(player)) return;
        ConfirmGUI confirmGUI = inConfirmationGui.get(player);

        int slot = e.getRawSlot();
        Inventory inventory = e.getView().getTopInventory();

        if (inventory == null) return;
        if (inventory.getSize() <= slot) return;

        if (slot != 4) {
            if (confirmGUI.getBackToCancel() != null) {
                confirmGUI.getBackToCancel().open(player);
            } else {
                player.closeInventory();
            }
        } else {
            handleConfirm(player, confirmGUI.getType());

            if (confirmGUI.getBackToConfirm() != null) {
                confirmGUI.getBackToConfirm().open(player);
            } else {
                player.closeInventory();
            }
        }
    }

    public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {
    }

}
