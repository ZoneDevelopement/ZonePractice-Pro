package dev.nandi0813.practice.manager.matchhistory;

import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.util.Common;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * YAML-backed match history storage stored at:
 *   plugins/ZonePracticePro/match-history/<uuid>.yml
 *
 * Each file holds up to 5 match entries for that player.
 * Structure:
 *   matches:
 *     1:
 *       opponent_uuid: "..."
 *       player_name: "..."
 *       opponent_name: "..."
 *       kit_name: "..."
 *       arena_name: "..."
 *       player_score: 1
 *       opponent_score: 0
 *       player_final_health: 12.0
 *       opponent_final_health: 0.0
 *       winner_uuid: "..."
 *       match_duration: 87
 *       played_at: 1711234567890
 */
public class MatchHistoryFile extends ConfigFile {

    private static final int MAX_HISTORY = 5;
    private static final String ROOT = "matches";

    private final UUID playerUuid;

    public MatchHistoryFile(UUID playerUuid) {
        super("/match-history/", playerUuid.toString().toLowerCase());
        this.playerUuid = playerUuid;
    }

    // -----------------------------------------------------------------------
    // ConfigFile contract
    // -----------------------------------------------------------------------

    @Override
    public void setData() {
        // Not used directly — we manipulate sections ourselves via saveEntry / prune
        saveFile();
    }

    @Override
    public void getData() {
        // Not used directly — caller uses loadEntries()
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Loads all stored match history entries for this player, newest-first.
     */
    public List<MatchHistoryEntry> loadEntries() {
        List<MatchHistoryEntry> result = new ArrayList<>();

        ConfigurationSection root = config.getConfigurationSection(ROOT);
        if (root == null) return result;

        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) continue;

            try {
                UUID playerUuidParsed = UUID.fromString(section.getString("player_uuid", playerUuid.toString()));
                UUID opponentUuidParsed = UUID.fromString(section.getString("opponent_uuid", "00000000-0000-0000-0000-000000000000"));
                String winnerStr = section.getString("winner_uuid", null);
                UUID winnerUuid = (winnerStr != null && !winnerStr.isEmpty()) ? UUID.fromString(winnerStr) : null;

                result.add(new MatchHistoryEntry(
                        Integer.parseInt(key),
                        playerUuidParsed,
                        opponentUuidParsed,
                        section.getString("player_name", "Unknown"),
                        section.getString("opponent_name", "Unknown"),
                        section.getString("kit_name", "Unknown"),
                        section.getString("arena_name", "Unknown"),
                        section.getInt("player_score", 0),
                        section.getInt("opponent_score", 0),
                        section.getDouble("player_final_health", 0.0),
                        section.getDouble("opponent_final_health", 0.0),
                        winnerUuid,
                        section.getInt("match_duration", 0),
                        section.getLong("played_at", System.currentTimeMillis())
                ));
            } catch (Exception e) {
                Common.sendConsoleMMMessage("<yellow>[MatchHistory] Skipping corrupt entry " + key
                        + " for " + playerUuid + ": " + e.getMessage());
            }
        }

        // Sort newest-first (highest key = newest)
        result.sort((a, b) -> Integer.compare(b.getMatchId(), a.getMatchId()));
        return result;
    }

    /**
     * Saves a new entry and prunes so only the last MAX_HISTORY remain.
     * Returns the assigned match id (1-based auto-increment within this file).
     */
    public int saveEntry(MatchHistoryEntry entry) {
        // Determine next id
        int nextId = getNextId();

        String path = ROOT + "." + nextId;
        config.set(path + ".player_uuid", entry.getPlayerUuid().toString());
        config.set(path + ".opponent_uuid", entry.getOpponentUuid().toString());
        config.set(path + ".player_name", entry.getPlayerName());
        config.set(path + ".opponent_name", entry.getOpponentName());
        config.set(path + ".kit_name", entry.getKitName());
        config.set(path + ".arena_name", entry.getArenaName());
        config.set(path + ".player_score", entry.getPlayerScore());
        config.set(path + ".opponent_score", entry.getOpponentScore());
        config.set(path + ".player_final_health", entry.getPlayerFinalHealth());
        config.set(path + ".opponent_final_health", entry.getOpponentFinalHealth());
        config.set(path + ".winner_uuid", entry.getWinnerUuid() != null ? entry.getWinnerUuid().toString() : "");
        config.set(path + ".match_duration", entry.getMatchDuration());
        config.set(path + ".played_at", entry.getPlayedAt());

        pruneOldEntries();
        saveFile();
        return nextId;
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private int getNextId() {
        ConfigurationSection root = config.getConfigurationSection(ROOT);
        if (root == null) return 1;

        int max = 0;
        for (String key : root.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                if (id > max) max = id;
            } catch (NumberFormatException ignored) {}
        }
        return max + 1;
    }

    /**
     * Keeps only the last MAX_HISTORY entries (by id), deleting the rest.
     */
    private void pruneOldEntries() {
        ConfigurationSection root = config.getConfigurationSection(ROOT);
        if (root == null) return;

        List<Integer> ids = new ArrayList<>();
        for (String key : root.getKeys(false)) {
            try { ids.add(Integer.parseInt(key)); } catch (NumberFormatException ignored) {}
        }

        if (ids.size() <= MAX_HISTORY) return;

        ids.sort((a, b) -> Integer.compare(b, a)); // newest first
        List<Integer> toDelete = ids.subList(MAX_HISTORY, ids.size());
        for (int id : toDelete) {
            config.set(ROOT + "." + id, null);
        }
    }
}
