package dev.nandi0813.practice.manager.playerkit;

import dev.nandi0813.practice.util.Common;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum PlayerKitUtil {
    ;

    public static ItemStack getItem(String string) {
        try {
            if (string.contains("::")) {
                String[] split = string.split("::");
                return new ItemStack(Material.valueOf(split[0]), 1, Short.parseShort(split[1]));
            } else if (string.equalsIgnoreCase("")) {
                return new ItemStack(Material.AIR);
            } else {
                return new ItemStack(Material.valueOf(string));
            }
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Invalid item: " + string);
        }
        return null;
    }

}
