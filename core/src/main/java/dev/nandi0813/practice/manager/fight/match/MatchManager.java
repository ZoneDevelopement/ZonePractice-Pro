package dev.nandi0813.practice.manager.fight.match;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.fight.belowname.BelowNameManager;
import dev.nandi0813.practice.manager.fight.match.listener.LadderTypeListener;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class MatchManager {

    private static MatchManager instance;

    public static MatchManager getInstance() {
        if (instance == null)
            instance = new MatchManager();
        return instance;
    }

    private final BelowNameManager belowNameManager;

    private final Map<String, Match> matches = new ConcurrentHashMap<>();
    private final List<Match> liveMatches = new CopyOnWriteArrayList<>();
    private final Map<Player, Match> playerMatches = new ConcurrentHashMap<>();
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

        Bukkit.getPluginManager().registerEvents(new LadderTypeListener(), practice);


        this.belowNameManager = BelowNameManager.getInstance();
        // PacketEvents.getAPI().getEventManager().registerListener(this.belowNameManager, PacketListenerPriority.NORMAL);
    }

    public Match getLiveMatchByPlayer(Player player) {
        Match match = this.playerMatches.get(player);
        if (match != null) {
            return match;
        }

        // Recover from stale Player-object keys by resolving via UUID.
        UUID playerUuid = player.getUniqueId();
        for (Match liveMatch : this.liveMatches) {
            // Snapshot players to avoid CME if match player lists mutate during packet-thread checks.
            for (Player livePlayer : new ArrayList<>(liveMatch.getPlayers())) {
                if (playerUuid.equals(livePlayer.getUniqueId())) {
                    this.playerMatches.put(player, liveMatch);
                    return liveMatch;
                }
            }
        }

        return null;
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

    public void invalidateRematch(RematchRequest rematchRequest) {
        if (rematchRequest == null) return;

        if (rematches.remove(rematchRequest)) {
            rematchRequest.invalidate();
        }
    }

    public void invalidateRematchByPlayer(Player player) {
        if (player == null) return;

        RematchRequest rematchRequest = getRematchRequest(player);
        if (rematchRequest != null) {
            invalidateRematch(rematchRequest);
        }
    }

}
