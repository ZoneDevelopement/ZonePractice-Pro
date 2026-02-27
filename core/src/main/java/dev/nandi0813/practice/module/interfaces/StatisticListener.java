package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.sidebar.SidebarManager;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class StatisticListener implements Listener {

    @Getter
    protected final ZonePractice practice = ZonePractice.getInstance();
    @Getter
    protected static final Map<Player, Integer> CURRENT_CPS = new ConcurrentHashMap<>();
    @Getter
    protected static final Map<Player, Integer> CPS = new ConcurrentHashMap<>();
    @Getter
    protected static final Map<Player, Integer> CURRENT_COMBO = new ConcurrentHashMap<>();

    @EventHandler ( priority = EventPriority.LOWEST )
    public abstract void onClick(PlayerInteractEvent e);

    protected static @NotNull BukkitRunnable cpsRunnable(final Statistic statistic, Player player) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                // Remove atomically — avoids the TOCTOU race between containsKey and get
                // that can return null and cause an NPE when unboxing to int.
                Integer current = CURRENT_CPS.remove(player);
                if (current != null && current > 2) {
                    statistic.getCps().put(System.currentTimeMillis(), current);
                    CPS.put(player, current);
                }
            }
        };
    }

    @EventHandler ( priority = EventPriority.LOWEST )
    public abstract void onPlayerHit(EntityDamageByEntityEvent e);

    protected static @NotNull BukkitRunnable hitRunnable(final Player attacker, final Statistic attackerStats, final Player defender, final Statistic defenderStats) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (attackerStats != null) {
                    attackerStats.setHit(attackerStats.getHit() + 1);

                    CURRENT_COMBO.putIfAbsent(attacker, 1);
                    CURRENT_COMBO.computeIfPresent(attacker, (key, val) -> val + 1);
                }

                if (defenderStats != null) {
                    defenderStats.setGetHit(defenderStats.getGetHit() + 1);

                    if (CURRENT_COMBO.containsKey(defender) && defenderStats.getLongestCombo() < CURRENT_COMBO.get(defender)) {
                        defenderStats.setLongestCombo(CURRENT_COMBO.get(defender));
                    }
                    CURRENT_COMBO.put(defender, 0);
                }

                // Immediately update scoreboards for real-time hit counter display
                // Schedule on main thread since scoreboard updates must be on main thread
                if (attacker != null && defender != null) {
                    ZonePractice.getInstance().getServer().getScheduler().runTask(
                            ZonePractice.getInstance(),
                            () -> SidebarManager.getInstance().updatePlayersSidebar(attacker, defender)
                    );
                }
            }
        };
    }

    @EventHandler ( priority = EventPriority.LOWEST )
    public abstract void onPotionSplash(PotionSplashEvent e);

}