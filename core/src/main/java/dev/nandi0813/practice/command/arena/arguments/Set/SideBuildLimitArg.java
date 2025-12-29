package dev.nandi0813.practice.command.arena.arguments.Set;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSetupManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum SideBuildLimitArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.NO-PERMISSION"));
            return;
        }

        if (args.length != 4) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SIDEBUILDLIMIT.COMMAND-HELP").replace("%label%", label).replace("%label2%", args[1]));
            return;
        }

        Arena arena = ArenaManager.getInstance().getNormalArena(args[2]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SIDEBUILDLIMIT.ARENA-NOT-EXISTS").replace("%arena%", args[2]));
            return;
        }

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SIDEBUILDLIMIT.ARENA-ENABLED").replace("%arena%", arena.getName()));
            return;
        }

        if (!arena.isBuild()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SIDEBUILDLIMIT.NOT-BUILD").replace("%arena%", arena.getName()));
            return;
        }

        int sideBuildLimit = Integer.parseInt(args[3]);
        if (sideBuildLimit < 0 || sideBuildLimit > 10) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SIDEBUILDLIMIT.INVALID-NUMBER").replace("%arena%", arena.getName()));
            return;
        }

        arena.setSideBuildLimit(sideBuildLimit);
        ArenaSetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SIDEBUILDLIMIT.SET-SIDEBUILDLIMIT").replace("%arena%", arena.getName()).replace("%sideBuildLimit%", args[3]));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 3) {
            for (Arena arena : ArenaManager.getInstance().getNormalArenas())
                arguments.add(arena.getName());

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
