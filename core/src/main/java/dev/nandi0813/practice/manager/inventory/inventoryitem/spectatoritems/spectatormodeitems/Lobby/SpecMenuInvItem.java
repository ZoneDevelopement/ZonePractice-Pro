package dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Lobby;

import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import org.bukkit.entity.Player;

public class SpecMenuInvItem extends InvItem {

    public SpecMenuInvItem() {
        super(getItemStack("SPECTATOR.LOBBY.NORMAL.MENU.ITEM"), getInt("SPECTATOR.LOBBY.NORMAL.MENU.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        SpectatorManager.getInstance().spectateMenuUse(player);
    }

}
