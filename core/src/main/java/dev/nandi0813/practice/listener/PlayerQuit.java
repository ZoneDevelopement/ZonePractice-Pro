package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    @EventHandler ( priority = EventPriority.LOWEST )
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        final Player player = e.getPlayer();
        final Profile profile = ProfileManager.getInstance().getProfile(player);
        final Party party = PartyManager.getInstance().getParty(player);

        if (party != null)
            party.removeMember(player, false);

        if (profile != null) {
            profile.setLastJoin(System.currentTimeMillis());

            // Check how many custom kits the player is allowed to save.
            int customKitPerm = profile.getCustomKitPerm();
            if (customKitPerm > 0) profile.setAllowedCustomKits(customKitPerm);

            if (ZonePractice.getInstance().isEnabled()) {
                Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                        profile.setStatus(ProfileStatus.OFFLINE), 5L);
            }
        }
    }

}
