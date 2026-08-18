package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.actionbar.ActionBarPriority;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global combat-log util (anti-logout / anti-relog).
 *
 * <p>Marks players as "in combat" for a configurable duration so game modes can
 * block actions (e.g. switching kits, leaving) or attribute kills on disconnect.
 * The config section is {@code FFA.COMBAT-LOG}.</p>
 */
public class CombatLogUtil {

    private static final String CONFIG_PATH = "FFA.COMBAT-LOG";

    @Getter
    private boolean enabled;
    private int tagDuration;
    private boolean countAsKillOnQuit;
    private boolean actionBar;
    private String actionBarMsg;

    private static CombatLogUtil instance;

    public static CombatLogUtil getInstance() {
        if (instance == null)
            instance = new CombatLogUtil();
        return instance;
    }

    private final Map<UUID, Long> combatTags = new HashMap<>();
    private final Map<UUID, UUID> lastAttackers = new HashMap<>();
    private final Map<UUID, BukkitTask> actionBarTasks = new HashMap<>();

    private CombatLogUtil() {
        reload();
    }

    /**
     * Re-reads the combat-log config section. Call this after a {@code /zpa reload}
     * so toggles, durations and messages reflect the latest values.
     */
    public void reload() {
        enabled = ConfigManager.getBoolean(CONFIG_PATH + ".ENABLED");
        tagDuration = ConfigManager.getInt(CONFIG_PATH + ".TAG-DURATION", 10);
        countAsKillOnQuit = ConfigManager.getBoolean(CONFIG_PATH + ".COUNT-AS-KILL-ON-QUIT");
        actionBar = ConfigManager.getBoolean(CONFIG_PATH + ".ACTION-BAR");
        actionBarMsg = ConfigManager.getString(CONFIG_PATH + ".ACTION-BAR-MSG");

        // If combat-logging is turned off, drop any leftover combat state so
        // players aren't stuck kit/leave-blocked by stale tags.
        if (!enabled) {
            combatTags.clear();
            lastAttackers.clear();
            actionBarTasks.values().forEach(BukkitTask::cancel);
            actionBarTasks.clear();
        }
    }

    public boolean isKillOnQuit() {
        return isEnabled() && countAsKillOnQuit;
    }

    /**
     * Marks both {@code victim} and {@code attacker} as in combat for the
     * configured tag duration, and records the attacker so a later disconnect
     * can attribute a kill. Expiry matches {@code TAG_DURATION} so the attacker
     * is always available while the victim is still in combat.
     */
    public void tag(Player victim, Player attacker) {
        if (!isEnabled())
            return;

        // Anti-relog only applies to real opponent fights. Self-inflicted or
        // unresolved damage (your own crystal/anchor/TNT, fall, etc.) must never
        // tag the victim.
        if (attacker == null || attacker.equals(victim))
            return;

        long expiry = System.currentTimeMillis() + tagDuration * 1000L;
        combatTags.put(victim.getUniqueId(), expiry);
        combatTags.put(attacker.getUniqueId(), expiry);
        lastAttackers.put(victim.getUniqueId(), attacker.getUniqueId());
        lastAttackers.put(attacker.getUniqueId(), victim.getUniqueId());

        if (actionBar) {
            startActionBarTask(victim);
            if (!attacker.getUniqueId().equals(victim.getUniqueId()))
                startActionBarTask(attacker);
        }
    }

    private void startActionBarTask(Player player) {
        UUID playerId = player.getUniqueId();

        if (actionBarTasks.containsKey(playerId))
            return;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(ZonePractice.getInstance(), () -> {
            Player online = Bukkit.getPlayer(playerId);
            if (online == null || !online.isOnline() || !isInCombat(online)) {
                if (online != null)
                    removeActionBar(online);
                cancelTask(playerId);
                return;
            }

            int seconds = getRemainingSeconds(online);
            String text = actionBarMsg.replace("%remaining%", String.valueOf(seconds));
            ProfileManager.getInstance().getProfile(online).getActionBar().setMessage(
                    "combat_log",
                    text,
                    -1,
                    ActionBarPriority.HIGHEST
            );
        }, 0L, 20L);

        actionBarTasks.put(playerId, task);
    }

    private void cancelTask(UUID playerId) {
        BukkitTask removed = actionBarTasks.remove(playerId);
        if (removed != null)
            removed.cancel();
    }

    private void removeActionBar(Player player) {
        if (player == null)
            return;

        ProfileManager.getInstance().getProfile(player).getActionBar().removeMessage("combat_log");
    }

    /**
     * Returns the unexpired combat-tag expiry for {@code player}, cleaning up
     * the maps if it has already lapsed, or {@code null} if not tagged.
     */
    private Long getUnexpiredExpiry(Player player) {
        UUID playerId = player.getUniqueId();
        Long expiry = combatTags.get(playerId);
        if (expiry == null)
            return null;

        if (System.currentTimeMillis() > expiry) {
            combatTags.remove(playerId);
            lastAttackers.remove(playerId);
            return null;
        }
        return expiry;
    }

    /**
     * Returns whether {@code player} is currently in combat.
     */
    public boolean isInCombat(Player player) {
        if (!isEnabled())
            return false;

        return getUnexpiredExpiry(player) != null;
    }

    /**
     * Returns the last recorded attacker of {@code victim} while still in
     * combat, or {@code null} if there is none / not in combat.
     */
    public Player getLastAttacker(Player victim) {
        if (!isInCombat(victim))
            return null;

        UUID attackerId = lastAttackers.get(victim.getUniqueId());
        if (attackerId == null)
            return null;

        return Bukkit.getPlayer(attackerId);
    }

    /**
     * Returns the remaining combat time in seconds for {@code player},
     * rounded up, or {@code 0} if not in combat.
     */
    public int getRemainingSeconds(Player player) {
        Long expiry = getUnexpiredExpiry(player);
        if (expiry == null)
            return 0;

        long millis = expiry - System.currentTimeMillis();
        return (int) Math.ceil(millis / 1000.0);
    }

    public void clear(Player player) {
        combatTags.remove(player.getUniqueId());
        lastAttackers.remove(player.getUniqueId());

        cancelTask(player.getUniqueId());
        removeActionBar(player);
    }

}
