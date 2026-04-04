package dev.nandi0813.practice.util.playerutil;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
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

public enum PlayerUtil {
    ;

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
        player.setHealth(20);
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
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () ->
        {
            player.setHealth(20);
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> player.setHealth(20), 2L);
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> player.setFireTicks(0), 2L);
            player.setFoodLevel(25);
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(maxHealth.getDefaultValue());

                if (player.getHealth() > maxHealth.getValue()) {
                    player.setHealth(maxHealth.getValue());
                }
            }
            player.setFallDistance(0);
            player.setWalkSpeed(0.2F);
            for (PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());
            player.setGameMode(GameMode.SURVIVAL);
            player.setFlying(false);
            player.setAllowFlight(false);
            // Players can be marked non-collidable while temporarily spectating; restore combat hitboxes.
            dev.nandi0813.practice.manager.fight.util.PlayerUtil.setCollidesWithEntities(player, true);
            // Clear stale invulnerability frames so shield-stun / rapid follow-up hits work consistently.
        });
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
