package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.command.privatemessage.MessageCommand;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.nametag.NametagManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.telemetry.transport.stats.PracticeStatsTelemetryLogger;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    @EventHandler ( priority = EventPriority.LOWEST )
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        final Player player = e.getPlayer();
        NametagManager.getInstance().onPlayerQuit(player);
        ServerManager.getInstance().onPlayerQuit(player);
        PlayerCooldown.clearPlayer(player);

        StatisticListener.getCURRENT_CPS().remove(player);
        StatisticListener.getCPS().remove(player);
        StatisticListener.getCURRENT_COMBO().remove(player);

        MessageCommand.latestMessage.remove(player);
        MessageCommand.latestMessage.values().removeIf(target -> target.equals(player));

        final Profile profile = ProfileManager.getInstance().getProfile(player);
        final Party party = PartyManager.getInstance().getParty(player);

        if (party != null)
            party.removeMember(player, false);

        MatchManager.getInstance().invalidateRematchByPlayer(player);

        if (profile != null) {
            profile.getActionBar().resetForReconnect();

            profile.setLastJoin(System.currentTimeMillis());
            PracticeStatsTelemetryLogger.markDirty(profile);

            // Check how many custom kits the player is allowed to save.
            int customKitPerm = profile.getCustomKitPerm();
            if (customKitPerm > 0) profile.setAllowedCustomKits(customKitPerm);

            profile.setPlayerCustomKitSelector(null);

            if (ZonePractice.getInstance().isEnabled()) {
                Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                        profile.setStatus(ProfileStatus.OFFLINE), 5L);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent e) {
        Player player = e.getPlayer();
        NametagManager.getInstance().onPlayerQuit(player);
        ServerManager.getInstance().onPlayerQuit(player);
        PlayerCooldown.clearPlayer(player);

        StatisticListener.getCURRENT_CPS().remove(player);
        StatisticListener.getCPS().remove(player);
        StatisticListener.getCURRENT_COMBO().remove(player);

        MessageCommand.latestMessage.remove(player);
        MessageCommand.latestMessage.values().removeIf(target -> target.equals(player));

        MatchManager.getInstance().invalidateRematchByPlayer(player);

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile != null) {
            profile.getActionBar().resetForReconnect();
        }
    }

}
