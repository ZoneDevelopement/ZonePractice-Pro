package dev.nandi0813.practice.manager.fight.event;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.util.Common;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Reads AUTO-EVENTS time lists from config.yml (per event type) and fires
 * EventManager.startEvent(null, type) at the configured server-local times.
 *
 * Config layout (under EVENT.<TYPE>):
 *   AUTO-EVENTS:
 *     - "18:30"
 *     - "21:00"
 *
 * Global toggle: EVENT.AUTO-EVENTS: true/false
 */
public class AutoEventScheduler {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private static final Map<EventType, String> CONFIG_KEYS = new EnumMap<>(EventType.class);

    static {
        CONFIG_KEYS.put(EventType.LMS,        "EVENT.LMS.AUTO-EVENTS");
        CONFIG_KEYS.put(EventType.OITC,       "EVENT.OITC.AUTO-EVENTS");
        CONFIG_KEYS.put(EventType.TNTTAG,     "EVENT.TNTTAG.AUTO-EVENTS");
        CONFIG_KEYS.put(EventType.BRACKETS,   "EVENT.BRACKETS.AUTO-EVENTS");
        CONFIG_KEYS.put(EventType.SUMO,       "EVENT.SUMO.AUTO-EVENTS");
        CONFIG_KEYS.put(EventType.SPLEGG,     "EVENT.SPLEGG.AUTO-EVENTS");
        CONFIG_KEYS.put(EventType.JUGGERNAUT, "EVENT.JUGGERNAUT.AUTO-EVENTS");
    }

    // Parsed times per event type
    private final Map<EventType, List<LocalTime>> scheduledTimes = new EnumMap<>(EventType.class);

    // Tracks which (type -> HH:mm) already fired this minute to avoid double-firing
    private final Map<EventType, String> lastFired = new EnumMap<>(EventType.class);

    private BukkitRunnable task;
    private boolean taskRunning = false;

    public AutoEventScheduler() {
        reload();
    }

    /** Re-reads the config. Call this if the config is reloaded at runtime. */
    public void reload() {
        scheduledTimes.clear();

        if (!ConfigManager.getBoolean("EVENT.AUTO-EVENTS")) {
            return;
        }

        for (Map.Entry<EventType, String> entry : CONFIG_KEYS.entrySet()) {
            List<String> raw;
            try {
                raw = ConfigManager.getList(entry.getValue());
            } catch (Exception e) {
                continue; // key not present for this event type
            }

            if (raw.isEmpty()) continue;

            List<LocalTime> times = new ArrayList<>();
            for (String s : raw) {
                // Strip legacy colour codes that ConfigManager.getList may inject,
                // then trim whitespace.
                String clean = s.replaceAll("(?i)§[0-9a-fk-or]", "").trim();
                if (clean.isEmpty()) continue;

                // SnakeYAML parses bare HH:MM values (e.g. 18:38) as sexagesimal
                // integers (18*60+38 = 1118) when they are not quoted in the yml.
                // Detect a plain integer and convert it back to HH:mm.
                if (clean.matches("\\d+")) {
                    int total = Integer.parseInt(clean);
                    clean = String.format("%d:%02d", total / 60, total % 60);
                }

                try {
                    times.add(LocalTime.parse(clean, TIME_FORMAT));
                } catch (DateTimeParseException ex) {
                    Common.sendConsoleMMMessage(
                            "<yellow>[AutoEvent] Invalid time \"" + clean
                            + "\" for " + entry.getKey().name() + " — expected HH:mm");
                }
            }

            if (!times.isEmpty()) {
                scheduledTimes.put(entry.getKey(), times);
            }
        }
    }

    /** Starts the repeating check task. Called once after event data is loaded. */
    public void start() {
        if (scheduledTimes.isEmpty() || taskRunning) return;

        taskRunning = true;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!ConfigManager.getBoolean("EVENT.AUTO-EVENTS")) return;

                // Truncate to HH:mm — same resolution as the config values
                LocalTime now = LocalTime.now().withSecond(0).withNano(0);
                String nowKey = now.format(TIME_FORMAT);

                for (Map.Entry<EventType, List<LocalTime>> entry : scheduledTimes.entrySet()) {
                    EventType type = entry.getKey();

                    // Already fired for this minute-window — skip
                    if (nowKey.equals(lastFired.get(type))) continue;

                    for (LocalTime scheduled : entry.getValue()) {
                        if (scheduled.equals(now)) {
                            lastFired.put(type, nowKey);
                            Common.sendConsoleMMMessage(
                                    "<green>[AutoEvent] Auto-starting event: <yellow>" + type.getName());
                            EventManager.getInstance().startEvent(null, type);
                            break;
                        }
                    }
                }
            }
        };

        // Poll every 20 seconds — fine-grained enough to never miss a minute
        task.runTaskTimer(ZonePractice.getInstance(), 20L, 20L * 20L);
    }

    /** Cancels the scheduler (called on plugin disable). */
    public void cancel() {
        if (task != null && taskRunning) {
            task.cancel();
            taskRunning = false;
        }
    }
}
