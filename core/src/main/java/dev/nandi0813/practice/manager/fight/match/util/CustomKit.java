package dev.nandi0813.practice.manager.fight.match.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class CustomKit {

    private ItemStack book;
    private ItemStack[] inventory;
    private ItemStack[] extra;

    public CustomKit(ItemStack book, ItemStack[] inventory, ItemStack[] extra) {
        this.book = book;
        this.inventory = inventory;
        this.extra = extra;
    }

}
