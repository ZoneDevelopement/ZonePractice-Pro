package dev.nandi0813.practice.manager.fight.match.bot.neural;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NeuralInferenceClient {
    private static final Logger LOGGER = Logger.getLogger(NeuralInferenceClient.class.getName());
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofMillis(250))
            .build();
    private static final URI PREDICT_URI = URI.create("http://127.0.0.1:8000/predict");
    private static final Duration REQUEST_TIMEOUT = Duration.ofMillis(120);
    // Keep runtime behavior configurable in code without JVM flags.
    private static final boolean EXTENDED_PAYLOAD = false;
    private static final boolean DEBUG_INFERENCE = true;
    private static final int MAX_DEBUG_BODY_CHARS = 6000;
    private static final long DEBUG_LOG_INTERVAL_MILLIS = 500L;

    private final Gson gson = new Gson();
    private final AtomicLong lastDebugLogMillis = new AtomicLong(0L);

    public CompletableFuture<BotPrediction> fetchPrediction(GameState state) {
        String body = buildRequestBody(state);
        long startedAt = System.currentTimeMillis();
        String botId = state == null ? "unknown" : state.getBotId();

        byte[] payload = body.getBytes(StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder(PREDICT_URI)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .header("User-Agent", "ZonePractice-NeuralClient/1.0")
                .timeout(REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();

        return HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> parsePredictionResponse(response, body, botId, startedAt))
                .exceptionally(throwable -> {
                    debugLog("request-failed", botId,
                            "uri=" + PREDICT_URI + ", reason=" + throwable.getClass().getSimpleName() + ": " + throwable.getMessage(),
                            body,
                            null);
                    return null;
                });
    }

    private BotPrediction parsePredictionResponse(HttpResponse<String> response, String requestBody, String botId, long startedAt) {
        int status = response.statusCode();
        String responseBody = response.body();
        long latency = System.currentTimeMillis() - startedAt;

        if (status < 200 || status >= 300) {
            debugLog("non-2xx", botId,
                    "status=" + status + ", latencyMs=" + latency + ", uri=" + PREDICT_URI + ", responseVersion=" + response.version(),
                    requestBody,
                    responseBody);
            return null;
        }

        try {
            BotPrediction parsed = gson.fromJson(responseBody, BotPrediction.class);
            if (parsed == null) {
                debugLog("empty-prediction", botId,
                        "status=" + status + ", latencyMs=" + latency,
                        requestBody,
                        responseBody);
            }
            return parsed;
        } catch (Exception ex) {
            debugLog("json-parse-error", botId,
                    "status=" + status + ", latencyMs=" + latency + ", reason=" + ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                    requestBody,
                    responseBody);
            return null;
        }
    }

    private String buildRequestBody(GameState state) {
        JsonObject root = new JsonObject();
        root.addProperty("bot_id", state.getBotId());
        root.add("bot", gson.toJsonTree(state.getBot()));
        root.add("target", gson.toJsonTree(state.getTarget()));

        InventoryState inventory = state.getInventory();
        JsonObject inventoryJson = new JsonObject();
        inventoryJson.addProperty("main_hand", inventory.getMainHand());
        inventoryJson.addProperty("off_hand", inventory.getOffHand());

        JsonArray hotbar = new JsonArray();
        for (String slot : inventory.getHotbar()) {
            hotbar.add(slot);
        }
        inventoryJson.add("hotbar", hotbar);

        // FastAPI v1 contract rejects unknown keys (additionalProperties=false).
        if (EXTENDED_PAYLOAD) {
            inventoryJson.addProperty("selected_slot", inventory.getSelectedSlot());
            inventoryJson.addProperty("total_armor", inventory.getTotalArmor());

            JsonArray armor = new JsonArray();
            for (String piece : inventory.getArmor()) {
                armor.add(piece);
            }
            inventoryJson.add("armor", armor);
        }

        root.add("inventory", inventoryJson);
        return gson.toJson(root);
    }

    private void debugLog(String kind, String botId, String meta, String requestBody, String responseBody) {
        if (!DEBUG_INFERENCE && !"non-2xx".equals(kind)) {
            return;
        }
        if (!shouldEmitDebugLog()) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[bot-inference] ")
                .append(kind)
                .append(" botId=")
                .append(botId)
                .append(" ")
                .append(meta);

        if (requestBody != null) {
            builder.append(" request=").append(truncateForDebug(requestBody));
        }
        if (responseBody != null) {
            builder.append(" response=").append(truncateForDebug(responseBody));
        }

        LOGGER.log(Level.WARNING, builder.toString());
    }

    private boolean shouldEmitDebugLog() {
        long now = System.currentTimeMillis();
        long last = lastDebugLogMillis.get();
        if (now - last < DEBUG_LOG_INTERVAL_MILLIS) {
            return false;
        }
        lastDebugLogMillis.set(now);
        return true;
    }

    private static String truncateForDebug(String source) {
        if (source == null) {
            return "null";
        }
        if (source.length() <= MAX_DEBUG_BODY_CHARS) {
            return source;
        }
        return source.substring(0, MAX_DEBUG_BODY_CHARS) + "...(truncated)";
    }
}

