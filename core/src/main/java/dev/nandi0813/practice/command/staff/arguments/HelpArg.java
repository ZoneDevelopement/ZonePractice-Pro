package dev.nandi0813.practice.command.staff.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum HelpArg {
    ;

    public static void run(Player player, String label) {
        if (!player.hasPermission("zpp.staffmode")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.NO-PERMISSION"));
            return;
        }

        for (String line : LanguageManager.getList("COMMAND.STAFF.ARGUMENTS.HELP"))
            Common.sendMMMessage(player, line.replaceAll("%label%", label));
    }

    public static void run(String label) {
        for (String line : LanguageManager.getList("COMMAND.STAFF.ARGUMENTS.HELP"))
            Common.sendConsoleMMMessage(line.replaceAll("%label%", label));
    }

}
