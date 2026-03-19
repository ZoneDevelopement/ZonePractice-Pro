package dev.nandi0813.practice.manager.gui.guis.cosmetics.armortrim;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorSlot;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorTrimTier;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArmorPieceHubGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BACK-TO").get();
    
    private static final int INVENTORY_ROWS = GUIFile.getConfig().getInt("GUIS.COSMETICS.ARMOR-PIECE-HUB.INVENTORY-ROWS", 4);
    private static final int BACK_SLOT = GUIFile.getConfig().getInt("GUIS.COSMETICS.ARMOR-PIECE-HUB.SLOTS.BACK", 27);
    private static final int PREVIEW_SLOT = GUIFile.getConfig().getInt("GUIS.COSMETICS.ARMOR-PIECE-HUB.SLOTS.PREVIEW", 13);
    private static final int PATTERN_MENU_SLOT = GUIFile.getConfig().getInt("GUIS.COSMETICS.ARMOR-PIECE-HUB.SLOTS.PATTERN-MENU", 20);
    private static final int MATERIAL_MENU_SLOT = GUIFile.getConfig().getInt("GUIS.COSMETICS.ARMOR-PIECE-HUB.SLOTS.MATERIAL-MENU", 24);
    
    private static final Material DEFAULT_PATTERN_BUTTON_MATERIAL = Material.valueOf(
        GUIFile.getConfig().getString("GUIS.COSMETICS.ARMOR-PIECE-HUB.PATTERN-SELECTION-BUTTON.DEFAULT-MATERIAL", "SMITHING_TABLE")
    );
    private static final Material DEFAULT_MATERIAL_BUTTON_MATERIAL = Material.valueOf(
        GUIFile.getConfig().getString("GUIS.COSMETICS.ARMOR-PIECE-HUB.MATERIAL-SELECTION-BUTTON.DEFAULT-MATERIAL", "ANVIL")
    );

    private final Profile profile;
    private final ArmorSlot armorSlot;
    private final GUI backToGui;

    public ArmorPieceHubGui(Profile profile, ArmorSlot armorSlot, GUI backToGui) {
        super(resolveGuiType(armorSlot));
        this.profile = profile;
        this.armorSlot = armorSlot;
        this.backToGui = backToGui;
        this.gui.put(1, InventoryUtil.createInventory("&8" + armorSlot.getDisplayName() + " Cosmetics", INVENTORY_ROWS));
        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);
        inventory.clear();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, FILLER_ITEM);
        }

        ArmorTrimTier tier = profile.getCosmeticsData().getActiveTier();
        TrimPattern pattern = profile.getCosmeticsData().getPattern(tier, armorSlot);
        TrimMaterial material = profile.getCosmeticsData().getMaterial(tier, armorSlot);

        inventory.setItem(BACK_SLOT, BACK_TO_ITEM == null ? new ItemStack(Material.ARROW) : BACK_TO_ITEM);
        inventory.setItem(PREVIEW_SLOT, buildPreviewItem(tier, pattern, material));
        inventory.setItem(PATTERN_MENU_SLOT, buildPatternSelectionItem(pattern));
        inventory.setItem(MATERIAL_MENU_SLOT, buildMaterialSelectionItem(material));

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == BACK_SLOT) {
            backToGui.update(true);
            backToGui.open(player);
            return;
        }

        if (slot == PATTERN_MENU_SLOT) {
            new PatternSelectionGui(profile, armorSlot, this).open(player);
            return;
        }

        if (slot == MATERIAL_MENU_SLOT) {
            new MaterialSelectionGui(profile, armorSlot, this).open(player);
        }
    }

    private ItemStack buildPreviewItem(ArmorTrimTier tier, TrimPattern pattern, TrimMaterial material) {
        Material previewMaterial = tier.getMaterial(armorSlot);
        GUIItem item = new GUIItem(previewMaterial);
        item.setName(GUIFile.getConfig().getString("GUIS.COSMETICS.ARMOR-PIECE-HUB.PREVIEW-ITEM.NAME", "&eCurrent Preview"));

        ItemStack itemStack = item.get();
        if (armorSlot != ArmorSlot.SHIELD && pattern != null && material != null && itemStack.getItemMeta() instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(new ArmorTrim(material, pattern));
            itemStack.setItemMeta(armorMeta);
        }

        return itemStack;
    }

    private ItemStack buildNavigationItem(Material material, String name, String loreLine) {
        GUIItem item = new GUIItem(material);
        item.setName(name);

        List<String> lore = new ArrayList<>();
        lore.add(loreLine);
        lore.add("&eClick to open.");
        item.setLore(lore);

        return item.get();
    }

    private ItemStack buildPatternSelectionItem(TrimPattern activePattern) {
        Material buttonMaterial = DEFAULT_PATTERN_BUTTON_MATERIAL;
        if (activePattern != null) {
            String patternId = CosmeticsPermissionManager.getTrimId(activePattern);
            Material activePatternMaterial = resolveMaterial(patternId.toUpperCase(Locale.ROOT) + "_ARMOR_TRIM_SMITHING_TEMPLATE");
            if (activePatternMaterial != null) {
                buttonMaterial = activePatternMaterial;
            }
        }

        String name = GUIFile.getConfig().getString("GUIS.COSMETICS.ARMOR-PIECE-HUB.PATTERN-SELECTION-BUTTON.NAME", "&bPattern Selection");
        String loreLine = GUIFile.getConfig().getStringList("GUIS.COSMETICS.ARMOR-PIECE-HUB.PATTERN-SELECTION-BUTTON.LORE").getFirst();
        return buildNavigationItem(buttonMaterial, name, loreLine);
    }

    private ItemStack buildMaterialSelectionItem(TrimMaterial activeMaterial) {
        Material buttonMaterial = DEFAULT_MATERIAL_BUTTON_MATERIAL;
        if (activeMaterial != null) {
            Material activeMaterialIcon = resolveTrimMaterialIcon(CosmeticsPermissionManager.getTrimId(activeMaterial));
            if (activeMaterialIcon != null) {
                buttonMaterial = activeMaterialIcon;
            }
        }

        String name = GUIFile.getConfig().getString("GUIS.COSMETICS.ARMOR-PIECE-HUB.MATERIAL-SELECTION-BUTTON.NAME", "&6Material Selection");
        String loreLine = GUIFile.getConfig().getStringList("GUIS.COSMETICS.ARMOR-PIECE-HUB.MATERIAL-SELECTION-BUTTON.LORE").getFirst();
        return buildNavigationItem(buttonMaterial, name, loreLine);
    }

    private Material resolveTrimMaterialIcon(String materialId) {
        return switch (materialId) {
            case "lapis" -> Material.LAPIS_LAZULI;
            case "amethyst" -> Material.AMETHYST_SHARD;
            case "resin" -> resolveMaterial("RESIN_BRICK") == null ? Material.BRICK : resolveMaterial("RESIN_BRICK");
            default -> {
                Material ingot = resolveMaterial(materialId.toUpperCase(Locale.ROOT) + "_INGOT");
                if (ingot != null) {
                    yield ingot;
                }
                yield resolveMaterial(materialId.toUpperCase(Locale.ROOT));
            }
        };
    }

    private static GUIType resolveGuiType(ArmorSlot armorSlot) {
        return switch (armorSlot) {
            case HELMET -> GUIType.Cosmetics_Helmet;
            case CHESTPLATE -> GUIType.Cosmetics_Chestplate;
            case LEGGINGS -> GUIType.Cosmetics_Leggings;
            case BOOTS -> GUIType.Cosmetics_Boots;
            case SHIELD -> GUIType.Cosmetics_Shield;
        };
    }

    private Material resolveMaterial(String materialName) {
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}


