package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum FightUtil {
    ;

    public static @Nullable Player getKiller(Entity entity) {
        Player killer = null;
        if (entity instanceof Player) {
            killer = (Player) entity;
        } else if (entity instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player) {
                killer = (Player) arrow.getShooter();
            }
        } else if (entity instanceof Fireball fireball) {
            if (fireball.getShooter() instanceof Player) {
                killer = (Player) fireball.getShooter();
            }
        }
        return killer;
    }

    public static List<Spectatable> getSpectatables() {
        List<Spectatable> spectatables = new ArrayList<>();
        spectatables.addAll(MatchManager.getInstance().getLiveMatches());
        spectatables.addAll(FFAManager.getInstance().getOpenFFAs());
        spectatables.addAll(EventManager.getInstance().getEvents());
        return spectatables;
    }

    /**
     * Returns only the active Spectatables that have build mechanics enabled.
     * Used by {@link dev.nandi0813.practice.manager.fight.listener.BuildListener}
     * to quickly find the owner of a block change without iterating all contexts.
     */
    public static List<Spectatable> getActiveBuildSpectatables() {
        List<Spectatable> result = new ArrayList<>();
        for (Spectatable s : getSpectatables()) {
            if (s.isBuild() && s.getFightChange() != null) {
                result.add(s);
            }
        }
        return result;
    }

    public static DeathCause convert(DamageType damageType) {
        if (damageType == null) {
            return DeathCause.DEFAULT;
        }

        if (damageType.equals(DamageType.IN_FIRE) || damageType.equals(DamageType.ON_FIRE) ||
                damageType.equals(DamageType.CAMPFIRE) || damageType.equals(DamageType.HOT_FLOOR)) {
            return DeathCause.FIRE;
        } else if (damageType.equals(DamageType.LAVA)) {
            return DeathCause.LAVA;
        } else if (damageType.equals(DamageType.DROWN)) {
            return DeathCause.WATER;
        } else if (damageType.equals(DamageType.FALL) || damageType.equals(DamageType.STALAGMITE)) {
            return DeathCause.FALL;
        } else if (damageType.equals(DamageType.EXPLOSION) || damageType.equals(DamageType.PLAYER_EXPLOSION)) {
            return DeathCause.EXPLOSION;
        } else if (damageType.equals(DamageType.MOB_ATTACK) || damageType.equals(DamageType.PLAYER_ATTACK)
                || damageType.equals(DamageType.MACE_SMASH)) {
            return DeathCause.PLAYER_ATTACK;
        } else if (damageType.equals(DamageType.ARROW) || damageType.equals(DamageType.TRIDENT) ||
                damageType.equals(DamageType.MOB_PROJECTILE)) {
            return DeathCause.PLAYER_PROJECTILE;
        } else if (damageType.equals(DamageType.OUT_OF_WORLD)) {
            return DeathCause.VOID;
        } else {
            return DeathCause.DEFAULT;
        }
    }

}
