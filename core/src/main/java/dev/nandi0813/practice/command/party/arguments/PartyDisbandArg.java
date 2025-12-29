package dev.nandi0813.practice.command.party.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum PartyDisbandArg {
    ;

    public static void DisbandCommand(Player player, String label, String[] args) {
        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.DISBAND.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        Party party = PartyManager.getInstance().getParty(player);
        if (party == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.DISBAND.NO-PARTY"));
            return;
        }

        if (!party.getLeader().equals(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.DISBAND.NOT-LEADER"));
            return;
        }

        party.disband();
    }

}
