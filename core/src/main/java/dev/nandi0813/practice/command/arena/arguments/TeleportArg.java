package dev.nandi0813.practice.command.arena.arguments;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum TeleportArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.no-permission"));
            return;
        }

        if (args.length != 3) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.TELEPORT.COMMAND-HELP").replace("%label%", label));
            return;
        }

        DisplayArena arena = ArenaManager.getInstance().getArena(args[1]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.TELEPORT.ARENA-NOT-EXISTS").replace("%arena%", args[1]));
            return;
        }

        arena.teleport(player);
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 2) {
            for (DisplayArena arena : ArenaManager.getInstance().getArenaList())
                arguments.add(arena.getName());

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
