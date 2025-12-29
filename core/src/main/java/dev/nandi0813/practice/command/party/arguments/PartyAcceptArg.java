package dev.nandi0813.practice.command.party.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public enum PartyAcceptArg {
    ;

    public static void AcceptCommand(Player player, String label, String[] args) {
        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.ACCEPT.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.ACCEPT.CANT-JOIN-NOW"));
            return;
        }

        if (profile.isParty()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.ACCEPT.ALREADY-MEMBER"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.ACCEPT.TARGET-OFFLINE").replace("%target%", args[1]));
            return;
        }

        Party party = PartyManager.getInstance().getParty(target);
        if (party == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.ACCEPT.PARTY-NOT-EXISTS"));
            return;
        }

        if (party.getMembers().size() >= party.getMaxPlayerLimit()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.ACCEPT.PARTY-FULL").replaceAll("%leader%", party.getLeader().getName()));
            return;
        }

        if (party.isPublicParty()) {
            party.addMember(player);
        } else {
            if (!party.getInvites().containsKey(player)) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.ACCEPT.NO-INVITE").replaceAll("%leader%", party.getLeader().getName()));
                return;
            }

            if (party.getInvites().get(player) + PartyManager.INVITE_COOLDOWN > System.currentTimeMillis()) {
                profile.setSpectatorMode(false);
                party.addMember(player);
            } else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PARTY.ARGUMENTS.ACCEPT.INVITE-EXPIRED").replaceAll("%leader%", party.getLeader().getName()));

            party.getInvites().remove(player);
        }
    }


}
