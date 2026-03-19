package dev.nandi0813.practice.manager.gui.guis.cosmetics.armortrim;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorSlot;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorTrimTier;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Main cosmetics GUI that displays armor pieces for the player to customize.
 */
public class ArmorTrimMainGui extends GUI {

    private static final int MAIN_ROWS = 5;
    private static final int BACK_SLOT = 36;
    private static final int HELMET_SLOT = 10;
    private static final int CHESTPLATE_SLOT = 12;
    private static final int LEGGINGS_SLOT = 14;
    private static final int BOOTS_SLOT = 16;
    private static final int INFO_SLOT = 31;

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    private static final GUIItem HELMET_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.HELMET-ICON");
    private static final GUIItem CHESTPLATE_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.CHESTPLATE-ICON");
    private static final GUIItem LEGGINGS_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.LEGGINGS-ICON");
    private static final GUIItem BOOTS_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BOOTS-ICON");
    private static final GUIItem INFO_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.INFO-ICON");
    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BACK-TO").get();

    private final Profile profile;
    private final GUI backToGui;

    public ArmorTrimMainGui(Profile profile, GUI backToGui) {
        super(GUIType.ArmorTrimMainGui);
        this.profile = profile;
        this.backToGui = backToGui;

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.COSMETICS.MAIN-TITLE"), MAIN_ROWS));
        this.build();
    }

    @Override
    public void build() {
        this.update();
    }

