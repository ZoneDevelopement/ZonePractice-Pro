package dev.nandi0813.practice.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LastAttackerTracker {

    private final Map<UUID, UUID> lastAttackerMap = new HashMap<>();
    private final Map<UUID, Long> lastAttackerTime = new HashMap<>();
    private static final long LAST_ATTACKER_EXPIRY_MS = 4_000L;

    public void recordAttack(Player victim, Player attacker) {
        if (victim == null || attacker == null || victim.equals(attacker)) return;
        lastAttackerMap.put(victim.getUniqueId(), attacker.getUniqueId());
        lastAttackerTime.put(victim.getUniqueId(), System.currentTimeMillis());
    }

    public @Nullable Player getLastAttacker(Player victim, Collection<? extends Player> candidates) {
        Long time = lastAttackerTime.get(victim.getUniqueId());
        if (time == null || System.currentTimeMillis() - time > LAST_ATTACKER_EXPIRY_MS) return null;
        UUID attackerUuid = lastAttackerMap.get(victim.getUniqueId());
        if (attackerUuid == null) return null;
        for (Player p : candidates) {
            if (attackerUuid.equals(p.getUniqueId())) return p;
        }
        return null;
    }
}
