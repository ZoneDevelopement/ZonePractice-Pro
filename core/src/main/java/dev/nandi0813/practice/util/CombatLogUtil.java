package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.actionbar.ActionBarPriority;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

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

    private static final boolean ENABLED = ConfigManager.getBoolean(CONFIG_PATH + ".ENABLED");
    private static final int TAG_DURATION = ConfigManager.getInt(CONFIG_PATH + ".TAG-DURATION", 10);
    private static final boolean COUNT_AS_KILL_ON_QUIT = ConfigManager.getBoolean(CONFIG_PATH + ".COUNT-AS-KILL-ON-QUIT");
    private static final boolean ACTION_BAR = ConfigManager.getBoolean(CONFIG_PATH + ".ACTION-BAR");
    private static final String ACTION_BAR_MSG = ConfigManager.getString(CONFIG_PATH + ".ACTION-BAR-MSG");

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
    }

    public boolean isEnabled() {
        return ENABLED;
    }

    public boolean isKillOnQuit() {
        return isEnabled() && COUNT_AS_KILL_ON_QUIT;
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

        long expiry = System.currentTimeMillis() + TAG_DURATION * 1000L;
        combatTags.put(victim.getUniqueId(), expiry);
        if (attacker != null) {
            combatTags.put(attacker.getUniqueId(), expiry);
            lastAttackers.put(victim.getUniqueId(), attacker.getUniqueId());
            lastAttackers.put(attacker.getUniqueId(), victim.getUniqueId());
        }

        if (ACTION_BAR) {
            startActionBarTask(victim);
            if (attacker != null && !attacker.getUniqueId().equals(victim.getUniqueId()))
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
            String text = ACTION_BAR_MSG.replace("%remaining%", String.valueOf(seconds));
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
     * Returns whether {@code player} is currently in combat.
     */
    public boolean isInCombat(Player player) {
        if (!isEnabled())
            return false;

        Long expiry = combatTags.get(player.getUniqueId());
        if (expiry == null)
            return false;

        if (System.currentTimeMillis() > expiry) {
            combatTags.remove(player.getUniqueId());
            lastAttackers.remove(player.getUniqueId());
            return false;
        }
        return true;
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
        Long expiry = combatTags.get(player.getUniqueId());
        if (expiry == null)
            return 0;

        long millis = expiry - System.currentTimeMillis();
        if (millis <= 0) {
            combatTags.remove(player.getUniqueId());
            lastAttackers.remove(player.getUniqueId());
            return 0;
        }
        return (int) Math.ceil(millis / 1000.0);
    }

    public void clear(Player player) {
        combatTags.remove(player.getUniqueId());
        lastAttackers.remove(player.getUniqueId());

        cancelTask(player.getUniqueId());
        removeActionBar(player);
    }

}
