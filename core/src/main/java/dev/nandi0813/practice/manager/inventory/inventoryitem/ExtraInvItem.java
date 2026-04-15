package dev.nandi0813.practice.manager.inventory.inventoryitem;

import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import org.bukkit.entity.Player;

public class ExtraInvItem extends InvItem {

    private final String path;

    public ExtraInvItem(String path) {
        super(getItemStack(path + ".ITEM"), getInt(path + ".SLOT"));

        this.path = path;
    }

    @Override
    public void handleClickEvent(Player player) {
        String command = InventoryManager.getInstance().getString(path + ".COMMAND");
        if (command == null) return;

        command = command.trim();
        if (command.isEmpty()) return;

        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        ServerManager.runConsoleCommand(command.replace("%player%", player.getName()));
    }

}
