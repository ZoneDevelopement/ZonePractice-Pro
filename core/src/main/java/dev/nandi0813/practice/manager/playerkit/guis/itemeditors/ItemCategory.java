package dev.nandi0813.practice.manager.playerkit.guis.itemeditors;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.playerkit.PlayerKitEditing;
import dev.nandi0813.practice.manager.playerkit.PlayerKitManager;
import dev.nandi0813.practice.manager.playerkit.StaticItems;
import dev.nandi0813.practice.manager.playerkit.items.EditorIcon;
import dev.nandi0813.practice.manager.playerkit.items.KitItem;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class ItemCategory extends ItemEditor {

    public ItemCategory() {
        super(GUIType.PlayerCustom_Category, new ArrayList<>(Arrays.asList(
                StaticItems.CATEGORY_GUI_BACK_ICON,
                StaticItems.CATEGORY_GUI_NONE_ICON,
                StaticItems.CATEGORY_GUI_ARMOR_ICON,
                StaticItems.CATEGORY_GUI_WEAPON_TOOLS_ICON,
                StaticItems.CATEGORY_GUI_BOWS_ICON,
                StaticItems.CATEGORY_GUI_POTIONS_ICON,
                StaticItems.CATEGORY_GUI_FOOD_ICON,
                StaticItems.CATEGORY_GUI_BLOCKS_ICON)));

        this.gui.put(1, InventoryUtil.createInventory(StaticItems.CATEGORY_GUI_TITLE, StaticItems.CATEGORY_GUI_SIZE));

        this.build();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        PlayerKitEditing playerKitEditing = PlayerKitManager.getInstance().getEditing().get(player);
        int slot = e.getRawSlot();
        EditorIcon icon = getIcon(slot);

        if (icon == null) {
            return;
        }

        KitItem kitItem = playerKitEditing.getKitItem();
        GUI mainGUI = playerKitEditing.getCustomLadder().getMainGUI();

        if (icon.equals(StaticItems.CATEGORY_GUI_BACK_ICON)) {
            mainGUI.open(player);
        } else if (icon.equals(StaticItems.CATEGORY_GUI_NONE_ICON)) {
            kitItem.reset();
            mainGUI.update();
            mainGUI.open(player);
        } else if (icon.equals(StaticItems.CATEGORY_GUI_ARMOR_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Armor).open(player);
        } else if (icon.equals(StaticItems.CATEGORY_GUI_WEAPON_TOOLS_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Weapons_Tools).open(player);
        } else if (icon.equals(StaticItems.CATEGORY_GUI_BOWS_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Bows).open(player);
        } else if (icon.equals(StaticItems.CATEGORY_GUI_POTIONS_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Potions).open(player);
        } else if (icon.equals(StaticItems.CATEGORY_GUI_FOOD_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Food).open(player);
        } else if (icon.equals(StaticItems.CATEGORY_GUI_BLOCKS_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Blocks).open(player);
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        PlayerKitEditing playerKitEditing = PlayerKitManager.getInstance().getEditing().get(player);
        GUI mainGUI = playerKitEditing.getCustomLadder().getMainGUI();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (GUIManager.getInstance().getOpenGUI().containsKey(player))
                return;

            mainGUI.open(player);
        }, 5L);
    }

}
