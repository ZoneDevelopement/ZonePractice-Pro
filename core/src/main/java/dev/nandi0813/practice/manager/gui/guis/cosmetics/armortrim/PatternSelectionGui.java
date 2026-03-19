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
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.*;

public class PatternSelectionGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BACK-TO").get();
    
    private static final int INVENTORY_ROWS = GUIFile.getConfig().getInt("GUIS.COSMETICS.PATTERN-SELECTION.INVENTORY-ROWS", 5);
    private static final int BACK_SLOT = GUIFile.getConfig().getInt("GUIS.COSMETICS.PATTERN-SELECTION.BACK-SLOT", 36);
    private static final int START_SLOT = GUIFile.getConfig().getInt("GUIS.COSMETICS.PATTERN-SELECTION.START-SLOT", 10);

    private final Profile profile;
    private final ArmorSlot armorSlot;
    private final GUI backToGui;
    private final Map<Integer, TrimPattern> patternBySlot = new HashMap<>();

    public PatternSelectionGui(Profile profile, ArmorSlot armorSlot, GUI backToGui) {
        super(GUIType.Cosmetics_Pattern_Selection);
        this.profile = profile;
        this.armorSlot = armorSlot;
        this.backToGui = backToGui;
        String title = GUIFile.getConfig().getString("GUIS.COSMETICS.PATTERN-SELECTION.INVENTORY-TITLE", "&8Select Pattern - %armor%")
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
        patternBySlot.clear();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, FILLER_ITEM);
        }

        inventory.setItem(BACK_SLOT, BACK_TO_ITEM == null ? new ItemStack(Material.ARROW) : BACK_TO_ITEM);

        ArmorTrimTier tier = profile.getCosmeticsData().getActiveTier();
        int slot = START_SLOT;
        for (TrimPattern pattern : CosmeticsPermissionManager.getRegisteredPatterns()) {
            if (slot >= inventory.getSize()) {
                break;
            }

            while (slot < inventory.getSize() && (slot % 9 == 0 || slot % 9 == 8)) {
                slot++;
            }

            if (slot >= inventory.getSize()) {
                break;
            }

            patternBySlot.put(slot, pattern);
            inventory.setItem(slot, buildPatternItem(profile.getPlayer().getPlayer(), tier, pattern));
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

        TrimPattern pattern = patternBySlot.get(slot);
        if (pattern == null) {
            return;
        }

        ArmorTrimTier tier = profile.getCosmeticsData().getActiveTier();
        if (!player.hasPermission(tier.getPermissionNode())) {
            String tierPermDeniedMessage = GUIFile.getConfig().getString("GUIS.COSMETICS.TIER-PERMISSION-DENIED-MESSAGE", "<red>You do not have permission for this armor tier.");
            Common.sendMMMessage(player, tierPermDeniedMessage);
            return;
        }

        String permissionNode = "zpp.cosmetics.armortrim.pattern." + CosmeticsPermissionManager.getTrimId(pattern);
        if (!player.hasPermission(permissionNode)) {
            String permDeniedMessage = GUIFile.getConfig().getString("GUIS.COSMETICS.PATTERN-PERMISSION-DENIED-MESSAGE", "<red>You do not have permission to use this trim pattern.");
            Common.sendMMMessage(player, permDeniedMessage);
            return;
        }

        profile.getCosmeticsData().setPattern(tier, armorSlot, pattern);
        profile.saveData();
        update(true);
    }

    private ItemStack buildPatternItem(Player player, ArmorTrimTier tier, TrimPattern pattern) {
        String patternId = CosmeticsPermissionManager.getTrimId(pattern);
        String permissionNode = "zpp.cosmetics.armortrim.pattern." + patternId;
        boolean hasPermission = player != null && player.hasPermission(permissionNode);

        TrimPattern activePattern = profile.getCosmeticsData().getPattern(tier, armorSlot);
        boolean active = activePattern != null
                && CosmeticsPermissionManager.getTrimId(activePattern).equalsIgnoreCase(patternId);

        Material templateMaterial = resolveMaterial(patternId.toUpperCase(Locale.ROOT) + "_ARMOR_TRIM_SMITHING_TEMPLATE");
        if (templateMaterial == null) {
            templateMaterial = resolveMaterial(patternId.toUpperCase(Locale.ROOT) + "_SMITHING_TEMPLATE");
        }
        GUIItem item = new GUIItem(templateMaterial == null ? Material.PAPER : templateMaterial);
        
        String itemName = GUIFile.getConfig().getString("GUIS.COSMETICS.PATTERN-SELECTION.PATTERN-ITEM.NAME", "&b%pattern_name% Pattern")
            .replace("%pattern_name%", StringUtil.getNormalizedName(patternId));
        item.setName(itemName);

        List<String> configLore = GUIFile.getConfig().getStringList("GUIS.COSMETICS.PATTERN-SELECTION.PATTERN-ITEM.LORE");
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

    private Material resolveMaterial(String materialName) {
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
