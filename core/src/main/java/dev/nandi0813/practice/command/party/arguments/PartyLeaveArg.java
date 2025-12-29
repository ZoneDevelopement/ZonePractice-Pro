package dev.nandi0813.practice.command.party.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum PartyLeaveArg {
    ;

    public static void LeaveCommand(Player player, String label, String[] args) {
        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.LEAVE.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        Party party = PartyManager.getInstance().getParty(player);
        if (party == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.LEAVE.NO-PARTY"));
            return;
        }

        party.removeMember(player, false);
    }

}
