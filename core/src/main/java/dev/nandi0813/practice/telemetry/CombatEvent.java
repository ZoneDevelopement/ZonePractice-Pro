package dev.nandi0813.practice.telemetry;

import org.json.JSONObject;

/**
 * Represents a granular time-series combat event during a match.
 * Used for ML analytics, coaching reports, and playstyle detection.
 */
public record CombatEvent(
        TelemetryEvent eventType,
        long eventTs,
        int eventSeq,
        String actorUuid,
        String targetUuid,
        Double x,
        Double y,
        Double z,
        Double distance,
        Float deltaYaw,
        Float deltaPitch,
        Double damageAmount,
        JSONObject meta
) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("event_type", eventType.name());
        json.put("event_ts", eventTs);
        json.put("event_seq", eventSeq);
        json.put("actor_uuid", actorUuid);
        putNullable(json, "target_uuid", targetUuid);
        putNullable(json, "x", x);
        putNullable(json, "y", y);
        putNullable(json, "z", z);
        putNullable(json, "distance", distance);
        putNullable(json, "delta_yaw", deltaYaw);
        putNullable(json, "delta_pitch", deltaPitch);
        putNullable(json, "damage_amount", damageAmount);
        if (meta != null && !meta.isEmpty()) {
            json.put("meta", meta);
        }
        return json;
    }

    private static void putNullable(JSONObject json, String key, Object value) {
        json.put(key, value != null ? value : JSONObject.NULL);
    }
}

