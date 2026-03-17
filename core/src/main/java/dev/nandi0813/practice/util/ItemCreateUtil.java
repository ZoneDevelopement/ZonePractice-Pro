package dev.nandi0813.practice.util;

import net.kyori.adventure.text.Component;
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
import java.util.stream.Collectors;

public class ItemCreateUtil {

    public static ItemStack createItem(String displayname, Material material, Short damage, int amount, List<String> lore) {
        ItemStack itemStack = new ItemStack(material, amount);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(Component.text(StringUtil.CC(displayname)));
            itemMeta.lore(StringUtil.CC(lore).stream().map(Component::text).collect(Collectors.toList()));

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
            itemMeta.displayName(Component.text(StringUtil.CC(displayname)));

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
            itemMeta.displayName(Component.text(StringUtil.CC(displayname)));

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
            itemMeta.displayName(Component.text(StringUtil.CC(displayname)));
            itemMeta.lore(StringUtil.CC(lore).stream().map(Component::text).collect(Collectors.toList()));

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItem(String displayname, Material material, Short damage, List<String> lore) {
        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(Component.text(StringUtil.CC(displayname)));
            itemMeta.lore(StringUtil.CC(lore).stream().map(Component::text).collect(Collectors.toList()));

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
            itemMeta.lore(StringUtil.CC(lore).stream().map(Component::text).collect(Collectors.toList()));

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack createItem(ItemStack item, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(item);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(Component.text(StringUtil.CC(name)));
            itemMeta.lore(StringUtil.CC(lore).stream().map(Component::text).collect(Collectors.toList()));

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
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
    }

    public static ItemStack hideItemFlags(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        hideItemFlags(itemMeta);
        item.setItemMeta(itemMeta);
        return item;
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