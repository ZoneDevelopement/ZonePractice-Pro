package dev.nandi0813.practice.util.actionbar;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.profile.Profile;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class ActionBar {

    private final Profile profile;
    private static final int TICK_PERIOD = 2;
    private static final long INFINITE_EXPIRY = Long.MAX_VALUE;
    private static final int DEBUG_HISTORY_LIMIT = 40;

    /**
     * Stores active messages using an ID (e.g., "golden_head", "queue").
     */
    private final Map<String, ActionMessage> activeMessages = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentLinkedDeque<String> debugHistory = new ConcurrentLinkedDeque<>();

    private volatile String lastWinnerId = "-";
    private volatile long lastSendAtMillis = 0L;

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
        debug("SET", "id=" + id + ", duration=" + duration + ", priority=" + priority + ", active=" + activeMessages.size());

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
        debug("REMOVE", "id=" + id + ", active=" + activeMessages.size());
        Player player = profile.getPlayer().getPlayer();
        if (player != null && player.isOnline()) {
            if (activeMessages.isEmpty()) {
                player.sendActionBar(Component.empty());
                debug("CLEAR", "removeMessage emptied actionbar");
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

        debug("RUNNABLE_START", "period=" + TICK_PERIOD + "t");

        actionBarRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    tick();
                } catch (Throwable throwable) {
                    ZonePractice.getInstance().getLogger().warning("ActionBar tick failed for "
                            + profile.getUuid() + ": " + throwable.getMessage());
                    debug("ERROR", "tick exception=" + throwable.getClass().getSimpleName() + ":" + throwable.getMessage());
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
            debug("RUNNABLE_STOP", "active=" + activeMessages.size());
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
            debug("OFFLINE_CLEAR", "player offline while actionbar active");
            stopRunnable();
            return;
        }

        pruneExpiredMessages();

        if (!activeMessages.isEmpty()) {
            sendHighestPriority(player);
        } else {
            player.sendActionBar(Component.empty());
            debug("CLEAR", "tick emptied actionbar");
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
            try {
                player.sendActionBar(highest.component);
                lastWinnerId = highestId == null ? "-" : highestId;
                lastSendAtMillis = System.currentTimeMillis();
                debug("SEND", "winner=" + lastWinnerId + ", priority=" + highest.priority + ", active=" + activeMessages.size());
            } catch (Throwable throwable) {
                debug("ERROR", "send exception=" + throwable.getClass().getSimpleName() + ":" + throwable.getMessage());
                ZonePractice.getInstance().getLogger().warning("ActionBar send failed for " + profile.getUuid() + ": " + throwable.getMessage());
            }
        } else if (!activeMessages.isEmpty()) {
            debug("ANOMALY", "active messages present but no winner selected");
        }
    }

    public String getDebugStateSnapshot() {
        return "active=" + activeMessages.size()
                + ", runnable=" + (actionBarRunnable != null)
                + ", lastWinner=" + lastWinnerId
                + ", lastSendAt=" + lastSendAtMillis;
    }

    public List<String> getDebugHistorySnapshot() {
        return new ArrayList<>(debugHistory);
    }

    public void debugDumpToConsole(String reason) {
        if (!isDebugEnabledForCurrentPlayer()) {
            return;
        }

        ZonePractice.getInstance().getLogger().warning("[ActionBarDebug] dump reason=" + reason + " uuid=" + profile.getUuid() + " " + getDebugStateSnapshot());
        for (String line : getDebugHistorySnapshot()) {
            ZonePractice.getInstance().getLogger().warning("[ActionBarDebug] " + line);
        }
    }

    private void debug(String type, String details) {
        if (!isDebugEnabledForCurrentPlayer()) {
            return;
        }

        String line = System.currentTimeMillis() + " | " + type + " | " + details;
        debugHistory.addLast(line);
        while (debugHistory.size() > DEBUG_HISTORY_LIMIT) {
            debugHistory.pollFirst();
        }

        ZonePractice.getInstance().getLogger().warning("[ActionBarDebug] uuid=" + profile.getUuid() + " " + line);
    }

    private boolean isDebugEnabledForCurrentPlayer() {
        if (ConfigManager.getConfig() == null) {
            return false;
        }

        if (!ConfigManager.getConfig().getBoolean("DEBUG.ACTIONBAR.ENABLED", false)) {
            return false;
        }

        List<String> targets = ConfigManager.getConfig().getStringList("DEBUG.ACTIONBAR.TARGETS");
        if (targets == null || targets.isEmpty()) {
            return true;
        }

        Player player = profile.getPlayer().getPlayer();
        if (player == null) {
            return false;
        }

        String playerName = player.getName();
        for (String target : targets) {
            if (target != null && target.equalsIgnoreCase(playerName)) {
                return true;
            }
        }

        return false;
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