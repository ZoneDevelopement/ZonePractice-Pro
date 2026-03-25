package dev.nandi0813.practice.premium.telemetry;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public enum TelemetryBootstrap {
    ;

    private static final String ENABLED_PATH = "TELEMETRY.ENABLED";
    private static final String STATS_ENABLED_SEGMENT = "stats-enabled/";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicBoolean resolved = new AtomicBoolean(false);

    private static final Object BOOTSTRAP_LOCK = new Object();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    private static volatile boolean active;
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

            if (!ConfigManager.getBoolean(ENABLED_PATH)) {
                active = false;
                resolved.set(true);
                ZonePractice.getInstance().getLogger().info("Telemetry disabled in config (" + ENABLED_PATH + ").");
                initializationFuture = CompletableFuture.completedFuture(false);
                return initializationFuture;
            }

            URI telemetryEndpoint = TelemetryLogger.resolveConfiguredEndpoint();
            if (telemetryEndpoint == null) {
                active = false;
                resolved.set(true);
                ZonePractice.getInstance().getLogger().info("Telemetry disabled: endpoint is not configured.");
                initializationFuture = CompletableFuture.completedFuture(false);
                return initializationFuture;
            }

            URI statsEndpoint = resolveStatsEnabledEndpoint(telemetryEndpoint);
            if (statsEndpoint == null) {
                active = false;
                resolved.set(true);
                ZonePractice.getInstance().getLogger().info("Telemetry disabled: failed to resolve stats-enabled endpoint.");
                initializationFuture = CompletableFuture.completedFuture(false);
                return initializationFuture;
            }

            String token = TelemetryLogger.resolveConfiguredToken();
            initializationFuture = fetchStatsEnabledAsync(statsEndpoint, token)
                    .exceptionally(ignored -> false)
                    .thenApply(Boolean::booleanValue)
                    .whenComplete((enabledByApi, throwable) -> {
                        active = throwable == null && enabledByApi;
                        resolved.set(true);

                        if (active) {
                            ZonePractice.getInstance().getLogger().info("Telemetry enabled by remote stats flag.");
                        } else {
                            ZonePractice.getInstance().getLogger().info("Telemetry disabled: remote stats flag is false or unreachable.");
                        }
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

    private static URI resolveStatsEnabledEndpoint(URI telemetryEndpoint) {
        try {
            String path = telemetryEndpoint.getPath();
            if (path == null || path.isBlank()) {
                return null;
            }

            String normalizedPath = path.endsWith("/") ? path : path + "/";
            String statsPath;
            if (normalizedPath.endsWith("/matches/") || normalizedPath.endsWith("/matches")) {
                statsPath = normalizedPath.replaceAll("/matches/?$", "/" + STATS_ENABLED_SEGMENT);
            } else {
                statsPath = normalizedPath + STATS_ENABLED_SEGMENT;
            }

            return new URI(
                    telemetryEndpoint.getScheme(),
                    telemetryEndpoint.getUserInfo(),
                    telemetryEndpoint.getHost(),
                    telemetryEndpoint.getPort(),
                    statsPath,
                    null,
                    null
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private static CompletableFuture<Boolean> fetchStatsEnabledAsync(URI statsEndpoint, String token) {
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
                    if (throwable != null || response == null) {
                        return false;
                    }

                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        return false;
                    }

                    return parseBooleanResponse(response.body());
                });
    }

    private static Boolean parseBooleanResponse(String body) {
        if (body == null) {
            return false;
        }

        String normalized = body.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("true") || normalized.equals("\"true\"")) {
            return true;
        }
        if (normalized.equals("false") || normalized.equals("\"false\"")) {
            return false;
        }

        if (normalized.contains("\"enabled\":true")) {
            return true;
        }
        if (normalized.contains("\"enabled\":false")) {
            return false;
        }
        return false;
    }
}
