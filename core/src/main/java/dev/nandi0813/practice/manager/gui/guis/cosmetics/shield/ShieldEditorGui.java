package dev.nandi0813.practice.manager.gui.guis.cosmetics.shield;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.shield.ShieldLayout;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Shield editor with a clear layout: preview + controls on top, 6 layer slots centered.
 */
public class ShieldEditorGui extends GUI {

    /* ── Slot map ─────────────────────────────────────────────────── */
    private static final int ROWS          = 6;
    private static final int PREVIEW_SLOT  = 4;   // row 1 center
    private static final int BASE_COLOR_SLOT = 2; // row 1 left-side control
    private static final int APPLY_SLOT    = 6;   // row 1 right-side control
    private static final int ADD_LAYER_SLOT = 24; // row 3 right utility
    private static final int REMOVE_LAYER_SLOT = 33; // row 4 right utility
    private static final int BACK_SLOT     = 45;  // row 6

    /* Two rows of three layer slots (max 6) */
    private static final int[] LAYER_SLOTS = {19, 20, 21, 28, 29, 30};

    private static final ItemStack FILLER = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    private static final ItemStack BACK   = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BACK-TO").get();

    private final Profile profile;
    /** Index into profile.getCosmeticsData().getShieldLayouts() */
    private final int     layoutIndex;
    private final GUI     backToGui;

