package dev.nandi0813.practice.manager.inventory.inventoryitem;

import dev.nandi0813.practice.manager.inventory.InventoryManager;
import org.bukkit.entity.Player;

public class ExtraInvItem extends InvItem {

    private final String command;

    public ExtraInvItem(String path) {
        super(getItemStack(path + ".ITEM"), getInt(path + ".SLOT"));

        this.command = InventoryManager.getInstance().getString(path + ".COMMAND");
    }

    @Override
    public void handleClickEvent(Player player) {
        player.performCommand(command);
    }

}
