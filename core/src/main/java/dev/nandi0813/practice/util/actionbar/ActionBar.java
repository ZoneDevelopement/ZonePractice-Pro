package dev.nandi0813.practice.util.actionbar;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import lombok.Getter;
import net.kyori.adventure.text.Component;
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

    /**
     * Stores active messages using an ID (e.g., "golden_head", "queue").
     */
    private final Map<String, ActionMessage> activeMessages = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

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
        if (id == null || id.isEmpty() || priority == null) {
            return;
        }

        Component component = deserializeOrFallback(text);
        long expiresAtMillis = duration < 0
                ? INFINITE_EXPIRY
                : (System.currentTimeMillis() + (Math.max(1, duration) * 1000L));

        activeMessages.put(id, new ActionMessage(
                component,
                expiresAtMillis,
                priority,
                sequence.incrementAndGet()
        ));

        startRunnable();
        sendHighestPriority(profile.getPlayer().getPlayer()); // immediate update
    }

    /**
     * Manually remove a specific action bar message before its duration expires.
     *
     * @param id Unique identifier of the message
     */
    public void removeMessage(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }

        activeMessages.remove(id);
        Player player = profile.getPlayer().getPlayer();
        if (player != null && player.isOnline()) {
            if (activeMessages.isEmpty()) {
                player.sendActionBar(Component.empty());
            } else {
                sendHighestPriority(player);
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

        actionBarRunnable.runTaskTimer(ZonePractice.getInstance(), 0L, TICK_PERIOD); // every 2 ticks (~0.1 sec)
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
        Player player = profile.getPlayer().getPlayer();
        if (player == null || !player.isOnline()) {
            // Do not keep stale state/runnables around for offline players.
            activeMessages.clear();
            stopRunnable();
            return;
        }

        pruneExpiredMessages();

        if (!activeMessages.isEmpty()) {
            sendHighestPriority(player);
        } else {
            player.sendActionBar(Component.empty());
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
    private void sendHighestPriority(Player player) {
        if (player == null || !player.isOnline()) return;

        ActionMessage highest = null;
        for (ActionMessage msg : activeMessages.values()) {
            if (highest == null) {
                highest = msg;
                continue;
            }

            if (msg.priority.getWeight() > highest.priority.getWeight() ||
                    (msg.priority.getWeight() == highest.priority.getWeight() &&
                            msg.updatedAtSequence > highest.updatedAtSequence)) {
                highest = msg;
            }
        }

        if (highest != null) {
            player.sendActionBar(highest.component);
        }
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
        private final long expiresAtMillis;
        private final ActionBarPriority priority;
        private final long updatedAtSequence;

        public ActionMessage(Component component, long expiresAtMillis, ActionBarPriority priority, long updatedAtSequence) {
            this.component = component;
            this.expiresAtMillis = expiresAtMillis;
            this.priority = priority;
            this.updatedAtSequence = updatedAtSequence;
        }

        private boolean isExpired(long now) {
            return expiresAtMillis != INFINITE_EXPIRY && now >= expiresAtMillis;
        }
    }
}