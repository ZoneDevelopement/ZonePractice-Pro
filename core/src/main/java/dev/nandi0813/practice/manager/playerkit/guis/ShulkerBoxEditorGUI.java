package dev.nandi0813.practice.manager.playerkit.guis;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.playerkit.PlayerKitEditing;
import dev.nandi0813.practice.manager.playerkit.PlayerKitManager;
import dev.nandi0813.practice.manager.playerkit.StaticItems;
import dev.nandi0813.practice.manager.playerkit.items.KitItem;
import dev.nandi0813.practice.util.Common;
import org.bukkit.enchantments.Enchantment;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Arrays;

/**
 * Edits the contents of a shulker box already placed in a kit slot.
 *
 * Layout (5 rows, 45 slots):
 *   Rows 0-2 (slots 0-26): the 27 shulker content slots
 *   Row 3 (slots 27-35):   fillers
 *   Row 4: back (36), clear (40)
 */
public class ShulkerBoxEditorGUI extends GUI {

    private static final int ROWS      = 5;
    private static final int BACK_SLOT   = 36;
    private static final int CLEAR_SLOT  = 40;
    private static final int REMOVE_SLOT = 44;

    /** The kit slot item that contains the shulker box */
    private final KitItem shulkerKitItem;
    /** GUI to return to when done */
    private final GUI returnGui;
    /** The 27 content slots — mirrors what's inside the shulker's BlockStateMeta */
    private final ItemStack[] contents = new ItemStack[27];

    /**
     * When the player is editing a specific slot via EnchantGUI or AmountChangeGUI,
     * these track which slot is being edited and the KitItem being modified.
     * Kept here (not in PlayerKitEditing) so backTo.update() calls can sync
     * the item without destroying the context.
     */
    private int    activeSlot    = -1;
    private KitItem activeKitItem = null;

    private static final ItemStack FILLER;
    static {
        FILLER = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var m = FILLER.getItemMeta();
        if (m != null) { m.displayName(net.kyori.adventure.text.Component.text(" ")); FILLER.setItemMeta(m); }
    }

    public ShulkerBoxEditorGUI(GUI returnGui, KitItem shulkerKitItem) {
        super(GUIType.PlayerCustom_ShulkerEditor);
        this.returnGui = returnGui;
        this.shulkerKitItem = shulkerKitItem;

        // Load existing contents from the shulker's BlockStateMeta
        loadContents();

        String title = StringUtil.CC("&8Shulker Box Contents");
        this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
        build();
    }

