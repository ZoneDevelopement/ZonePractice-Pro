package dev.nandi0813.practice.manager.leaderboard;

import dev.nandi0813.api.Event.FFARemovePlayerEvent;
import dev.nandi0813.api.Event.Match.MatchEndEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramManager;
import dev.nandi0813.practice.manager.leaderboard.types.LbMainType;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.statistics.LadderStats;
import dev.nandi0813.practice.util.StartUpCallback;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

@Getter
public class LeaderboardManager implements Listener {

    private static LeaderboardManager instance;

    public static LeaderboardManager getInstance() {
        if (instance == null)
            instance = new LeaderboardManager();
        return instance;
    }

    private final List<Leaderboard> leaderboards = new ArrayList<>();

    private LeaderboardManager() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    public Leaderboard searchLB(final LbMainType mainType, final LbSecondaryType secondaryType, final Ladder ladder) {
        for (Leaderboard lb : new ArrayList<>(leaderboards)) {
            if (lb.getMainType().equals(mainType) && lb.getSecondaryType().equals(secondaryType)) {
                if (ladder == null && lb.getLadder() == null)
                    return lb;
                else if (ladder != null && lb.getLadder() != null && ladder == lb.getLadder())
                    return lb;
            }
        }
        return null;
    }

    public void createAllLB(final StartUpCallback startUpCallback) {
        for (LbMainType lbMainType : LbMainType.values()) {
            for (LbSecondaryType lbSecondaryType : LbSecondaryType.values()) {
                if (lbMainType.equals(LbMainType.LADDER)) {
                    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
                        if (ladder.isEnabled()) {
                            updateLB(lbMainType, lbSecondaryType, ladder);
                        }
                    }
                } else {
                    updateLB(lbMainType, lbSecondaryType, null);
                }
            }
        }

        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), startUpCallback::onLoadingDone);
    }

    public void removeLadder(NormalLadder ladder) {
        HologramManager.getInstance().removeLadder(ladder);
        leaderboards.removeIf(leaderboard -> leaderboard.getLadder() != null && leaderboard.getLadder() == ladder);
    }

    public interface LeaderboardCallback {
        void onLeaderboardBuildDone(Map<OfflinePlayer, Integer> list);
    }

    public void updateLB(final LbMainType mainType, final LbSecondaryType secondaryType, final NormalLadder ladder) {
        if (!ZonePractice.getInstance().isEnabled()) {
            return;
        }

        createLB(mainType, secondaryType, ladder, list ->
        {
            if (list == null) {
                return;
            }

            Leaderboard leaderboard = searchLB(mainType, secondaryType, ladder);
            if (leaderboard != null) {
                leaderboard.setList(list);
            } else {
                leaderboard = new Leaderboard(mainType, secondaryType, ladder, list);
                leaderboards.add(leaderboard);
            }

            // NOTE: We do NOT trigger immediate hologram updates here anymore.
            // The HologramRunnable handles periodic updates on its own timer.
            // Triggering updates here was causing:
            // 1. Race conditions with multiple leaderboard updates happening simultaneously
            // 2. Excessive calls to getNextLeaderboard() which was rotating dynamic hologram ladders
            // 3. Holograms flickering/disappearing due to overlapping update cycles
            //
            // Holograms will show updated data on their next scheduled update cycle,
            // which is typically within a few seconds (configurable).
        });
    }

    public void createLB(final LbMainType mainType, final LbSecondaryType secondaryType, final NormalLadder ladder, final LeaderboardCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            // Use UUID as key to prevent duplicate players
            HashMap<UUID, ProfileData> tempMap = new HashMap<>();

            switch (mainType) {
                case GLOBAL:
                    switch (secondaryType) {
                        case ELO:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), profile.getStats().getGlobalElo()));
                            }
                            break;
                        case WIN:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), profile.getStats().getGlobalWins()));
                            }
                            break;
                        case KILLS:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), profile.getStats().getKills()));
                            }
                            break;
                        case DEATHS:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), profile.getStats().getDeaths()));
                            }
                            break;
                        case WIN_STREAK:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), profile.getStats().getWinStreak()));
                            }
                            break;
                        case LOSE_STREAK:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), profile.getStats().getLoseStreak()));
                            }
                            break;
                        case BEST_WIN_STREAK:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), profile.getStats().getBestWinStreak()));
                            }
                            break;
                        case BEST_LOSE_STREAK:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), profile.getStats().getBestLoseStreak()));
                            }
                            break;
                    }
                    break;
                case LADDER:
                    if (ladder == null)
                        break;

                    switch (secondaryType) {
                        case ELO:
                            if (ladder.isRanked()) {
                                for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                    LadderStats ladderStat = profile.getStats().getLadderStat(ladder);
                                    UUID uuid = profile.getPlayer().getUniqueId();
                                    tempMap.put(uuid, new ProfileData(profile.getPlayer(), ladderStat.getElo()));
                                }
                            }
                            break;
                        case WIN:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                LadderStats ladderStat = profile.getStats().getLadderStat(ladder);
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), ladderStat.getUnRankedWins() + ladderStat.getRankedWins()));
                            }
                            break;
                        case KILLS:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                LadderStats ladderStat = profile.getStats().getLadderStat(ladder);
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), ladderStat.getKills()));
                            }
                            break;
                        case DEATHS:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                LadderStats ladderStat = profile.getStats().getLadderStat(ladder);
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), ladderStat.getDeaths()));
                            }
                            break;
                        case WIN_STREAK:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                LadderStats ladderStat = profile.getStats().getLadderStat(ladder);
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), ladderStat.getUnRankedWinStreak() + ladderStat.getRankedWinStreak()));
                            }
                            break;
                        case LOSE_STREAK:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                LadderStats ladderStat = profile.getStats().getLadderStat(ladder);
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), ladderStat.getUnRankedLoseStreak() + ladderStat.getRankedLoseStreak()));
                            }
                            break;
                        case BEST_WIN_STREAK:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                LadderStats ladderStat = profile.getStats().getLadderStat(ladder);
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), ladderStat.getUnRankedBestWinStreak() + ladderStat.getRankedBestWinStreak()));
                            }
                            break;
                        case BEST_LOSE_STREAK:
                            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                                LadderStats ladderStat = profile.getStats().getLadderStat(ladder);
                                UUID uuid = profile.getPlayer().getUniqueId();
                                tempMap.put(uuid, new ProfileData(profile.getPlayer(), ladderStat.getUnRankedBestLoseStreak() + ladderStat.getRankedBestLoseStreak()));
                            }
                            break;
                    }
                    break;
            }

            // Convert tempMap to final map with OfflinePlayer as key
            HashMap<OfflinePlayer, Integer> unsorted = new HashMap<>();
            for (ProfileData data : tempMap.values()) {
                unsorted.put(data.player, data.value);
            }

            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> callback.onLeaderboardBuildDone(sortByValue(unsorted)));
        });
    }

    // Helper class to store player data temporarily
    private static class ProfileData {
        final OfflinePlayer player;
        final int value;

        ProfileData(OfflinePlayer player, int value) {
            this.player = player;
            this.value = value;
        }
    }

    /**
     * It takes a HashMap of OfflinePlayers and Integers, sorts it by the Integer value, and returns a LinkedHashMap of
     * OfflinePlayers and Integers
     *
     * @param map The HashMap you want to sort.
     * @return A LinkedHashMap with the keys sorted by value in descending order.
     */
    public static Map<OfflinePlayer, Integer> sortByValue(Map<OfflinePlayer, Integer> map) {
        if (map.isEmpty()) return map;

        LinkedHashMap<OfflinePlayer, Integer> reverseSortedMap = new LinkedHashMap<>();

        map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        return reverseSortedMap;
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent e) {
        Match match = (Match) e.getMatch();

        if (match instanceof Duel duel && match.getLadder() instanceof NormalLadder ladder) {
            for (LbMainType lbMainType : LbMainType.values()) {
                for (LbSecondaryType lbSecondaryType : LbSecondaryType.values()) {
                    if (!duel.isRanked() && lbSecondaryType.isRankedRelated()) {
                        continue;
                    }

                    switch (lbMainType) {
                        case GLOBAL:
                            updateLB(lbMainType, lbSecondaryType, null);
                            break;
                        case LADDER:
                            updateLB(lbMainType, lbSecondaryType, ladder);
                            break;
                    }
                }
            }
        }

        if (ZonePractice.getInstance().isEnabled()) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(ZonePractice.getInstance(), () -> {
                GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();
                GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
            }, 20L);
        }
    }

    @EventHandler
    public void onFFARemovePlayer(FFARemovePlayerEvent e) {
        FFA ffa = (FFA) e.getFfa();

        updateLB(LbMainType.GLOBAL, LbSecondaryType.KILLS, null);
        updateLB(LbMainType.GLOBAL, LbSecondaryType.DEATHS, null);

        // It's important to call this before the player gets removed from the FFA
        updateLB(LbMainType.LADDER, LbSecondaryType.KILLS, ffa.getPlayers().get(e.getPlayer()));
        updateLB(LbMainType.LADDER, LbSecondaryType.DEATHS, ffa.getPlayers().get(e.getPlayer()));
    }

}
