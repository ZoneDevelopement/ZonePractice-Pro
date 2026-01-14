package dev.nandi0813.practice.command.party.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum PartyInviteArg {
    ;

    public static void InviteCommand(Player player, String label, String[] args) {
        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Party party = PartyManager.getInstance().getParty(player);
        if (party == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.NO-PARTY"));
            return;
        }

        if (!party.isAllInvite() && !party.getLeader().equals(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.CANT-INVITE"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.TARGET-OFFLINE").replace("%target%", args[1]));
            return;
        }

        if (player.equals(target)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.CANT-INVITE-YOURSELF"));
            return;
        }

        if (party.getMembers().contains(target)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.PLAYER-ALREADY-IN-PARTY").replace("%target%", target.getName()));
            return;
        }

        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (!targetProfile.isPartyInvites()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.NO-INVITES").replace("%target%", target.getName()));
            return;
        }

        if (party.getInvites().containsKey(player) && (party.getInvites().get(player) + PartyManager.INVITE_COOLDOWN > System.currentTimeMillis())) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.ALREADY-INVITED").replace("%target%", target.getName()));
            return;
        }

        party.getInvites().put(target, System.currentTimeMillis());

        party.sendMessage(LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.PLAYER-INVITED").replace("%target%", target.getName()));
        Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.INVITE.PLAYER-GOT-INVITE").replace("%inviter%", player.getName()));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        Party party = PartyManager.getInstance().getParty(player);

        if (party != null && (party.getLeader().equals(player) || party.isAllInvite())) {
            if (args.length == 2) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.equals(player)) continue;
                    Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
                    if (onlineProfile.isParty()) continue;

                    arguments.add(online.getName());
                }

                return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
            }
        }

        return arguments;
    }

}
