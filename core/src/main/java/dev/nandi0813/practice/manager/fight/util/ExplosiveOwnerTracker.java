package dev.nandi0813.practice.manager.fight.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ExplosiveOwnerTracker {

    private ExplosiveOwnerTracker() {
    }

    private static final long OWNER_TTL_MS = 5_000L;


    /**
     * TNT minecart UUID -> owner
     */
    private static final Map<UUID, ExplosiveData> MINECART_OWNERS = new ConcurrentHashMap<>();


    /**
     * Respawn anchor coordinates -> owner
     * World intentionally ignored (old logic).
     */
    private static final Map<BlockPosition, ExplosiveData> ANCHOR_OWNERS = new ConcurrentHashMap<>();


    private record ExplosiveData(
            UUID owner,
            long createdAt
    ) {
    }


    private record BlockPosition(
            int x,
            int y,
            int z
    ) {
    }


    public static void recordMinecartOwner(Minecart minecart, Player owner) {
        MINECART_OWNERS.put(
                minecart.getUniqueId(),
                new ExplosiveData(
                        owner.getUniqueId(),
                        System.currentTimeMillis()
                )
        );
    }


    public static void recordAnchorOwner(Location location, Player owner) {
        ANCHOR_OWNERS.put(
                new BlockPosition(
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ()
                ),
                new ExplosiveData(
                        owner.getUniqueId(),
                        System.currentTimeMillis()
                )
        );
    }


    public static @Nullable Player getMinecartOwner(Minecart minecart) {
        UUID id = minecart.getUniqueId();

        ExplosiveData data = MINECART_OWNERS.get(id);

        if (data == null) {
            return null;
        }

        if (expired(data)) {
            MINECART_OWNERS.remove(id);
            return null;
        }

        return Bukkit.getPlayer(data.owner());
    }


    public static @Nullable Player getAnchorOwner(Location location) {
        BlockPosition position = new BlockPosition(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        ExplosiveData data = ANCHOR_OWNERS.get(position);

        if (data == null) {
            return null;
        }

        if (expired(data)) {
            ANCHOR_OWNERS.remove(position);
            return null;
        }

        return Bukkit.getPlayer(data.owner());
    }


    private static boolean expired(ExplosiveData data) {
        return System.currentTimeMillis() - data.createdAt() > OWNER_TTL_MS;
    }
}