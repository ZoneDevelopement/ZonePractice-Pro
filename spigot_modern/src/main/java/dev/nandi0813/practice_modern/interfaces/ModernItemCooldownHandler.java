package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice.module.interfaces.ItemCooldownHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Modern (Paper) implementation of {@link ItemCooldownHandler}.
 *
 * <p>Uses Paper's native {@link Player#setCooldown(Material, int)} / {@link Player#hasCooldown(Material)}
 * exclusively — no runnables, no internal cooldown maps, no chat messages.
 *
 * <p><b>Ender Pearl:</b> duration is NOT set here. Paper fires {@code PlayerItemCooldownEvent}
 * automatically after every pearl throw, and {@code EPCountdownListener.onEnderPearlCooldownSet}
 * overrides the tick count to {@code duration * 20} in that event. This handler only
 * blocks re-throws while {@code hasCooldown} is true.
 *
 * <p><b>Golden Apple / Firework Rocket / Fireball:</b> no vanilla cooldown is applied by the
 * server, so we call {@code setCooldown} here directly when the action is first performed.
 */
public class ModernItemCooldownHandler implements ItemCooldownHandler {

    // -------------------------------------------------------------------------
    // Ender Pearl — duration controlled by PlayerItemCooldownEvent in EPCountdownListener
    // -------------------------------------------------------------------------

    @Override
    public void handleEnderPearlFFA(Player player, FightPlayer fightPlayer, int duration, boolean expBar,
                                    Cancellable event, String langKey) {
        if (player.hasCooldown(Material.ENDER_PEARL)) {
            if (event != null) {
                event.setCancelled(true);
            }
        }
        // If no cooldown: let the throw proceed; PlayerItemCooldownEvent will set the correct duration.
    }

    @Override
    public void handleEnderPearlMatch(Player player, FightPlayer fightPlayer, int duration, boolean expBar,
                                      Cancellable event, String langKey) {
        if (player.hasCooldown(Material.ENDER_PEARL)) {
            if (event != null) {
                event.setCancelled(true);
            }
        }
        // If no cooldown: let the throw proceed; PlayerItemCooldownEvent will set the correct duration.
    }

    // -------------------------------------------------------------------------
    // Golden Apple
    // -------------------------------------------------------------------------

    @Override
    public void handleGoldenAppleFFA(Player player, int duration, Cancellable event, String langKey) {
        if (player.hasCooldown(Material.GOLDEN_APPLE)) {
            if (event != null) {
                event.setCancelled(true);
            }
        } else {
            player.setCooldown(Material.GOLDEN_APPLE, duration * 20);
        }
    }

    @Override
    public void handleGoldenAppleMatch(Player player, int duration, Cancellable event, String langKey) {
        if (player.hasCooldown(Material.GOLDEN_APPLE)) {
            if (event != null) {
                event.setCancelled(true);
            }
        } else {
            player.setCooldown(Material.GOLDEN_APPLE, duration * 20);
        }
    }

    // -------------------------------------------------------------------------
    // Firework Rocket
    // -------------------------------------------------------------------------

    @Override
    public void handleFireworkRocketFFA(Player player, FightPlayer fightPlayer, int duration,
                                        Cancellable event, String langKey) {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (player.hasCooldown(Material.FIREWORK_ROCKET)) {
                if (event != null) {
                    event.setCancelled(true);
                }
            } else {
                player.setCooldown(Material.FIREWORK_ROCKET, duration * 20);
            }
        }, 2L);
    }

    @Override
    public void handleFireworkRocketMatch(Player player, FightPlayer fightPlayer, int duration,
                                          Cancellable event, String langKey) {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (player.hasCooldown(Material.FIREWORK_ROCKET)) {
                if (event != null) {
                    event.setCancelled(true);
                }
            } else {
                player.setCooldown(Material.FIREWORK_ROCKET, duration * 20);
            }
        }, 2L);
    }

    // -------------------------------------------------------------------------
    // Fireball
    // -------------------------------------------------------------------------

    @Override
    public boolean handleFireballMatch(Player player, double duration, String langKey) {
        if (player.hasCooldown(Material.FIRE_CHARGE)) {
            return false;
        } else {
            player.setCooldown(Material.FIRE_CHARGE, (int) (duration * 20));
            return true;
        }
    }
}
