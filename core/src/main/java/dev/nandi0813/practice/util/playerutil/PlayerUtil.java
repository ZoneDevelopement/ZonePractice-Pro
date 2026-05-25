package dev.nandi0813.practice.util.playerutil;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Method;
import java.util.*;

public final class PlayerUtil {

    private PlayerUtil() {}

    private static final double DEFAULT_FIGHT_MAX_HEALTH = 20.0D;

    private static void clearStuckArrows(Player player) {
        try {
            Method setArrowsInBody = player.getClass().getMethod("setArrowsInBody", int.class);
            setArrowsInBody.invoke(player, 0);
        } catch (Throwable ignored) {
            // Older APIs may not expose arrow body count.
        }
    }

    public static void clearPlayer(Player player, boolean deleteInv, boolean fly, boolean entityCollide) {
        player.setFallDistance(0);
        resetMaxHealth(player);
        healToMaxHealth(player);
        player.setExp(0);
        player.setLevel(0);
        player.setFoodLevel(23);
        clearStuckArrows(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(fly);
        player.setFlying(fly);
        dev.nandi0813.practice.manager.fight.util.PlayerUtil.setCollidesWithEntities(player, entityCollide);

        if (ZonePractice.getInstance().isEnabled()) {
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> player.setFireTicks(0), 2L);
        } else {
            player.setFireTicks(0);
        }

        if (deleteInv) dev.nandi0813.practice.manager.fight.util.PlayerUtil.clearInventory(player);

        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
    }

    public static void setFightPlayer(Player player) {
        setFightPlayer(player, null);
    }

    public static void setFightPlayer(Player player, Ladder ladder) {
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () ->
        {
            applyFightMaxHealth(player, ladder);
            healToMaxHealth(player);
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
                applyFightMaxHealth(player, ladder);
                healToMaxHealth(player);
            }, 2L);
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> player.setFireTicks(0), 2L);
            player.setFoodLevel(25);
            player.setFallDistance(0);
            player.setWalkSpeed(0.2F);
            for (PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());
            player.setGameMode(GameMode.SURVIVAL);
            player.setFlying(false);
            player.setAllowFlight(false);
            dev.nandi0813.practice.manager.fight.util.PlayerUtil.setCollidesWithEntities(player, true);
        });
    }

    public static void healToMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealthValue = maxHealth != null ? maxHealth.getValue() : DEFAULT_FIGHT_MAX_HEALTH;
        player.setHealth(Math.max(1.0D, maxHealthValue));
    }

    public static void resetMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.setBaseValue(maxHealth.getDefaultValue());
    }

    private static void applyFightMaxHealth(Player player, Ladder ladder) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;

        double targetMaxHealth = DEFAULT_FIGHT_MAX_HEALTH;
        if (ladder != null) {
            targetMaxHealth = Math.clamp(ladder.getHearts() * 2.0D, 2.0D, 40.0D);
        }

        maxHealth.setBaseValue(targetMaxHealth);
    }

    public static void setPlayerWorldTime(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        player.setPlayerTime(profile.getWorldTime().getTime(), false);
    }

    public static List<String> getPlayerNames(List<Player> base) {
        List<String> names = new ArrayList<>();
        for (Player player : base)
            names.add(player.getName());
        return names;
    }

    public static void sendStaffMessage(Player sender, String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("zpp.staffmode.chat")) {
                Common.sendMMMessage(online, LanguageManager.getString("GENERAL-CHAT.STAFF-CHAT")
                        .replace("%%player%%", (sender != null ? sender.getName() : LanguageManager.getString("CONSOLE-NAME")))
                        .replace("%%message%%", message));
            }
        }
    }

    public static List<Player> getOnlineStaff() {
        List<Player> staff = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers())
            if (online.hasPermission("zpp.staff"))
                staff.add(online);
        return staff;
    }

    public static Map<Player, Integer> sortByValue(Map<Player, Integer> map) {
        LinkedHashMap<Player, Integer> reverseSortedMap = new LinkedHashMap<>();

        map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        return reverseSortedMap;
    }

}
