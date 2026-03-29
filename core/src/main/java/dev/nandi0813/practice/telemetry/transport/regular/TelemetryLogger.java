package dev.nandi0813.practice.telemetry.transport.regular;

import dev.nandi0813.practice.telemetry.MatchTelemetry;
import dev.nandi0813.practice.telemetry.bootstrap.TelemetryBootstrap;
import dev.nandi0813.practice.telemetry.bootstrap.TelemetryDebugLog;
import dev.nandi0813.practice.telemetry.config.TelemetryConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public enum TelemetryLogger {
    ;

    private static final int MAX_QUEUE_SIZE = 2000;
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

    public static void logAsync(MatchTelemetry telemetry) {
        if (telemetry == null) {
            return;
        }

        ensureInitialized();
        if (!transportEnabled || writerExecutor == null || writerExecutor.isShutdown()) {
            droppedRecords.incrementAndGet();
            return;
        }

        try {
            writerExecutor.execute(() -> sendRecord(telemetry));
        } catch (Exception exception) {
            droppedRecords.incrementAndGet();
            TelemetryDebugLog.console("<red>Telemetry logger rejected a record: " + exception.getMessage());
        }
    }

    public static int getDroppedRecords() {
        return droppedRecords.get();
    }

    public static int getFailedRequests() {
        return failedRequests.get();
    }

    public static int getSentRequests() {
        return sentRequests.get();
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
        if (initialized.compareAndSet(false, true)) {
            if (!TelemetryBootstrap.isResolved()) {
                initialized.set(false);
                return;
            }

            if (!TelemetryBootstrap.isActive()) {
                transportEnabled = false;
                return;
            }

            endpointUri = TelemetryConfig.resolveMatchesEndpoint();
            authToken = TelemetryConfig.resolveConfiguredToken();

            if (endpointUri == null) {
                transportEnabled = false;
                TelemetryDebugLog.console("<yellow>Telemetry transport disabled: invalid telemetry endpoint in TelemetryLogger.");
                return;
            }

            ThreadFactory factory = runnable -> {
                Thread thread = new Thread(runnable, "zpp-telemetry-writer");
                thread.setDaemon(true);
                return thread;
            };

            writerExecutor = new ThreadPoolExecutor(
                    1,
                    1,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new java.util.concurrent.ArrayBlockingQueue<>(MAX_QUEUE_SIZE),
                    factory,
                    new ThreadPoolExecutor.AbortPolicy()
            );

            httpClient = HttpClient.newBuilder()
                    .connectTimeout(CONNECT_TIMEOUT)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            transportEnabled = true;
        }
    }

    public static URI resolveConfiguredBaseEndpoint() {
        return TelemetryConfig.resolveConfiguredBaseEndpoint();
    }

    public static String resolveConfiguredToken() {
        return TelemetryConfig.resolveConfiguredToken();
    }

    private static void sendRecord(MatchTelemetry telemetry) {
        if (!transportEnabled || endpointUri == null || httpClient == null) {
            droppedRecords.incrementAndGet();
            return;
        }

        sendRecordAttemptAsync(telemetry, telemetry.toJson().toString(), telemetry.matchId(), 1);
    }

    private static boolean isRetryable(int statusCode) {
        return statusCode == 408 || statusCode == 429 || (statusCode >= 500 && statusCode <= 599);
    }

    private static void sendRecordAttemptAsync(MatchTelemetry telemetry, String payload, String idempotencyKey, int attempt) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(endpointUri)
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Telemetry-Schema-Version", String.valueOf(telemetry.schemaVersion()))
                .header("X-Telemetry-Source", "ZonePracticePro")
                .header("X-Idempotency-Key", idempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload));

        if (authToken != null && !authToken.isBlank()) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) -> {
                    if (throwable == null && response != null) {
                        int statusCode = response.statusCode();
                        if (statusCode >= 200 && statusCode < 300) {
                            sentRequests.incrementAndGet();
                            return;
                        }

                        if (!isRetryable(statusCode) || attempt >= MAX_ATTEMPTS) {
                            failedRequests.incrementAndGet();
                            TelemetryDebugLog.console("<red>Telemetry REST failed (status=" + statusCode + ", match=" + telemetry.matchId() + ")");
                            return;
                        }
                    } else if (attempt >= MAX_ATTEMPTS) {
                        failedRequests.incrementAndGet();
                        String message = throwable != null ? throwable.getMessage() : "unknown error";
                        TelemetryDebugLog.console("<red>Telemetry REST exception (match=" + telemetry.matchId() + "): " + message);
                        return;
                    }

                    if (writerExecutor == null || writerExecutor.isShutdown()) {
                        droppedRecords.incrementAndGet();
                        return;
                    }

                    CompletableFuture.delayedExecutor(RETRY_BACKOFF_MS, TimeUnit.MILLISECONDS, writerExecutor)
                            .execute(() -> sendRecordAttemptAsync(telemetry, payload, idempotencyKey, attempt + 1));
                });
    }

}


