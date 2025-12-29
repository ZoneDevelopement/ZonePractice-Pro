package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemCreateUtil extends dev.nandi0813.practice.module.interfaces.ItemCreateUtil {

    @Override
    public ItemStack createItem(String displayname, Material material, Short damage, int amount, List<String> lore) {
        ItemStack itemstack = new ItemStack(material, amount, damage);
        ItemMeta itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(StringUtil.CC(displayname));
        itemmeta.setLore(StringUtil.CC(lore));
        hideItemFlags(itemmeta);
        itemstack.setItemMeta(itemmeta);
        return itemstack;
    }

    @Override
    public ItemStack createItem(String displayname, Material material) {
        ItemStack itemstack = new ItemStack(material);
        ItemMeta itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(StringUtil.CC(displayname));
        hideItemFlags(itemmeta);
        itemstack.setItemMeta(itemmeta);
        return itemstack;
    }

    @Override
    public ItemStack createItem(Material material, Short damage) {
        ItemStack itemstack = new ItemStack(material, 1, damage);
        ItemMeta itemmeta = itemstack.getItemMeta();
        hideItemFlags(itemmeta);
        itemstack.setItemMeta(itemmeta);
        return itemstack;
    }

    @Override
    public ItemStack createItem(String displayname, Material material, Short damage) {
        ItemStack itemstack = new ItemStack(material, 1, damage);
        ItemMeta itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(StringUtil.CC(displayname));
        itemstack.setItemMeta(itemmeta);
        return itemstack;
    }

    @Override
    public ItemStack createItem(String displayname, Material material, List<String> lore) {
        ItemStack itemstack = new ItemStack(material);
        ItemMeta itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(StringUtil.CC(displayname));
        itemmeta.setLore(StringUtil.CC(lore));
        hideItemFlags(itemmeta);
        itemstack.setItemMeta(itemmeta);
        return itemstack;
    }

    @Override
    public ItemStack createItem(String displayname, Material material, Short damage, List<String> lore) {
        ItemStack itemstack = new ItemStack(material, 1, damage);
        ItemMeta itemmeta = itemstack.getItemMeta();
        itemmeta.setDisplayName(StringUtil.CC(displayname));
        itemmeta.setLore(StringUtil.CC(lore));
        hideItemFlags(itemmeta);
        itemstack.setItemMeta(itemmeta);
        return itemstack;
    }

    @Override
    public ItemStack createItem(ItemStack item, List<String> lore) {
        ItemStack itemstack = new ItemStack(item.getType());
        itemstack.setDurability(item.getDurability());
        if (item.hasItemMeta()) {
            ItemMeta itemmeta = itemstack.getItemMeta();
            itemmeta.setDisplayName(StringUtil.CC(item.getItemMeta().getDisplayName()));
            itemmeta.setLore(StringUtil.CC(lore));
            hideItemFlags(itemmeta);
            itemstack.setItemMeta(itemmeta);
        }
        return itemstack;
    }

    @Override
    public ItemStack createItem(ItemStack item, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(item.getType());
        itemStack.setDurability(item.getDurability());

        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(name);
        itemMeta.setLore(lore);
        hideItemFlags(itemMeta);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}
