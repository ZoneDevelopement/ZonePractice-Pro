package dev.nandi0813.practice.util;

import org.bukkit.inventory.ItemStack;

public enum ArmorUtil {
    ;

    public static boolean isHelmet(ItemStack item) {
        return item.getType().name().endsWith("_HELMET") || item.getType().name().equalsIgnoreCase("CARVED_PUMPKIN");
    }

    public static boolean isChestplate(ItemStack item) {
        return item.getType().name().endsWith("_CHESTPLATE") || item.getType().name().endsWith("ELYTRA");
    }

    public static boolean isLeggings(ItemStack item) {
        return item.getType().name().endsWith("_LEGGINGS");
    }

    public static boolean isBoots(ItemStack item) {
        return item.getType().name().endsWith("_BOOTS");
    }

}
