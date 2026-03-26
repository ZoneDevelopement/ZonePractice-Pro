package dev.nandi0813.practice.telemetry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public record RoundTelemetry(
        int roundNumber,
        long roundStartTs,
        long roundEndTs,
        long roundDurationMs,
        UUID roundWinnerUuid,
        List<PlayerRoundTelemetry> playerRoundStats
) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("round_number", roundNumber);
        json.put("round_start_ts", roundStartTs);
        json.put("round_end_ts", roundEndTs);
        json.put("round_duration_ms", roundDurationMs);
        json.put("round_winner_uuid", roundWinnerUuid != null ? roundWinnerUuid.toString() : JSONObject.NULL);

        JSONArray players = new JSONArray();
        for (PlayerRoundTelemetry playerRoundStat : playerRoundStats) {
            players.put(playerRoundStat.toJson());
        }
        json.put("player_round_stats", players);
        return json;
    }

    public record PlayerRoundTelemetry(
            UUID playerUuid,
            int hitsLanded,
            int hitsTaken,
            int kills,
            int deaths,
            int potionThrown,
            int potionMissed,
            int potionAccuracy,
            int longestCombo,
            double avgCps
    ) {

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("player_uuid", playerUuid.toString());
            json.put("hits_landed", hitsLanded);
            json.put("hits_taken", hitsTaken);
            json.put("kills", kills);
            json.put("deaths", deaths);
            json.put("potion_thrown", potionThrown);
            json.put("potion_missed", potionMissed);
            json.put("potion_accuracy", potionAccuracy);
            json.put("longest_combo", longestCombo);
            json.put("avg_cps", avgCps);
            return json;
        }
    }
}

