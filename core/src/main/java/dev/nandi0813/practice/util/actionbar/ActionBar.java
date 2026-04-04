package dev.nandi0813.practice.util.actionbar;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ActionBar {

    private final Profile profile;

    /**
     * Stores active messages using an ID (e.g., "golden_head", "queue").
     */
    private final Map<String, ActionMessage> activeMessages = new ConcurrentHashMap<>();

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
        Component component = ZonePractice.getMiniMessage().deserialize(text);

        activeMessages.put(id, new ActionMessage(
                component,
                duration * 20, // convert seconds → ticks
                priority,
                System.nanoTime()
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
        activeMessages.remove(id);
        Player player = profile.getPlayer().getPlayer();
        if (player != null && player.isOnline()) {
            sendHighestPriority(player);
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
                tick();
                if (activeMessages.isEmpty()) {
                    cancel();
                    actionBarRunnable = null;
                }
            }
        };

        actionBarRunnable.runTaskTimer(ZonePractice.getInstance(), 0L, 2L); // every 2 ticks (~0.1 sec)
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
        if (player == null || !player.isOnline()) return;

        // Update durations and remove expired messages
        Iterator<Map.Entry<String, ActionMessage>> it = activeMessages.entrySet().iterator();
        while (it.hasNext()) {
            ActionMessage msg = it.next().getValue();
            if (msg.duration > 0) {
                msg.duration -= 2; // because tick runs every 2 ticks
                if (msg.duration <= 0) {
                    it.remove();
                }
            }
        }

        if (!activeMessages.isEmpty()) {
            sendHighestPriority(player);
        } else {
            player.sendActionBar(Component.empty());
        }
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
                            msg.updatedAt > highest.updatedAt)) {
                highest = msg;
            }
        }

        if (highest != null) {
            player.sendActionBar(highest.component);
        }
    }

    /**
     * Represents a single action bar message.
     */
    private static class ActionMessage {

        private final Component component;
        private int duration; // in ticks
        private final ActionBarPriority priority;
        private final long updatedAt;

        public ActionMessage(Component component, int duration, ActionBarPriority priority, long updatedAt) {
            this.component = component;
            this.duration = duration;
            this.priority = priority;
            this.updatedAt = updatedAt;
        }
    }
}