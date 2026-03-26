package dev.nandi0813.practice.telemetry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public record MatchTelemetry(
        int schemaVersion,
        String matchId,
        String matchType,
        String ladder,
        String arena,
        boolean ranked,
        int winsNeeded,
        String serverId,
        String pluginVersion,
        String mcProtocolVersion,
        long matchStartTs,
        long matchEndTs,
        long matchDurationMs,
        String terminationReason,
        List<PlayerTelemetry> players,
        List<RoundTelemetry> rounds,
        List<CombatEvent> events,
        int droppedEventCount,
        long createdAtTs
) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("schema_version", schemaVersion);
        json.put("match_id", matchId);
        json.put("match_type", matchType);
        json.put("ladder", ladder);
        json.put("arena", arena);
        json.put("ranked", ranked);
        json.put("wins_needed", winsNeeded);
        json.put("server_id", serverId);
        json.put("plugin_version", pluginVersion);
        json.put("mc_protocol_version", mcProtocolVersion);
        json.put("match_start_ts", matchStartTs);
        json.put("match_end_ts", matchEndTs);
        json.put("match_duration_ms", matchDurationMs);
        json.put("termination_reason", terminationReason);
        json.put("dropped_event_count", droppedEventCount);
        json.put("created_at_ts", createdAtTs);

        JSONArray playersArray = new JSONArray();
        for (PlayerTelemetry player : players) {
            playersArray.put(player.toJson());
        }
        json.put("players", playersArray);

        JSONArray roundsArray = new JSONArray();
        for (RoundTelemetry round : rounds) {
            roundsArray.put(round.toJson());
        }
        json.put("rounds", roundsArray);

        JSONArray eventsArray = new JSONArray();
        for (CombatEvent event : events) {
            eventsArray.put(event.toJson());
        }
        json.put("events", eventsArray);

        return json;
    }
}


