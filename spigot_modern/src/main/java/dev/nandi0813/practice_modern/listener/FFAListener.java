package dev.nandi0813.practice_modern.listener;

import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.fight.util.FightUtil;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class FFAListener extends dev.nandi0813.practice.manager.fight.ffa.FFAListener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        e.setCancelled(true);

        DamageSource damageSource = e.getDamageSource();

        // Void deaths are already handled by onPlayerMove in the core FFAListener.
        // Skip here to avoid sending the death message twice.
        if (damageSource.getDamageType().equals(DamageType.OUT_OF_WORLD)) {
            return;
        }

        Player killer = null;
        if (damageSource.getCausingEntity() instanceof Entity damageEntity) {
            killer = FightUtil.getKiller(damageEntity);
        }

        DeathCause cause = dev.nandi0813.practice_modern.listener.FightUtil.convert(damageSource.getDamageType());
        ffa.killPlayer(player, killer, cause.getMessage().replace("%killer%", killer != null ? killer.getName() : "Unknown"));

        if (killer != null) {
            Statistic statistic = ffa.getStatistics().get(killer);
            statistic.setKills(statistic.getKills() + 1);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player target)) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(target);
        if (profile == null) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(target);
        if (ffa == null) return;

        // Resolve the attacker (direct hit or projectile shooter)
        Player attacker = null;
        if (e.getDamager() instanceof Player damager) {
            attacker = damager;
        } else if (e.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                attacker = shooter;

                if (projectile instanceof Arrow) {
                    arrowDisplayHearth(shooter, target, e.getFinalDamage());
                }
            }
        }

        // Record the attacker for void-kill attribution
        if (attacker != null) {
            ffa.recordAttack(target, attacker);
        }
    }

}
