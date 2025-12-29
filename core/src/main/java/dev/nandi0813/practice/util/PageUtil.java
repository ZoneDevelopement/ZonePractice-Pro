package dev.nandi0813.practice.util;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum PageUtil {
    ;

    public static List<ItemStack> getPageItems(List<ItemStack> items, int page, int spaces) {
        int upperBound = page * spaces;
        int lowerBound = upperBound - spaces;

        List<ItemStack> newItems = new ArrayList<>();
        for (int i = lowerBound; i < upperBound; i++) {
            try {
                newItems.add(items.get(i));
            } catch (IndexOutOfBoundsException x) {
                break;
            }
        }

        return newItems;
    }

    public static boolean isPageValid(List<ItemStack> items, int page, int spaces) {
        if (page <= 0) {
            return false;
        }

        int upperBound = page * spaces;
        int lowerBound = upperBound - spaces;

        return items.size() > lowerBound;
    }

    public static boolean isPageValid(int size, int page, int spaces) {
        if (page <= 0) {
            return false;
        }

        int upperBound = page * spaces;
        int lowerBound = upperBound - spaces;

        return size > lowerBound;
    }

    public static int getMaxPage(List<ItemStack> icons, int spaces) {
        int maxPage = 1;
        do maxPage++;
        while (PageUtil.isPageValid(icons, maxPage, spaces));
        return maxPage - 1;
    }

}
