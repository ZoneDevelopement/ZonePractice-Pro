package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemCreateUtil extends dev.nandi0813.practice.module.interfaces.ItemCreateUtil {

    @Override
    public ItemStack createItem(String displayname, Material material, Short damage, int amount, List<String> lore) {
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

    @Override
    public ItemStack createItem(String displayname, Material material) {
        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(StringUtil.CC(displayname));

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    @Override
    public ItemStack createItem(Material material, Short damage) {
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

    @Override
    public ItemStack createItem(String displayname, Material material, Short damage) {
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

    @Override
    public ItemStack createItem(String displayname, Material material, List<String> lore) {
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

    @Override
    public ItemStack createItem(String displayname, Material material, Short damage, List<String> lore) {
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

    @Override
    public ItemStack createItem(ItemStack item, List<String> lore) {
        ItemStack itemStack = new ItemStack(item);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(StringUtil.CC(lore));

            hideItemFlags(itemMeta);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    @Override
    public ItemStack createItem(ItemStack item, String name, List<String> lore) {
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

}
