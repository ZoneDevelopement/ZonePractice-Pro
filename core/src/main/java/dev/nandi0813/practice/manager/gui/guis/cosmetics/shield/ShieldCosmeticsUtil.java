package dev.nandi0813.practice.manager.gui.guis.cosmetics.shield;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.cosmetics.shield.ShieldLayout;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;

/**
 * Static utility for applying shield layout designs to ItemStacks and player inventories.
 */
public final class ShieldCosmeticsUtil {

    private ShieldCosmeticsUtil() {}

    /**
     * Applies the active layout of the player's profile to every SHIELD item in their inventory.
     * Called on join and whenever the active layout changes.
     */
    public static void applyShieldToPlayer(Player player) {
        if (player == null) return;
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null || profile.getCosmeticsData() == null) return;

        ShieldLayout active = profile.getCosmeticsData().getActiveShieldLayout();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.SHIELD) continue;
            if (active != null) {
                applyLayoutToItem(item, active);
            }
            // No active layout → leave the shield completely untouched (plain default look)
        }
        player.updateInventory();
    }

    /** Applies a ShieldLayout to a shield ItemStack using the BlockStateMeta API. */
    public static void applyLayoutToItem(ItemStack shield, ShieldLayout layout) {
        if (shield == null || shield.getType() != Material.SHIELD || layout == null) return;

        var meta = shield.getItemMeta();
        if (!(meta instanceof BlockStateMeta bsm)) return;

        BlockState bs = bsm.getBlockState();
        if (!(bs instanceof Banner banner)) return;

        banner.setBaseColor(layout.getBaseColor() != null ? layout.getBaseColor() : DyeColor.WHITE);
        banner.setPatterns(new ArrayList<>());
        for (ShieldLayout.PatternLayer layer : layout.getLayers()) {
            banner.addPattern(new Pattern(layer.color(), layer.pattern()));
        }
        bsm.setBlockState(banner);
        shield.setItemMeta(bsm);
    }

    /** Removes all banner data from a shield (resets to blank). */
    public static void clearShield(ItemStack shield) {
        if (shield == null || shield.getType() != Material.SHIELD) return;
        var meta = shield.getItemMeta();
        if (!(meta instanceof BlockStateMeta bsm)) return;
        BlockState bs = bsm.getBlockState();
        if (!(bs instanceof Banner banner)) return;
        banner.setBaseColor(DyeColor.WHITE);
        banner.setPatterns(new ArrayList<>());
        bsm.setBlockState(banner);
        shield.setItemMeta(bsm);
    }
}
