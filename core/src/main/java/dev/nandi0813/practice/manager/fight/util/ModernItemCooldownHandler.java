package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.ZonePractice;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class ModernItemCooldownHandler {

    public static void handleEnderPearl(Player player, double duration, Cancellable event) {
        if (player.hasCooldown(Material.ENDER_PEARL)) {
            if (event != null) {
                event.setCancelled(true);
            }
        } else {
            player.setCooldown(Material.ENDER_PEARL, (int) (duration * 20));
        }
    }

    public static void handleGoldenApple(Player player, double duration, Cancellable event) {
        if (player.hasCooldown(Material.GOLDEN_APPLE)) {
            if (event != null) {
                event.setCancelled(true);
            }
        } else {
            player.setCooldown(Material.GOLDEN_APPLE, (int) (duration * 20));
        }
    }

    public static void handleFireworkRocket(Player player, double duration, Cancellable event) {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (player.hasCooldown(Material.FIREWORK_ROCKET)) {
                if (event != null) {
                    event.setCancelled(true);
                }
            } else {
                player.setCooldown(Material.FIREWORK_ROCKET, (int) (duration * 20));
            }
        }, 2L);
    }

    public static boolean handleFireballMatch(Player player, double duration) {
        if (player.hasCooldown(Material.FIRE_CHARGE)) {
            return false;
        } else {
            player.setCooldown(Material.FIRE_CHARGE, (int) (duration * 20));
            return true;
        }
    }
}