    private void loadContents() {
        Arrays.fill(contents, null);
        ItemStack shulkerItem = shulkerKitItem.get();
        if (shulkerItem == null || shulkerItem.getType() == Material.AIR) return;
        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta bsm)) return;
        if (!(bsm.getBlockState() instanceof ShulkerBox box)) return;

        Inventory inv = box.getInventory();
        for (int i = 0; i < Math.min(27, inv.getSize()); i++) {
            contents[i] = inv.getItem(i);
        }
    }

    private void saveContents() {
        ItemStack shulkerItem = shulkerKitItem.get();
        if (shulkerItem == null || shulkerItem.getType() == Material.AIR) return;
        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta bsm)) return;
        if (!(bsm.getBlockState() instanceof ShulkerBox box)) return;

        box.getInventory().clear();
        for (int i = 0; i < 27; i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                box.getInventory().setItem(i, contents[i].clone());
            }
        }
        bsm.setBlockState(box);
        shulkerItem.setItemMeta(bsm);
        shulkerKitItem.setItemStack(shulkerItem);
    }

    @Override public void build() { update(); }

    @Override
    public void update() {
        // If a slot is being edited via EnchantGUI / AmountChangeGUI, sync it now.
        // We do NOT clear activeSlot here because update() is called many times
        // during enchanting (once per click). We only clear on open().
        if (activeSlot >= 0 && activeSlot < 27 && activeKitItem != null) {
            ItemStack updated = activeKitItem.get();
            contents[activeSlot] = (updated != null && updated.getType() != Material.AIR)
                    ? updated.clone() : null;
            saveContents();
        }

        Inventory inv = gui.get(1);
        inv.clear();

        // Shulker content slots 0-26
        for (int i = 0; i < 27; i++) {
            ItemStack c = contents[i];
            if (c == null || c.getType() == Material.AIR) {
                inv.setItem(i, buildEmptySlot(i));
            } else {
                inv.setItem(i, buildFilledSlot(c));
            }
        }

        // Fillers row 3
        for (int i = 27; i < 36; i++) inv.setItem(i, FILLER);

        // Back button
        inv.setItem(BACK_SLOT, StaticItems.CATEGORY_GUI_PAGE_BACK_ICON.get());

        // Clear all button
        ItemStack clearItem = new ItemStack(Material.BARRIER);
        var clearMeta = clearItem.getItemMeta();
        if (clearMeta != null) {
            clearMeta.displayName(net.kyori.adventure.text.Component.text(StringUtil.CC("&cClear All Contents")));
            java.util.List<net.kyori.adventure.text.Component> clearLore = new java.util.ArrayList<>();
            clearLore.add(net.kyori.adventure.text.Component.text(StringUtil.CC("&7Removes all items inside.")));
            clearMeta.lore(clearLore);
            clearItem.setItemMeta(clearMeta);
        }
        inv.setItem(CLEAR_SLOT, clearItem);

        // Remove shulker button
        ItemStack removeItem = new ItemStack(Material.SHULKER_BOX);
        var removeMeta = removeItem.getItemMeta();
        if (removeMeta != null) {
            removeMeta.displayName(net.kyori.adventure.text.Component.text(StringUtil.CC("&cRemove Shulker Box")));
            java.util.List<net.kyori.adventure.text.Component> removeLore = new java.util.ArrayList<>();
            removeLore.add(net.kyori.adventure.text.Component.text(StringUtil.CC("&7Removes this shulker box")));
            removeLore.add(net.kyori.adventure.text.Component.text(StringUtil.CC("&7from the kit slot entirely.")));
            removeMeta.lore(removeLore);
            removeItem.setItemMeta(removeMeta);
        }
        inv.setItem(REMOVE_SLOT, removeItem);

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == BACK_SLOT) {
            saveContents();
            returnGui.update();
            returnGui.open(player);
            return;
        }

        if (slot == CLEAR_SLOT) {
            Arrays.fill(contents, null);
            saveContents();
            update(true);
            return;
        }

        if (slot == REMOVE_SLOT) {
            // Remove the shulker from the kit slot entirely and go back to main kit GUI
            shulkerKitItem.reset();
            returnGui.update();
            returnGui.open(player);
            return;
        }

        // Content slots 0-26
        if (slot >= 0 && slot < 27) {
            ClickType click = e.getClick();

            if (click == ClickType.LEFT) {
                // Open item category to pick what goes in this shulker slot
                PlayerKitEditing editing = PlayerKitManager.getInstance().getEditing().get(player);
                if (editing == null) return;

                // Build a KitItem from current slot contents
                KitItem tempKitItem = new KitItem(
                        contents[slot] != null && contents[slot].getType() != Material.AIR
                                ? contents[slot] : new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE),
                        contents[slot]);

                // Store in ShulkerBoxEditorGUI (for EnchantGUI/AmountChangeGUI)
                // AND in PlayerKitEditing (for CategoryGUI routing)
                activeSlot    = slot;
                activeKitItem = tempKitItem;
                editing.setKitItem(tempKitItem);
                editing.setShulkerEditor(this);
                editing.setShulkerSlot(slot);

                GUIManager.getInstance().searchGUI(GUIType.PlayerCustom_Category).open(player);

            } else if (click == ClickType.SHIFT_LEFT) {
                // Remove item
                contents[slot] = null;
                saveContents();
                update(true);

            } else if (click == ClickType.RIGHT) {
                // Right-click: change amount (stackable) OR enchant (non-stackable)
                ItemStack cur = contents[slot];
                if (cur == null || cur.getType() == Material.AIR) return;

                // Store in our own fields so update() can sync without destroying context
                activeSlot    = slot;
                activeKitItem = new KitItem(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE), cur);
                saveContents();

                if (cur.getType().getMaxStackSize() > 1) {
                    new AmountChangeGUI(activeKitItem, this).open(player);
                } else if (canBeEnchanted(cur)) {
                    new EnchantGUI(activeKitItem, this).open(player);
                }

            } else if (click == ClickType.SHIFT_RIGHT) {
                // Shift-right: always enchant
                ItemStack cur = contents[slot];
                if (cur == null || cur.getType() == Material.AIR) return;
                if (!canBeEnchanted(cur)) return;

                activeSlot    = slot;
                activeKitItem = new KitItem(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE), cur);
                saveContents();
                new EnchantGUI(activeKitItem, this).open(player);
            }
        }
    }

    @Override
    public void open(Player player) {
        // Clear any pending slot edit when player actually returns to this GUI
        activeSlot    = -1;
        activeKitItem = null;

        // Also clear the PlayerKitEditing shulker context so CategoryGUI routes correctly
        PlayerKitEditing ed = PlayerKitManager.getInstance().getEditing().get(player);
        if (ed != null && ed.getShulkerEditor() == this) {
            ed.clearShulkerContext();
        }

        super.open(player);
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        saveContents();
        Player player = (Player) e.getPlayer();
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (!GUIManager.getInstance().getOpenGUI().containsKey(player))
                returnGui.open(player);
        }, 5L);
    }

    /** Called when the item category returns an item to place into this shulker slot. */
    public void onItemSelected(int shulkerSlot, ItemStack item) {
        contents[shulkerSlot] = (item != null && item.getType() != Material.AIR) ? item.clone() : null;
        saveContents();
    }

    private ItemStack buildFilledSlot(ItemStack item) {
        ItemStack display = item.clone();
        var meta = display.getItemMeta();
        if (meta == null) return display;

        // Build action hints as lore appended below existing lore
        java.util.List<net.kyori.adventure.text.Component> lore = meta.lore() != null
                ? new java.util.ArrayList<>(meta.lore())
                : new java.util.ArrayList<>();

        lore.add(net.kyori.adventure.text.Component.text(StringUtil.CC("")));
        lore.add(net.kyori.adventure.text.Component.text(StringUtil.CC("&eLeft-click &7to replace item")));
        lore.add(net.kyori.adventure.text.Component.text(StringUtil.CC("&eShift-click &7to remove")));

        boolean stackable = item.getType().getMaxStackSize() > 1;
        boolean enchantable = canBeEnchanted(item);

        if (stackable) {
            lore.add(net.kyori.adventure.text.Component.text(StringUtil.CC("&eRight-click &7to change amount")));
        }
        if (enchantable) {
            if (stackable) {
                lore.add(net.kyori.adventure.text.Component.text(StringUtil.CC("&eShift-right-click &7to enchant")));
            } else {
                lore.add(net.kyori.adventure.text.Component.text(StringUtil.CC("&eRight-click &7to enchant")));
            }
        }

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private static boolean canBeEnchanted(ItemStack item) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) return false;
        for (Enchantment e : Common.getAllEnchantments()) {
            if (e.canEnchantItem(item)) return true;
        }
        return false;
    }

    private static ItemStack buildEmptySlot(int index) {
        ItemStack item = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(net.kyori.adventure.text.Component.text(StringUtil.CC("&7Slot " + (index + 1))));
            item.setItemMeta(meta);
        }
        return item;
    }
}
