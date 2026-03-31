package dev.nandi0813.practice.manager.matchhistory;

import dev.nandi0813.practice.manager.backend.MysqlManager;
import dev.nandi0813.practice.util.Common;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Central manager for match history.
 *
 * Storage strategy (dual-backend):
 *   1. YAML files  — always used. Stored at match-history/<uuid>.yml
 *                    Works whether MySQL is enabled or not.
 *   2. MySQL/MariaDB — also used when the connection is available.
 *                    Useful for cross-server or external queries.
 *
 * In-memory cache is kept for fast GUI opens without hitting disk each time.
 */
public class MatchHistoryManager {

    private static MatchHistoryManager instance;

    public static MatchHistoryManager getInstance() {
        if (instance == null) {
            instance = new MatchHistoryManager();
        }
        return instance;
    }

    private static final int MAX_HISTORY = 5;

    /** In-memory cache: player UUID → last 5 entries, newest-first. */
    private final Map<UUID, List<MatchHistoryEntry>> cache = new ConcurrentHashMap<>();

    private MatchHistoryManager() {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Records a completed duel match for both participants.
     * Writes to YAML immediately (async) and also to MySQL if connected.
     */
    public void saveMatchAsync(UUID playerUuid, UUID opponentUuid,
                               String playerName, String opponentName,
                               String kitName, String arenaName,
                               int playerScore, int opponentScore,
                               double playerFinalHealth, double opponentFinalHealth,
                               UUID winnerUuid, int matchDuration) {

        long now = System.currentTimeMillis();

        CompletableFuture.runAsync(() -> {
            // Build a temporary entry with id=-1 (YAML will assign the real id)
            MatchHistoryEntry tempEntry = new MatchHistoryEntry(
                    -1, playerUuid, opponentUuid,
                    playerName, opponentName,
                    kitName, arenaName,
                    playerScore, opponentScore,
                    playerFinalHealth, opponentFinalHealth,
                    winnerUuid, matchDuration, now);

            // --- YAML storage (always) ---
            // Save from player's perspective
            int assignedId = saveToYaml(playerUuid, tempEntry);

            // Save from opponent's perspective (swap player/opponent fields)
            MatchHistoryEntry opponentEntry = new MatchHistoryEntry(
                    -1, opponentUuid, playerUuid,
                    opponentName, playerName,
                    kitName, arenaName,
                    opponentScore, playerScore,
                    opponentFinalHealth, playerFinalHealth,
                    winnerUuid, matchDuration, now);
            saveToYaml(opponentUuid, opponentEntry);

            // Build the final entry with the id assigned by player's YAML
            MatchHistoryEntry finalEntry = new MatchHistoryEntry(
                    assignedId, playerUuid, opponentUuid,
                    playerName, opponentName,
                    kitName, arenaName,
                    playerScore, opponentScore,
                    playerFinalHealth, opponentFinalHealth,
                    winnerUuid, matchDuration, now);

            // Update cache for both players (from their own perspective)
            addToCache(playerUuid, finalEntry);
            addToCache(opponentUuid, new MatchHistoryEntry(
                    assignedId, opponentUuid, playerUuid,
                    opponentName, playerName,
                    kitName, arenaName,
                    opponentScore, playerScore,
                    opponentFinalHealth, playerFinalHealth,
                    winnerUuid, matchDuration, now));

            // --- MySQL storage (optional) ---
            if (MysqlManager.isConnected(false)) {
                try (Connection conn = MysqlManager.getConnection()) {
                    insertMatchRowMySQL(conn,
                            playerUuid, opponentUuid, playerName, opponentName,
                            kitName, arenaName, playerScore, opponentScore,
                            playerFinalHealth, opponentFinalHealth,
                            winnerUuid, matchDuration, now);
                    pruneOldRowsMySQL(conn, playerUuid);
                    pruneOldRowsMySQL(conn, opponentUuid);
                } catch (SQLException e) {
                    Common.sendConsoleMMMessage(
                            "<yellow>[MatchHistory] MySQL write failed (YAML still saved): " + e.getMessage());
                }
            }
        });
    }

    /**
     * Loads match history for the given player.
     * Checks cache first; falls back to YAML file; also checks MySQL if connected.
     * Returns a CompletableFuture with newest-first entries.
     */
    public CompletableFuture<List<MatchHistoryEntry>> loadHistoryAsync(UUID playerUuid) {
        if (cache.containsKey(playerUuid)) {
            return CompletableFuture.completedFuture(new ArrayList<>(cache.get(playerUuid)));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<MatchHistoryEntry> entries = loadFromYaml(playerUuid);

            // If YAML is empty and MySQL is available, try pulling from MySQL
            if (entries.isEmpty() && MysqlManager.isConnected(false)) {
                try (Connection conn = MysqlManager.getConnection()) {
                    entries = fetchFromMySQL(conn, playerUuid);
                } catch (SQLException e) {
                    Common.sendConsoleMMMessage(
                            "<yellow>[MatchHistory] MySQL read failed: " + e.getMessage());
                }
            }

            cache.put(playerUuid, new ArrayList<>(entries));
            return entries;
        });
    }

