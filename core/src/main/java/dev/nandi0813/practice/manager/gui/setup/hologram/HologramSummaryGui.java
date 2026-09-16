package dev.nandi0813.practice.manager.gui.setup.hologram;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.leaderboard.hologram.Hologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramManager;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.PageUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HologramSummaryGui extends GUI {

    @Getter
    private final int spaces = 18;
    private final Map<Integer, Map<Integer, Hologram>> hologramSlots = new HashMap<>();

    public HologramSummaryGui() {
        super(GUIType.Hologram_Summary);

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        List<Hologram> holograms = new ArrayList<>(HologramManager.getInstance().getHolograms());
        holograms.sort(Comparator.comparing(Hologram::getName, String::compareToIgnoreCase));

        Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
        Map<Integer, Inventory> newGui = new HashMap<>();
        Map<Integer, Map<Integer, Hologram>> newHologramSlots = new HashMap<>();

        for (int page = 1; PageUtil.isPageValid(holograms.size(), page, spaces) || page == 1; page++) {
            Inventory inventory = existingInventories.get(page);
            String title = GUIFile.getString("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.TITLE").replace("%page%", String.valueOf(page));
            if (inventory == null || inventory.getSize() != 3 * 9) {
                inventory = InventoryUtil.createInventory(title, 3);
            } else {
                inventory.clear();
            }
            newGui.put(page, inventory);

            // Frame
            for (int i = 18; i < 27; i++) {
                inventory.setItem(i, GUIManager.getFILLER_ITEM());
            }

            Map<Integer, Hologram> pageSlots = new HashMap<>();
            int startIndex = (page - 1) * spaces;
            int endIndex = Math.min(startIndex + spaces, holograms.size());
            for (int i = startIndex; i < endIndex; i++) {
                int slot = inventory.firstEmpty();
                if (slot == -1 || slot >= 18) {
                    break;
                }

                Hologram hologram = holograms.get(i);
                inventory.setItem(slot, this.getSummaryHologramMainItem(hologram));
                pageSlots.put(slot, hologram);
            }
            newHologramSlots.put(page, pageSlots);

            // Left navigation
            ItemStack left;
            if (page == 1)
                left = GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.ICONS.BACK-TO").get();
            else
                left = GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.ICONS.PAGE-LEFT").replace("%page%", String.valueOf(page - 1)).get();
            inventory.setItem(18, left);

            // Right navigation
            ItemStack right;
            if (PageUtil.isPageValid(holograms.size(), page + 1, spaces))
                right = GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.ICONS.PAGE-RIGHT").replace("%page%", String.valueOf(page + 1)).get();
            else
                right = GUIManager.getFILLER_ITEM();
            inventory.setItem(26, right);
        }

        for (Map.Entry<Integer, Inventory> entry : new LinkedHashMap<>(gui).entrySet()) {
            if (newGui.containsKey(entry.getKey())) {
                continue;
            }

            gui.remove(entry.getKey());
            for (Player player : inGuiPlayers.keySet()) {
                if (inGuiPlayers.get(player) == entry.getKey()) {
                    open(player, entry.getKey() - 1);
                }
            }
        }

        gui.putAll(newGui);
        hologramSlots.clear();
        hologramSlots.putAll(newHologramSlots);

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        ClickType click = e.getClick();
        int slot = e.getRawSlot();
        ItemStack currentItem = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (currentItem == null) return;
        if (currentItem.equals(GUIManager.getFILLER_ITEM())) return;

        int page = inGuiPlayers.getOrDefault(player, 1);
        if (slot == 18) {
            if (page == 1) {
                GUIManager.getInstance().searchGUI(GUIType.Setup_Hub).open(player);
            } else if (gui.containsKey(page - 1)) {
                open(player, page - 1);
            }
        } else if (slot == 26) {
            if (gui.containsKey(page + 1)) {
                open(player, page + 1);
            }
        } else if (hologramSlots.containsKey(page) && hologramSlots.get(page).containsKey(slot)) {
            Hologram hologram = hologramSlots.get(page).get(slot);

            if (click.isRightClick())
                player.teleport(hologram.getBaseLocation().clone().subtract(0, -2, 0));
            else
                HologramSetupManager.getInstance().getHologramSetupGUIs().get(hologram).get(GUIType.Hologram_Main).open(player);
        }
    }

    private ItemStack getSummaryHologramMainItem(Hologram hologram) {
        return GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.ICONS.HOLOGRAM-ICON")
                .replace("%hologramName%", hologram.getName())
                .replace("%state%", (hologram.isEnabled() ? GUIFile.getString("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.ICONS.HOLOGRAM-ICON.STATUS-NAMES.ENABLED") : GUIFile.getString("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.ICONS.HOLOGRAM-ICON.STATUS-NAMES.DISABLED")))
                .replace("%type%", (hologram.getHologramType() != null ? hologram.getHologramType().getName() : GUIFile.getString("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.ICONS.HOLOGRAM-ICON.STATUS-NAMES.TYPE-NULL")))
                .replace("%statsShow%", String.valueOf(hologram.getShowStat()))
                .get();
    }

}