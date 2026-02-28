package dev.nandi0813.practice.module.interfaces;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class ItemCreateUtil {

    public abstract ItemStack createItem(String displayname, Material material, Short damage, int amount, List<String> lore);

    public abstract ItemStack createItem(String displayname, Material material);

    public abstract ItemStack createItem(Material material, Short damage);

    public abstract ItemStack createItem(String displayname, Material material, Short damage);

    public abstract ItemStack createItem(String displayname, Material material, List<String> lore);

    public abstract ItemStack createItem(String displayname, Material material, Short damage, List<String> lore);

    public abstract ItemStack createItem(ItemStack item, List<String> lore);

    public abstract ItemStack createItem(ItemStack item, String name, List<String> lore);

    public static void hideItemFlags(ItemMeta itemMeta) {
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
    }

    public static ItemStack hideItemFlags(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        hideItemFlags(itemMeta);
        item.setItemMeta(itemMeta);
        return item;
    }

    public static short getDurabilityByColor(char c) {
        return switch (c) {
            case '0' -> Short.parseShort("15");
            case '1', '9' -> Short.parseShort("11");
            case '2' -> Short.parseShort("13");
            case '3' -> Short.parseShort("9");
            case '4', 'c' -> Short.parseShort("14");
            case '5' -> Short.parseShort("10");
            case '6' -> Short.parseShort("35");
            case '7' -> Short.parseShort("8");
            case '8' -> Short.parseShort("7");
            case 'a' -> Short.parseShort("5");
            case 'b' -> Short.parseShort("3");
            case 'd' -> Short.parseShort("6");
            case 'e' -> Short.parseShort("4");
            default -> 0;
        };
    }

}