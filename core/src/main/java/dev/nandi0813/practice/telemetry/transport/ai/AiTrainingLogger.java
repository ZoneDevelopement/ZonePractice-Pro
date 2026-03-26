package dev.nandi0813.practice.telemetry.transport.ai;

import dev.nandi0813.practice.telemetry.bootstrap.TelemetryBootstrap;
import dev.nandi0813.practice.telemetry.config.TelemetryConfig;
import dev.nandi0813.practice.util.Common;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public enum AiTrainingLogger {
    ;

    private static final Set<String> ALLOWED_LADDER_TYPES = Set.of("basic", "build", "sumo", "boxing");
    private static final int MAX_QUEUE_SIZE = 300;
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 300L;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicInteger droppedRecords = new AtomicInteger(0);
    private static final AtomicInteger failedRequests = new AtomicInteger(0);
    private static final AtomicInteger sentRequests = new AtomicInteger(0);

    private static ExecutorService writerExecutor;
    private static HttpClient httpClient;
    private static URI endpointUri;
    private static String authToken;
    private static volatile boolean transportEnabled;

    public static void logAsync(AiTrainingMatchPayload payload) {
        if (payload == null || payload.rows() == null || payload.rows().isEmpty()) {
            return;
        }

        String normalizedLadderType = payload.ladderType() == null
                ? ""
                : payload.ladderType().trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_LADDER_TYPES.contains(normalizedLadderType)) {
            return;
        }

        ensureInitialized();
        if (!transportEnabled || writerExecutor == null || writerExecutor.isShutdown()) {
            droppedRecords.incrementAndGet();
            return;
        }

        try {
            writerExecutor.execute(() -> sendRecord(payload));
        } catch (Exception exception) {
            droppedRecords.incrementAndGet();
            Common.sendConsoleMMMessage("<red>AI training logger rejected a record: " + exception.getMessage());
        }
    }

    public static void shutdown() {
        if (writerExecutor == null) {
            return;
        }

        writerExecutor.shutdown();
        try {
            if (!writerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                writerExecutor.shutdownNow();
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            writerExecutor.shutdownNow();
        } finally {
            initialized.set(false);
            writerExecutor = null;
            httpClient = null;
            endpointUri = null;
            authToken = null;
            transportEnabled = false;
        }
    }

    private static void ensureInitialized() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        if (!TelemetryBootstrap.isResolved()) {
            initialized.set(false);
            return;
        }

        if (!TelemetryBootstrap.isAiCollectionActive()) {
            transportEnabled = false;
            return;
        }

        endpointUri = TelemetryConfig.resolveAiMatchesEndpoint();
        authToken = TelemetryConfig.resolveConfiguredToken();

        if (endpointUri == null) {
            transportEnabled = false;
            Common.sendConsoleMMMessage("<yellow>AI training transport disabled: invalid AI endpoint.");
            return;
        }

        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "zpp-ai-training-writer");
            thread.setDaemon(true);
            return thread;
        };

        writerExecutor = new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(MAX_QUEUE_SIZE),
                factory,
                new ThreadPoolExecutor.AbortPolicy()
        );

        httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        transportEnabled = true;
    }

    private static void sendRecord(AiTrainingMatchPayload payload) {
        if (!transportEnabled || endpointUri == null || httpClient == null) {
            droppedRecords.incrementAndGet();
            return;
        }

        String jsonPayload = payload.toJson().toString();
        String idempotencyKey = payload.matchId();
        if (payload.schemaVersion() != 1 || idempotencyKey == null || idempotencyKey.isBlank()) {
            droppedRecords.incrementAndGet();
            return;
        }

        TelemetryBootstrap.fetchAiCollectionEnabledAsync()
                .whenComplete((enabled, throwable) -> {
                    if (throwable != null || !Boolean.TRUE.equals(enabled)) {
                        return;
                    }

                    sendRecordAttemptAsync(payload, jsonPayload, idempotencyKey, 1);
                });
    }

    private static boolean isRetryable(int statusCode) {
        return statusCode == 408 || statusCode == 429 || (statusCode >= 500 && statusCode <= 599);
    }

    private static void sendRecordAttemptAsync(AiTrainingMatchPayload payload, String jsonPayload, String idempotencyKey, int attempt) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(endpointUri)
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Idempotency-Key", idempotencyKey)
                .header("X-AI-Training-Schema-Version", String.valueOf(payload.schemaVersion()))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

        if (authToken != null && !authToken.isBlank()) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) -> {
                    if (throwable == null && response != null) {
                        int statusCode = response.statusCode();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 202) {
                            sentRequests.incrementAndGet();
                            return;
                        }

                        if (!isRetryable(statusCode) || attempt >= MAX_ATTEMPTS) {
                            failedRequests.incrementAndGet();
                            Common.sendConsoleMMMessage("<red>AI training REST failed (status=" + statusCode + ", match=" + payload.matchId() + ")");
                            return;
                        }
                    } else if (attempt >= MAX_ATTEMPTS) {
                        failedRequests.incrementAndGet();
                        String message = throwable != null ? throwable.getMessage() : "unknown error";
                        Common.sendConsoleMMMessage("<red>AI training REST exception (match=" + payload.matchId() + "): " + message);
                        return;
                    }

                    if (writerExecutor == null || writerExecutor.isShutdown()) {
                        droppedRecords.incrementAndGet();
                        return;
                    }

                    CompletableFuture.delayedExecutor(RETRY_BACKOFF_MS, TimeUnit.MILLISECONDS, writerExecutor)
                            .execute(() -> sendRecordAttemptAsync(payload, jsonPayload, idempotencyKey, attempt + 1));
                });
    }

}


