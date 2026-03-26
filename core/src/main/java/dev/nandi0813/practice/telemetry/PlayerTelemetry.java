package dev.nandi0813.practice.telemetry;

import org.json.JSONObject;

import java.util.UUID;

public record PlayerTelemetry(
        UUID playerUuid,
        String playerUsername,
        UUID opponentUuid,
        String opponentUsername,
        Integer eloBefore,
        Integer eloAfter,
        Integer eloDelta,
        Long queueWaitMs,
        Integer queueSearchRangeStart,
        Integer queueSearchRangeEnd,
        boolean winner,
        int roundsWon,
        int kills,
        int deaths,
        int hitsLanded,
        int hitsTaken,
        int longestCombo,
        double avgCps,
        int potionThrown,
        int potionMissed,
        int potionAccuracy,
        double reachAvg,
        double reachMax,
        int sprintResetCount,
        double aimDeltaYawAvg,
        double aimDeltaPitchAvg,
        double damageDealt,
        double damageTaken,
        Integer pingAvg,
        Integer pingP95,
        double forwardMovementRatio,
        double retreatingMovementRatio,
        int sprintResetFrequency,
        int blockPlaceCount,
        int healingItemUsageCount
) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("player_uuid", playerUuid.toString());
        json.put("player_username", playerUsername != null ? playerUsername : JSONObject.NULL);
        putNullableUuid(json, "opponent_uuid", opponentUuid);
        json.put("opponent_username", opponentUsername != null ? opponentUsername : JSONObject.NULL);
        putNullable(json, "elo_before", eloBefore);
        putNullable(json, "elo_after", eloAfter);
        putNullable(json, "elo_delta", eloDelta);
        json.put("queue_wait_ms", queueWaitMs != null ? queueWaitMs : 0L);
        json.put("queue_search_range_start", queueSearchRangeStart != null ? queueSearchRangeStart : 0);
        json.put("queue_search_range_end", queueSearchRangeEnd != null ? queueSearchRangeEnd : 0);
        json.put("winner", winner);
        json.put("rounds_won", roundsWon);
        json.put("kills", kills);
        json.put("deaths", deaths);
        json.put("hits_landed", hitsLanded);
        json.put("hits_taken", hitsTaken);
        json.put("longest_combo", longestCombo);
        json.put("avg_cps", avgCps);
        json.put("potion_thrown", potionThrown);
        json.put("potion_missed", potionMissed);
        json.put("potion_accuracy", potionAccuracy);
        json.put("reach_avg", reachAvg);
        json.put("reach_max", reachMax);
        json.put("sprint_reset_count", sprintResetCount);
        json.put("aim_delta_yaw_avg", aimDeltaYawAvg);
        json.put("aim_delta_pitch_avg", aimDeltaPitchAvg);
        json.put("damage_dealt", damageDealt);
        json.put("damage_taken", damageTaken);
        putNullable(json, "ping_avg", pingAvg);
        putNullable(json, "ping_p95", pingP95);
        json.put("forward_movement_ratio", forwardMovementRatio);
        json.put("retreating_movement_ratio", retreatingMovementRatio);
        json.put("sprint_reset_frequency", sprintResetFrequency);
        json.put("block_place_count", blockPlaceCount);
        json.put("healing_item_usage_count", healingItemUsageCount);
        return json;
    }

    private static void putNullable(JSONObject json, String key, Object value) {
        json.put(key, value != null ? value : JSONObject.NULL);
    }

    private static void putNullableUuid(JSONObject json, String key, UUID value) {
        json.put(key, value != null ? value.toString() : JSONObject.NULL);
    }
}
