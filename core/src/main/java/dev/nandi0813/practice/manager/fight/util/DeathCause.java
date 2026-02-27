package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import lombok.Getter;
import org.bukkit.event.entity.EntityDamageEvent;

@Getter
public enum DeathCause {

    VOID(LanguageManager.getString("FIGHT.DEATH-MESSAGES.VOID")),
    VOID_BY_PLAYER(LanguageManager.getString("FIGHT.DEATH-MESSAGES.VOID-BY-PLAYER")),
    WATER(LanguageManager.getString("FIGHT.DEATH-MESSAGES.WATER")),
    LAVA(LanguageManager.getString("FIGHT.DEATH-MESSAGES.LAVA")),
    FIRE(LanguageManager.getString("FIGHT.DEATH-MESSAGES.FIRE")),
    FALL(LanguageManager.getString("FIGHT.DEATH-MESSAGES.FALL")),
    EXPLOSION(LanguageManager.getString("FIGHT.DEATH-MESSAGES.EXPLOSION")),
    PLAYER_ATTACK(LanguageManager.getString("FIGHT.DEATH-MESSAGES.PLAYER")),
    PLAYER_PROJECTILE(LanguageManager.getString("FIGHT.DEATH-MESSAGES.PROJECTILE")),
    SUMO(LanguageManager.getString("FIGHT.DEATH-MESSAGES.SUMO-FALL")),
    SPLEEF(LanguageManager.getString("FIGHT.DEATH-MESSAGES.SPLEEF-FALL")),
    PORTAL_OWN_JUMP(LanguageManager.getString("FIGHT.DEATH-MESSAGES.OWN-PORTAL-JUMP")),
    DEFAULT(LanguageManager.getString("FIGHT.DEATH-MESSAGES.DEFAULT"));

    private final String message;

    DeathCause(final String message) {
        this.message = message;
    }

    public static DeathCause convert(EntityDamageEvent.DamageCause damageCause) {
        return switch (damageCause) {
            case VOID -> VOID;
            case DROWNING -> WATER;
            case LAVA -> LAVA;
            case FIRE -> FIRE;
            case FALL -> FALL;
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> EXPLOSION;
            case ENTITY_ATTACK -> PLAYER_ATTACK;
            case PROJECTILE -> PLAYER_PROJECTILE;
            default -> DEFAULT;
        };
    }

}
