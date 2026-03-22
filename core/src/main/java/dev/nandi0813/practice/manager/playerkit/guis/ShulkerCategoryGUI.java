package dev.nandi0813.practice.manager.playerkit.guis;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.playerkit.PlayerKitEditing;
import dev.nandi0813.practice.manager.playerkit.PlayerKitManager;
import dev.nandi0813.practice.manager.playerkit.StaticItems;
import dev.nandi0813.practice.manager.playerkit.items.KitItem;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shows all 16 shulker box colors.
 * Left-click → set that color shulker into the kit slot.
 * Right-click → open ShulkerBoxEditorGUI to edit its contents.
 */
public class ShulkerCategoryGUI extends GUI {

    // Ordered map: display name → Material
    private static final Map<String, Material> SHULKERS = new LinkedHashMap<>();
    static {
        SHULKERS.put("&fWhite Shulker Box",      Material.WHITE_SHULKER_BOX);
        SHULKERS.put("&6Orange Shulker Box",     Material.ORANGE_SHULKER_BOX);
        SHULKERS.put("&dMagenta Shulker Box",    Material.MAGENTA_SHULKER_BOX);
        SHULKERS.put("&bLight Blue Shulker Box", Material.LIGHT_BLUE_SHULKER_BOX);
        SHULKERS.put("&eYellow Shulker Box",     Material.YELLOW_SHULKER_BOX);
        SHULKERS.put("&aLime Shulker Box",       Material.LIME_SHULKER_BOX);
        SHULKERS.put("&dPink Shulker Box",       Material.PINK_SHULKER_BOX);
        SHULKERS.put("&8Gray Shulker Box",       Material.GRAY_SHULKER_BOX);
        SHULKERS.put("&7Light Gray Shulker Box", Material.LIGHT_GRAY_SHULKER_BOX);
        SHULKERS.put("&3Cyan Shulker Box",       Material.CYAN_SHULKER_BOX);
        SHULKERS.put("&5Purple Shulker Box",     Material.PURPLE_SHULKER_BOX);
        SHULKERS.put("&9Blue Shulker Box",       Material.BLUE_SHULKER_BOX);
        SHULKERS.put("&cBrown Shulker Box",      Material.BROWN_SHULKER_BOX);
        SHULKERS.put("&2Green Shulker Box",      Material.GREEN_SHULKER_BOX);
        SHULKERS.put("&cRed Shulker Box",        Material.RED_SHULKER_BOX);
        SHULKERS.put("&0Black Shulker Box",      Material.BLACK_SHULKER_BOX);
    }

    // Slot positions for the 16 shulkers (two rows of 8, centred in a 5-row GUI)
    private static final int[] SHULKER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
    };
    // Overflow to row 3 for the 15th and 16th items
    private static final int[] SHULKER_SLOTS_FULL = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            29, 30
    };

    private static final int BACK_SLOT = 45;
    private static final Material[] SHULKER_MATERIALS = SHULKERS.values().toArray(new Material[0]);
    private static final String[]   SHULKER_NAMES     = SHULKERS.keySet().toArray(new String[0]);

    public ShulkerCategoryGUI() {
        super(GUIType.PlayerCustom_Shulker);
        this.gui.put(1, InventoryUtil.createInventory(
                StaticItems.CATEGORY_SHULKER_TITLE, 6));
        build();
    }

    @Override public void build() { update(); }

    @Override
    public void update() {
        Inventory inv = gui.get(1);
        inv.clear();

        for (int i = 0; i < SHULKER_MATERIALS.length; i++) {
            int slot = SHULKER_SLOTS_FULL[i];
            ItemStack item = buildShulkerItem(SHULKER_MATERIALS[i], SHULKER_NAMES[i]);
            inv.setItem(slot, item);
        }

        inv.setItem(BACK_SLOT, StaticItems.CATEGORY_GUI_PAGE_BACK_ICON.get());
        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == BACK_SLOT) {
            GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Category).open(player);
            return;
        }

        for (int i = 0; i < SHULKER_SLOTS_FULL.length; i++) {
            if (slot == SHULKER_SLOTS_FULL[i]) {
                PlayerKitEditing editing = PlayerKitManager.getInstance().getEditing().get(player);
                if (editing == null) return;
                KitItem kitItem = editing.getKitItem();

                if (e.isRightClick()) {
                    // Right-click → open the shulker contents editor
                    ItemStack current = kitItem.get();
                    if (current.getType() == Material.AIR) {
                        // No item yet — place the shulker first then open editor
                        kitItem.setItemStack(new ItemStack(SHULKER_MATERIALS[i]));
                    }
                    new ShulkerBoxEditorGUI(editing.getCustomLadder().getMainGUI(), kitItem).open(player);
                } else {
                    // Left-click → just set the shulker (keep any existing contents)
                    ItemStack existing = kitItem.get();
                    boolean isShulker = isShulkerBox(existing.getType());
                    if (isShulker && existing.hasItemMeta()) {
                        // Preserve contents, just change color
                        ItemStack recolored = new ItemStack(SHULKER_MATERIALS[i]);
                        org.bukkit.inventory.meta.BlockStateMeta bsm =
                                (org.bukkit.inventory.meta.BlockStateMeta) recolored.getItemMeta();
                        org.bukkit.inventory.meta.BlockStateMeta oldBsm =
                                (existing.getItemMeta() instanceof org.bukkit.inventory.meta.BlockStateMeta b) ? b : null;
                        if (bsm != null && oldBsm != null && oldBsm.getBlockState() instanceof org.bukkit.block.ShulkerBox oldBox) {
                            if (bsm.getBlockState() instanceof org.bukkit.block.ShulkerBox newBox) {
                                // Copy contents slot-by-slot (ShulkerBox has no setInventory in 1.21)
                                org.bukkit.inventory.Inventory src = oldBox.getInventory();
                                org.bukkit.inventory.Inventory dst = newBox.getInventory();
                                for (int s = 0; s < Math.min(src.getSize(), dst.getSize()); s++) {
                                    dst.setItem(s, src.getItem(s));
                                }
                                bsm.setBlockState(newBox);
                            }
                        }
                        recolored.setItemMeta(bsm);
                        kitItem.setItemStack(recolored);
                    } else {
                        kitItem.setItemStack(new ItemStack(SHULKER_MATERIALS[i]));
                    }

                    GUI mainGUI = editing.getCustomLadder().getMainGUI();
                    mainGUI.update();
                    mainGUI.open(player);
                }
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

    private static ItemStack buildShulkerItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(net.kyori.adventure.text.Component.text(
                    dev.nandi0813.practice.util.StringUtil.CC(name)));
            var lore = new java.util.ArrayList<net.kyori.adventure.text.Component>();
            lore.add(net.kyori.adventure.text.Component.text(
                    dev.nandi0813.practice.util.StringUtil.CC("&eLeft-click &7to add to kit")));
            lore.add(net.kyori.adventure.text.Component.text(
                    dev.nandi0813.practice.util.StringUtil.CC("&eRight-click &7to edit contents")));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isShulkerBox(Material m) {
        return m.name().endsWith("_SHULKER_BOX") || m == Material.SHULKER_BOX;
    }
}
