package dev.nandi0813.practice.util.actionbar;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class ActionBar {

    private final Profile profile;
    private static final int TICK_PERIOD = 2;
    private static final long INFINITE_EXPIRY = Long.MAX_VALUE;
    private static final long KEEPALIVE_INTERVAL_MILLIS = 1200L;
    private static final long MESSAGE_FALLBACK_WINDOW_MILLIS = 6000L;
    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();

    /**
     * Stores active messages using an ID (e.g., "golden_head", "queue").
     */
    private final Map<String, ActionMessage> activeMessages = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    private volatile String lastWinnerId = "-";
    private volatile String lastWinnerPlainText = "";
    private volatile String lastSentSignature = "";
    private volatile boolean lastSentWasEmpty = true;
    private volatile long lastSendAtMillis = 0L;
    private volatile long fallbackUntilMillis = 0L;

    /**
     * Internal runnable to update the action bar periodically.
     */
    private BukkitRunnable actionBarRunnable;

    public ActionBar(Profile profile) {
        this.profile = profile;
    }

    /**
     * Set or update a message in the action bar.
     *
     * @param id       Unique identifier for this action (e.g., "golden_head")
     * @param text     The text to display (MiniMessage format)
     * @param duration Duration in seconds (-1 for infinite)
     * @param priority Priority level (higher weight overrides lower weight)
     */
    public void setMessage(String id, String text, int duration, ActionBarPriority priority) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> setMessage(id, text, duration, priority));
            return;
        }

        if (id == null || id.isEmpty() || priority == null) {
            return;
        }

        Component component = deserializeOrFallback(text);
        String plainText = PLAIN_TEXT.serialize(component);
        if (plainText.trim().isEmpty()) {
            return;
        }

        ActionMessage previous = activeMessages.get(id);
        boolean infiniteDuration = duration < 0;
        if (previous != null
                && previous.priority == priority
                && previous.plainText.equals(plainText)
                && previous.isInfinite() == infiniteDuration) {
            return;
        }

        long expiresAtMillis = duration < 0
                ? INFINITE_EXPIRY
                : (System.currentTimeMillis() + (Math.max(1, duration) * 1000L));

        activeMessages.put(id, new ActionMessage(
                component,
                plainText,
                expiresAtMillis,
                priority,
                sequence.incrementAndGet()
        ));
        fallbackUntilMillis = System.currentTimeMillis() + MESSAGE_FALLBACK_WINDOW_MILLIS;

        startRunnable();
        sendHighestPriority(resolveLivePlayer(), true); // immediate update without runnable overlap
    }

    public void resetForReconnect() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), this::resetForReconnect);
            return;
        }

        activeMessages.clear();
        stopRunnable();
        resetLastSentState();
    }

    /**
     * Manually remove a specific action bar message before its duration expires.
     *
     * @param id Unique identifier of the message
     */
    public void removeMessage(String id) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> removeMessage(id));
            return;
        }

        if (id == null || id.isEmpty()) {
            return;
        }

        activeMessages.remove(id);
        Player player = resolveLivePlayer();
        if (player != null && player.isOnline()) {
            if (activeMessages.isEmpty()) {
                clearActionBarIfNeeded(player);
            } else {
                sendHighestPriority(player, true);
            }
        }

        if (activeMessages.isEmpty()) {
            stopRunnable();
        }
    }

    /**
     * Starts the internal runnable if it's not already running.
     */
    private void startRunnable() {
        if (actionBarRunnable != null) return;

        actionBarRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    tick();
                } catch (Throwable throwable) {
                    ZonePractice.getInstance().getLogger().warning("ActionBar tick failed for "
                            + profile.getUuid() + ": " + throwable.getMessage());
                    stopRunnable();
                }
            }
        };

        // Start on next period because setMessage() already performs the immediate send path.
        actionBarRunnable.runTaskTimer(ZonePractice.getInstance(), TICK_PERIOD, TICK_PERIOD);
    }

    /**
     * Stops the internal runnable if running.
     */
    private void stopRunnable() {
        if (actionBarRunnable != null) {
            actionBarRunnable.cancel();
            actionBarRunnable = null;
        }
    }

    /**
     * Called periodically by the internal runnable.
     * Handles duration updates, message expiration, and sending the highest priority message.
     */
    private void tick() {
        Player player = resolveLivePlayer();
        if (player == null || !player.isOnline()) {
            // Do not keep stale state/runnables around for offline players.
            activeMessages.clear();
            resetLastSentState();
            stopRunnable();
            return;
        }

        pruneExpiredMessages();

        if (!activeMessages.isEmpty()) {
            sendHighestPriority(player, false);
        } else {
            clearActionBarIfNeeded(player);
            stopRunnable();
        }
    }

    private void pruneExpiredMessages() {
        long now = System.currentTimeMillis();
        activeMessages.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    /**
     * Sends the highest priority message to the player.
     * If multiple messages share the same priority, the most recently updated one is shown.
     */
    private void sendHighestPriority(Player player, boolean force) {
        if (player == null || !player.isOnline()) return;

        String highestId = null;
        ActionMessage highest = null;
        for (Map.Entry<String, ActionMessage> entry : activeMessages.entrySet()) {
            ActionMessage msg = entry.getValue();
            if (highest == null) {
                highest = msg;
                highestId = entry.getKey();
                continue;
            }

            if (msg.priority.getWeight() > highest.priority.getWeight() ||
                    (msg.priority.getWeight() == highest.priority.getWeight() &&
                            msg.updatedAtSequence > highest.updatedAtSequence)) {
                highest = msg;
                highestId = entry.getKey();
            }
        }

        if (highest != null) {
            long now = System.currentTimeMillis();
            String signature = buildSignature(highestId, highest);
            boolean winnerChanged = !signature.equals(lastSentSignature);
            boolean keepAliveDue = (now - lastSendAtMillis) >= KEEPALIVE_INTERVAL_MILLIS;

            if (!force && !winnerChanged && !keepAliveDue) {
                return;
            }

            try {
                Component outbound = highest.component;
                if (!winnerChanged) {
                    outbound = withResendNonce(outbound, now);
                }

                player.sendActionBar(outbound);
                maybeSendRejoinFallback(player, outbound, now);
                lastWinnerId = highestId == null ? "-" : highestId;
                lastWinnerPlainText = highest.plainText;
                lastSentSignature = signature;
                lastSentWasEmpty = false;
                lastSendAtMillis = now;
            } catch (Throwable throwable) {
                ZonePractice.getInstance().getLogger().warning("ActionBar send failed for " + profile.getUuid() + ": " + throwable.getMessage());
            }
        }
    }

    private String buildSignature(String id, ActionMessage message) {
        return Objects.toString(id, "-") + "|" + message.priority + "|" + message.plainText;
    }

    private Player resolveLivePlayer() {
        return profile.getOnlinePlayer();
    }

    private Component withResendNonce(Component source, long now) {
        // Keep visual content identical while producing a non-identical payload for resend packets.
        String nonce = (now & 1L) == 0L ? "a" : "b";
        return source.append(Component.text("").insertion(nonce));
    }

    private void maybeSendRejoinFallback(Player player, Component outbound, long now) {
        if (now > fallbackUntilMillis) {
            return;
        }

        try {
            // Fallback transport for the first seconds after join where some clients/proxies can drop action-bar packets.
            String legacy = Common.serializeComponentToLegacyString(outbound).replace('&', '\u00A7');
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(legacy));
        } catch (Throwable ignored) {
            // Keep action-bar pipeline resilient: primary send already succeeded.
        }
    }

    private void clearActionBarIfNeeded(Player player) {
        if (lastSentWasEmpty) {
            return;
        }

        player.sendActionBar(Component.empty());
        resetLastSentState();
    }

    private void resetLastSentState() {
        lastSentSignature = "";
        lastSentWasEmpty = true;
        lastWinnerId = "-";
        lastWinnerPlainText = "";
        lastSendAtMillis = 0L;
        fallbackUntilMillis = 0L;
    }


    private Component deserializeOrFallback(String text) {
        String safeText = text == null ? "" : text;
        try {
            return ZonePractice.getMiniMessage().deserialize(safeText);
        } catch (Exception ignored) {
            return Component.text(Objects.toString(text, ""));
        }
    }

    /**
     * Represents a single action bar message.
     */
    private static class ActionMessage {

        private final Component component;
        private final String plainText;
        private final long expiresAtMillis;
        private final ActionBarPriority priority;
        private final long updatedAtSequence;

        public ActionMessage(Component component, String plainText, long expiresAtMillis, ActionBarPriority priority, long updatedAtSequence) {
            this.component = component;
            this.plainText = plainText;
            this.expiresAtMillis = expiresAtMillis;
            this.priority = priority;
            this.updatedAtSequence = updatedAtSequence;
        }

        private boolean isInfinite() {
            return expiresAtMillis == INFINITE_EXPIRY;
        }

        private boolean isExpired(long now) {
            return expiresAtMillis != INFINITE_EXPIRY && now >= expiresAtMillis;
        }
    }
}