package dev.nandi0813.practice.manager.fight.match;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.fight.belowname.BelowNameManager;
import dev.nandi0813.practice.manager.fight.match.listener.MatchEventListener;
import dev.nandi0813.practice.manager.fight.match.listener.MatchLifecycleListener;
import dev.nandi0813.practice.manager.fight.match.listener.StartListener;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.util.RematchRequest;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.settings.CentralizedSettingListener;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class MatchManager {

    private static MatchManager instance;

    public static MatchManager getInstance() {
        if (instance == null)
            instance = new MatchManager();
        return instance;
    }

    private final BelowNameManager belowNameManager;

    private final Map<String, Match> matches = new HashMap<>();
    private final List<Match> liveMatches = new ArrayList<>();
    private final Map<Player, Match> playerMatches = new HashMap<>();
    private final Set<RematchRequest> rematches = new HashSet<>();

    private MatchManager() {
        ZonePractice practice = ZonePractice.getInstance();

        // Register match lifecycle listener (start/end events)
        Bukkit.getPluginManager().registerEvents(new MatchLifecycleListener(), practice);

        // Register match event listener (teleport, projectile, kit selection, etc.)
        Bukkit.getPluginManager().registerEvents(new MatchEventListener(), practice);

        // Register centralized setting listener (all setting handlers)
        Bukkit.getPluginManager().registerEvents(new CentralizedSettingListener(), practice);

        // Register start command listener
        Bukkit.getPluginManager().registerEvents(new StartListener(), practice);

        this.belowNameManager = BelowNameManager.getInstance();
        PacketEvents.getAPI().getEventManager().registerListener(this.belowNameManager, PacketListenerPriority.NORMAL);
    }

    public Match getLiveMatchByPlayer(Player player) {
        return this.playerMatches.getOrDefault(player, null);
    }

    public Match getLiveMatchBySpectator(Player spectator) {
        Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(spectator);
        if (spectatable instanceof Match)
            return (Match) spectatable;
        else
            return null;
    }

    public List<Match> getLiveMatchesByArena(BasicArena arena) {
        List<Match> list = new ArrayList<>();
        for (Match match : liveMatches) {
            if (match.getArena().equals(arena))
                list.add(match);
        }
        return list;
    }

    public List<Match> getLiveMatchesByLadder(Ladder ladder) {
        List<Match> list = new ArrayList<>();
        for (Match match : liveMatches) {
            if (match.getLadder().equals(ladder))
                list.add(match);
        }
        return list;
    }

    public int getDuelMatchSize(Ladder ladder, boolean ranked) {
        int size = 0;
        for (Match match : liveMatches) {
            if (match.getLadder().equals(ladder)) {
                if (ranked && match instanceof Duel duel) {
                    if (duel.isRanked()) size++;
                } else
                    size++;
            }
        }
        return size * 2;
    }

    public int getPlayerInMatchSize() {
        int size = 0;
        for (Match match : liveMatches)
            size += match.getPlayers().size();
        return size;
    }

    public int getPlayerInMatchSize(final Ladder ladder) {
        int size = 0;
        for (Match match : liveMatches)
            if (match.getLadder().equals(ladder))
                size += match.getPlayers().size();
        return size;
    }

    /**
     * "Get all players in the arena that are not in the match or spectating the match."
     * <p>
     * The first thing we do is create a new list of players. We'll be adding players to this list and returning it at the
     * end of the function
     *
     * @param match The match that the players are being hidden from.
     * @return A list of players that are not in the match and are not spectating the match.
     */
    public List<Player> getHidePlayers(Match match) {
        List<Player> players = new ArrayList<>();

        for (Player player : match.getArena().getCuboid().getPlayers()) {
            if (!match.getPlayers().contains(player) && !match.getSpectators().contains(player)) {
                players.add(player);
            }
        }

        return players;
    }

    /**
     * End all live matches.
     */
    public void endMatches() {
        for (Match match : liveMatches)
            match.endMatch();
    }

    /**
     * Return the rematch request that contains the given player.
     *
     * @param player The player who is requesting the rematch.
     * @return A rematch request
     */
    public RematchRequest getRematchRequest(Player player) {
        for (RematchRequest rematchRequest : rematches)
            if (rematchRequest.getPlayers().contains(player))
                return rematchRequest;
        return null;
    }

}
