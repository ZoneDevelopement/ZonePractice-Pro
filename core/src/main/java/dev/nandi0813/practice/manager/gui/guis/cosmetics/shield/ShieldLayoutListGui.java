package dev.nandi0813.practice.manager.gui.guis.cosmetics.shield;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.cosmetics.shield.ShieldLayout;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists all of a player's saved shield layouts.
 * Layout slots: 10-16, 19-25, 28-34 (up to 21 layouts).
 * Bottom row: back (slot 45), create-new (slot 49).
 */
public class ShieldLayoutListGui extends GUI {

    private static final ItemStack FILLER = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    private static final ItemStack BACK   = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BACK-TO").get();

    private static final int ROWS      = 6;
    private static final int BACK_SLOT = 45;
    private static final int NEW_SLOT  = 49;

    private static final int[] LAYOUT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final Profile profile;
    private final GUI     backToGui;

    public ShieldLayoutListGui(Profile profile, GUI backToGui) {
        super(GUIType.Cosmetics_Shield_Layouts);
        this.profile   = profile;
        this.backToGui = backToGui;
        String title = GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.LAYOUTS.TITLE", "&8Shield Layouts");
        this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
        build();
    }

    @Override public void build() { update(); }

    @Override
    public void update() {
        Inventory inv = gui.get(1);
        inv.clear();
        for (int i = 0; i < inv.getSize(); i++) {
            if (isLayoutSlot(i)) {
                continue;
            }
            inv.setItem(i, FILLER);
        }

        List<ShieldLayout> layouts = profile.getCosmeticsData().getShieldLayouts();
        int active = profile.getCosmeticsData().getActiveShieldLayoutIndex();

        for (int i = 0; i < LAYOUT_SLOTS.length && i < layouts.size(); i++) {
            inv.setItem(LAYOUT_SLOTS[i], buildLayoutItem(layouts.get(i), i, i == active));
        }

        // "New layout" button
        int maxLayouts = getMaxLayouts(profile.getPlayer().getPlayer());
        boolean canCreate = layouts.size() < maxLayouts;
        GUIItem newItem = new GUIItem(
                GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.LAYOUTS.NEW-BUTTON.NAME",
                        canCreate ? "&aNew Layout" : "&cLayout limit reached"),
                canCreate ? Material.LIME_DYE : Material.RED_DYE);
        List<String> newLore = new ArrayList<>();
        newLore.add("&7Saved: &e" + layouts.size() + "&7/&e" + maxLayouts);
        if (canCreate) newLore.add("&eClick to create a new layout.");
        else newLore.add("&cGet a higher rank for more slots.");
        newItem.setLore(newLore);
        inv.setItem(NEW_SLOT, newItem.get());

