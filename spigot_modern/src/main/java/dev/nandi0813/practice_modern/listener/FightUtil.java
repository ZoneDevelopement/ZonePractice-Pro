package dev.nandi0813.practice_modern.listener;

import dev.nandi0813.practice.manager.fight.util.DeathCause;
import org.bukkit.damage.DamageType;

public enum FightUtil {
    ;

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
        } else if (damageType.equals(DamageType.MOB_ATTACK) || damageType.equals(DamageType.PLAYER_ATTACK)) {
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
