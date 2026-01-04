package dev.nandi0813.practice.command.party.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum PartyJoinArg {
    ;

    public static void JoinCommand(Player player, String label, String[] args) {
        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.ACCEPT.COMMAND-HELP2").replace("%label%", label));
            return;
        }

        PartyAcceptArg.AcceptCommand(player, label, args);
    }

}
