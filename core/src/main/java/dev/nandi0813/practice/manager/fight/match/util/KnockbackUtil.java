package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.ladder.enums.KnockbackType;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public enum KnockbackUtil {
    ;

    public static void setPlayerKnockback(Entity target, Entity attacker, KnockbackType knockbackType) {
        Vector currentVelocity = target.getVelocity().clone();

        double horizontalScale = target.isOnGround()
                ? knockbackType.getHorizontal()
                : knockbackType.getAirhorizontal();
        double verticalScale = target.isOnGround()
                ? knockbackType.getVertical()
                : knockbackType.getAirvertical();

        Vector awayFromAttacker = target.getLocation().toVector().subtract(attacker.getLocation().toVector());
        awayFromAttacker.setY(0);

        // Fallback for overlapping hitboxes where distance can be ~0.
        if (awayFromAttacker.lengthSquared() < 1.0E-6D) {
            awayFromAttacker = attacker.getLocation().getDirection().multiply(-1);
            awayFromAttacker.setY(0);
        }

        if (awayFromAttacker.lengthSquared() < 1.0E-6D) {
            return;
        }

        awayFromAttacker.normalize();

        double horizontalMagnitude = Math.hypot(currentVelocity.getX(), currentVelocity.getZ());
        if (horizontalMagnitude < 0.08D) {
            horizontalMagnitude = 0.35D;
        }

        double appliedHorizontal = horizontalMagnitude * horizontalScale;
        double appliedVertical = Math.max(0.0D, currentVelocity.getY()) * verticalScale;
        if (appliedVertical < 0.08D) {
            appliedVertical = 0.08D * verticalScale;
        }

        Vector adjustedVelocity = new Vector(
                awayFromAttacker.getX() * appliedHorizontal,
                appliedVertical,
                awayFromAttacker.getZ() * appliedHorizontal
        );

        target.setVelocity(adjustedVelocity);
    }

}
