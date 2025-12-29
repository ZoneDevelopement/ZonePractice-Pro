package dev.nandi0813.practice.command.ffa.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum HelpArg {
    ;

    public static void run(Player player, String label) {
        for (String line : LanguageManager.getList("FFA.COMMAND.HELP")) {
            Common.sendMMMessage(player, line.replace("%label%", label));
        }
    }

}
