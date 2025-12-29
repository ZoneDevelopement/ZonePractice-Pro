package dev.nandi0813.practice.command.party;

import dev.nandi0813.practice.command.party.arguments.*;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartyCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.isStaffMode() || profile.isSpectatorMode()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.CANT-USE-COMMAND"));
            return false;
        }

        if (args.length > 0) {
            switch (args[0]) {
                case "accept":
                    PartyAcceptArg.AcceptCommand(player, label, args);
                    break;
                case "disband":
                    PartyDisbandArg.DisbandCommand(player, label, args);
                    break;
                case "info":
                    PartyInfoArg.InfoCommand(player, label, args);
                    break;
                case "invite":
                    PartyInviteArg.InviteCommand(player, label, args);
                    break;
                case "join":
                    PartyJoinArg.JoinCommand(player, label, args);
                    break;
                case "create":
                    PartyManager.getInstance().createParty(player);
                    break;
                case "kick":
                    PartyKickArg.KickCommand(player, label, args);
                    break;
                case "leader":
                    PartyLeaderArg.LeaderCommand(player, label, args);
                    break;
                case "leave":
                    PartyLeaveArg.LeaveCommand(player, label, args);
                    break;
                default:
                    PartyHelpArg.HelpCommand(player, label);
            }
        } else
            PartyHelpArg.HelpCommand(player, label);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();
        if (!(sender instanceof Player player)) return arguments;

        Party party = PartyManager.getInstance().getParty(player);

        if (args.length == 1) {
            if (party != null) {
                if (party.getLeader().equals(player)) {
                    arguments.add("disband");
                    arguments.add("kick");
                    arguments.add("leader");
                }
                if (party.getLeader().equals(player) || party.isAllInvite())
                    arguments.add("invite");

                arguments.add("leave");
            } else {
                arguments.add("create");
                arguments.add("join");
                arguments.add("accept");
            }

            arguments.add("help");
            arguments.add("info");

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        } else {
            switch (args[0]) {
                case "invite":
                    completion = PartyInviteArg.tabComplete(player, args);
                    break;
                case "kick":
                    completion = PartyKickArg.tabComplete(player, args);
                    break;
                case "leader":
                    completion = PartyLeaderArg.tabComplete(player, args);
                    break;
            }
        }

        Collections.sort(completion);
        return completion;
    }

}
