package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice_1_8_8.cooldown.EnderPearlRunnable;
import dev.nandi0813.practice_1_8_8.cooldown.FireworkRocketRunnable;
import dev.nandi0813.practice.module.interfaces.ItemCooldownHandler;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice_1_8_8.cooldown.GoldenAppleRunnable;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import dev.nandi0813.practice_1_8_8.cooldown.FireballRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Legacy (1.8.8) implementation of {@link ItemCooldownHandler}.
 *
 * <p>When a cooldown is active the action is cancelled, the player's inventory
 * is refreshed, and a chat message is sent with the remaining time.
 * No native hotbar cooldown visualisation is available on 1.8.8.
 */
public class LegacyItemCooldownHandler implements ItemCooldownHandler {

    // -------------------------------------------------------------------------
    // Ender Pearl
    // -------------------------------------------------------------------------

    @Override
    public void handleEnderPearlFFA(Player player, FightPlayer fightPlayer, int duration, boolean expBar,
                                    Cancellable event, String langKey) {
        if (PlayerCooldown.isActive(player, CooldownObject.ENDER_PEARL)) {
            Common.sendMMMessage(player, StringUtil.replaceSecondString(
                    LanguageManager.getString(langKey),
                    PlayerCooldown.getLeftInDouble(player, CooldownObject.ENDER_PEARL)));
            event.setCancelled(true);
            refreshInventory(event, player);
        } else {
            EnderPearlRunnable runnable = new EnderPearlRunnable(player, fightPlayer, duration, expBar);
            runnable.begin();
        }
    }

    @Override
    public void handleEnderPearlMatch(Player player, FightPlayer fightPlayer, int duration, boolean expBar,
                                      Cancellable event, String langKey) {
        if (PlayerCooldown.isActive(player, CooldownObject.ENDER_PEARL)) {
            Common.sendMMMessage(player, StringUtil.replaceSecondString(
                    LanguageManager.getString(langKey),
                    PlayerCooldown.getLeftInDouble(player, CooldownObject.ENDER_PEARL)));
            event.setCancelled(true);
            refreshInventory(event, player);
        } else {
            EnderPearlRunnable runnable = new EnderPearlRunnable(player, fightPlayer, duration, expBar);
            runnable.begin();
        }
    }

    // -------------------------------------------------------------------------
    // Golden Apple
    // -------------------------------------------------------------------------

    @Override
    public void handleGoldenAppleFFA(Player player, int duration, Cancellable event, String langKey) {
        if (PlayerCooldown.isActive(player, CooldownObject.GOLDEN_APPLE)) {
            event.setCancelled(true);
            Common.sendMMMessage(player, StringUtil.replaceSecondString(
                    LanguageManager.getString(langKey),
                    PlayerCooldown.getLeftInDouble(player, CooldownObject.GOLDEN_APPLE)));
            player.updateInventory();
        } else {
            GoldenAppleRunnable runnable = new GoldenAppleRunnable(player, duration);
            runnable.begin();
        }
    }

    @Override
    public void handleGoldenAppleMatch(Player player, int duration, Cancellable event, String langKey) {
        if (PlayerCooldown.isActive(player, CooldownObject.GOLDEN_APPLE)) {
            event.setCancelled(true);
            Common.sendMMMessage(player, StringUtil.replaceSecondString(
                    LanguageManager.getString(langKey),
                    PlayerCooldown.getLeftInDouble(player, CooldownObject.GOLDEN_APPLE)));
            player.updateInventory();
        } else {
            GoldenAppleRunnable runnable = new GoldenAppleRunnable(player, duration);
            runnable.begin();
        }
    }

    // -------------------------------------------------------------------------
    // Firework Rocket
    // -------------------------------------------------------------------------

    @Override
    public void handleFireworkRocketFFA(Player player, FightPlayer fightPlayer, int duration,
                                        Cancellable event, String langKey) {
        if (PlayerCooldown.isActive(player, CooldownObject.FIREWORK_ROCKET)) {
            Common.sendMMMessage(player, StringUtil.replaceSecondString(
                    LanguageManager.getString(langKey),
                    PlayerCooldown.getLeftInDouble(player, CooldownObject.FIREWORK_ROCKET)));
            event.setCancelled(true);
            refreshInventory(event, player);
        } else {
            FireworkRocketRunnable runnable = new FireworkRocketRunnable(player, fightPlayer, duration, false);
            runnable.begin();
        }
    }

    @Override
    public void handleFireworkRocketMatch(Player player, FightPlayer fightPlayer, int duration,
                                          Cancellable event, String langKey) {
        if (PlayerCooldown.isActive(player, CooldownObject.FIREWORK_ROCKET)) {
            Common.sendMMMessage(player, StringUtil.replaceSecondString(
                    LanguageManager.getString(langKey),
                    PlayerCooldown.getLeftInDouble(player, CooldownObject.FIREWORK_ROCKET)));
            event.setCancelled(true);
            refreshInventory(event, player);
        } else {
            FireworkRocketRunnable runnable = new FireworkRocketRunnable(player, fightPlayer, duration, false);
            runnable.begin();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Calls {@link Player#updateInventory()} when the underlying event is a {@link PlayerInteractEvent}. */
    private static void refreshInventory(Cancellable event, Player player) {
        if (event instanceof PlayerInteractEvent) {
            player.updateInventory();
        }
    }

    // -------------------------------------------------------------------------
    // Fireball
    // -------------------------------------------------------------------------

    @Override
    public boolean handleFireballMatch(Player player, double duration, String langKey) {
        if (PlayerCooldown.isActive(player, CooldownObject.FIREBALL_FIGHT_FIREBALL)) {
            Common.sendMMMessage(player, StringUtil.replaceSecondString(
                    LanguageManager.getString(langKey),
                    PlayerCooldown.getLeftInDouble(player, CooldownObject.FIREBALL_FIGHT_FIREBALL)));
            return false;
        } else {
            FireballRunnable fireballRunnable = new FireballRunnable(player, duration);
            fireballRunnable.begin();
            return true;
        }
    }
}



