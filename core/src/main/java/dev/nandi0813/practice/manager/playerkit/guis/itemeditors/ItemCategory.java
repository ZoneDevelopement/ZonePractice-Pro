package dev.nandi0813.practice.manager.playerkit.guis.itemeditors;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.playerkit.DynamicCategory;
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
import java.util.List;

/**
 * The item category picker.
 *
 * Fixed categories (hardcoded GUIType): Armor, Weapons/Tools, Bows, Potions, Shulker Boxes.
 * Dynamic categories: loaded from GUI.ITEMS.CATEGORY-GUI.DYNAMIC-CATEGORIES in playerkit.yml.
 * Adding a new category requires ONLY a config change — no code change.
 */
public class ItemCategory extends ItemEditor {

    public ItemCategory() {
        super(GUIType.PlayerCustom_Category, buildIconList());
        this.gui.put(1, InventoryUtil.createInventory(StaticItems.CATEGORY_GUI_TITLE, StaticItems.CATEGORY_GUI_SIZE));
        this.build();
    }

    /** Builds the icon list: fixed icons + all dynamic category icons (in config order). */
    private static List<EditorIcon> buildIconList() {
        List<EditorIcon> icons = new ArrayList<>(Arrays.asList(
                StaticItems.CATEGORY_GUI_BACK_ICON,
                StaticItems.CATEGORY_GUI_NONE_ICON,
                // Fixed-type categories
                StaticItems.CATEGORY_GUI_ARMOR_ICON,
                StaticItems.CATEGORY_GUI_WEAPON_TOOLS_ICON,
                StaticItems.CATEGORY_GUI_BOWS_ICON,
                StaticItems.CATEGORY_GUI_POTIONS_ICON,
                StaticItems.CATEGORY_GUI_SHULKER_ICON));

        // Dynamic categories (loaded from config)
        for (DynamicCategory cat : PlayerKitManager.getInstance().getDynamicCategories()) {
            icons.add(cat.getIcon());
        }
        return icons;
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        PlayerKitEditing editing = PlayerKitManager.getInstance().getEditing().get(player);
        if (editing == null) return;

        int slot = e.getRawSlot();
        EditorIcon icon = getIcon(slot);
        if (icon == null) return;

        KitItem kitItem = editing.getKitItem();
        GUI mainGUI = editing.getCustomLadder().getMainGUI();

        // ── Back ──────────────────────────────────────────────────────
        if (icon.equals(StaticItems.CATEGORY_GUI_BACK_ICON)) {
            if (editing.isEditingShulker()) {
                dev.nandi0813.practice.manager.playerkit.guis.ShulkerBoxEditorGUI editor = editing.getShulkerEditor();
                editor.update(true);
                editor.open(player);
            } else {
                mainGUI.open(player);
            }
            return;
        }

        // ── None ─────────────────────────────────────────────────────
        if (icon.equals(StaticItems.CATEGORY_GUI_NONE_ICON)) {
            kitItem.reset();
            if (editing.isEditingShulker()) {
                dev.nandi0813.practice.manager.playerkit.guis.ShulkerBoxEditorGUI editor = editing.getShulkerEditor();
                int shulkerSlot = editing.getShulkerSlot();
                editing.clearShulkerContext();
                editor.onItemSelected(shulkerSlot, null);
                editor.update(true);
                editor.open(player);
            } else {
                mainGUI.update();
                mainGUI.open(player);
            }
            return;
        }

        // ── Fixed categories ─────────────────────────────────────────
        if (icon.equals(StaticItems.CATEGORY_GUI_ARMOR_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Armor).open(player);
            return;
        }
        if (icon.equals(StaticItems.CATEGORY_GUI_WEAPON_TOOLS_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Weapons_Tools).open(player);
            return;
        }
        if (icon.equals(StaticItems.CATEGORY_GUI_BOWS_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Bows).open(player);
            return;
        }
        if (icon.equals(StaticItems.CATEGORY_GUI_POTIONS_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Potions).open(player);
            return;
        }
        if (icon.equals(StaticItems.CATEGORY_GUI_SHULKER_ICON)) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Shulker).open(player);
            return;
        }

        // ── Dynamic categories ───────────────────────────────────────
        for (DynamicCategory cat : PlayerKitManager.getInstance().getDynamicCategories()) {
            if (icon.equals(cat.getIcon())) {
                cat.getGui().open(player);
                return;
            }
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        PlayerKitEditing editing = PlayerKitManager.getInstance().getEditing().get(player);
        if (editing == null) return;
        GUI mainGUI = editing.getCustomLadder().getMainGUI();
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (!GUIManager.getInstance().getOpenGUI().containsKey(player))
                mainGUI.open(player);
        }, 5L);
    }
}