        inv.setItem(BACK_SLOT, BACK != null ? BACK : new ItemStack(Material.ARROW));
        updatePlayers();
    }

    private static boolean isLayoutSlot(int slot) {
        for (int layoutSlot : LAYOUT_SLOTS) {
            if (layoutSlot == slot) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == BACK_SLOT) { backToGui.update(true); backToGui.open(player); return; }

        if (!CosmeticsPermissionManager.hasShieldPermission(player)) {
            Common.sendMMMessage(player, GUIFile.getConfig().getString(
                    "GUIS.COSMETICS.SHIELD.NO-PERMISSION-MESSAGE", "<red>You do not have permission to use shield cosmetics."));
            return;
        }

        if (slot == NEW_SLOT) { handleCreateNew(player); return; }

        // Layout slot clicked
        List<ShieldLayout> layouts = profile.getCosmeticsData().getShieldLayouts();
        for (int i = 0; i < LAYOUT_SLOTS.length && i < layouts.size(); i++) {
            if (slot == LAYOUT_SLOTS[i]) {
                if (e.isRightClick()) {
                    // Right-click = delete
                    handleDelete(player, i);
                } else if (e.isShiftClick()) {
                    // Shift-click = rename
                    handleRename(player, i);
                } else {
                    // Left-click = open editor OR apply if already active
                    new ShieldEditorGui(profile, i, this).open(player);
                }
                return;
            }
        }
    }

    private void handleCreateNew(Player player) {
        List<ShieldLayout> layouts = profile.getCosmeticsData().getShieldLayouts();
        int max = getMaxLayouts(player);
        if (layouts.size() >= max) {
            Common.sendMMMessage(player, GUIFile.getConfig().getString(
                    "GUIS.COSMETICS.SHIELD.LAYOUTS.LIMIT-REACHED", "<red>You've reached your layout limit!"));
            return;
        }

        // Ask for a name via AnvilGUI
        new AnvilGUI.Builder()
                .onClose(state -> {})
                .onClick((anvilSlot, state) -> {
                    if (anvilSlot != AnvilGUI.Slot.OUTPUT) return List.of();
                    String name = state.getText().trim();
                    if (name.isEmpty()) name = "Layout " + (layouts.size() + 1);
                    if (name.length() > 24) name = name.substring(0, 24);
                    ShieldLayout newLayout = new ShieldLayout(name, DyeColor.WHITE);
                    profile.getCosmeticsData().getShieldLayouts().add(newLayout);
                    int newIndex = layouts.size() - 1;
                    profile.saveData();
                    final int finalIndex = newIndex;
                    org.bukkit.Bukkit.getScheduler().runTask(ZonePractice.getInstance(),
                            () -> new ShieldEditorGui(profile, finalIndex, this).open(player));
                    return List.of(AnvilGUI.ResponseAction.close());
                })
                .text("My Layout")
                .title(GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.LAYOUTS.NAME-TITLE", "Layout Name"))
                .plugin(ZonePractice.getInstance())
                .open(player);
    }

    private void handleDelete(Player player, int index) {
        List<ShieldLayout> layouts = profile.getCosmeticsData().getShieldLayouts();
        if (index < 0 || index >= layouts.size()) return;
        layouts.remove(index);
        // Fix active index
        int active = profile.getCosmeticsData().getActiveShieldLayoutIndex();
        if (active == index) {
            profile.getCosmeticsData().setActiveShieldLayoutIndex(-1);
            ShieldCosmeticsUtil.applyShieldToPlayer(player); // clear shield
        } else if (active > index) {
            profile.getCosmeticsData().setActiveShieldLayoutIndex(active - 1);
        }
        profile.saveData();
        Common.sendMMMessage(player, GUIFile.getConfig().getString(
                "GUIS.COSMETICS.SHIELD.LAYOUTS.DELETED-MESSAGE", "<green>Layout deleted."));
        update(true);
    }

    private void handleRename(Player player, int index) {
        List<ShieldLayout> layouts = profile.getCosmeticsData().getShieldLayouts();
        if (index < 0 || index >= layouts.size()) return;
        String currentName = layouts.get(index).getName();

        new AnvilGUI.Builder()
                .onClose(state -> {})
                .onClick((anvilSlot, state) -> {
                    if (anvilSlot != AnvilGUI.Slot.OUTPUT) return List.of();
                    String name = state.getText().trim();
                    if (name.isEmpty()) name = currentName;
                    if (name.length() > 24) name = name.substring(0, 24);
                    layouts.get(index).setName(name);
                    profile.saveData();
                    org.bukkit.Bukkit.getScheduler().runTask(ZonePractice.getInstance(),
                            () -> update(true));
                    return List.of(AnvilGUI.ResponseAction.close());
                })
                .text(currentName)
                .title(GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.LAYOUTS.RENAME-TITLE", "Rename Layout"))
                .plugin(ZonePractice.getInstance())
                .open(player);
    }

    // ── Builders ─────────────────────────────────────────────────────

    private ItemStack buildLayoutItem(ShieldLayout layout, int index, boolean active) {
        ItemStack shield = new ItemStack(Material.SHIELD);
        ShieldCosmeticsUtil.applyLayoutToItem(shield, layout);

        var meta = shield.getItemMeta();
        if (meta != null) {
            String nameColor = active ? "&a✔ " : "&e";
            meta.displayName(net.kyori.adventure.text.Component.text(
                    dev.nandi0813.practice.util.StringUtil.CC(nameColor + layout.getName())));

            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            String base = layout.getBaseColor() != null ? formatName(layout.getBaseColor().name()) : "White";
            lore.add(tc("&7Base color: &f" + base));
            lore.add(tc("&7Layers: &f" + layout.getLayers().size() + "&7/&f" + ShieldLayout.MAX_LAYERS));
            if (!layout.getLayers().isEmpty()) {
                lore.add(tc("&8─────────────────"));
                for (int i = 0; i < layout.getLayers().size(); i++) {
                    ShieldLayout.PatternLayer layer = layout.getLayers().get(i);
                    lore.add(tc("&7" + (i + 1) + ". &f" + formatName(layer.color().name())
                            + " &8│ &f" + getPatternDisplayName(layer.pattern())));
                }
            }
            lore.add(tc("&8─────────────────"));
            if (active) {
                lore.add(tc("&a&lCurrently Active"));
            }
            lore.add(tc("&eLeft-click &7to edit"));
            lore.add(tc("&bShift-click &7to rename"));
            lore.add(tc("&cRight-click &7to delete"));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            shield.setItemMeta(meta);
        }
        return ItemCreateUtil.hideItemFlags(shield);
    }

    /** Returns how many layouts the player is allowed to have. */
    public static int getMaxLayouts(Player player) {
        return CosmeticsPermissionManager.getMaxShieldLayouts(player);
    }

    private static net.kyori.adventure.text.Component tc(String legacy) {
        return net.kyori.adventure.text.Component.text(
                dev.nandi0813.practice.util.StringUtil.CC(legacy));
    }

    private static String formatName(String raw) {
        String lower = raw.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String getPatternDisplayName(PatternType pattern) {
        if (pattern == null) {
            return "Unknown";
        }

        NamespacedKey key = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.BANNER_PATTERN)
                .getKey(pattern);

        if (key != null) {
            return formatName(key.getKey());
        }

        return formatName(String.valueOf(pattern));
    }
}
