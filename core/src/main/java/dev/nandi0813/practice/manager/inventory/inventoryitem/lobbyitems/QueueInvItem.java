package dev.nandi0813.practice.manager.inventory.inventoryitem.lobbyitems;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import org.bukkit.entity.Player;

import java.util.Locale;

public class QueueInvItem extends InvItem {

    private static final String DEFAULT_QUEUE_GUI_PATH = "QUEUE.COMBINED.DEFAULT-GUI";

    public QueueInvItem() {
        super(getItemStack("LOBBY-BASIC.NORMAL.QUEUE.ITEM"), getInt("LOBBY-BASIC.NORMAL.QUEUE.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        String defaultGui = ConfigManager.getString(DEFAULT_QUEUE_GUI_PATH)
                .trim()
                .toUpperCase(Locale.ROOT);

        if (defaultGui.equals("RANKED")) {
            player.performCommand("ranked");
            return;
        }

        player.performCommand("unranked");
    }
}

