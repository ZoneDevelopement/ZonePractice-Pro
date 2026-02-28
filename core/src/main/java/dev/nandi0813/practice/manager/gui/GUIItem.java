package dev.nandi0813.practice.manager.gui;

import dev.nandi0813.practice.module.interfaces.ItemCreateUtil;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIItem {

    @Getter
    @Setter
    private String name;
    @Getter
    private Material material;
    @Getter
    private Short damage = -1;
    @Getter
    private int amount = 1;
    @Getter
    private List<String> lore = new ArrayList<>();
    @Getter
    private boolean glowing = false;
    @Getter
    private boolean unbreakable = false;
    private final List<ItemFlag> itemFlags = new ArrayList<>();
    private final Map<Enchantment, Integer> enchantments = new HashMap<>();
    private int durability = -1;
    @Getter
    private ItemStack baseItemStack = null;

    public GUIItem() {
    }

    public GUIItem(Material material) {
        this.material = material;
    }

    public GUIItem(String name, Material material) {
        this.name = name;
        this.material = material;
    }

    public GUIItem(String name, Material material, int damage) {
        this.name = name;
        this.material = material;
        this.damage = Short.valueOf(String.valueOf(damage));
    }

    public GUIItem(String name, Material material, List<String> lore) {
        this.name = name;
        this.material = material;
        this.lore = lore;
    }

    public GUIItem(String name, Material material, short damage, List<String> lore) {
        this.name = name;
        this.material = material;
        this.damage = damage;
        this.lore = lore;
    }

    public GUIItem(String name, Material material, short damage, int amount) {
        this.name = name;
        this.material = material;
        this.damage = damage;
        this.amount = amount;
    }

    public GUIItem(String name, Material material, short damage, int amount, List<String> lore) {
        this.name = name;
        this.material = material;
        this.damage = damage;
        this.amount = amount;
        this.lore = lore;
    }

    public GUIItem(ItemStack itemStack) {
        if (itemStack == null) return;

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            this.name = itemMeta.getDisplayName();
            this.lore = itemMeta.getLore();
            this.glowing = itemMeta.hasEnchants();
        }
        this.amount = itemStack.getAmount();
        this.damage = itemStack.getDurability();
        this.material = itemStack.getType();
    }

    public GUIItem setGlowing(boolean glowing) {
        this.glowing = glowing;
        this.addItemFlag(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public GUIItem setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }


    public ItemStack get() {
        ItemStack itemStack;

        if (baseItemStack != null) {
            // Clone the base item to preserve special meta (e.g. PotionMeta for modern Minecraft)
            itemStack = baseItemStack.clone();
            itemStack.setAmount(amount);
            ItemCreateUtil.hideItemFlags(itemStack);
        } else {
            if (material == null) return null;

            if (damage == -1 && amount == 1) {
                itemStack = new ItemStack(material);
            } else if (damage == -1) {
                itemStack = new ItemStack(material, amount);
            } else {
                itemStack = new ItemStack(material, amount, damage);
            }

            if (durability > 0) {
                ClassImport.getClasses().getLadderUtil().setDurability(itemStack, durability);
            }
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (name != null) {
                itemMeta.setDisplayName(StringUtil.CC(name));
            }

            if (lore != null) {
                itemMeta.setLore(StringUtil.CC(lore));
            }

            if (glowing && enchantments.isEmpty()) {
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            }

            // Apply unbreakable status if set (for modern versions to prevent durability bars)
            if (unbreakable) {
                itemMeta = ClassImport.getClasses().getLadderUtil().setUnbreakable(itemMeta, true);
            }

            if (!itemFlags.isEmpty()) {
                itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
            } else {
                ItemCreateUtil.hideItemFlags(itemMeta);
            }

            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            }

            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    public ItemStack getForPlayerKit() {
        if (material == null) return null;

        ItemStack itemStack;
        if (damage == -1 && amount == 1) {
            itemStack = new ItemStack(material);
        } else if (damage == -1) {
            itemStack = new ItemStack(material, amount);
        } else {
            itemStack = new ItemStack(material, amount, damage);
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            if (name != null) {
                itemMeta.setDisplayName(StringUtil.CC(name));
            }

            if (lore != null) {
                itemMeta.setLore(StringUtil.CC(lore));
            }
            
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    public GUIItem replace(String regex, String replacement) {
        if (name != null) {
            this.setName(name.replace(regex, replacement));
        }

        if (lore != null) {
            List<String> replacementLore = new ArrayList<>();
            for (String line : lore) {
                replacementLore.add(line.replace(regex, replacement));
            }
            setLore(replacementLore);
        }

        return this;
    }

    public GUIItem setMaterial(Material material) {
        if (material == null) {
            return this;
        }

        this.material = material;
        return this;
    }

    public GUIItem setDamage(short damage) {
        if (damage == -1) {
            return this;
        }

        this.damage = damage;
        return this;
    }

    public GUIItem setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public GUIItem setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public void addItemFlag(ItemFlag itemFlag) {
        this.itemFlags.add(itemFlag);
    }

    public void addEnchantment(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
    }

    public GUIItem setDurability(int durability) {
        this.durability = durability;
        return this;
    }

    public GUIItem setBaseItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return this;
        }
        this.baseItemStack = itemStack.clone();
        this.material = itemStack.getType();
        return this;
    }

    public GUIItem cloneItem() {
        GUIItem guiItem = new GUIItem();
        guiItem.setName(this.name);
        guiItem.setLore(this.lore);
        guiItem.setDamage(this.damage);
        guiItem.setMaterial(this.material);
        guiItem.setGlowing(this.glowing);
        guiItem.setUnbreakable(this.unbreakable);
        guiItem.setAmount(this.amount);
        guiItem.setDurability(this.durability);
        if (this.baseItemStack != null) {
            guiItem.baseItemStack = this.baseItemStack.clone();
        }
        guiItem.itemFlags.addAll(this.itemFlags);
        guiItem.enchantments.putAll(this.enchantments);
        return guiItem;
    }

    public GUIItem replaceMMtoNormal() {
        this.name = Common.mmToNormal(this.name);

        List<String> lore = new ArrayList<>();
        for (String s : this.lore) {
            lore.add(Common.mmToNormal(s));
        }
        this.lore = lore;

        return this;
    }

}
