package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Version-specific strategy for handling item cooldowns on Ender Pearls,
 * Golden Apples, and Firework Rockets.
 *
 * <p>Legacy (1.8.8): blocks the action via {@link Cancellable#setCancelled(boolean)}
 * and sends a chat message to the player.
 *
 * <p>Modern (Paper): additionally calls {@code Player#setCooldown(Material, int)} so the item
 * is visually grayed-out in the hotbar; no chat message is sent.
 */
public interface ItemCooldownHandler {

    /**
     * Called when a player attempts to throw an ender pearl in an FFA.
     *
     * @param player   the player
     * @param fightPlayer the FFA fight-player context
     * @param duration cooldown duration in seconds
     * @param expBar   whether to use the EXP bar to display the remaining time
     * @param event    the cancellable event
     * @param langKey  language key for the blocked-message (e.g. {@code "FFA.GAME.COOLDOWN.ENDER-PEARL"})
     */
    void handleEnderPearlFFA(Player player, FightPlayer fightPlayer, int duration, boolean expBar,
                             Cancellable event, String langKey);

    /**
     * Called when a player attempts to throw an ender pearl in a match.
     *
     * @param player   the player
     * @param fightPlayer the match fight-player context
     * @param duration cooldown duration in seconds
     * @param expBar   whether to use the EXP bar to display the remaining time
     * @param event    the cancellable event
     * @param langKey  language key for the blocked-message (e.g. {@code "MATCH.COOLDOWN.ENDER-PEARL"})
     */
    void handleEnderPearlMatch(Player player, FightPlayer fightPlayer, int duration, boolean expBar,
                               Cancellable event, String langKey);

    /**
     * Called when a player attempts to consume a golden apple in an FFA.
     *
     * @param player   the player
     * @param duration cooldown duration in seconds
     * @param event    the cancellable event
     * @param langKey  language key for the blocked-message (e.g. {@code "FFA.GAME.COOLDOWN.GOLDEN-APPLE"})
     */
    void handleGoldenAppleFFA(Player player, int duration, Cancellable event, String langKey);

    /**
     * Called when a player attempts to consume a golden apple in a match.
     *
     * @param player   the player
     * @param duration cooldown duration in seconds
     * @param event    the cancellable event
     * @param langKey  language key for the blocked-message (e.g. {@code "MATCH.COOLDOWN.GOLDEN-APPLE"})
     */
    void handleGoldenAppleMatch(Player player, int duration, Cancellable event, String langKey);

    /**
     * Called when a player attempts to use a firework rocket in an FFA.
     *
     * @param player      the player
     * @param fightPlayer the FFA fight-player context
     * @param duration    cooldown duration in seconds
     * @param event       the cancellable event
     * @param langKey     language key for the blocked-message (e.g. {@code "MATCH.COOLDOWN.FIREWORK-ROCKET-COOLDOWN"})
     */
    void handleFireworkRocketFFA(Player player, FightPlayer fightPlayer, int duration,
                                 Cancellable event, String langKey);

    /**
     * Called when a player attempts to use a firework rocket in a match.
     *
     * @param player      the player
     * @param fightPlayer the match fight-player context
     * @param duration    cooldown duration in seconds
     * @param event       the cancellable event
     * @param langKey     language key for the blocked-message (e.g. {@code "MATCH.COOLDOWN.FIREWORK-ROCKET-COOLDOWN"})
     */
    void handleFireworkRocketMatch(Player player, FightPlayer fightPlayer, int duration,
                                   Cancellable event, String langKey);

    /**
     * Called when a player attempts to launch a fireball in a match (FireballFight ladder).
     *
     * @param player   the player
     * @param duration cooldown duration in seconds (double, as configured per-ladder)
     * @param langKey  language key for the blocked-message (e.g. {@code "MATCH.COOLDOWN.FIREBALL"})
     * @return {@code true} if the fireball should be launched (no cooldown active),
     *         {@code false} if the action was blocked because a cooldown is active
     */
    boolean handleFireballMatch(Player player, double duration, String langKey);
}


