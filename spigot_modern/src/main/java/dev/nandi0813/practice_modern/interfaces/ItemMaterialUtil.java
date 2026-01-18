package dev.nandi0813.practice_modern.interfaces;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemMaterialUtil implements dev.nandi0813.practice.module.interfaces.ItemMaterialUtil {
    @Override
    public Material getSnowball() {
        return Material.SNOWBALL;
    }

    @Override
    public Material getIronShovel() {
        return Material.IRON_SHOVEL;
    }

    @Override
    public Material getEyeOfEnder() {
        return Material.ENDER_EYE;
    }

    @Override
    public Material getRedBed() {
        return Material.RED_BED;
    }

    @Override
    public Material getMushroomSoup() {
        return Material.MUSHROOM_STEW;
    }

    @Override
    public Material getWater() {
        return Material.WATER;
    }

    @Override
    public Material getLava() {
        return Material.LAVA;
    }

    @Override
    public Material getFireball() {
        return Material.FIRE_CHARGE;
    }

    @Override
    public Material getEndPortal() {
        return Material.END_PORTAL;
    }

    @Override
    public Material getLilyPad() {
        return Material.LILY_PAD;
    }

    @Override
    public Material getStainedClay() {
        return Material.WHITE_TERRACOTTA;
    }

    @Override
    public Material getSplashPotion() {
        return Material.SPLASH_POTION;
    }

    @Override
    public Material getGoldSword() {
        return Material.GOLDEN_SWORD;
    }

    @Override
    public Material getClock() {
        return Material.CLOCK;
    }

    @Override
    public EntityType getTNTMineCart() {
        return EntityType.TNT_MINECART;
    }

    @Override
    public ItemStack getPlayerHead(OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null)
            meta.setOwningPlayer(player);
        item.setItemMeta(meta);

        return item;
    }
}
