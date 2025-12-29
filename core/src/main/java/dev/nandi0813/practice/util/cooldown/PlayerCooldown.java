package dev.nandi0813.practice.util.cooldown;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public enum PlayerCooldown {
    ;

    @Getter
    private static final Map<Player, Map<CooldownObject, Long>> cooldowns = new HashMap<>();

    public static void addCooldown(Player player, CooldownObject object, int time) {
        if (!getCooldowns().containsKey(player)) {
            Map<CooldownObject, Long> cooldowns = new HashMap<>();
            getCooldowns().put(player, cooldowns);
        }
        Map<CooldownObject, Long> cooldowns = getCooldowns().get(player);
        cooldowns.put(object, System.currentTimeMillis() + time * 1000L);
        getCooldowns().put(player, cooldowns);
    }

    public static void addCooldown(Player player, CooldownObject object, double time) {
        if (!getCooldowns().containsKey(player)) {
            Map<CooldownObject, Long> cooldowns = new HashMap<>();
            getCooldowns().put(player, cooldowns);
        }
        Map<CooldownObject, Long> cooldowns = getCooldowns().get(player);
        cooldowns.put(object, (long) (System.currentTimeMillis() + time * 1000L));
        getCooldowns().put(player, cooldowns);
    }

    public static void removeCooldown(Player player, CooldownObject object) {
        if (cooldowns.containsKey(player))
            cooldowns.get(player).remove(object);
    }

    public static boolean isActive(Player player, CooldownObject object) {
        if (cooldowns.containsKey(player))
            return cooldowns.get(player).containsKey(object) && System.currentTimeMillis() < cooldowns.get(player).get(object);
        return false;
    }

    public static long getLeft(Player player, CooldownObject object) {
        if (isActive(player, object))
            return Math.max(cooldowns.get(player).get(object) - System.currentTimeMillis(), 0L);
        return 0L;
    }

    public static double getLeftInDouble(Player player, CooldownObject object) {
        BigDecimal bd = BigDecimal.valueOf(getLeft(player, object) / (float) 1000);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
