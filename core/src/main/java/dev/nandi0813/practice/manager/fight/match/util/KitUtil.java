package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.manager.gui.guis.cosmetics.shield.ShieldCosmeticsUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorSlot;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorTrimTier;
import dev.nandi0813.practice.util.KitData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum KitUtil {
    ;

    public static void loadDefaultLadderKit(Player player, TeamEnum team, Ladder ladder) {
        KitData kitData = ladder.getKitData();
        loadKit(player, team, kitData.getArmor(), kitData.getStorage(), kitData.getExtra());
    }

    public static void loadKit(Player player, TeamEnum team, ItemStack[] armor, ItemStack[] inventory, ItemStack[] extra) {
        PlayerUtil.clearInventory(player);

        ItemStack[] armorCopy = cloneItems(armor);
        ItemStack[] inventoryCopy = cloneItems(inventory);
        ItemStack[] extraCopy = cloneItems(extra);

        if (team == null) {
            LadderUtil.loadInventory(player, armorCopy, inventoryCopy, extraCopy);
        } else {
            List<ItemStack> inventoryList = new ArrayList<>();
            for (ItemStack item : new ArrayList<>(Arrays.asList(inventoryCopy))) {
                if (item != null) {
                    item = LadderUtil.changeItemColor(item, team.getColor());
                    inventoryList.add(item);
                } else {
                    inventoryList.add(null);
                }
            }

            List<ItemStack> armorList = new ArrayList<>();
            for (ItemStack item : new ArrayList<>(Arrays.asList(armorCopy))) {
                if (item != null) {
                    item = LadderUtil.changeItemColor(item, team.getColor());
                    armorList.add(item);
                } else {
                    armorList.add(null);
                }
            }

            List<ItemStack> extraList = new ArrayList<>();
            if (extraCopy != null) {
                for (ItemStack item : new ArrayList<>(Arrays.asList(extraCopy))) {
                    if (item != null) {
                        item = LadderUtil.changeItemColor(item, team.getColor());
                        extraList.add(item);
                    } else {
                        extraList.add(null);
                    }
                }
            }

            LadderUtil.loadInventory(player,
                    armorList.toArray(new ItemStack[0]),
                    inventoryList.toArray(new ItemStack[0]),
                    extraCopy != null ? extraList.toArray(new ItemStack[0]) : null);
        }

        applyArmorTrimCosmetics(player);
        applyShieldCosmetics(player);
        player.updateInventory();
    }

    /**
     * Apply armor trim cosmetics to the player's equipped armor.
     * Retrieves the player's saved cosmetics from their profile and applies them to armor pieces.
     */
    private static void applyArmorTrimCosmetics(Player player) {
        try {
            Profile profile = ProfileManager.getInstance().getProfile(player);
            if (profile == null || profile.getCosmeticsData() == null) {
                return;
            }

            ItemStack[] armorContents = player.getInventory().getArmorContents();
            if (armorContents.length < 4) {
                return;
            }

            // Helmet (index 3)
            applyTrimToArmor(player, profile, armorContents, ArmorSlot.HELMET, 3);

            // Chestplate (index 2)
            applyTrimToArmor(player, profile, armorContents, ArmorSlot.CHESTPLATE, 2);

            // Leggings (index 1)
            applyTrimToArmor(player, profile, armorContents, ArmorSlot.LEGGINGS, 1);

            // Boots (index 0)
            applyTrimToArmor(player, profile, armorContents, ArmorSlot.BOOTS, 0);

            player.getInventory().setArmorContents(armorContents);

        } catch (Exception e) {
            // Silently fail - if cosmetics cannot be applied, continue with kit distribution
        }
    }

    private static void applyShieldCosmetics(Player player) {
        try {
            ShieldCosmeticsUtil.applyShieldToPlayer(player);
        } catch (Exception e) {
            // Silently fail - if shield cosmetics cannot be applied, continue with kit distribution
        }
    }

    /**
     * Apply a trim pattern and material to an armor piece if both are set.
     */
    private static void applyTrimToArmor(Player player, Profile profile, ItemStack[] armorContents,
                                         ArmorSlot slot, int armorIndex) {
        ItemStack item = armorContents[armorIndex];
        if (item == null) {
            return;
        }

        if (!(item.getItemMeta() instanceof ArmorMeta armorMeta)) {
            return;
        }

        ArmorTrim targetTrim = null;
        ArmorTrimTier armorTier = getArmorTier(item.getType());
        if (armorTier != null && CosmeticsPermissionManager.hasBasePermission(player, armorTier)) {
            TrimPattern pattern = profile.getCosmeticsData().getPattern(armorTier, slot);
            TrimMaterial material = profile.getCosmeticsData().getMaterial(armorTier, slot);
            if (pattern != null
                    && material != null
                    && CosmeticsPermissionManager.hasPatternPermission(player, pattern)
                    && CosmeticsPermissionManager.hasMaterialPermission(player, material)) {
                targetTrim = new ArmorTrim(material, pattern);
            }
        }

        try {
            ArmorTrim currentTrim = armorMeta.getTrim();
            if (!Objects.equals(currentTrim, targetTrim)) {
                armorMeta.setTrim(targetTrim);
                item.setItemMeta(armorMeta);
                armorContents[armorIndex] = item;
            }
        } catch (Exception e) {
            // Silently fail - trim application may not be supported on this version or item type
        }
    }

    private static ArmorTrimTier getArmorTier(Material material) {
        return switch (material) {
            case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> ArmorTrimTier.LEATHER;
            case GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS -> ArmorTrimTier.GOLD;
            case IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS -> ArmorTrimTier.IRON;
            case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> ArmorTrimTier.DIAMOND;
            case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> ArmorTrimTier.NETHERITE;
            default -> null;
        };
    }

    private static ItemStack[] cloneItems(ItemStack[] source) {
        if (source == null) {
            return null;
        }

        ItemStack[] copy = source.clone();
        for (int i = 0; i < copy.length; i++) {
            if (copy[i] != null) {
                copy[i] = copy[i].clone();
            }
        }
        return copy;
    }

}