package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.module.interfaces.ConfigItemProvider;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

public class ModernConfigItemProvider implements ConfigItemProvider {

    @Override
    public GUIItem getGuiItem(YamlConfiguration config, String loc) {
        GUIItem guiItem = new GUIItem();

        if (config.isString(loc + ".NAME")) {
            guiItem.setName(config.getString(loc + ".NAME"));
        }

        if (config.isString(loc + ".MATERIAL")) {
            guiItem.setMaterial(Material.valueOf(config.getString(loc + ".MATERIAL")));
        }

        if (config.isInt(loc + ".AMOUNT")) {
            int amount = config.getInt(loc + ".AMOUNT");
            if (amount <= 0 || amount > 64) {
                amount = 1;
            }
            guiItem.setAmount(amount);
        }

        if (config.isInt(loc + ".DURABILITY")) {
            int durability = config.getInt(loc + ".DURABILITY");
            if (durability < 0) {
                durability = 1;
            }
            guiItem.setDurability(durability);
        }

        if (config.isList(loc + ".LORE")) {
            guiItem.setLore(config.getStringList(loc + ".LORE"));
        }

        if (config.isList(loc + ".FLAGS")) {
            List<String> flags = config.getStringList(loc + ".FLAGS");
            for (String flag : flags) {
                try {
                    ItemFlag itemFlag = ItemFlag.valueOf(flag);
                    guiItem.addItemFlag(itemFlag);
                } catch (IllegalArgumentException e) {
                    Common.sendConsoleMMMessage("<red>Invalid ItemFlag: " + flag);
                }
            }
        }

        // Always add flags to hide unbreakable and attributes for clean lobby items
        guiItem.addItemFlag(ItemFlag.HIDE_UNBREAKABLE);
        guiItem.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);

        // Set unbreakable to prevent durability bars from showing
        guiItem.setUnbreakable(true);

        if (config.isList(loc + ".ENCHANTMENTS")) {
            for (String s : config.getStringList(loc + ".ENCHANTMENTS")) {
                String[] enchantmentSplit = s.split(":");
                if (enchantmentSplit.length != 2) {
                    continue;
                }

                try {
                    Enchantment enchantment = Enchantment.getByName(enchantmentSplit[0]);
                    int level = Integer.parseInt(enchantmentSplit[1]);

                    if (enchantment.getStartLevel() > level) {
                        level = enchantment.getStartLevel();
                    }

                    guiItem.addEnchantment(enchantment, level);
                } catch (Exception e) {
                    Common.sendConsoleMMMessage("<red>Enchantment not found: " + e);
                }
            }
        }

        return guiItem;
    }

}
