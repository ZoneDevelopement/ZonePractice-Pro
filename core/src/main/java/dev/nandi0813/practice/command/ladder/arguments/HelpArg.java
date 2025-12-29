package dev.nandi0813.practice.command.ladder.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum HelpArg {
    ;

    public static void run(Player player, String label) {
        if (player.hasPermission("zpp.setup")) {
            for (String line : LanguageManager.getList("COMMAND.LADDER.ARGUMENTS.HELP.ADMIN"))
                Common.sendMMMessage(player, line.replaceAll("%label%", label));
        } else if (player.hasPermission("zpp.ladder.freeze") || player.hasPermission("zpp.ladder.stop")) {
            for (String line : LanguageManager.getList("COMMAND.LADDER.ARGUMENTS.HELP.STAFF"))
                Common.sendMMMessage(player, line.replaceAll("%label%", label));
        } else
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.NO-PERMISSION"));
    }

    public static void run_setCommand(Player player, String label) {
        if (player.hasPermission("zpp.setup")) {
            for (String line : LanguageManager.getList("COMMAND.LADDER.ARGUMENTS.HELP.SET-COMMAND"))
                Common.sendMMMessage(player, line.replaceAll("%label%", label));
        } else
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.NO-PERMISSION"));
    }

}
