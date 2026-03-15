package dev.nandi0813.practice.moved;

import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class ItemCreateUtil {

    public static ItemStack createItem(String displayname, Material material, Short damage, int amount, List<String> lore) {
        ItemStack itemStack = new ItemStack(material, amount);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(StringUtil.CC(displayname));
            itemMeta.setLore(StringUtil.CC(lore));

            if (itemMeta instanceof Damageable)
                ((Damageable) itemMeta).setDamage(damage);

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItem(String displayname, Material material) {
        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(StringUtil.CC(displayname));

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItem(Material material, Short damage) {
        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta instanceof Damageable)
                ((Damageable) itemMeta).setDamage(damage);

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItem(String displayname, Material material, Short damage) {
        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(StringUtil.CC(displayname));

            if (itemMeta instanceof Damageable)
                ((Damageable) itemMeta).setDamage(damage);

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItem(String displayname, Material material, List<String> lore) {
        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(StringUtil.CC(displayname));
            itemMeta.setLore(StringUtil.CC(lore));

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItem(String displayname, Material material, Short damage, List<String> lore) {
        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(StringUtil.CC(displayname));
            itemMeta.setLore(StringUtil.CC(lore));

            if (itemMeta instanceof Damageable)
                ((Damageable) itemMeta).setDamage(damage);

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItem(ItemStack item, List<String> lore) {
        ItemStack itemStack = new ItemStack(item);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(StringUtil.CC(lore));

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItem(ItemStack item, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(item);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(StringUtil.CC(name));
            itemMeta.setLore(StringUtil.CC(lore));

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static void hideItemFlags(ItemMeta itemMeta) {
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
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

    public static ItemStack getPlayerHead(OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null)
            meta.setOwningPlayer(player);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack getRedBoots() {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.RED);
            boots.setItemMeta(meta);
        }
        return boots;
    }

}