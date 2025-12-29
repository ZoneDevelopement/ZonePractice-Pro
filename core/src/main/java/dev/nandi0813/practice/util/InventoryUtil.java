package dev.nandi0813.practice.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public enum InventoryUtil {
    ;

    public static Inventory createInventory(String title, int row) {
        return Bukkit.getServer().createInventory(null, row * 9, StringUtil.CC(title));
    }

}
