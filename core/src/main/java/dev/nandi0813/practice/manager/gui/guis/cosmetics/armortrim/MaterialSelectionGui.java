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
import org.bukkit.inventory.meta.trim.TrimMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialSelectionGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BACK-TO").get();
    
    private static final int INVENTORY_ROWS = GUIFile.getConfig().getInt("GUIS.COSMETICS.MATERIAL-SELECTION.INVENTORY-ROWS", 5);
    private static final int BACK_SLOT = GUIFile.getConfig().getInt("GUIS.COSMETICS.MATERIAL-SELECTION.BACK-SLOT", 36);
    private static final int START_SLOT = GUIFile.getConfig().getInt("GUIS.COSMETICS.MATERIAL-SELECTION.START-SLOT", 10);

    private final Profile profile;
    private final ArmorSlot armorSlot;
    private final GUI backToGui;
    private final Map<Integer, TrimMaterial> materialBySlot = new HashMap<>();

    public MaterialSelectionGui(Profile profile, ArmorSlot armorSlot, GUI backToGui) {
        super(GUIType.Cosmetics_Material_Selection);
        this.profile = profile;
        this.armorSlot = armorSlot;
        this.backToGui = backToGui;
        String title = GUIFile.getConfig().getString("GUIS.COSMETICS.MATERIAL-SELECTION.INVENTORY-TITLE", "&8Select Material - %armor%")
            .replace("%armor%", armorSlot.getDisplayName());
        this.gui.put(1, InventoryUtil.createInventory(title, INVENTORY_ROWS));
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
        materialBySlot.clear();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, FILLER_ITEM);
        }

        inventory.setItem(BACK_SLOT, BACK_TO_ITEM == null ? new ItemStack(Material.ARROW) : BACK_TO_ITEM);

        ArmorTrimTier tier = profile.getCosmeticsData().getActiveTier();
        int slot = START_SLOT;
        for (TrimMaterial material : CosmeticsPermissionManager.getRegisteredMaterials()) {
            if (slot >= inventory.getSize()) {
                break;
            }

            while (slot < inventory.getSize() && (slot % 9 == 0 || slot % 9 == 8)) {
                slot++;
            }

            if (slot >= inventory.getSize()) {
                break;
            }

            materialBySlot.put(slot, material);
            inventory.setItem(slot, buildMaterialItem(profile.getPlayer().getPlayer(), tier, material));
            slot++;
        }

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

        TrimMaterial material = materialBySlot.get(slot);
        if (material == null) {
            return;
        }

        ArmorTrimTier tier = profile.getCosmeticsData().getActiveTier();
        if (!player.hasPermission(tier.getPermissionNode())) {
            String tierPermDeniedMessage = GUIFile.getConfig().getString("GUIS.COSMETICS.TIER-PERMISSION-DENIED-MESSAGE", "<red>You do not have permission for this armor tier.");
            Common.sendMMMessage(player, tierPermDeniedMessage);
            return;
        }

        String permissionNode = "zpp.cosmetics.armortrim.material." + CosmeticsPermissionManager.getTrimId(material);
        if (!player.hasPermission(permissionNode)) {
            String permDeniedMessage = GUIFile.getConfig().getString("GUIS.COSMETICS.MATERIAL-PERMISSION-DENIED-MESSAGE", "<red>You do not have permission to use this trim material.");
            Common.sendMMMessage(player, permDeniedMessage);
            return;
        }

        profile.getCosmeticsData().setMaterial(tier, armorSlot, material);
        profile.saveData();
        update(true);
    }

    private ItemStack buildMaterialItem(Player player, ArmorTrimTier tier, TrimMaterial material) {
        String materialId = CosmeticsPermissionManager.getTrimId(material);
        String permissionNode = "zpp.cosmetics.armortrim.material." + materialId;
        boolean hasPermission = player != null && player.hasPermission(permissionNode);

        TrimMaterial activeMaterial = profile.getCosmeticsData().getMaterial(tier, armorSlot);
        boolean active = activeMaterial != null
                && CosmeticsPermissionManager.getTrimId(activeMaterial).equalsIgnoreCase(materialId);

        GUIItem item = new GUIItem(resolveIcon(materialId));
        
        String itemName = GUIFile.getConfig().getString("GUIS.COSMETICS.MATERIAL-SELECTION.MATERIAL-ITEM.NAME", "&6%material_name% Material")
            .replace("%material_name%", StringUtil.getNormalizedName(materialId));
        item.setName(itemName);

        List<String> configLore = GUIFile.getConfig().getStringList("GUIS.COSMETICS.MATERIAL-SELECTION.MATERIAL-ITEM.LORE");
        List<String> lore = new ArrayList<>();
        for (String loreLine : configLore) {
            lore.add(loreLine
                .replace("%state%", active ? "&aActive" : "&cInactive")
                .replace("%access%", hasPermission ? "&aUnlocked" : "&cLocked")
                .replace("%permission%", permissionNode));
        }
        item.setLore(lore);
        item.setGlowing(active);

        return item.get();
    }

    private Material resolveIcon(String materialId) {
        // Check if material icon is configured
        String configuredMaterial = GUIFile.getConfig().getString("GUIS.COSMETICS.MATERIAL-SELECTION.MATERIAL-ICONS." + materialId.toUpperCase(), null);
        if (configuredMaterial != null) {
            try {
                return Material.valueOf(configuredMaterial);
            } catch (IllegalArgumentException ignored) {
                // Fall through to default resolution
            }
        }
        
        // Default resolution logic
        return switch (materialId) {
            case "lapis" -> Material.LAPIS_LAZULI;
            case "amethyst" -> Material.AMETHYST_SHARD;
            case "resin" -> resolveByName("RESIN_BRICK", Material.BRICK);
            default -> {
                Material resolved = resolveByName(materialId.toUpperCase() + "_INGOT", null);
                if (resolved == null) {
                    resolved = resolveByName(materialId.toUpperCase(), null);
                }
                yield resolved == null ? Material.NETHER_STAR : resolved;
            }
        };
    }

    private Material resolveByName(String materialName, Material fallback) {
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}





