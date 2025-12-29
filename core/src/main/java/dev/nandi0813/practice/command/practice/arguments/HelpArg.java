package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum HelpArg {
    ;

    public static void run(Player player, String label) {
        if (player.hasPermission("zpp.admin")) {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.HELP.ADMIN"))
                Common.sendMMMessage(player, line.replaceAll("%label%", label));
        } else if (player.hasPermission("zpp.staff")) {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.HELP.STAFF"))
                Common.sendMMMessage(player, line.replaceAll("%label%", label));
        } else
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
    }

    public static void run(String label) {
        for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.HELP.ADMIN"))
            Common.sendConsoleMMMessage(line.replaceAll("%label%", label));
    }

}
