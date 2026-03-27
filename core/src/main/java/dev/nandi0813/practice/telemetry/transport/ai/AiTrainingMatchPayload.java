package dev.nandi0813.practice.telemetry.transport.ai;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public record AiTrainingMatchPayload(
        int schemaVersion,
        String matchId,
        String ladderName,
        String ladderType,
        String arenaName,
        String arenaType,
        String serverHash,
        long createdAtTs,
        List<JSONObject> rows
) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("schema_version", schemaVersion);
        json.put("match_id", matchId);
        json.put("ladderName", ladderName);
        json.put("ladderType", ladderType);
        json.put("arenaName", arenaName);
        json.put("arenaType", arenaType);
        json.put("serverHash", serverHash);
        json.put("created_at_ts", createdAtTs);

        JSONArray rowArray = new JSONArray();
        for (JSONObject row : rows) {
            rowArray.put(row);
        }
        json.put("rows", rowArray);
        return json;
    }
}

