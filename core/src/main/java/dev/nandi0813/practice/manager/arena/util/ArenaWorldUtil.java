package dev.nandi0813.practice.manager.arena.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

public enum ArenaWorldUtil {
    ;

    @Getter
    public static World arenasWorld = Bukkit.getWorld("arenas");
    @Getter
    public static World arenasCopyWorld = Bukkit.getWorld("arenas_copy");

    public static void createArenaWorld() {
        if (arenasWorld == null) {
            arenasWorld = ArenaUtil.createEmptyWorld("arenas");
            ArenaUtil.setGamerules(arenasWorld);
        }

        if (arenasCopyWorld == null) {
            arenasCopyWorld = ArenaUtil.createEmptyWorld("arenas_copy");
            ArenaUtil.setGamerules(arenasCopyWorld);
        }
    }

}
