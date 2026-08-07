package dev.nandi0813.practice.manager.matchhistory;

import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.util.Common;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * YAML-backed per-player match history stored at:
 *   plugins/ZonePracticePro/match-history/<uuid>.yml
 *
 * Stores up to 5 recent match entries per player.
 */
public class MatchHistoryFile extends ConfigFile {

    private static final int MAX_HISTORY = 5;
    private static final String ROOT = "matches";

    private final MatchHistory matchHistory;

    public MatchHistoryFile(MatchHistory matchHistory) {
        super("/match-history/", matchHistory.getUuid().toString().toLowerCase());
        this.matchHistory = matchHistory;

        saveFile();
        reloadFile();
    }

    @Override
    public void setData() {
        saveFile();
    }

    /**
     * Saves a new match and prunes entries beyond the cap.
     * Returns the assigned id.
     */
    public int saveMatch(MatchHistoryEntry entry) {
        int nextId = getNextId();
        setMatch(entry, nextId);
        pruneOldMatches();
        saveFile();
        return nextId;
    }

    private void setMatch(MatchHistoryEntry entry, int matchId) {
        String match = ROOT + "." + matchId;

        config.set(match + ".player_uuid",           entry.getPlayerUuid().toString());
        config.set(match + ".opponent_uuid",          entry.getOpponentUuid().toString());
        config.set(match + ".player_name",            entry.getPlayerName());
        config.set(match + ".opponent_name",          entry.getOpponentName());
        config.set(match + ".kit_name",               entry.getKitName());
        config.set(match + ".arena_name",             entry.getArenaName());
        config.set(match + ".player_score",           entry.getPlayerScore());
        config.set(match + ".opponent_score",         entry.getOpponentScore());
        config.set(match + ".player_final_health",    entry.getPlayerFinalHealth());
        config.set(match + ".opponent_final_health",  entry.getOpponentFinalHealth());
        config.set(match + ".winner_uuid",            entry.getWinnerUuid() != null ? entry.getWinnerUuid().toString() : "");
        config.set(match + ".match_duration",         entry.getMatchDuration());
        config.set(match + ".played_at",              entry.getPlayedAt());
    }

    @Override
    public void getData() {
        loadMatches();
    }

    private void loadMatches() {
        matchHistory.getMatches().clear();

        ConfigurationSection root = config.getConfigurationSection(ROOT);
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            MatchHistoryEntry entry = loadMatch(key);
            if (entry != null) {
                matchHistory.getMatches().add(entry);
            }
        }

        matchHistory.getMatches().sort((a, b) -> Integer.compare(b.getMatchId(), a.getMatchId()));
    }

    private MatchHistoryEntry loadMatch(String matchId) {
        String match = ROOT + "." + matchId;

        try {
            return new MatchHistoryEntry(
                    Integer.parseInt(matchId),
                    UUID.fromString(config.getString(match + ".player_uuid",   matchHistory.getUuid().toString())),
                    UUID.fromString(config.getString(match + ".opponent_uuid", "00000000-0000-0000-0000-000000000000")),
                    config.getString(match + ".player_name",         "Unknown"),
                    config.getString(match + ".opponent_name",       "Unknown"),
                    config.getString(match + ".kit_name",            "Unknown"),
                    config.getString(match + ".arena_name",          "Unknown"),
                    config.getInt(match + ".player_score",           0),
                    config.getInt(match + ".opponent_score",         0),
                    config.getDouble(match + ".player_final_health", 0.0),
                    config.getDouble(match + ".opponent_final_health", 0.0),
                    getWinnerUuid(match),
                    config.getInt(match + ".match_duration",         0),
                    config.getLong(match + ".played_at",             System.currentTimeMillis())
            );
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<yellow>[MatchHistory] Skipping corrupt entry " + matchId
                    + " for " + matchHistory.getUuid() + ": " + e.getMessage());
            return null;
        }
    }

    private UUID getWinnerUuid(String match) {
        String winnerUuid = config.getString(match + ".winner_uuid");
        return (winnerUuid != null && !winnerUuid.isEmpty()) ? UUID.fromString(winnerUuid) : null;
    }

    private int getNextId() {
        ConfigurationSection root = config.getConfigurationSection(ROOT);
        if (root == null) return 1;
        int max = 0;
        for (String key : root.getKeys(false)) {
            try { int id = Integer.parseInt(key); if (id > max) max = id; }
            catch (NumberFormatException ignored) {}
        }
        return max + 1;
    }

    private void pruneOldMatches() {
        ConfigurationSection root = config.getConfigurationSection(ROOT);
        if (root == null) return;

        List<Integer> ids = new ArrayList<>();
        for (String key : root.getKeys(false)) {
            try { ids.add(Integer.parseInt(key)); } catch (NumberFormatException ignored) {}
        }

        if (ids.size() <= MAX_HISTORY) return;

        ids.sort((a, b) -> Integer.compare(b, a)); // newest first
        for (int id : ids.subList(MAX_HISTORY, ids.size())) {
            config.set(ROOT + "." + id, null);
        }
    }
}