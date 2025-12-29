package dev.nandi0813.practice.manager.inventory.inventoryitem;

import dev.nandi0813.practice.manager.inventory.InventoryManager;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class InvItem {

    private final ItemStack item;
    private final int slot;

    public InvItem(final ItemStack item, final int slot) {
        this.item = item;
        this.slot = slot;
    }

    public abstract void handleClickEvent(Player player);

    protected static ItemStack getItemStack(String path) {
        return InventoryManager.getInstance().getGuiItem(path).get();
    }

    protected static int getInt(String path) {
        return InventoryManager.getInstance().getConfig().getInt(path);
    }

}
