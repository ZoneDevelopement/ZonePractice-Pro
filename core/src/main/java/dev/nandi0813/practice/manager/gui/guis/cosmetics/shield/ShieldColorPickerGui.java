package dev.nandi0813.practice.manager.gui.guis.cosmetics.shield;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.shield.ShieldLayout;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Color picker for either:
 *   - Base color of a shield layout (layerIndex == -1)
 *   - Color of a specific pattern layer (layerIndex >= 0, can be a new layer being added)
 *     After picking a color → if base color: save and return to editor.
 *                          if layer: open ShieldPatternPickerGui with the chosen color.
 */
public class ShieldColorPickerGui extends GUI {

    private static final ItemStack FILLER = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    private static final ItemStack BACK   = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BACK-TO").get();

    private static final int ROWS      = 5;
    private static final int BACK_SLOT = 36;

    /* 16 colors in a 4×4 block, rows 1-2, centred */
    private static final int[] COLOR_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29
    };
    private static final DyeColor[] DYE_COLORS = DyeColor.values(); // exactly 16

    private final Profile  profile;
    private final int      layoutIndex;
    /** Pre-selected color when editing an existing layer. Null when creating new. */
    private final DyeColor preselected;
    /** -1 = editing base color. >=0 = editing/adding this layer index. */
    private final int      layerIndex;
    private final GUI      backToGui;

    public ShieldColorPickerGui(Profile profile, int layoutIndex,
                                DyeColor preselected, int layerIndex, GUI backToGui) {
        super(GUIType.Cosmetics_Shield_ColorPicker);
        this.profile     = profile;
        this.layoutIndex = layoutIndex;
        this.preselected = preselected;
        this.layerIndex  = layerIndex;
        this.backToGui   = backToGui;

        boolean isBase = (layerIndex == -1);
        String title = isBase
                ? GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.COLOR-PICKER.BASE-TITLE",  "&8Pick Base Color")
                : GUIFile.getConfig().getString("GUIS.COSMETICS.SHIELD.COLOR-PICKER.LAYER-TITLE", "&8Pick Layer Color");
        this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
        build();
    }

    @Override public void build() { update(); }

    @Override
    public void update() {
        Inventory inv = gui.get(1);
        inv.clear();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, FILLER);

        for (int i = 0; i < COLOR_SLOTS.length && i < DYE_COLORS.length; i++) {
            inv.setItem(COLOR_SLOTS[i], buildColorItem(DYE_COLORS[i]));
        }

        inv.setItem(BACK_SLOT, BACK != null ? BACK : new ItemStack(Material.ARROW));
        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == BACK_SLOT) { backToGui.update(true); backToGui.open(player); return; }

        for (int i = 0; i < COLOR_SLOTS.length && i < DYE_COLORS.length; i++) {
            if (slot == COLOR_SLOTS[i]) {
                handleColorPick(player, DYE_COLORS[i]);
                return;
            }
        }
    }

    private void handleColorPick(Player player, DyeColor color) {
        ShieldLayout layout = getLayout();
        if (layout == null) return;

        if (layerIndex == -1) {
            // Base color
            layout.setBaseColor(color);
            profile.saveData();
            if (profile.getCosmeticsData().getActiveShieldLayoutIndex() == layoutIndex) {
                ShieldCosmeticsUtil.applyShieldToPlayer(player);
            }
            backToGui.update(true);
            backToGui.open(player);
        } else {
            // Layer color picked — now pick pattern
            new ShieldPatternPickerGui(profile, layoutIndex, color, layerIndex, backToGui).open(player);
        }
    }

    private ItemStack buildColorItem(DyeColor color) {
        Material wool = ShieldEditorGui.dyeToWool(color);
        GUIItem item  = new GUIItem(wool);
        boolean active = (color == preselected);

        String prefix = active ? "&a✔ " : "&f";
        item.setName(prefix + fmt(color.name()));
        List<String> lore = new ArrayList<>();
        lore.add(active ? "&7Currently selected." : "&eClick to select.");
        item.setLore(lore);
        if (active) item.setGlowing(true);
        return item.get();
    }

    private ShieldLayout getLayout() {
        List<ShieldLayout> layouts = profile.getCosmeticsData().getShieldLayouts();
        if (layoutIndex < 0 || layoutIndex >= layouts.size()) return null;
        return layouts.get(layoutIndex);
    }

    private static String fmt(String raw) {
        String lower = raw.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
