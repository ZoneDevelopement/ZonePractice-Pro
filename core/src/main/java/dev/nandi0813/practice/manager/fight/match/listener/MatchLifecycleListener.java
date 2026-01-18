package dev.nandi0813.practice.manager.fight.match.listener;

import dev.nandi0813.api.Event.Match.MatchEndEvent;
import dev.nandi0813.api.Event.Match.MatchStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.util.DeleteRunnable;
import dev.nandi0813.practice.manager.fight.match.util.RematchRequest;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Handles match lifecycle events (start/end) and match management.
 * Does NOT handle setting-specific events - those are handled by CentralizedSettingListener.
 */
public class MatchLifecycleListener implements Listener {

    @EventHandler
    public void onMatchStart(MatchStartEvent e) {
        Match match = (Match) e.getMatch();

        // Register match in manager
        for (Player player : match.getPlayers())
            MatchManager.getInstance().getPlayerMatches().put(player, match);

        MatchManager.getInstance().getMatches().put(match.getId(), match);
        MatchManager.getInstance().getLiveMatches().add(match);

        // Update GUIs
        if (match instanceof Duel && ((Duel) match).isRanked())
            GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
        else
            GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent e) {
        Match match = (Match) e.getMatch();

        // Unregister match from manager
        for (Player player : match.getPlayers())
            MatchManager.getInstance().getPlayerMatches().remove(player);

        Party party = PartyManager.getInstance().getParty(match);
        if (party != null)
            party.setMatch(null);

        MatchManager.getInstance().getLiveMatches().remove(match);

        // Update GUIs
        if (match instanceof Duel && ((Duel) match).isRanked())
            GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
        else
            GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();

        SpectatorManager.getInstance().getSpectatorMenuGui().update();

        DeleteRunnable.start(match);

        // Set rematch request items
        if (ZonePractice.getInstance().isEnabled() &&
                match.getType().equals(MatchType.DUEL) &&
                ConfigManager.getBoolean("MATCH-SETTINGS.REMATCH.ENABLED")) {

            boolean sendRematchRequest = true;
            for (Player matchPlayer : match.getPlayers()) {
                Profile matchPlayerProfile = ProfileManager.getInstance().getProfile(matchPlayer);
                if (matchPlayerProfile.isParty()) {
                    sendRematchRequest = false;
                    break;
                }
            }

            if (sendRematchRequest) {
                RematchRequest rematchRequest = new RematchRequest(match);
                MatchManager.getInstance().getRematches().add(rematchRequest);
            }
        }
    }
}
