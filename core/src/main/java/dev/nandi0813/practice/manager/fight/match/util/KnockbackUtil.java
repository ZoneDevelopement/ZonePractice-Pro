package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.ladder.enums.KnockbackType;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public enum KnockbackUtil {
    ;

    public static void setPlayerKnockback(Entity player, KnockbackType knockbackType) {
        Vector vel = player.getVelocity();

        if (player.isOnGround()) {
            vel.setX(vel.getX() * knockbackType.getHorizontal());
            vel.setZ(vel.getZ() * knockbackType.getHorizontal());
            vel.setY(vel.getY() * knockbackType.getVertical());
        } else {
            vel.setX(vel.getX() * knockbackType.getAirhorizontal());
            vel.setZ(vel.getZ() * knockbackType.getAirhorizontal());
            vel.setY(vel.getY() * knockbackType.getVertical());
        }
        player.setVelocity(vel);
    }

}
