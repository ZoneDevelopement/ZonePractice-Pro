package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.util.Common;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public enum ArenasArg {
    ;

    public static void run(Player player) {
        if (!player.hasPermission("zpp.practice.arenas")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
            return;
        }

        PlayerUtil.clearInventory(player);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);

        player.teleport(ArenaWorldUtil.getArenasWorld().getSpawnLocation());
    }

}
