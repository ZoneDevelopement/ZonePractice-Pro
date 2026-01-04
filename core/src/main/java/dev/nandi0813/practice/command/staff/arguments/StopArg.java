package dev.nandi0813.practice.command.staff.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum StopArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.staffmode.stop")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.NO-PERMISSION"));
            return;
        }

        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (target == null || targetProfile == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.TARGET-OFFLINE").replace("%target%", args[1]));
            return;
        }

        if (player != target && target.hasPermission("zpp.bypass.stop")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.TARGET-BYPASS").replace("%target%", target.getName()));
            return;
        }

        ProfileStatus profileStatus = targetProfile.getStatus();
        switch (profileStatus) {
            case MATCH:
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
                if (match == null) return;

                match.removePlayer(target, false);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.MATCH.PLAYER").replace("%target%", target.getName()));
                target.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.MATCH.TARGET").replace("%player%", player.getName()));
                match.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.MATCH.MATCH").replace("%target%", target.getName()), true);
                break;
            case EVENT:
                Event event = EventManager.getInstance().getEventByPlayer(target);
                if (event == null) return;

                event.removePlayer(target, false);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.EVENT.PLAYER").replace("%target%", target.getName()));
                target.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.EVENT.TARGET").replace("%player%", player.getName()));
                event.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.EVENT.EVENT").replace("%target%", target.getName()), true);
                break;
            case SPECTATE:
                Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(target);
                if (spectatable != null) {
                    spectatable.removeSpectator(target);

                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.SPECTATE.PLAYER").replace("%target%", target.getName()));
                    Common.sendMMMessage(target, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.SPECTATE.TARGET").replace("%player%", player.getName()));
                }
                break;
        }
    }

    public static void run(String label, String[] args) {
        if (args.length != 2) {
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (target == null || targetProfile == null) {
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.TARGET-OFFLINE").replace("%target%", args[1]));
            return;
        }

        ProfileStatus profileStatus = targetProfile.getStatus();
        switch (profileStatus) {
            case MATCH:
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
                if (match == null) return;

                match.removePlayer(target, false);

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.MATCH.PLAYER").replace("%target%", target.getName()));
                target.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.MATCH.TARGET").replace("%player%", LanguageManager.getString("CONSOLE-NAME")));
                match.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.MATCH.MATCH").replace("%target%", target.getName()), true);
                break;
            case EVENT:
                Event event = EventManager.getInstance().getEventByPlayer(target);
                if (event == null) return;

                event.removePlayer(target, false);

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.EVENT.PLAYER").replace("%target%", target.getName()));
                target.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.EVENT.TARGET").replace("%player%", LanguageManager.getString("CONSOLE-NAME")));
                event.sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.EVENT.EVENT").replace("%target%", target.getName()), true);
                break;
            case SPECTATE:
                Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(target);
                if (spectatable != null) {
                    spectatable.removeSpectator(target);

                    Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.SPECTATE.PLAYER").replace("%target%", target.getName()));
                    Common.sendMMMessage(target, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.STOP.SPECTATE.TARGET").replace("%player%", LanguageManager.getString("CONSOLE-NAME")));
                }
                break;
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (!player.hasPermission("zpp.staffmode.stop")) return arguments;

        if (args.length == 2) {
            for (Player online : Bukkit.getOnlinePlayers())
                arguments.add(online.getName());

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
