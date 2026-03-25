package dev.nandi0813.practice.premium.telemetry;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public enum TelemetryBootstrap {
    ;

    private static final String ENABLED_PATH = "TELEMETRY.ENABLED";
    private static final String STATS_ENABLED_SEGMENT = "stats-enabled/";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private static volatile boolean active;

    public static void initialize() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        if (!ConfigManager.getBoolean(ENABLED_PATH)) {
            active = false;
            ZonePractice.getInstance().getLogger().info("Telemetry disabled in config (" + ENABLED_PATH + ").");
            return;
        }

        URI telemetryEndpoint = TelemetryLogger.resolveConfiguredEndpoint();
        if (telemetryEndpoint == null) {
            active = false;
            ZonePractice.getInstance().getLogger().info("Telemetry disabled: endpoint is not configured.");
            return;
        }

        URI statsEndpoint = resolveStatsEnabledEndpoint(telemetryEndpoint);
        if (statsEndpoint == null) {
            active = false;
            ZonePractice.getInstance().getLogger().info("Telemetry disabled: failed to resolve stats-enabled endpoint.");
            return;
        }

        String token = TelemetryLogger.resolveConfiguredToken();
        Boolean enabledByApi = fetchStatsEnabled(statsEndpoint, token);
        active = Boolean.TRUE.equals(enabledByApi);

        if (active) {
            ZonePractice.getInstance().getLogger().info("Telemetry enabled by remote stats flag.");
        } else {
            ZonePractice.getInstance().getLogger().info("Telemetry disabled: remote stats flag is false or unreachable.");
        }
    }

    public static boolean isActive() {
        return active;
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

    private static Boolean fetchStatsEnabled(URI statsEndpoint, String token) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(CONNECT_TIMEOUT)
                    .build();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(statsEndpoint)
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .header("Accept", "application/json");

            if (token != null && !token.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return false;
            }

            return parseBooleanResponse(response.body());
        } catch (Exception ignored) {
            return false;
        }
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
