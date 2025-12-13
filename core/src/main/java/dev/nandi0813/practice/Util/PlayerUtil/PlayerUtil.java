package dev.nandi0813.practice.Util.PlayerUtil;

import dev.nandi0813.practice.Manager.Backend.LanguageManager;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileManager;
import dev.nandi0813.practice.Module.Util.ClassImport;
import dev.nandi0813.practice.Util.Common;
import dev.nandi0813.practice.ZonePractice;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public enum PlayerUtil {
    ;

    public static void clearPlayer(Player player, boolean deleteInv, boolean fly, boolean entityCollide) {
        player.setFallDistance(0);
        player.setHealth(20);
        player.setExp(0);
        player.setLevel(0);
        player.setFoodLevel(23);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(fly);
        player.setFlying(fly);
        ClassImport.getClasses().getPlayerUtil().setCollidesWithEntities(player, entityCollide);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> player.setFireTicks(0), 2L);

        if (deleteInv) ClassImport.getClasses().getPlayerUtil().clearInventory(player);

        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());

        player.setNoDamageTicks(20);
    }

    public static void setFightPlayer(Player player) {
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () ->
        {
            player.setHealth(20);
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> player.setHealth(20), 2L);
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> player.setFireTicks(0), 2L);
            player.setFoodLevel(25);
            player.resetMaxHealth();
            player.setFallDistance(0);
            player.setWalkSpeed(0.2F);
            for (PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());
            player.setGameMode(GameMode.SURVIVAL);
            player.setFlying(false);
            player.setAllowFlight(false);
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
                        .replaceAll("%%player%%", (sender != null ? sender.getName() : LanguageManager.getString("CONSOLE-NAME")))
                        .replaceAll("%%message%%", message));
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
