package dev.nandi0813.practice.command.staff.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.runnable.round.RoundEndRunnable;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum ForceEndArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.staffmode.forceend")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.NO-PERMISSION"));
            return;
        }

        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.isStaffMode()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.ONLY-IN-STAFFMODE"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.TARGET-OFFLINE").replaceAll("%target%", args[1]));
            return;
        }

        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (targetProfile.getStatus().equals(ProfileStatus.MATCH)) {
            Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
            RoundEndRunnable roundEndRunnable = match.getCurrentRound().getRoundEndRunnable();

            if (roundEndRunnable != null && roundEndRunnable.isRunning() && roundEndRunnable.isEnded()) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.MATCH.ENDED"));
                return;
            }

            for (Player matchPlayer : match.getPlayers()) {
                if (player != matchPlayer && matchPlayer.hasPermission("zpp.bypass.forceend")) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.MATCH.CANT-END"));
                    return;
                }
            }

            match.endMatch();
            match.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.MATCH.MATCH-END-MSG").replaceAll("%player%", player.getName()), false);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.MATCH.PLAYER-END-MSG").replaceAll("%target%", target.getName()));
        } else if (targetProfile.getStatus().equals(ProfileStatus.EVENT)) {
            Event event = EventManager.getInstance().getEventByPlayer(target);

            if (event.getEndRunnable() != null && event.getEndRunnable().isRunning()) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.EVENT.ENDED"));
                return;
            }

            for (Player matchPlayer : event.getPlayers()) {
                if (player != matchPlayer && matchPlayer.hasPermission("zpp.bypass.forceend")) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.EVENT.CANT-END"));
                    return;
                }
            }

            event.forceEnd(player);
        } else
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.PLAYER-INACTIVE"));
    }

    public static void run(String label, String[] args) {
        if (args.length != 2) {
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.TARGET-OFFLINE").replaceAll("%target%", args[1]));
            return;
        }

        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (targetProfile.getStatus().equals(ProfileStatus.MATCH)) {
            Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
            RoundEndRunnable roundEndRunnable = match.getCurrentRound().getRoundEndRunnable();

            if (roundEndRunnable != null && roundEndRunnable.isRunning() && roundEndRunnable.isEnded()) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.MATCH.ENDED"));
                return;
            }

            match.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.MATCH.MATCH-END-MSG").replaceAll("%player%", LanguageManager.getString("CONSOLE-NAME")), false);
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.MATCH.PLAYER-END-MSG").replaceAll("%target%", target.getName()));

            match.endMatch();
        } else if (targetProfile.getStatus().equals(ProfileStatus.EVENT)) {
            Event event = EventManager.getInstance().getEventByPlayer(target);

            if (event.getEndRunnable() != null && event.getEndRunnable().isRunning()) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.EVENT.ENDED"));
                return;
            }

            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.EVENT.PLAYER-END-MSG"));
            event.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.EVENT.MATCH-END-MSG").replaceAll("%player%", LanguageManager.getString("CONSOLE-NAME")), true);

            event.forceEnd(null);
        } else
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.PLAYER-INACTIVE"));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (!player.hasPermission("zpp.staffmode.forceend")) return arguments;

        if (args.length == 2) {
            for (Player online : Bukkit.getOnlinePlayers())
                arguments.add(online.getName());

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
