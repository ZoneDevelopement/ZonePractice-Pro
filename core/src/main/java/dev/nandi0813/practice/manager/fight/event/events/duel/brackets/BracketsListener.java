package dev.nandi0813.practice.manager.fight.event.events.duel.brackets;

import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelFight;
import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelListener;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class BracketsListener extends DuelListener {

    @Override
    public void onEntityDamage(Event event, EntityDamageEvent e) {
        if (event instanceof Brackets brackets) {
            Player player = (Player) e.getEntity();

            if (!brackets.getStatus().equals(EventStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            if (!brackets.isInFight(player)) {
                e.setCancelled(true);
                return;
            }

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                brackets.killPlayer(player, true);
                return;
            }

            if (player.getHealth() - e.getFinalDamage() <= 0) {
                e.setDamage(0);
                player.setHealth(player.getMaxHealth());

                brackets.killPlayer(player, true);
            }
        }
    }

    @Override
    public void onProjectileLaunch(Event event, ProjectileLaunchEvent e) {
        if (event instanceof Brackets brackets) {
            Player player = (Player) e.getEntity().getShooter();

            DuelFight duelFight = brackets.getFight(player);
            if (duelFight == null) {
                e.setCancelled(true);
                return;
            }

            for (Player eventPlayer : brackets.getPlayers()) {
                if (duelFight.getPlayers().contains(eventPlayer)) return;

                ClassImport.getClasses().getEntityHider().hideEntity(eventPlayer, e.getEntity());
            }

            for (Player eventSpectator : brackets.getSpectators()) {
                if (duelFight.getSpectators().contains(eventSpectator)) return;

                ClassImport.getClasses().getEntityHider().hideEntity(eventSpectator, e.getEntity());
            }
        }
    }

    @Override
    public void onPlayerEggThrow(Event event, PlayerEggThrowEvent e) {
    }

}
