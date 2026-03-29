package dev.nandi0813.practice.telemetry.bootstrap;

import dev.nandi0813.practice.telemetry.config.TelemetryConfig;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public enum TelemetryBootstrap {
    ;

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicBoolean resolved = new AtomicBoolean(false);

    private static final Object BOOTSTRAP_LOCK = new Object();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static volatile boolean active;
    private static volatile boolean aiCollectionActive;
    private static volatile boolean practiceStatsActive;
    private static volatile boolean debugEnabled;
    private static volatile CompletableFuture<Boolean> initializationFuture;

    public static CompletableFuture<Boolean> initializeAsync() {
        synchronized (BOOTSTRAP_LOCK) {
            if (resolved.get()) {
                return CompletableFuture.completedFuture(active);
            }

            if (initializationFuture != null) {
                return initializationFuture;
            }

            if (!initialized.compareAndSet(false, true)) {
                return initializationFuture != null ? initializationFuture : CompletableFuture.completedFuture(active);
            }

            TelemetryDebugLog.setDebugEnabled(false);
            debugEnabled = false;

            boolean regularEnabled = TelemetryConfig.isRegularEnabled();
            boolean aiEnabled = TelemetryConfig.isAiEnabled();
            if (!regularEnabled && !aiEnabled) {
                active = false;
                aiCollectionActive = false;
                practiceStatsActive = false;
                resolved.set(true);
                initializationFuture = CompletableFuture.completedFuture(false);
                return initializationFuture;
            }

            URI statsEndpoint = TelemetryConfig.resolveStatsEnabledEndpoint();
            if (statsEndpoint == null) {
                active = false;
                aiCollectionActive = false;
                practiceStatsActive = false;
                resolved.set(true);
                initializationFuture = CompletableFuture.completedFuture(false);
                return initializationFuture;
            }

            String token = TelemetryConfig.resolveConfiguredToken();
            initializationFuture = fetchStatsEnabledAsync(statsEndpoint, token)
                    .thenApply(result -> {
                        boolean receivingEnabled = false;
                        boolean aiRemoteEnabled = false;
                        boolean practiceStatsRemoteEnabled = false;

                        if (!result.success()) {
                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(result.body);
                                receivingEnabled = extractBoolean(jsonObject, "is_receiving_enabled", false);
                                aiRemoteEnabled = extractBoolean(jsonObject, "is_ai_collection_enabled", false);
                                practiceStatsRemoteEnabled = extractBoolean(jsonObject, "is_practice_stats_enabled", false);
                                debugEnabled = extractBoolean(jsonObject, "is_debug_enabled", false);
                                TelemetryDebugLog.setDebugEnabled(debugEnabled);
                            } catch (Exception exception) {
                                debugEnabled = false;
                                TelemetryDebugLog.setDebugEnabled(false);
                            }
                        }

                        active = regularEnabled && receivingEnabled;
                        aiCollectionActive = aiEnabled && aiRemoteEnabled;
                        practiceStatsActive = regularEnabled && practiceStatsRemoteEnabled;
                        resolved.set(true);

                        TelemetryDebugLog.info(
                                "Telemetry flags -> regular=" + active + ", ai=" + aiCollectionActive
                                        + " (localRegular=" + regularEnabled
                                        + ", localAi=" + aiEnabled
                                        + ", remoteRegular=" + receivingEnabled
                                        + ", remoteAi=" + aiRemoteEnabled
                                        + ", practiceStats=" + practiceStatsActive
                                        + ", remotePracticeStats=" + practiceStatsRemoteEnabled
                                        + ", debug=" + debugEnabled + ")"
                        );

                        return active || aiCollectionActive || practiceStatsActive;
                    });
            return initializationFuture;
        }
    }

    public static void initialize() {
        initializeAsync();
    }

    public static boolean isActive() {
        return resolved.get() && active;
    }

    public static boolean isResolved() {
        return resolved.get();
    }

    public static boolean isAiCollectionActive() {
        return resolved.get() && aiCollectionActive;
    }

    public static boolean isPracticeStatsActive() {
        return resolved.get() && practiceStatsActive;
    }

    public static boolean isDebugEnabled() {
        return resolved.get() && debugEnabled;
    }

    public static CompletableFuture<Boolean> fetchAiCollectionEnabledAsync() {
        URI statsEndpoint = TelemetryConfig.resolveStatsEnabledEndpoint();
        if (statsEndpoint == null) {
            return CompletableFuture.completedFuture(false);
        }

        String token = TelemetryConfig.resolveConfiguredToken();
        return fetchStatsEnabledAsync(statsEndpoint, token)
                .thenApply(result -> {
                    if (!result.success()) {
                        return false;
                    }

                    try {
                        JSONObject jsonObject = new JSONObject(result.body);
                        return extractBoolean(jsonObject, "is_ai_collection_enabled", false);
                    } catch (Exception ignored) {
                        return false;
                    }
                });
    }


    private static CompletableFuture<StatsFetchResult> fetchStatsEnabledAsync(URI statsEndpoint, String token) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(statsEndpoint)
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .header("Accept", "application/json");

        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        return HTTP_CLIENT
                .sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .handle((response, throwable) -> {
                    if (throwable != null) {
                        return StatsFetchResult.error(throwable.getMessage());
                    }

                    if (response == null) {
                        return StatsFetchResult.error("empty response");
                    }

                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        return StatsFetchResult.failed(response.statusCode(), response.body());
                    }

                    return StatsFetchResult.success(response.statusCode(), response.body());
                });
    }

    private static boolean extractBoolean(JSONObject object, String key, boolean defaultValue) {
        if (object.has(key)) {
            return object.optBoolean(key, defaultValue);
        }

        JSONObject dataObject = object.optJSONObject("data");
        if (dataObject != null && dataObject.has(key)) {
            return dataObject.optBoolean(key, defaultValue);
        }

        return defaultValue;
    }

    private static final class StatsFetchResult {
        private final int statusCode;
        private final String body;
        private final String error;

        private StatsFetchResult(int statusCode, String body, String error) {
            this.statusCode = statusCode;
            this.body = body;
            this.error = error;
        }

        private boolean success() {
            return error == null && statusCode >= 200 && statusCode < 300 && body != null;
        }

        private static StatsFetchResult success(int statusCode, String body) {
            return new StatsFetchResult(statusCode, body, null);
        }

        private static StatsFetchResult failed(int statusCode, String body) {
            return new StatsFetchResult(statusCode, body, "non-2xx");
        }

        private static StatsFetchResult error(String error) {
            return new StatsFetchResult(-1, null, error);
        }
    }
}
