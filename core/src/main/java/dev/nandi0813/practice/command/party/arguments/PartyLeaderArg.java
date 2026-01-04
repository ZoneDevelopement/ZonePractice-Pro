package dev.nandi0813.practice.command.party.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum PartyLeaderArg {
    ;

    public static void LeaderCommand(Player player, String label, String[] args) {
        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.LEADER.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Party party = PartyManager.getInstance().getParty(player);
        if (party == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.LEADER.NO-PARTY"));
            return;
        }

        if (!party.getLeader().equals(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.LEADER.NOT-LEADER"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.LEADER.TARGET-OFFLINE").replace("%target%", args[1]));
            return;
        }

        if (target.equals(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.LEADER.ALREADY-LEADER"));
            return;
        }

        if (!party.getMembers().contains(target)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.LEADER.NOT-MEMBER").replace("%target%", target.getName()));
            return;
        }

        party.setNewOwner(target);
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        Party party = PartyManager.getInstance().getParty(player);
        if (party == null || !party.getLeader().equals(player)) return arguments;

        if (args.length == 2) {
            for (Player member : party.getMembers()) {
                if (member.equals(player)) continue;

                arguments.add(member.getName());
            }

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
