package dev.nandi0813.practice.manager.ladder.settings;

import dev.nandi0813.api.Event.Match.MatchEndEvent;
import dev.nandi0813.api.Event.Match.MatchStartEvent;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Centralized event listener that delegates to setting handlers.
 * This replaces scattered logic across multiple listener classes.
 * <p>
 * Instead of having multiple listeners with conditional logic for different settings,
 * this listener routes events to the appropriate handlers based on the match's active settings.
 */
public class CentralizedSettingListener implements Listener {

    /**
     * Handles all events that can be processed by setting handlers.
     * Gets the player's current match and delegates to registered handlers.
     */
    private void processEvent(org.bukkit.event.Event event, Player player) {
        if (player == null) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null || !profile.getStatus().equals(ProfileStatus.MATCH)) {
            return;
        }

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        // Delegate to the registry which will route to appropriate handlers
        SettingHandlerRegistry.processEvent(event, match, player);
    }

    /**
     * Called when a match starts - triggers onMatchStart for all active setting handlers.
     */
    @EventHandler ( priority = EventPriority.NORMAL )
    public void onMatchStart(MatchStartEvent e) {
        Match match = (Match) e.getMatch();

        // Trigger onMatchStart for all active settings
        for (SettingType settingType : match.getLadder().getType().getSettingTypes()) {
            SettingHandler<?> handler = SettingHandlerRegistry.getHandler(settingType);
            if (handler != null) {
                try {
                    handler.onMatchStart(match);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Called when a match ends - triggers onMatchEnd for all active setting handlers.
     */
    @EventHandler ( priority = EventPriority.NORMAL )
    public void onMatchEnd(MatchEndEvent e) {
        Match match = (Match) e.getMatch();

        // Trigger onMatchEnd for all active settings
        for (SettingType settingType : match.getLadder().getType().getSettingTypes()) {
            SettingHandler<?> handler = SettingHandlerRegistry.getHandler(settingType);
            if (handler != null) {
                try {
                    handler.onMatchEnd(match);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @EventHandler ( priority = EventPriority.NORMAL )
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        processEvent(e, player);
    }

    @EventHandler ( priority = EventPriority.NORMAL )
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        processEvent(e, player);
    }

    @EventHandler ( priority = EventPriority.NORMAL )
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        processEvent(e, e.getPlayer());
    }

    @EventHandler ( priority = EventPriority.NORMAL )
    public void onPlayerMove(PlayerMoveEvent e) {
        processEvent(e, e.getPlayer());
    }

}
