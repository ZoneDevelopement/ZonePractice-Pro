package dev.nandi0813.practice.command.party.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public enum PartyInfoArg {
    ;

    public static void InfoCommand(Player player, String label, String[] args) {
        if (args.length != 1 && args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INFO.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        Party party;
        if (args.length == 1) {
            party = PartyManager.getInstance().getParty(player);
            if (party == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INFO.NO-PARTY"));
                return;
            }
        } else {
            if (!player.hasPermission("zpp.party.info.others")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INFO.NO-PERMISSION"));
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INFO.NOT-ONLINE").replaceAll("%target%", args[1]));
                return;
            }

            party = PartyManager.getInstance().getParty(target);
            if (party == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INFO.PLAYER-NO-PARTY").replaceAll("%target%", target.getName()));
                return;
            }
        }

        for (String line : LanguageManager.getList("COMMAND.PARTY.ARGUMENTS.INFO.PARTY-INFO")) {
            Common.sendMMMessage(player, line

                    .replaceAll("%leader%", party.getLeader().getName())
                    .replaceAll("%partyState%", (party.isPublicParty() ? LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INFO.PARTY-STATES.PUBLIC") : LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INFO.PARTY-STATES.PRIVATE")))
                    .replaceAll("%maxPlayerLimit%", String.valueOf(party.getMaxPlayerLimit()))
                    .replaceAll("%memberSize%", String.valueOf(party.getMembers().size()))
                    .replaceAll("%members%", party.getMemberNames().toString().replace("[", "").replace("]", ""))
            );
        }
    }

}
