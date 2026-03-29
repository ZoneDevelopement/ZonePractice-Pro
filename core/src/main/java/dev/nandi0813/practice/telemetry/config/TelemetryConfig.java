package dev.nandi0813.practice.telemetry.config;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.telemetry.bootstrap.TelemetryDebugLog;

import java.net.URI;

public enum TelemetryConfig {
    ;

    private static final String DEFAULT_BASE_ENDPOINT = "https://nandi0813.hu";
    //private static final String DEFAULT_BASE_ENDPOINT = "http://localhost:8000";
    private static final String ACCESS_TOKEN = "c22afd81c269b86c3c479cf1941c4aba5b842afda8228ad3540dab20e014b746";

    private static final String REGULAR_ENABLED_PATH = "TELEMETRY.ENABLED";
    private static final String AI_ENABLED_PATH = "TELEMETRY.AI.ENABLED";

    private static final String TELEMETRY_PATH_PREFIX = "/api/v1/telemetry/";
    private static final String MATCHES_PATH = "matches/";
    private static final String AI_MATCHES_PATH = "ai-training/matches/";
    private static final String STATS_ENABLED_PATH = "stats-enabled/";
    private static final String PRACTICE_STATS_UPLOAD_PATH = "practice-stats/upload/";

    public static boolean isRegularEnabled() {
        return ConfigManager.getBoolean(REGULAR_ENABLED_PATH);
    }

    public static boolean isAiEnabled() {
        return ConfigManager.getBoolean(AI_ENABLED_PATH);
    }

    public static URI resolveConfiguredBaseEndpoint() {
        URI parsed = parseHttpUri(DEFAULT_BASE_ENDPOINT);
        return normalizeToBaseHost(parsed);
    }

    public static String resolveConfiguredToken() {
        return ACCESS_TOKEN;
    }

    public static URI resolveMatchesEndpoint() {
        return appendTelemetryPath(MATCHES_PATH);
    }

    public static URI resolveAiMatchesEndpoint() {
        return appendTelemetryPath(AI_MATCHES_PATH);
    }

    public static URI resolveStatsEnabledEndpoint() {
        return appendTelemetryPath(STATS_ENABLED_PATH);
    }

    public static URI resolvePracticeStatsUploadEndpoint() {
        return appendTelemetryPath(PRACTICE_STATS_UPLOAD_PATH);
    }

    private static URI appendTelemetryPath(String suffix) {
        URI baseEndpoint = resolveConfiguredBaseEndpoint();
        if (baseEndpoint == null) {
            return null;
        }

        try {
            String basePath = baseEndpoint.getPath() == null ? "" : baseEndpoint.getPath();
            String normalizedBasePath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
            String fullPath = normalizedBasePath + TELEMETRY_PATH_PREFIX + suffix;

            return new URI(
                    baseEndpoint.getScheme(),
                    baseEndpoint.getUserInfo(),
                    baseEndpoint.getHost(),
                    baseEndpoint.getPort(),
                    fullPath,
                    null,
                    null
            );
        } catch (Exception exception) {
            TelemetryDebugLog.console("<red>Invalid telemetry endpoint path (" + exception.getMessage() + ")");
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isBlank()) {
                    return trimmed;
                }
            }
        }

        return null;
    }

    private static String readRawString(String path) {
        Object value = ConfigManager.get(path);
        return value == null ? null : String.valueOf(value);
    }

    private static URI parseHttpUri(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(endpoint.trim());
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                TelemetryDebugLog.console("<red>Telemetry endpoint must use http/https: " + endpoint);
                return null;
            }
            return uri;
        } catch (Exception exception) {
            TelemetryDebugLog.console("<red>Invalid telemetry endpoint: " + endpoint + " (" + exception.getMessage() + ")");
            return null;
        }
    }

    private static URI normalizeToBaseHost(URI endpointUri) {
        if (endpointUri == null) {
            return null;
        }

        String path = endpointUri.getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            return endpointUri;
        }

        String normalizedPath = path;
        String[] knownSuffixes = {
                "/api/v1/telemetry/matches/",
                "/api/v1/telemetry/matches",
                "/api/v1/telemetry/ai-training/matches/",
                "/api/v1/telemetry/ai-training/matches",
                "/api/v1/telemetry/stats-enabled/",
                "/api/v1/telemetry/stats-enabled",
                "/api/v1/telemetry/",
                "/api/v1/telemetry"
        };

        for (String suffix : knownSuffixes) {
            if (normalizedPath.endsWith(suffix)) {
                normalizedPath = normalizedPath.substring(0, normalizedPath.length() - suffix.length());
                if (normalizedPath.isBlank()) {
                    normalizedPath = "/";
                }
                break;
            }
        }

        try {
            return new URI(
                    endpointUri.getScheme(),
                    endpointUri.getUserInfo(),
                    endpointUri.getHost(),
                    endpointUri.getPort(),
                    normalizedPath,
                    null,
                    null
            );
        } catch (Exception exception) {
            return endpointUri;
        }
    }
}

