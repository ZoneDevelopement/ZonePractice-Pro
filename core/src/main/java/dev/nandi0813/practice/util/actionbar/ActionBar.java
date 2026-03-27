package dev.nandi0813.practice.util.actionbar;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionBar {

    @Getter
    private final Profile profile;

    // Stores active messages using an ID (e.g., "golden_head", "queue")
    private final Map<String, ActionMessage> activeMessages = new ConcurrentHashMap<>();
    private ActionBarRunnable actionBarRunnable;

    public ActionBar(final Profile profile) {
        this.profile = profile;
    }

    /**
     * Set or update a message in the action bar.
     * * @param id       Unique identifier for this action (e.g., "golden_head")
     * @param text     The text to display (MiniMessage format)
     * @param duration Duration in seconds (-1 for infinite)
     * @param priority Priority level (higher weight overrides lower weight)
     */
    public void setMessage(String id, String text, int duration, ActionBarPriority priority) {
        Component component = ZonePractice.getMiniMessage().deserialize(text);
        activeMessages.put(id, new ActionMessage(component, duration, priority, System.nanoTime()));

        startRunnableIfNeeded();
        updateDisplay(); // Instantly update the screen
    }

    /**
     * Manually remove a specific action bar message before its duration expires.
     */
    public void removeMessage(String id) {
        activeMessages.remove(id);
        updateDisplay();
    }

    /**
     * Clears all current action bar messages.
     */
    public void clearAll() {
        activeMessages.clear();
        updateDisplay();
    }

    private void startRunnableIfNeeded() {
        if (actionBarRunnable == null || !actionBarRunnable.isRunning()) {
            actionBarRunnable = new ActionBarRunnable(this);
            actionBarRunnable.begin();
        }
    }

    /**
     * Called automatically every second by the ActionBarRunnable.
     */
    protected void tick() {
        Player player = profile.getPlayer().getPlayer();
        if (player == null || !player.isOnline()) {
            if (actionBarRunnable != null) actionBarRunnable.cancel();
            return;
        }

        boolean changed = false;

        // Decrease duration for all active messages
        for (Map.Entry<String, ActionMessage> entry : activeMessages.entrySet()) {
            ActionMessage msg = entry.getValue();
            if (msg.getDuration() > 0) {
                msg.setDuration(msg.getDuration() - 1);

                // Remove if time has expired
                if (msg.getDuration() <= 0) {
                    activeMessages.remove(entry.getKey());
                    changed = true;
                }
            }
        }

        if (changed) {
            updateDisplay();
        } else {
            // Vanilla Minecraft fades action bars after ~3 seconds.
            // We resend the highest priority message every tick to keep it on screen.
            sendHighestPriority();
        }
    }

    private void updateDisplay() {
        if (activeMessages.isEmpty()) {
            clearScreen();
            if (actionBarRunnable != null) {
                actionBarRunnable.cancel();
                actionBarRunnable = null;
            }
        } else {
            sendHighestPriority();
        }
    }

    private void sendHighestPriority() {
        Player player = profile.getPlayer().getPlayer();
        if (player == null || !player.isOnline()) return;

        ActionMessage highest = null;
        for (ActionMessage msg : activeMessages.values()) {
            if (highest == null) {
                highest = msg;
                continue;
            }

            // For same-priority messages, always show the most recently updated one.
            if (msg.getPriority().getWeight() > highest.getPriority().getWeight() ||
                (msg.getPriority().getWeight() == highest.getPriority().getWeight() &&
                 msg.getUpdatedAtNanos() > highest.getUpdatedAtNanos())) {
                highest = msg;
            }
        }

        if (highest != null) {
            player.sendActionBar(highest.getComponent());
        }
    }

    private void clearScreen() {
        Player player = profile.getPlayer().getPlayer();
        if (player != null && player.isOnline()) {
            player.sendActionBar(Component.empty());
        }
    }

    @Getter
    @Setter
    public static class ActionMessage {
        private Component component;
        private int duration;
        private final ActionBarPriority priority;
        private final long updatedAtNanos;

        public ActionMessage(Component component, int duration, ActionBarPriority priority, long updatedAtNanos) {
            this.component = component;
            this.duration = duration;
            this.priority = priority;
            this.updatedAtNanos = updatedAtNanos;
        }
    }
}