package dev.nandi0813.practice.manager.playerkit.guis;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.playerkit.PlayerKitEditing;
import dev.nandi0813.practice.manager.playerkit.PlayerKitManager;
import dev.nandi0813.practice.manager.playerkit.PlayerKitUtil;
import dev.nandi0813.practice.manager.playerkit.StaticItems;
import dev.nandi0813.practice.manager.playerkit.items.KitItem;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.PageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CategoryGUI extends GUI {

    private static final int ITEMS_PER_PAGE = (StaticItems.CATEGORY_GUI_PAGE_SIZE - 1) * 9;
    private static final int LEFT_NAV_SLOT = (StaticItems.CATEGORY_GUI_PAGE_SIZE - 1) * 9;
    private static final int RIGHT_NAV_SLOT = (StaticItems.CATEGORY_GUI_PAGE_SIZE * 9) - 1;

    private final String title;
    private final List<ItemStack> items = new ArrayList<>();

    public CategoryGUI(GUIType type, String title, List<String> items) {
        super(type);
        this.title = title;

        for (String item : items) {
            ItemStack itemStack = PlayerKitUtil.getItem(item);
            this.items.add(Objects.requireNonNullElseGet(itemStack, () -> new ItemStack(Material.AIR)));
        }

        this.build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        int page = 1;
        do {
            Inventory inventory = InventoryUtil.createInventory(title, StaticItems.CATEGORY_GUI_PAGE_SIZE);
            this.gui.put(page, inventory);
            List<ItemStack> pageItems = PageUtil.getPageItems(items, page, ITEMS_PER_PAGE);

            if (page == 1) {
                inventory.setItem(LEFT_NAV_SLOT, StaticItems.CATEGORY_GUI_PAGE_BACK_ICON.get());
            } else {
                inventory.setItem(LEFT_NAV_SLOT, StaticItems.CATEGORY_GUI_PAGE_PREVIOUS_ICON.get());
            }

            if (PageUtil.getMaxPage(items, ITEMS_PER_PAGE) > 1 && PageUtil.isPageValid(items, page + 1, ITEMS_PER_PAGE)) {
                inventory.setItem(RIGHT_NAV_SLOT, StaticItems.CATEGORY_GUI_PAGE_NEXT_ICON.get());
            }

            for (int i = 0; i < pageItems.size(); i++) {
                inventory.setItem(i, pageItems.get(i));
            }

            page++;
        } while (PageUtil.isPageValid(items, page, ITEMS_PER_PAGE));

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int page = inGuiPlayers.get(player);

        if (e.getClickedInventory() == null) return;
        int slot = e.getRawSlot();
        if (e.getView().getTopInventory().getSize() <= slot) {
            return;
        }

        if (slot == LEFT_NAV_SLOT && page == 1) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Category).open(player);
        } else if (slot == RIGHT_NAV_SLOT && gui.containsKey(page + 1)) {
            open(player, page + 1);
        } else if (slot == LEFT_NAV_SLOT && gui.containsKey(page - 1)) {
            open(player, page - 1);
        } else {
            ItemStack item = gui.get(page).getItem(slot);
            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            PlayerKitEditing playerKitEditing = PlayerKitManager.getInstance().getEditing().get(player);
            KitItem kitItem = playerKitEditing.getKitItem();
            kitItem.setItemStack(item.clone());

            GUI mainGUI = playerKitEditing.getCustomLadder().getMainGUI();
            mainGUI.update();
            mainGUI.open(player);
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        PlayerKitEditing playerKitEditing = PlayerKitManager.getInstance().getEditing().get(player);
        GUI mainGUI = playerKitEditing.getCustomLadder().getMainGUI();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (GUIManager.getInstance().getOpenGUI().containsKey(player)) {
                return;
            }

            mainGUI.open(player);
        }, 5L);
    }

}
