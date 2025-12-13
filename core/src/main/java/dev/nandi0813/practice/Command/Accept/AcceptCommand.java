package dev.nandi0813.practice.Command.Accept;

import dev.nandi0813.practice.Manager.Backend.LanguageManager;
import dev.nandi0813.practice.Manager.Duel.DuelManager;
import dev.nandi0813.practice.Manager.Duel.DuelRequest;
import dev.nandi0813.practice.Manager.Party.MatchRequest.PartyRequest;
import dev.nandi0813.practice.Manager.Party.Party;
import dev.nandi0813.practice.Manager.Party.PartyManager;
import dev.nandi0813.practice.Manager.Profile.Enum.ProfileStatus;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileManager;
import dev.nandi0813.practice.Manager.Spectator.SpectatorManager;
import dev.nandi0813.practice.Util.Common;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AcceptCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("command.accept.command-help").replace("%label%", label));
            return false;
        }

        if ((!profile.getStatus().equals(ProfileStatus.LOBBY) && !profile.getStatus().equals(ProfileStatus.SPECTATE))) {
            Common.sendMMMessage(player, LanguageManager.getString("command.accept.cant-accept"));
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (target == null || targetProfile == null) {
            Common.sendMMMessage(player, LanguageManager.getString("command.accept.player-offline").replace("%target%", args[0]));
            return false;
        }

        if (target.equals(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("command.accept.self-duel"));
            return false;
        }

        if (profile.isParty()) {
            if (!targetProfile.isParty()) {
                Common.sendMMMessage(player, LanguageManager.getString("command.accept.no-longer-party").replace("%target%", target.getName()));
                return false;
            }

            Party playerParty = PartyManager.getInstance().getParty(player);
            Party targetParty = PartyManager.getInstance().getParty(target);

            List<PartyRequest> partyRequests = PartyManager.getInstance().getRequestManager().getRequests().get(playerParty);
            if (partyRequests != null) {
                for (PartyRequest partyRequest : partyRequests) {
                    if (!partyRequest.getSender().equals(targetParty)) continue;

                    if (targetProfile.getStatus().equals(ProfileStatus.LOBBY) || targetProfile.getStatus().equals(ProfileStatus.EDITOR)) {
                        partyRequest.acceptRequest();
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("command.accept.party-not-available").replace("%target%", target.getName()));

                    return true;
                }
            }
            Common.sendMMMessage(player, LanguageManager.getString("command.accept.party-no-invite"));
        } else {
            List<DuelRequest> requests = DuelManager.getInstance().getRequests().get(player);

            if (requests != null) {
                for (DuelRequest request : requests) {
                    if (!request.getSender().equals(target)) continue;

                    if ((targetProfile.getStatus().equals(ProfileStatus.LOBBY) || targetProfile.getStatus().equals(ProfileStatus.EDITOR)) && !targetProfile.isParty()) {
                        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
                            SpectatorManager.getInstance().getSpectators().get(player).removeSpectator(player);
                        }

                        request.acceptRequest();
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("command.accept.player-not-available").replace("%target%", target.getName()));

                    return true;
                }
            }
            Common.sendMMMessage(player, LanguageManager.getString("command.accept.player-no-invite").replace("%target%", target.getName()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();
        if (!(sender instanceof Player player)) return arguments;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if ((!profile.getStatus().equals(ProfileStatus.LOBBY) && !profile.getStatus().equals(ProfileStatus.SPECTATE)))
            return arguments;

        if (args.length == 1) {
            if (profile.isParty()) {
                List<PartyRequest> requests = PartyManager.getInstance().getRequestManager().getRequests().get(PartyManager.getInstance().getParty(player));

                if (requests != null) {
                    for (PartyRequest partyRequest : requests) {
                        if (!arguments.contains(partyRequest.getSender().getLeader().getName()))
                            arguments.add(partyRequest.getSender().getLeader().getName());
                    }
                }
            } else {
                List<DuelRequest> requests = DuelManager.getInstance().getRequests().get(player);

                if (requests != null) {
                    for (DuelRequest duelRequest : DuelManager.getInstance().getRequests().get(player)) {
                        if (!arguments.contains(duelRequest.getSender().getName()))
                            arguments.add(duelRequest.getSender().getName());
                    }
                }
            }

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        }

        Collections.sort(completion);
        return completion;
    }

}
