package dev.nandi0813.practice.manager.gui.setup.hologram;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.leaderboard.hologram.Hologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramManager;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class HologramSummaryGui extends GUI {

    @Getter
    private final Map<Integer, Hologram> hologramSlots = new HashMap<>();

    public HologramSummaryGui() {
        super(GUIType.Hologram_Summary);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.TITLE"), 3));

        build();
    }

    @Override
    public void build() {
        // Frame
        for (int i : new int[]{19, 20, 21, 22, 23, 24, 25, 26})
            gui.get(1).setItem(i, GUIManager.getFILLER_ITEM());

        // Back to Manager Icon
        gui.get(1).setItem(18, GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MANAGER.ICONS.BACK-TO").get());

        update();
    }

    @Override
    public void update() {
        // Clear
        hologramSlots.clear();
        for (int i = 0; i < 18; i++) gui.get(1).setItem(i, null);

        // Set the hologram icons - sort alphanumerically by internal name (case-insensitive)
        HologramManager.getInstance().getHolograms()
                .stream()
                .sorted(Comparator.comparing(Hologram::getName, String::compareToIgnoreCase))
                .forEach(hologram -> {
                    int slot = gui.get(1).firstEmpty();

                    gui.get(1).setItem(slot, this.getSummaryHologramMainItem(hologram));
                    hologramSlots.put(slot, hologram);
                });

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

        if (inventory.getSize() > slot && currentItem != null && !currentItem.equals(GUIManager.getFILLER_ITEM())) {
            if (slot == 18)
                GUIManager.getInstance().searchGUI(GUIType.Setup_Hub).open(player);
            else if (hologramSlots.containsKey(slot)) {
                Hologram hologram = hologramSlots.get(slot);

                if (click.isRightClick())
                    player.teleport(hologram.getBaseLocation().clone().subtract(0, -2, 0));
                else
                    HologramSetupManager.getInstance().getHologramSetupGUIs().get(hologram).get(GUIType.Hologram_Main).open(player);
            }
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
