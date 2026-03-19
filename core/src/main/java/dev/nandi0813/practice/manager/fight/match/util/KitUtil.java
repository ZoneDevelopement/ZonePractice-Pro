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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum KitUtil {
    ;

    public static void loadDefaultLadderKit(Player player, TeamEnum team, Ladder ladder) {
        KitData kitData = ladder.getKitData();
        loadKit(player, team, kitData.getArmor(), kitData.getStorage(), kitData.getExtra());
    }

    public static void loadKit(Player player, TeamEnum team, ItemStack[] armor, ItemStack[] inventory, ItemStack[] extra) {
        PlayerUtil.clearInventory(player);

        if (team == null) {
            LadderUtil.loadInventory(player, armor, inventory, extra);
        } else {
            List<ItemStack> inventoryList = new ArrayList<>();
            for (ItemStack item : new ArrayList<>(Arrays.asList(inventory.clone()))) {
                if (item != null) {
                    item = LadderUtil.changeItemColor(item, team.getColor());
                    inventoryList.add(item);
                } else {
                    inventoryList.add(null);
                }
            }

            List<ItemStack> armorList = new ArrayList<>();
            for (ItemStack item : new ArrayList<>(Arrays.asList(armor.clone()))) {
                if (item != null) {
                    item = LadderUtil.changeItemColor(item, team.getColor());
                    armorList.add(item);
                } else {
                    armorList.add(null);
                }
            }

            List<ItemStack> extraList = new ArrayList<>();
            if (extra != null) {
                for (ItemStack item : new ArrayList<>(Arrays.asList(extra.clone()))) {
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
                    extra != null ? extraList.toArray(new ItemStack[0]) : null);
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

            ArmorTrimTier activeTier = profile.getCosmeticsData().getActiveTier();
            if (!player.hasPermission(activeTier.getPermissionNode())) {
                return;
            }

            // Helmet (index 3)
            applyTrimToArmor(player, armorContents[3],
                profile.getCosmeticsData().getPattern(activeTier, ArmorSlot.HELMET),
                profile.getCosmeticsData().getMaterial(activeTier, ArmorSlot.HELMET), 3);

            // Chestplate (index 2)
            applyTrimToArmor(player, armorContents[2],
                profile.getCosmeticsData().getPattern(activeTier, ArmorSlot.CHESTPLATE),
                profile.getCosmeticsData().getMaterial(activeTier, ArmorSlot.CHESTPLATE), 2);

            // Leggings (index 1)
            applyTrimToArmor(player, armorContents[1],
                profile.getCosmeticsData().getPattern(activeTier, ArmorSlot.LEGGINGS),
                profile.getCosmeticsData().getMaterial(activeTier, ArmorSlot.LEGGINGS), 1);

            // Boots (index 0)
            applyTrimToArmor(player, armorContents[0],
                profile.getCosmeticsData().getPattern(activeTier, ArmorSlot.BOOTS),
                profile.getCosmeticsData().getMaterial(activeTier, ArmorSlot.BOOTS), 0);

        } catch (Exception e) {
            // Silently fail - if cosmetics cannot be applied, continue with kit distribution
        }
    }

    private static void applyShieldCosmetics(Player player) {
        try {
            Profile profile = ProfileManager.getInstance().getProfile(player);
            if (profile == null || profile.getCosmeticsData() == null) {
                return;
            }

            if (!CosmeticsPermissionManager.hasShieldPermission(player)) {
                return;
            }

            if (profile.getCosmeticsData().getActiveShieldLayout() == null) {
                return;
            }

            ShieldCosmeticsUtil.applyShieldToPlayer(player);
        } catch (Exception e) {
            // Silently fail - if shield cosmetics cannot be applied, continue with kit distribution
        }
    }

    /**
     * Apply a trim pattern and material to an armor piece if both are set.
     */
    private static void applyTrimToArmor(Player player, ItemStack item,
                                         TrimPattern pattern, TrimMaterial material, int armorIndex) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        // Both pattern and material must be set to apply trim
        if (pattern == null || material == null) {
            return;
        }

        // Verify permission before applying
        if (!player.hasPermission("zpp.cosmetics.armortrim.pattern." + getTrimId(pattern)) ||
            !player.hasPermission("zpp.cosmetics.armortrim.material." + getTrimId(material))) {
            return;
        }

        try {
            var meta = (ArmorMeta) item.getItemMeta();
            if (meta != null) {
                meta.setTrim(new ArmorTrim(material, pattern));
                item.setItemMeta(meta);

                ItemStack[] armorContents = player.getInventory().getArmorContents();
                armorContents[armorIndex] = item;
                player.getInventory().setArmorContents(armorContents);
            }
        } catch (Exception e) {
            // Silently fail - trim application may not be supported on this version or item type
        }
    }

    private static String getTrimId(TrimPattern pattern) {
        return CosmeticsPermissionManager.getTrimId(pattern);
    }

    private static String getTrimId(TrimMaterial material) {
        return CosmeticsPermissionManager.getTrimId(material);
    }

}
