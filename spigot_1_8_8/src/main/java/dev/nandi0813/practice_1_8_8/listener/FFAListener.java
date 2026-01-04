package dev.nandi0813.practice_1_8_8.listener;

import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.fight.util.FightUtil;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class FFAListener extends dev.nandi0813.practice.manager.fight.ffa.FFAListener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        if (e instanceof EntityDamageByEntityEvent) {
            onEntityDamageByEntity((EntityDamageByEntityEvent) e);
        }

        if (player.getHealth() - e.getFinalDamage() <= 0) {
            e.setDamage(0);

            if (e instanceof EntityDamageByEntityEvent) {
                Player killer = FightUtil.getKiller(((EntityDamageByEntityEvent) e).getDamager());

                ffa.killPlayer(player, killer, DeathCause.convert(e.getCause()).getMessage().replace("%killer%", killer != null ? killer.getName() : "Unknown"));

                if (killer != null) {
                    Statistic statistic = ffa.getStatistics().get(killer);
                    statistic.setKills(statistic.getKills() + 1);
                }
            } else
                ffa.killPlayer(player, null, DeathCause.convert(e.getCause()).getMessage());
        }
    }

    private static void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player target = (Player) e.getEntity();

        if (e.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) e.getDamager();
            if (projectile.getShooter() instanceof Player) {
                Player attacker = (Player) projectile.getShooter();

                if (projectile instanceof Arrow) {
                    arrowDisplayHearth(attacker, target, e.getFinalDamage());
                }
            }
        }
    }

}
