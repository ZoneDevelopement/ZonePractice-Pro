package dev.nandi0813.practice.telemetry.transport.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.BackendManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.statistics.LadderStats;
import dev.nandi0813.practice.manager.profile.statistics.ProfileStat;
import dev.nandi0813.practice.telemetry.ServerFingerprintUtil;
import dev.nandi0813.practice.telemetry.bootstrap.TelemetryDebugLog;
import dev.nandi0813.practice.telemetry.config.TelemetryConfig;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public enum PracticeStatsTelemetryLogger {
    ;

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final long FLUSH_PERIOD_TICKS = 20L * 60L * 2L;
    private static final long BOOTSTRAP_BURST_PERIOD_TICKS = 5L;
    private static final int MAX_BATCH_SIZE = 200;
    private static final int MAX_IN_FLIGHT_UPLOADS = 4;

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicBoolean flushInProgress = new AtomicBoolean(false);
    private static final AtomicBoolean unauthorizedLogged = new AtomicBoolean(false);
    private static final AtomicBoolean bootstrapCheckStarted = new AtomicBoolean(false);
    private static final AtomicBoolean profilesLoaded = new AtomicBoolean(false);
    private static final AtomicBoolean bootstrapBurstActive = new AtomicBoolean(false);
    private static final AtomicInteger inFlightUploads = new AtomicInteger(0);

    private static final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static volatile BukkitTask flushTask;
    private static volatile BukkitTask bootstrapBurstTask;
    private static volatile URI endpointUri;
    private static volatile URI serverUploadedEndpointUri;
    private static volatile URI serverUploadedFallbackEndpointUri;
    private static volatile String authToken;
    private static volatile String serverHash;
    private static volatile HttpClient httpClient;
    private static volatile boolean transportEnabled;

    public static void initialize() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        if (!TelemetryConfig.isRegularEnabled()) {
            return;
        }

        endpointUri = TelemetryConfig.resolvePracticeStatsUploadEndpoint();
        serverUploadedEndpointUri = TelemetryConfig.resolvePracticeStatsServerUploadedEndpoint();
        serverUploadedFallbackEndpointUri = TelemetryConfig.resolvePracticeStatsServerUploadedFallbackEndpoint();
        authToken = TelemetryConfig.resolveConfiguredToken();
        serverHash = resolveServerHash();

        if (endpointUri == null) {
            TelemetryDebugLog.warning("Practice stats telemetry disabled: endpoint is not configured.");
            return;
        }

        if (serverHash == null || serverHash.isBlank()) {
            TelemetryDebugLog.warning("Practice stats telemetry disabled: server hash is missing.");
            return;
        }

        httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        transportEnabled = true;

        flushTask = Bukkit.getScheduler().runTaskTimer(
                ZonePractice.getInstance(),
                PracticeStatsTelemetryLogger::flushDirtyBatch,
                FLUSH_PERIOD_TICKS,
                FLUSH_PERIOD_TICKS
        );

        tryStartBootstrapCheck("initialize");
    }

    public static void markDirty(Profile profile) {
        if (profile == null) {
            return;
        }

        markDirty(profile.getUuid());
    }

    public static void markDirty(UUID uuid) {
        if (uuid == null || !initialized.get() || !transportEnabled) {
            return;
        }

        dirtyPlayers.add(uuid);
    }

    public static void shutdown() {
        BukkitTask currentTask = flushTask;
        if (currentTask != null) {
            currentTask.cancel();
            flushTask = null;
        }

        BukkitTask currentBootstrapTask = bootstrapBurstTask;
        if (currentBootstrapTask != null) {
            currentBootstrapTask.cancel();
            bootstrapBurstTask = null;
        }

        flushDirtyBatchNow();

        dirtyPlayers.clear();
        httpClient = null;
        endpointUri = null;
        serverUploadedEndpointUri = null;
        serverUploadedFallbackEndpointUri = null;
        authToken = null;
        serverHash = null;
        transportEnabled = false;
        initialized.set(false);
        unauthorizedLogged.set(false);
        bootstrapCheckStarted.set(false);
        profilesLoaded.set(false);
        bootstrapBurstActive.set(false);
        inFlightUploads.set(0);
    }

    public static void onProfilesLoaded() {
        profilesLoaded.set(true);
        tryStartBootstrapCheck("profiles-loaded");
    }

    private static void tryStartBootstrapCheck(String source) {
        if (!initialized.get() || !transportEnabled || httpClient == null || serverUploadedEndpointUri == null || serverHash == null) {
            TelemetryDebugLog.info("Practice stats bootstrap check skipped (source=" + source + "): telemetry transport is not ready yet.");
            return;
        }

        if (!profilesLoaded.get()) {
            TelemetryDebugLog.info("Practice stats bootstrap check waiting for profiles (source=" + source + ").");
            return;
        }

        if (!bootstrapCheckStarted.compareAndSet(false, true)) {
            return;
        }

        TelemetryDebugLog.info("Practice stats bootstrap check started for server hash " + serverHash
                + " using endpoint " + serverUploadedEndpointUri + ".");
        checkServerUploaded(serverUploadedEndpointUri, true);
    }

    private static void checkServerUploaded(URI endpoint, boolean allowFallbackOn404) {
        if (endpoint == null || httpClient == null) {
            return;
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(resolveServerUploadedCheckUri(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET();

        if (authToken != null && !authToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) -> {
                    if (throwable != null || response == null) {
                        TelemetryDebugLog.warning("Practice stats bootstrap check failed; continuing with incremental updates only.");
                        return;
                    }

                    if (response.statusCode() == 401 && unauthorizedLogged.compareAndSet(false, true)) {
                        TelemetryDebugLog.warning("Practice stats telemetry unauthorized (401). Check Bearer token configuration.");
                        return;
                    }

                    if (response.statusCode() == 404 && allowFallbackOn404 && shouldUseFallbackEndpoint(endpoint)) {
                        TelemetryDebugLog.info("Practice stats bootstrap check got 404 on " + endpoint + ", retrying fallback endpoint " + serverUploadedFallbackEndpointUri + ".");
                        checkServerUploaded(serverUploadedFallbackEndpointUri, false);
                        return;
                    }

                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        TelemetryDebugLog.warning("Practice stats bootstrap check returned HTTP " + response.statusCode() + "; continuing with incremental updates only.");
                        return;
                    }

                    Boolean uploaded = parseUploadedFlag(response.body());
                    if (uploaded == null) {
                        TelemetryDebugLog.warning("Practice stats bootstrap check returned invalid JSON; continuing with incremental updates only.");
                        return;
                    }

                    if (uploaded) {
                        TelemetryDebugLog.info("Practice stats bootstrap check reports uploaded=true; skipping full initial sync.");
                        return;
                    }

                    Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
                        Set<UUID> allProfileUuids = new HashSet<>(ProfileManager.getInstance().getProfiles().keySet());
                        dirtyPlayers.addAll(allProfileUuids);
                        TelemetryDebugLog.info("Practice stats bootstrap check reports uploaded=false; queued "
                                + allProfileUuids.size() + " profiles for full initial sync.");
                        startBootstrapBurstFlush();
                    });
                });
    }

    private static void startBootstrapBurstFlush() {
        if (!bootstrapBurstActive.compareAndSet(false, true)) {
            return;
        }

        bootstrapBurstTask = Bukkit.getScheduler().runTaskTimer(
                ZonePractice.getInstance(),
                () -> {
                    if (!transportEnabled) {
                        stopBootstrapBurstFlush();
                        return;
                    }

                    flushDirtyBatch();

                    if (dirtyPlayers.isEmpty() && inFlightUploads.get() == 0) {
                        TelemetryDebugLog.info("Practice stats full initial sync completed.");
                        stopBootstrapBurstFlush();
                    }
                },
                1L,
                BOOTSTRAP_BURST_PERIOD_TICKS
        );
    }

    private static void stopBootstrapBurstFlush() {
        BukkitTask currentTask = bootstrapBurstTask;
        if (currentTask != null) {
            currentTask.cancel();
            bootstrapBurstTask = null;
        }
        bootstrapBurstActive.set(false);
    }

    private static boolean shouldUseFallbackEndpoint(URI endpoint) {
        if (serverUploadedFallbackEndpointUri == null) {
            return false;
        }

        return !serverUploadedFallbackEndpointUri.toString().equals(endpoint.toString());
    }

    private static void flushDirtyBatch() {
        if (!flushInProgress.compareAndSet(false, true)) {
            return;
        }

        if (!transportEnabled) {
            flushInProgress.set(false);
            return;
        }

        try {
            if (inFlightUploads.get() >= MAX_IN_FLIGHT_UPLOADS) {
                return;
            }

            List<UUID> batch = drainDirtyUuids(MAX_BATCH_SIZE);
            if (batch.isEmpty()) {
                return;
            }

            uploadBatch(batch, false);
        } finally {
            flushInProgress.set(false);
        }
    }

    private static void flushDirtyBatchNow() {
        List<UUID> batch = drainDirtyUuids(Integer.MAX_VALUE);
        if (batch.isEmpty()) {
            return;
        }

        uploadBatch(batch, true);
    }

    private static List<UUID> drainDirtyUuids(int limit) {
        List<UUID> drained = new ArrayList<>();

        for (UUID uuid : dirtyPlayers) {
            if (drained.size() >= limit) {
                break;
            }

            if (dirtyPlayers.remove(uuid)) {
                drained.add(uuid);
            }
        }

        return drained;
    }

    private static void uploadBatch(List<UUID> uuids, boolean syncOnCurrentThread) {
        if (uuids.isEmpty() || endpointUri == null || httpClient == null || serverHash == null) {
            return;
        }

        Map<String, Object> payload = buildPayload(uuids);
        if (payload == null) {
            return;
        }

        String payloadJson = GSON.toJson(payload);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpointUri)
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadJson));

        if (authToken != null && !authToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();

        if (syncOnCurrentThread) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                handleResponse(response.statusCode(), uuids);
            } catch (Exception ignored) {
            }
            return;
        }

        if (inFlightUploads.incrementAndGet() > MAX_IN_FLIGHT_UPLOADS) {
            inFlightUploads.decrementAndGet();
            requeue(uuids);
            return;
        }

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) -> {
                    try {
                        if (throwable != null) {
                            requeue(uuids);
                            return;
                        }

                        if (response == null) {
                            requeue(uuids);
                            return;
                        }

                        handleResponse(response.statusCode(), uuids);
                    } finally {
                        inFlightUploads.decrementAndGet();
                    }
                });
    }

    private static void handleResponse(int statusCode, List<UUID> uuids) {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }

        if (statusCode == 503) {
            return;
        }

        if (statusCode == 401) {
            if (unauthorizedLogged.compareAndSet(false, true)) {
                TelemetryDebugLog.warning("Practice stats telemetry unauthorized (401). Check Bearer token configuration.");
            }
            return;
        }

        requeue(uuids);
    }

    private static void requeue(List<UUID> uuids) {
        dirtyPlayers.addAll(uuids);
    }

    private static Map<String, Object> buildPayload(List<UUID> uuids) {
        List<Map<String, Object>> globalStats = new ArrayList<>();
        List<Map<String, Object>> ladderStats = new ArrayList<>();

        for (UUID uuid : uuids) {
            Profile profile = ProfileManager.getInstance().getProfile(uuid);
            if (profile == null) {
                continue;
            }

            ProfileStat stats = profile.getStats();
            String username = profile.getPlayer().getName() == null ? uuid.toString() : profile.getPlayer().getName();
            globalStats.add(toGlobalStatsMap(profile, stats, username));

            for (Map.Entry<NormalLadder, LadderStats> entry : stats.getLadderStats().entrySet()) {
                NormalLadder ladder = entry.getKey();
                LadderStats ladderStat = entry.getValue();
                ladderStats.add(toLadderStatsMap(profile, stats, username, ladder, ladderStat));
            }
        }

        if (globalStats.isEmpty() && ladderStats.isEmpty()) {
            return null;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("global_stats", globalStats);
        payload.put("ladder_stats", ladderStats);
        return payload;
    }

    private static String resolveServerHash() {
        String generated = ServerFingerprintUtil.getServerId();
        return BackendManager.getOrCreatePracticeStatsServerHash(generated);
    }

    private static URI resolveServerUploadedCheckUri(URI endpoint) {
        String encodedHash = URLEncoder.encode(serverHash, StandardCharsets.UTF_8);

        try {
            return new URI(
                    endpoint.getScheme(),
                    endpoint.getUserInfo(),
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getPath(),
                    "server_hash=" + encodedHash,
                    null
            );
        } catch (Exception exception) {
            return endpoint;
        }
    }

    private static Boolean parseUploadedFlag(String responseBody) {
        try {
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            if (!jsonObject.has("uploaded")) {
                return null;
            }
            return jsonObject.get("uploaded").getAsBoolean();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Map<String, Object> toGlobalStatsMap(Profile profile, ProfileStat stats, String username) {
        Map<String, Object> global = new LinkedHashMap<>();
        global.put("username", username);
        global.put("uuid", profile.getUuid().toString());
        global.put("serverHash", serverHash);
        global.put("firstJoin", profile.getFirstJoin());
        global.put("lastJoin", profile.getLastJoin());
        global.put("unrankedWins", stats.getWins(false));
        global.put("unrankedLosses", stats.getLosses(false));
        global.put("rankedWins", stats.getWins(true));
        global.put("rankedLosses", stats.getLosses(true));
        global.put("globalElo", stats.getGlobalElo());

        String division = stats.getDivision() == null ? null : stripDivisionColors(stats.getDivision().getFullName());
        global.put("globalRank", division);

        global.put("experience", stats.getExperience());
        global.put("winStreak", stats.getWinStreak());
        global.put("bestWinStreak", stats.getBestWinStreak());
        global.put("loseStreak", stats.getLoseStreak());
        global.put("bestLoseStreak", stats.getBestLoseStreak());
        return global;
    }

    private static Map<String, Object> toLadderStatsMap(Profile profile,
                                                         ProfileStat stats,
                                                         String username,
                                                         NormalLadder ladder,
                                                         LadderStats ladderStats) {
        Map<String, Object> ladderMap = new LinkedHashMap<>();
        ladderMap.put("username", username);
        ladderMap.put("uuid", profile.getUuid().toString());
        ladderMap.put("serverHash", serverHash);
        ladderMap.put("ladder", ladder.getName().toLowerCase());
        ladderMap.put("unrankedWins", ladderStats.getUnRankedWins());
        ladderMap.put("unrankedLosses", ladderStats.getUnRankedLosses());
        ladderMap.put("unrankedWinStreak", ladderStats.getUnRankedWinStreak());
        ladderMap.put("unrankedBestWinStreak", ladderStats.getUnRankedBestWinStreak());
        ladderMap.put("unrankedLoseStreak", ladderStats.getUnRankedLoseStreak());
        ladderMap.put("unrankedBestLoseStreak", ladderStats.getUnRankedBestLoseStreak());
        ladderMap.put("rankedWins", ladderStats.getRankedWins());
        ladderMap.put("rankedLosses", ladderStats.getRankedLosses());
        ladderMap.put("rankedWinStreak", ladderStats.getRankedWinStreak());
        ladderMap.put("rankedBestWinStreak", ladderStats.getRankedBestWinStreak());
        ladderMap.put("rankedLoseStreak", ladderStats.getRankedLoseStreak());
        ladderMap.put("rankedBestLoseStreak", ladderStats.getRankedBestLoseStreak());
        ladderMap.put("elo", ladderStats.getElo());

        String division = stats.getDivision() == null ? null : stripDivisionColors(stats.getDivision().getFullName());
        ladderMap.put("rank", division);

        ladderMap.put("kills", ladderStats.getKills());
        ladderMap.put("deaths", ladderStats.getDeaths());
        return ladderMap;
    }

    /**
     * Strips MiniMessage color codes (e.g., &lt;gray&gt;, &lt;gold&gt;) from a division name.
     * Converts &quot;&lt;gray&gt;Recruit&quot; to &quot;Recruit&quot;
     */
    private static String stripDivisionColors(String division) {
        if (division == null || division.isEmpty()) {
            return "";
        }
        // Deserialize the MiniMessage format and extract plain text
        return division.replaceAll("<[^>]+>", "");
    }
}



