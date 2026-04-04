package dev.nandi0813.practice.manager.matchhistory;

import dev.nandi0813.api.Event.Match.MatchEndEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.MysqlManager;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for match history.
 *
 * Storage strategy:
 *   1. YAML files  — always used (match-history/<uuid>.yml), same pattern as profiles/.
 *   2. MySQL        — also used when connected; logic lives in MysqlManager.
 *
 * Both reads and writes are async. An in-memory cache avoids repeated disk access.
 */
public class MatchHistoryManager implements Listener {

    private static MatchHistoryManager instance;

    public static MatchHistoryManager getInstance() {
        if (instance == null) instance = new MatchHistoryManager();
        return instance;
    }

    private static final int MAX_HISTORY = 5;

    /** In-memory cache: player UUID → last MAX_HISTORY entries, newest-first. */
    private final Map<UUID, List<MatchHistoryEntry>> cache = new ConcurrentHashMap<>();

    private MatchHistoryManager() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Records a completed 1v1 duel match for both participants asynchronously.
     * Always writes to YAML; also writes to MySQL when connected (via MysqlManager).
     */
    public void saveMatchAsync(UUID playerUuid, UUID opponentUuid,
                               String playerName, String opponentName,
                               String kitName, String arenaName,
                               int playerScore, int opponentScore,
                               double playerFinalHealth, double opponentFinalHealth,
                               UUID winnerUuid, int matchDuration) {

        long now = System.currentTimeMillis();

        CompletableFuture.runAsync(() -> {
            MatchHistoryEntry playerPov = new MatchHistoryEntry(
                    -1, playerUuid, opponentUuid, playerName, opponentName,
                    kitName, arenaName, playerScore, opponentScore,
                    playerFinalHealth, opponentFinalHealth, winnerUuid, matchDuration, now);

            MatchHistoryEntry opponentPov = new MatchHistoryEntry(
                    -1, opponentUuid, playerUuid, opponentName, playerName,
                    kitName, arenaName, opponentScore, playerScore,
                    opponentFinalHealth, playerFinalHealth, winnerUuid, matchDuration, now);

            // --- YAML (always) ---
            int assignedId = saveToYaml(playerUuid, playerPov);
            saveToYaml(opponentUuid, opponentPov);

            // Build final entries with real id for cache
            MatchHistoryEntry finalPlayerPov = new MatchHistoryEntry(
                    assignedId, playerUuid, opponentUuid, playerName, opponentName,
                    kitName, arenaName, playerScore, opponentScore,
                    playerFinalHealth, opponentFinalHealth, winnerUuid, matchDuration, now);

            MatchHistoryEntry finalOpponentPov = new MatchHistoryEntry(
                    assignedId, opponentUuid, playerUuid, opponentName, playerName,
                    kitName, arenaName, opponentScore, playerScore,
                    opponentFinalHealth, playerFinalHealth, winnerUuid, matchDuration, now);

            addToCache(playerUuid,   finalPlayerPov);
            addToCache(opponentUuid, finalOpponentPov);

            // --- MySQL (delegated to MysqlManager) ---
            if (MysqlManager.isConnected(false)) {
                MysqlManager.saveMatchHistoryAsync(
                        playerUuid, opponentUuid, playerName, opponentName,
                        kitName, arenaName, playerScore, opponentScore,
                        playerFinalHealth, opponentFinalHealth, winnerUuid, matchDuration, now);
            }
        });
    }

    /**
     * Loads history for a player asynchronously.
     * Returns from cache if available; otherwise reads YAML (and MySQL as fallback if YAML empty).
     */
    public CompletableFuture<List<MatchHistoryEntry>> loadHistoryAsync(UUID playerUuid) {
        if (cache.containsKey(playerUuid)) {
            return CompletableFuture.completedFuture(new ArrayList<>(cache.get(playerUuid)));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<MatchHistoryEntry> entries = loadFromYaml(playerUuid);

            if (entries.isEmpty() && MysqlManager.isConnected(false)) {
                entries = MysqlManager.loadMatchHistorySync(playerUuid, MAX_HISTORY);
            }

            cache.put(playerUuid, new ArrayList<>(entries));
            return entries;
        });
    }

    /** Non-blocking cache read — returns empty list if not yet loaded. */
    public List<MatchHistoryEntry> getCachedHistory(UUID playerUuid) {
        return cache.getOrDefault(playerUuid, Collections.emptyList());
    }

    /** Drops the cache for a player so the next load re-reads from disk. */
    public void invalidateCache(UUID playerUuid) {
        cache.remove(playerUuid);
    }

    // -----------------------------------------------------------------------
    // YAML helpers
    // -----------------------------------------------------------------------

    private int saveToYaml(UUID uuid, MatchHistoryEntry entry) {
        try {
            return new MatchHistoryFile(uuid).saveEntry(entry);
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>[MatchHistory] YAML save error for " + uuid + ": " + e.getMessage());
            return -1;
        }
    }

    private List<MatchHistoryEntry> loadFromYaml(UUID uuid) {
        try {
            return new MatchHistoryFile(uuid).loadEntries();
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>[MatchHistory] YAML load error for " + uuid + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // -----------------------------------------------------------------------
    // Cache helpers
    // -----------------------------------------------------------------------

    private void addToCache(UUID uuid, MatchHistoryEntry entry) {
        List<MatchHistoryEntry> list = cache.computeIfAbsent(uuid, k -> new ArrayList<>());
        list.add(0, entry); // prepend = newest first
        if (list.size() > MAX_HISTORY) list.subList(MAX_HISTORY, list.size()).clear();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMatchEnd(MatchEndEvent e) {
        if (!(e.getMatch() instanceof Duel duel)) {
            return;
        }

        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        if (player1 == null || player2 == null) {
            return;
        }

        Round lastRound = duel.getCurrentRound();
        Map<UUID, Statistic> stats = lastRound.getStatistics();

        Statistic stat1 = stats.get(player1.getUniqueId());
        Statistic stat2 = stats.get(player2.getUniqueId());

        double p1Health = (stat1 != null && stat1.isSet())
                ? stat1.getEndHeart()
                : (player1.isOnline() ? player1.getHealth() : 0.0);

        double p2Health = (stat2 != null && stat2.isSet())
                ? stat2.getEndHeart()
                : (player2.isOnline() ? player2.getHealth() : 0.0);

        UUID winnerUuid = duel.getMatchWinner() != null ? duel.getMatchWinner().getUniqueId() : null;

        MatchHistoryManager.getInstance().saveMatchAsync(
                player1.getUniqueId(), player2.getUniqueId(),
                player1.getName(), player2.getName(),
                duel.getLadder().getName(),
                duel.getArena().getName(),
                duel.getWonRounds(player1), duel.getWonRounds(player2),
                p1Health, p2Health,
                winnerUuid,
                duel.getDuration()
        );
    }

}
