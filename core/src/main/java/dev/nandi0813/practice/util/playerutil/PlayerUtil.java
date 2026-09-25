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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PlayerUtil {

    private static final double DEFAULT_FIGHT_MAX_HEALTH = 20.0D;
    private static final long RESET_DELAY_TICKS = 2L;

    private PlayerUtil() {
    }

    public static void clearPlayer(
            Player player,
            boolean deleteInventory,
            boolean allowFlight,
            boolean entityCollision
    ) {
        player.setFallDistance(0);
        resetMaxHealth(player);
        healToMaxHealth(player);

        player.setExp(0);
        player.setLevel(0);
        player.setFoodLevel(23);

        clearStuckArrows(player);

        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(allowFlight);
        player.setFlying(allowFlight);
        setEntityCollision(player, entityCollision);

        if (ZonePractice.getInstance().isEnabled()) {
            runLater(() -> player.setFireTicks(0));
        } else {
            player.setFireTicks(0);
        }

        if (deleteInventory) {
            dev.nandi0813.practice.manager.fight.util.PlayerUtil.clearInventory(player);
        }

        clearPotionEffects(player);
    }

    public static void setFightPlayer(Player player) {
        setFightPlayer(player, null);
    }

    public static void setFightPlayer(Player player, Ladder ladder) {
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            applyFightHealth(player, ladder);
            runLater(() -> applyFightHealth(player, ladder));
            runLater(() -> player.setFireTicks(0));

            player.setFoodLevel(25);
            player.setSaturation(0.0F);
            player.setFallDistance(0);
            player.setWalkSpeed(0.2F);

            clearPotionEffects(player);

            player.setGameMode(GameMode.SURVIVAL);
            player.setFlying(false);
            player.setAllowFlight(false);
            setEntityCollision(player, true);
        });
    }

    public static void healToMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double value = maxHealth != null
                ? maxHealth.getValue()
                : DEFAULT_FIGHT_MAX_HEALTH;

        player.setHealth(Math.max(1.0D, value));
    }

    public static void resetMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(DEFAULT_FIGHT_MAX_HEALTH);
        }
    }

    public static void setPlayerWorldTime(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        player.setPlayerTime(profile.getWorldTime().getTime(), false);
    }

    public static List<String> getPlayerNames(List<Player> players) {
        return players.stream()
                .map(Player::getName)
                .toList();
    }

    public static void sendStaffMessage(Player sender, String message) {
        String senderName = sender != null
                ? sender.getName()
                : LanguageManager.getString("CONSOLE-NAME");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.hasPermission("zpp.staffmode.chat")) {
                continue;
            }

            String formattedMessage = LanguageManager.getString("GENERAL-CHAT.STAFF-CHAT")
                    .replace("%%player%%", senderName)
                    .replace("%%message%%", message);

            Common.sendMMMessage(online, formattedMessage);
        }
    }

    public static List<Player> getOnlineStaff() {
        List<Player> staff = new ArrayList<>();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("zpp.staff")) {
                staff.add(online);
            }
        }

        return staff;
    }

    public static Map<Player, Integer> sortByValue(Map<Player, Integer> map) {
        Map<Player, Integer> sorted = new LinkedHashMap<>();

        map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(entry -> sorted.put(entry.getKey(), entry.getValue()));

        return sorted;
    }

    private static void applyFightHealth(Player player, Ladder ladder) {
        applyFightMaxHealth(player, ladder);
        healToMaxHealth(player);
    }

    private static void applyFightMaxHealth(Player player, Ladder ladder) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        double targetHealth = ladder == null
                ? DEFAULT_FIGHT_MAX_HEALTH
                : Math.clamp(ladder.getHearts() * 2.0D, 2.0D, 40.0D);

        maxHealth.setBaseValue(targetHealth);
    }

    private static void clearPotionEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private static void setEntityCollision(Player player, boolean enabled) {
        dev.nandi0813.practice.manager.fight.util.PlayerUtil
                .setCollidesWithEntities(player, enabled);
    }

    private static void runLater(Runnable task) {
        Bukkit.getScheduler().runTaskLater(
                ZonePractice.getInstance(),
                task,
                RESET_DELAY_TICKS
        );
    }

    private static void clearStuckArrows(Player player) {
        try {
            Method method = player.getClass().getMethod("setArrowsInBody", int.class);
            method.invoke(player, 0);
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // Older APIs may not expose the arrow count method.
        }
    }
}