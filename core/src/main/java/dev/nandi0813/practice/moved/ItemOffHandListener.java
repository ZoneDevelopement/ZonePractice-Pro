package dev.nandi0813.practice.moved;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ItemOffHandListener implements Listener {

    @EventHandler
    public void onItemSwitchHand(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (player.isOp() || player.hasPermission("*")) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile == null) return;

        switch (profile.getStatus()) {
            case LOBBY, QUEUE, EDITOR, SPECTATE -> e.setCancelled(true);
        }
    }

}
