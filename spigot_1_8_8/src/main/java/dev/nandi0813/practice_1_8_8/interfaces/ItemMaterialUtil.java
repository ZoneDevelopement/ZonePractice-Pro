package dev.nandi0813.practice_1_8_8.interfaces;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemMaterialUtil implements dev.nandi0813.practice.module.interfaces.ItemMaterialUtil {

    @Override
    public Material getSnowball() {
        return Material.SNOW_BALL;
    }

    @Override
    public Material getIronShovel() {
        return Material.IRON_SPADE;
    }

    @Override
    public Material getEyeOfEnder() {
        return Material.EYE_OF_ENDER;
    }

    @Override
    public Material getRedBed() {
        return Material.BED;
    }

    @Override
    public Material getMushroomSoup() {
        return Material.MUSHROOM_SOUP;
    }

    @Override
    public Material getWater() {
        return Material.STATIONARY_WATER;
    }

    @Override
    public Material getLava() {
        return Material.STATIONARY_LAVA;
    }

    @Override
    public Material getFireball() {
        return Material.FIREBALL;
    }

    @Override
    public Material getEndPortal() {
        return Material.ENDER_PORTAL;
    }

    @Override
    public Material getLilyPad() {
        return Material.WATER_LILY;
    }

    @Override
    public Material getStainedClay() {
        return Material.STAINED_CLAY;
    }

    @Override
    public Material getSplashPotion() {
        return Material.POTION;
    }

    @Override
    public Material getGoldSword() {
        return Material.GOLD_SWORD;
    }

    @Override
    public Material getClock() {
        return Material.WATCH;
    }

    @Override
    public EntityType getTNTMineCart() {
        return EntityType.MINECART_TNT;
    }

    @Override
    public ItemStack getPlayerHead(OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setOwner(player.getName());
        item.setItemMeta(skull);
        return item;
    }
}
