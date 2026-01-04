package dev.nandi0813.practice.command.party.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum PartyHelpArg {
    ;

    public static void HelpCommand(Player player, String label) {
        for (String line : LanguageManager.getList("COMMAND.PARTY.ARGUMENTS.HELP"))
            Common.sendMMMessage(player, line.replace("%label%", label));
    }

}