    /** Returns cached history without a DB hit — empty list if not yet loaded. */
    public List<MatchHistoryEntry> getCachedHistory(UUID playerUuid) {
        return cache.getOrDefault(playerUuid, Collections.emptyList());
    }

    /** Drops the in-memory cache for a player so the next load re-reads from disk. */
    public void invalidateCache(UUID playerUuid) {
        cache.remove(playerUuid);
    }

    // -----------------------------------------------------------------------
    // YAML helpers
    // -----------------------------------------------------------------------

    private int saveToYaml(UUID uuid, MatchHistoryEntry entry) {
        try {
            MatchHistoryFile file = new MatchHistoryFile(uuid);
            return file.saveEntry(entry);
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>[MatchHistory] YAML save error for " + uuid + ": " + e.getMessage());
            return -1;
        }
    }

    private List<MatchHistoryEntry> loadFromYaml(UUID uuid) {
        try {
            MatchHistoryFile file = new MatchHistoryFile(uuid);
            return file.loadEntries();
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>[MatchHistory] YAML load error for " + uuid + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // -----------------------------------------------------------------------
    // MySQL helpers
    // -----------------------------------------------------------------------

    private void insertMatchRowMySQL(Connection conn,
                                     UUID playerUuid, UUID opponentUuid,
                                     String playerName, String opponentName,
                                     String kitName, String arenaName,
                                     int playerScore, int opponentScore,
                                     double playerFinalHealth, double opponentFinalHealth,
                                     UUID winnerUuid, int matchDuration, long playedAt)
            throws SQLException {

        String sql = "INSERT INTO match_history " +
                "(player_uuid, opponent_uuid, player_name, opponent_name, kit_name, arena_name, " +
                " player_score, opponent_score, player_final_health, opponent_final_health, " +
                " winner_uuid, match_duration, played_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, opponentUuid.toString());
            stmt.setString(3, playerName);
            stmt.setString(4, opponentName);
            stmt.setString(5, kitName);
            stmt.setString(6, arenaName);
            stmt.setInt(7, playerScore);
            stmt.setInt(8, opponentScore);
            stmt.setDouble(9, playerFinalHealth);
            stmt.setDouble(10, opponentFinalHealth);
            stmt.setString(11, winnerUuid != null ? winnerUuid.toString() : null);
            stmt.setInt(12, matchDuration);
            stmt.setLong(13, playedAt);
            stmt.executeUpdate();
        }
    }

    private void pruneOldRowsMySQL(Connection conn, UUID uuid) throws SQLException {
        String uuidStr = uuid.toString();

        List<Integer> allIds = new ArrayList<>();
        String selectSql = "SELECT match_id FROM match_history " +
                "WHERE player_uuid = ? OR opponent_uuid = ? ORDER BY match_id DESC";
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, uuidStr);
            stmt.setString(2, uuidStr);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) allIds.add(rs.getInt("match_id"));
            }
        }

        if (allIds.size() <= MAX_HISTORY) return;

        List<Integer> toDelete = allIds.subList(MAX_HISTORY, allIds.size());
        StringBuilder sb = new StringBuilder("DELETE FROM match_history WHERE match_id IN (");
        for (int i = 0; i < toDelete.size(); i++) sb.append(i == 0 ? "?" : ", ?");
        sb.append(")");

        try (PreparedStatement stmt = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < toDelete.size(); i++) stmt.setInt(i + 1, toDelete.get(i));
            stmt.executeUpdate();
        }
    }

    private List<MatchHistoryEntry> fetchFromMySQL(Connection conn, UUID uuid) throws SQLException {
        String uuidStr = uuid.toString();
        String sql = "SELECT * FROM match_history " +
                "WHERE player_uuid = ? OR opponent_uuid = ? " +
                "ORDER BY match_id DESC LIMIT " + MAX_HISTORY;

        List<MatchHistoryEntry> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuidStr);
            stmt.setString(2, uuidStr);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String winnerStr = rs.getString("winner_uuid");
                    UUID winnerUuid = (winnerStr != null && !winnerStr.isEmpty())
                            ? UUID.fromString(winnerStr) : null;
                    result.add(new MatchHistoryEntry(
                            rs.getInt("match_id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            UUID.fromString(rs.getString("opponent_uuid")),
                            rs.getString("player_name"),
                            rs.getString("opponent_name"),
                            rs.getString("kit_name"),
                            rs.getString("arena_name"),
                            rs.getInt("player_score"),
                            rs.getInt("opponent_score"),
                            rs.getDouble("player_final_health"),
                            rs.getDouble("opponent_final_health"),
                            winnerUuid,
                            rs.getInt("match_duration"),
                            rs.getLong("played_at")
                    ));
                }
            }
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Cache helpers
    // -----------------------------------------------------------------------

    private void addToCache(UUID uuid, MatchHistoryEntry entry) {
        List<MatchHistoryEntry> list = cache.computeIfAbsent(uuid, k -> new ArrayList<>());
        list.add(0, entry); // prepend = newest first
        if (list.size() > MAX_HISTORY) {
            list.subList(MAX_HISTORY, list.size()).clear();
        }
    }
}