    public ShieldEditorGui(Profile profile, int layoutIndex, GUI backToGui) {
        super(GUIType.Cosmetics_Shield_Editor);
        this.profile     = profile;
        this.layoutIndex = layoutIndex;
        this.backToGui   = backToGui;

        ShieldLayout layout = getLayout();
        String layoutName   = layout != null ? layout.getName() : "Shield";
        String title = GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.EDITOR.TITLE",
                "&8Editing: &e%name%").replace("%name%", layoutName);
        this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
        build();
    }

    @Override public void build() { update(); }

    @Override
    public void update() {
        Inventory inv = gui.get(1);
        inv.clear();
        for (int i = 0; i < inv.getSize(); i++) {
            if (isLayerSlot(i)) {
                continue;
            }
            inv.setItem(i, FILLER);
        }

        ShieldLayout layout = getLayout();
        if (layout == null) { backToGui.open(null); return; }

        boolean isActive = (profile.getCosmeticsData().getActiveShieldLayoutIndex() == layoutIndex);

        // Preview shield
        inv.setItem(PREVIEW_SLOT, buildPreviewShield(layout));

        // Base color button
        inv.setItem(BASE_COLOR_SLOT, buildBaseColorButton(layout));

        // Apply / Unapply button
        inv.setItem(APPLY_SLOT, buildApplyButton(isActive));

        // Layer slots (unused ones intentionally remain empty for readability)
        for (int i = 0; i < LAYER_SLOTS.length && i < layout.getLayers().size(); i++) {
            inv.setItem(LAYER_SLOTS[i], buildLayerItem(layout.getLayers().get(i), i));
        }

        // Add layer button (only if below max)
        boolean canAdd = layout.getLayers().size() < ShieldLayout.MAX_LAYERS;
        GUIItem addBtn = new GUIItem(canAdd ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        addBtn.setName(canAdd
                ? GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.EDITOR.ADD-LAYER.NAME", "&aAdd Layer")
                : GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.EDITOR.MAX-LAYERS.NAME", "&cMax Layers Reached"));
        List<String> addLore = new ArrayList<>();
        addLore.add("&7Layers: &f" + layout.getLayers().size() + "&7/&f" + ShieldLayout.MAX_LAYERS);
        if (canAdd) {
            addLore.add("&eClick to add a new layer.");
            addLore.add("&7Step 1: choose color, Step 2: choose pattern.");
        }
        addBtn.setLore(addLore);
        inv.setItem(ADD_LAYER_SLOT, addBtn.get());

        // Remove top layer button
        boolean canRemove = !layout.getLayers().isEmpty();
        GUIItem removeBtn = new GUIItem(canRemove ? Material.ORANGE_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE);
        removeBtn.setName(canRemove
                ? GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.EDITOR.REMOVE-LAYER.NAME", "&cRemove Top Layer")
                : "&8No layers to remove");
        if (canRemove) {
            List<String> remLore = new ArrayList<>();
            remLore.add("&7Removes the most recently added layer.");
            remLore.add("&cClick to remove.");
            removeBtn.setLore(remLore);
        }
        inv.setItem(REMOVE_LAYER_SLOT, removeBtn.get());

        // Back
        inv.setItem(BACK_SLOT, BACK != null ? BACK : new ItemStack(Material.ARROW));

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        ShieldLayout layout = getLayout();
        if (layout == null) return;

        if (slot == BACK_SLOT) { backToGui.update(true); backToGui.open(player); return; }

        if (slot == APPLY_SLOT) {
            handleApply(player);
            return;
        }

        if (slot == BASE_COLOR_SLOT) {
            new ShieldColorPickerGui(profile, layoutIndex, null, -1, this).open(player);
            return;
        }

        if (slot == ADD_LAYER_SLOT) {
            if (layout.getLayers().size() >= ShieldLayout.MAX_LAYERS) {
                Common.sendMMMessage(player, GUIFile.getConfig().getString(
                        "GUIS.COSMETICS.SHIELD.EDITOR.MAX-LAYERS.MESSAGE", "<red>Maximum layers reached!"));
                return;
            }
            // Open color picker for new layer (layer index = layers.size() = about to be added)
            new ShieldColorPickerGui(profile, layoutIndex, null, layout.getLayers().size(), this).open(player);
            return;
        }

        if (slot == REMOVE_LAYER_SLOT) {
            if (layout.removeTopLayer()) {
                profile.saveData();
                if (profile.getCosmeticsData().getActiveShieldLayoutIndex() == layoutIndex) {
                    ShieldCosmeticsUtil.applyShieldToPlayer(player);
                }
                update(true);
            }
            return;
        }

        // Layer slot clicked → open layer editor (color+pattern picker for that index)
        for (int i = 0; i < LAYER_SLOTS.length && i < layout.getLayers().size(); i++) {
            if (slot == LAYER_SLOTS[i]) {
                new ShieldColorPickerGui(profile, layoutIndex,
                        layout.getLayers().get(i).color(), i, this).open(player);
                return;
            }
        }
    }

    // ── Apply / unapply ──────────────────────────────────────────────

    private void handleApply(Player player) {
        int current = profile.getCosmeticsData().getActiveShieldLayoutIndex();
        if (current == layoutIndex) {
            // Unapply
            profile.getCosmeticsData().setActiveShieldLayoutIndex(-1);
            ShieldCosmeticsUtil.applyShieldToPlayer(player);
            Common.sendMMMessage(player, GUIFile.getConfig().getString(
                    "GUIS.COSMETICS.SHIELD.EDITOR.UNAPPLIED-MESSAGE", "<gray>Shield cosmetic removed."));
        } else {
            profile.getCosmeticsData().setActiveShieldLayoutIndex(layoutIndex);
            ShieldCosmeticsUtil.applyShieldToPlayer(player);
            Common.sendMMMessage(player, GUIFile.getConfig().getString(
                    "GUIS.COSMETICS.SHIELD.EDITOR.APPLIED-MESSAGE",
                    "<green>Shield layout applied!"));
        }
        profile.saveData();
        update(true);
    }

    // ── Item builders ────────────────────────────────────────────────

    private ItemStack buildPreviewShield(ShieldLayout layout) {
        ItemStack shield = new ItemStack(Material.SHIELD);
        ShieldCosmeticsUtil.applyLayoutToItem(shield, layout);
        ItemMeta meta = shield.getItemMeta();
        if (meta != null) {
            meta.displayName(tc("&eShield Preview"));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(tc("&7Base: &f" + (layout.getBaseColor() != null ? fmt(layout.getBaseColor().name()) : "White")));
            lore.add(tc("&7Layers: &f" + layout.getLayers().size() + "&7/&f" + ShieldLayout.MAX_LAYERS));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            shield.setItemMeta(meta);
        }
        return shield;
    }

    private ItemStack buildBaseColorButton(ShieldLayout layout) {
        DyeColor base = layout.getBaseColor() != null ? layout.getBaseColor() : DyeColor.WHITE;
        Material wool = dyeToWool(base);
        GUIItem item  = new GUIItem(wool);
        item.setName("&bBase Color: &f" + fmt(base.name()));
        List<String> lore = new ArrayList<>();
        lore.add("&7The background color of your shield.");
        lore.add("&eClick to change.");
        item.setLore(lore);
        return item.get();
    }

    private ItemStack buildApplyButton(boolean isActive) {
        Material mat = isActive ? Material.LIME_WOOL : Material.GRAY_WOOL;
        GUIItem item = new GUIItem(mat);
        item.setName(isActive ? "&a&lLayout Active &7(Click to unapply)" : "&eApply This Layout");
        List<String> lore = new ArrayList<>();
        lore.add(isActive ? "&7This design is on your shield." : "&7Apply this design to your shield.");
        item.setLore(lore);
        if (isActive) item.setGlowing(true);
        return item.get();
    }

    private ItemStack buildLayerItem(ShieldLayout.PatternLayer layer, int index) {
        Material banner = dyeToBanner(layer.color());
        GUIItem item = new GUIItem(banner);
        item.setName("&fLayer " + (index + 1) + ": &e" + getPatternDisplayName(layer.pattern()));

        // Apply pattern to the banner icon
        ItemStack stack = item.get();
        if (stack.getItemMeta() instanceof org.bukkit.inventory.meta.BannerMeta bm) {
            bm.addPattern(new org.bukkit.block.banner.Pattern(layer.color(), layer.pattern()));
            stack.setItemMeta(bm);
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(tc("&7Color: &f" + fmt(layer.color().name())));
            lore.add(tc("&7Pattern: &f" + getPatternDisplayName(layer.pattern())));
            lore.add(tc(""));
            lore.add(tc("&eClick to edit this layer."));
            lore.add(tc("&7Use Add Layer for a new slot."));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private ShieldLayout getLayout() {
        List<ShieldLayout> layouts = profile.getCosmeticsData().getShieldLayouts();
        if (layoutIndex < 0 || layoutIndex >= layouts.size()) return null;
        return layouts.get(layoutIndex);
    }

    private static net.kyori.adventure.text.Component tc(String legacy) {
        return net.kyori.adventure.text.Component.text(StringUtil.CC(legacy));
    }

    private static String fmt(String raw) {
        String lower = raw.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String getPatternDisplayName(PatternType pattern) {
        if (pattern == null) {
            return "Unknown";
        }

        var key = RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN).getKey(pattern);
        if (key != null) {
            return fmt(key.getKey());
        }

        return fmt(String.valueOf(pattern));
    }

    private static boolean isLayerSlot(int slot) {
        for (int layerSlot : LAYER_SLOTS) {
            if (layerSlot == slot) {
                return true;
            }
        }
        return false;
    }

    static Material dyeToWool(DyeColor c) {
        return switch (c) {
            case WHITE -> Material.WHITE_WOOL; case ORANGE -> Material.ORANGE_WOOL;
            case MAGENTA -> Material.MAGENTA_WOOL; case LIGHT_BLUE -> Material.LIGHT_BLUE_WOOL;
            case YELLOW -> Material.YELLOW_WOOL; case LIME -> Material.LIME_WOOL;
            case PINK -> Material.PINK_WOOL; case GRAY -> Material.GRAY_WOOL;
            case LIGHT_GRAY -> Material.LIGHT_GRAY_WOOL; case CYAN -> Material.CYAN_WOOL;
            case PURPLE -> Material.PURPLE_WOOL; case BLUE -> Material.BLUE_WOOL;
            case BROWN -> Material.BROWN_WOOL; case GREEN -> Material.GREEN_WOOL;
            case RED -> Material.RED_WOOL; case BLACK -> Material.BLACK_WOOL;
        };
    }

    static Material dyeToBanner(DyeColor c) {
        return switch (c) {
            case WHITE -> Material.WHITE_BANNER; case ORANGE -> Material.ORANGE_BANNER;
            case MAGENTA -> Material.MAGENTA_BANNER; case LIGHT_BLUE -> Material.LIGHT_BLUE_BANNER;
            case YELLOW -> Material.YELLOW_BANNER; case LIME -> Material.LIME_BANNER;
            case PINK -> Material.PINK_BANNER; case GRAY -> Material.GRAY_BANNER;
            case LIGHT_GRAY -> Material.LIGHT_GRAY_BANNER; case CYAN -> Material.CYAN_BANNER;
            case PURPLE -> Material.PURPLE_BANNER; case BLUE -> Material.BLUE_BANNER;
            case BROWN -> Material.BROWN_BANNER; case GREEN -> Material.GREEN_BANNER;
            case RED -> Material.RED_BANNER; case BLACK -> Material.BLACK_BANNER;
        };
    }
}
