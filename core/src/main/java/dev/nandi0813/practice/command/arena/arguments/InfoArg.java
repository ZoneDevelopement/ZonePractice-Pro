package dev.nandi0813.practice.command.arena.arguments;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum InfoArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.no-permission"));
            return;
        }

        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.arguments.info.command-help").replace("%label%", label));
            return;
        }

        DisplayArena arena = ArenaManager.getInstance().getArena(args[1]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.arguments.info.not-exists").replace("%arena%", args[1]));
            return;
        }

        List<String> ladderNames = ArenaUtil.getLadderNames(arena);
        for (String line : LanguageManager.getList("command.arena.arguments.info.arena-info")) {
            Common.sendMMMessage(player, line
                    .replaceAll("%arena%", arena.getName())
                    .replaceAll("%type%", arena.getType().getName())
                    .replaceAll("%icon%", arena.getIcon() != null ? LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.INFO.STATUS-NAMES.SET") : LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.INFO.STATUS-NAMES.NOT-SET"))
                    .replaceAll("%displayName%", StringUtil.legacyColorToMiniMessage(arena.getDisplayName()))
                    .replaceAll("%ladders%", (ladderNames.isEmpty() ? StringUtil.CC("<red>NULL") : ladderNames.toString().replace("]", "").replace("[", "")))
                    .replaceAll("%corner1%", ArenaUtil.convertLocation(arena.getCorner1()))
                    .replaceAll("%corner2%", ArenaUtil.convertLocation(arena.getCorner2()))
                    .replaceAll("%position1%", ArenaUtil.convertLocation(arena.getPosition1()))
                    .replaceAll("%position2%", ArenaUtil.convertLocation(arena.getPosition2()))
                    .replaceAll("%status%", arena.isEnabled() ? LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.INFO.STATUS-NAMES.ENABLED") : LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.INFO.STATUS-NAMES.DISABLED"))
            );
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 2) {
            for (DisplayArena arena : ArenaManager.getInstance().getArenaList())
                arguments.add(arena.getName());

            return org.bukkit.util.StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