    @Override
    public void update() {
        Inventory inventory = this.gui.get(1);
        inventory.clear();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, FILLER_ITEM);
        }

        ArmorTrimTier activeTier = profile.getCosmeticsData().getActiveTier();
        Player player = profile.getPlayer().getPlayer();
        if (player != null && !player.hasPermission(activeTier.getPermissionNode())) {
            for (ArmorTrimTier tier : ArmorTrimTier.values()) {
                if (player.hasPermission(tier.getPermissionNode())) {
                    activeTier = tier;
                    profile.getCosmeticsData().setActiveTier(tier);
                    break;
                }
            }
        }

        inventory.setItem(HELMET_SLOT, buildArmorPreviewItem(HELMET_ITEM, activeTier, ArmorSlot.HELMET));
        inventory.setItem(CHESTPLATE_SLOT, buildArmorPreviewItem(CHESTPLATE_ITEM, activeTier, ArmorSlot.CHESTPLATE));
        inventory.setItem(LEGGINGS_SLOT, buildArmorPreviewItem(LEGGINGS_ITEM, activeTier, ArmorSlot.LEGGINGS));
        inventory.setItem(BOOTS_SLOT, buildArmorPreviewItem(BOOTS_ITEM, activeTier, ArmorSlot.BOOTS));
        inventory.setItem(INFO_SLOT, buildTierToggleItem(activeTier));

        if (backToGui != null) {
            ItemStack backItem = BACK_TO_ITEM == null ? new ItemStack(Material.ARROW) : BACK_TO_ITEM;
            inventory.setItem(BACK_SLOT, backItem);
        }

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (slot == BACK_SLOT && backToGui != null) {
            backToGui.open(player);
            return;
        }

        if (slot == INFO_SLOT) {
            if (e.isLeftClick()) {
                handleTierToggleClick(player, false);
            } else if (e.isRightClick()) {
                handleTierToggleClick(player, true);
            }
            return;
        }

        ArmorSlot armorSlot = getArmorSlotFromSlot(slot);
        if (armorSlot != null) {
            ArmorTrimTier activeTier = profile.getCosmeticsData().getActiveTier();
            if (!player.hasPermission(activeTier.getPermissionNode())) {
                String deniedMessage = GUIFile.getConfig().getString("GUIS.COSMETICS.PERMISSION-DENIED-MESSAGE", "<red>You do not have permission for the selected armor tier.");
                Common.sendMMMessage(player, deniedMessage);
                return;
            }

            if (e.isRightClick()) {
                resetArmorCosmetic(activeTier, armorSlot);
                return;
            }

            openArmorSubGui(player, armorSlot);
        }
    }

    private void resetArmorCosmetic(ArmorTrimTier activeTier, ArmorSlot armorSlot) {
        profile.getCosmeticsData().setPattern(activeTier, armorSlot, null);
        profile.getCosmeticsData().setMaterial(activeTier, armorSlot, null);
        update(true);
    }

    private ArmorSlot getArmorSlotFromSlot(int slot) {
        return switch (slot) {
            case HELMET_SLOT -> ArmorSlot.HELMET;
            case CHESTPLATE_SLOT -> ArmorSlot.CHESTPLATE;
            case LEGGINGS_SLOT -> ArmorSlot.LEGGINGS;
            case BOOTS_SLOT -> ArmorSlot.BOOTS;
            default -> null;
        };
    }

    private ItemStack buildArmorPreviewItem(GUIItem configuredItem, ArmorTrimTier tier, ArmorSlot slot) {
        Material baseMaterial = tier.getMaterial(slot);
        GUIItem guiItem = configuredItem.cloneItem();
        guiItem.setMaterial(baseMaterial);

        TrimPattern activePattern = profile.getCosmeticsData().getPattern(tier, slot);
        TrimMaterial activeMaterial = profile.getCosmeticsData().getMaterial(tier, slot);

        List<String> lore = guiItem.getLore() == null ? new ArrayList<>() : new ArrayList<>(guiItem.getLore());
        
        // Get dynamic lore template from guis.yml
        List<String> lorTemplate = GUIFile.getConfig().getStringList("GUIS.COSMETICS.ARMOR-PREVIEW-LORE");
        for (String line : lorTemplate) {
            String processedLine = line
                .replace("%tier%", tier.getDisplayName())
                .replace("%pattern%", activePattern == null ? "&cNone" : "&b" + formatDisplayName(activePattern))
                .replace("%material%", activeMaterial == null ? "&cNone" : "&6" + formatDisplayName(activeMaterial));
            lore.add(processedLine);
        }
        guiItem.setLore(lore);

        ItemStack item = guiItem.get();
        if (slot != ArmorSlot.SHIELD) {
            applyTrimPreview(item, activePattern, activeMaterial);
        }
        return item;
    }

    private ItemStack buildTierToggleItem(ArmorTrimTier activeTier) {
        GUIItem guiItem = INFO_ITEM.cloneItem();
        guiItem.setMaterial(guiItem.getMaterial() == null ? Material.NETHER_STAR : guiItem.getMaterial());

        Player player = profile.getPlayer().getPlayer();
        int totalPatternPermissions = CosmeticsPermissionManager.getRegisteredPatterns().size();
        int totalMaterialPermissions = CosmeticsPermissionManager.getRegisteredMaterials().size();
        int playerPatternPermissions = 0;
        int playerMaterialPermissions = 0;

        if (player != null) {
            for (TrimPattern pattern : CosmeticsPermissionManager.getRegisteredPatterns()) {
                if (player.hasPermission("zpp.cosmetics.armortrim.pattern." + getPermissionId(pattern))) {
                    playerPatternPermissions++;
                }
            }

            for (TrimMaterial material : CosmeticsPermissionManager.getRegisteredMaterials()) {
                if (player.hasPermission("zpp.cosmetics.armortrim.material." + getPermissionId(material))) {
                    playerMaterialPermissions++;
                }
            }
        }

        guiItem.replace("%tier%", activeTier.getDisplayName());
        guiItem.replace("%tier_permission%", activeTier.getPermissionNode());
        guiItem.replace("%pattern_unlocked%", String.valueOf(playerPatternPermissions));
        guiItem.replace("%pattern_total%", String.valueOf(totalPatternPermissions));
        guiItem.replace("%material_unlocked%", String.valueOf(playerMaterialPermissions));
        guiItem.replace("%material_total%", String.valueOf(totalMaterialPermissions));
        guiItem.setName("&bArmor Tier: &e" + activeTier.getDisplayName());

        List<String> lore = guiItem.getLore() == null ? new ArrayList<>() : new ArrayList<>(guiItem.getLore());
        
        // Get dynamic lore template from guis.yml
        List<String> loreTemplate = GUIFile.getConfig().getStringList("GUIS.COSMETICS.TIER-TOGGLE-LORE");
        for (String line : loreTemplate) {
            String processedLine = line.replace("%tier_permission%", activeTier.getPermissionNode());
            lore.add(processedLine);
        }
        guiItem.setLore(lore);
        return guiItem.get();
    }

    private void handleTierToggleClick(Player player, boolean forward) {
        ArmorTrimTier currentTier = profile.getCosmeticsData().getActiveTier();
        ArmorTrimTier nextTier = forward ? currentTier.next() : previousTier(currentTier);

        for (int i = 0; i < ArmorTrimTier.values().length; i++) {
            if (player.hasPermission(nextTier.getPermissionNode())) {
                profile.getCosmeticsData().setActiveTier(nextTier);
                profile.saveData();
                update(true);
                return;
            }

            nextTier = forward ? nextTier.next() : previousTier(nextTier);
        }

        String noPermMessage = GUIFile.getConfig().getString("GUIS.COSMETICS.NO-TIER-PERMISSION-MESSAGE", "<red>You do not have permission for any armor tier.");
        Common.sendMMMessage(player, noPermMessage);
    }

    private ArmorTrimTier previousTier(ArmorTrimTier tier) {
        ArmorTrimTier[] tiers = ArmorTrimTier.values();
        int index = tier.ordinal() - 1;
        if (index < 0) {
            index = tiers.length - 1;
        }
        return tiers[index];
    }

    private static void applyTrimPreview(ItemStack item, TrimPattern pattern, TrimMaterial material) {
        if (item == null || pattern == null || material == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof ArmorMeta armorMeta)) {
            return;
        }

        armorMeta.setTrim(new ArmorTrim(material, pattern));
        item.setItemMeta(armorMeta);
    }

    private static String getPermissionId(Object trimValue) {
        if (trimValue instanceof TrimPattern trimPattern) {
            return CosmeticsPermissionManager.getTrimId(trimPattern);
        }

        if (trimValue instanceof TrimMaterial trimMaterial) {
            return CosmeticsPermissionManager.getTrimId(trimMaterial);
        }

        return "unknown";
    }

    private static String formatDisplayName(Object trimValue) {
        return StringUtil.getNormalizedName(getPermissionId(trimValue));
    }

    private void openArmorSubGui(Player player, ArmorSlot armorSlot) {
        new ArmorPieceHubGui(profile, armorSlot, this).open(player);
    }
}




