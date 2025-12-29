package dev.nandi0813.practice.manager.playerkit.items;

import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitItem {

    private ItemStack itemStack;
    private final ItemStack placeholder;

    public KitItem(final ItemStack placeholder) {
        this.placeholder = placeholder.clone();
        this.reset();
    }

    public KitItem(final ItemStack placeholder, final ItemStack itemStack) {
        this(placeholder);

        if (itemStack != null) {
            setItemStack(itemStack.clone());
        } else {
            reset();
        }
    }

    public void setItemStack(final ItemStack itemStack) {
        this.itemStack = itemStack;
        if (this.itemStack.getItemMeta() == null) {
            this.itemStack.setItemMeta(Bukkit.getItemFactory().getItemMeta(this.itemStack.getType()));
        }
    }

    public KitItem reset() {
        setItemStack(new ItemStack(Material.AIR));
        return this;
    }

    public void addEnchant(Enchantment enchantment, int level) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        for (Enchantment enchant : itemMeta.getEnchants().keySet()) {
            if (enchant.equals(enchantment) || enchant.conflictsWith(enchantment)) {
                itemMeta.removeEnchant(enchant);
            }
        }
        itemMeta.addEnchant(enchantment, level, false);
        itemStack.setItemMeta(itemMeta);
    }

    public void clearEnchants() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        for (Enchantment enchant : itemMeta.getEnchants().keySet()) {
            itemMeta.removeEnchant(enchant);
        }
        itemStack.setItemMeta(itemMeta);
    }

    public boolean isNull() {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    public ItemStack getForDisplay() {
        if (this.isNull()) {
            return placeholder;
        }

        return this.get();
    }

    public ItemStack get() {
        return itemStack.clone();
    }

    // Getters
    public Material getMaterial() {
        return itemStack.getType();
    }

    public boolean isUnbreakable() {
        return ClassImport.getClasses().getLadderUtil().isUnbreakable(itemStack);
    }

    // Setters
    public KitItem setName(String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(StringUtil.CC(name));
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public void setUnbreakable(boolean unbreakable) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta = ClassImport.getClasses().getLadderUtil().setUnbreakable(itemMeta, unbreakable);
        itemStack.setItemMeta(itemMeta);
    }

    public void setDurability(int durability) {
        itemStack = ClassImport.getClasses().getLadderUtil().setDurability(itemStack, durability);
    }

    public KitItem setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

}
