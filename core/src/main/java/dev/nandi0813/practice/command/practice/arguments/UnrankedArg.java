package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum UnrankedArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("zpp.practice.unranked.default")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.TARGET-OFFLINE").replaceAll("%target%", args[2]));
                return;
            }

            if (player != target.getPlayer() && target.getPlayer().isOp() && ConfigManager.getBoolean("ADMIN-SETTINGS.OP-BYPASS-UNRANKED-CHANGE")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.TARGET-OP"));
                return;
            }

            final Group group = target.getGroup();
            if (group != null) {
                target.setUnrankedLeft(group.getUnrankedLimit());

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.RESET-DAILY-LIMIT")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%dailyUnrankedLimit%", String.valueOf(group.getUnrankedLimit()))
                );
            } else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.NO-GROUP").replaceAll("%target%", target.getPlayer().getName()));
        } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
            if (!player.hasPermission("zpp.practice.unranked.add")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.TARGET-OFFLINE"));
                return;
            }

            if (player != target.getPlayer() && target.getPlayer().isOp() && ConfigManager.getBoolean("ADMIN-SETTINGS.OP-BYPASS-UNRANKED-CHANGE")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.TARGET-OP"));
                return;
            }

            final int extraUnranked = Integer.parseInt(args[3]);
            if (extraUnranked <= 0) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.INVALID-NUMBER"));
                return;
            }

            target.setUnrankedLeft(target.getUnrankedLeft() + extraUnranked);

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.ADD-EXTRA-UNRANKED")
                    .replaceAll("%extraUnranked%", String.valueOf(extraUnranked))
                    .replaceAll("%target%", target.getPlayer().getName())
                    .replaceAll("%newUnranked%", String.valueOf(target.getUnrankedLeft()))
            );
        } else {
            if (player.hasPermission("zpp.practice.unranked.default") || player.hasPermission("zpp.practice.unranked.add")) {
                for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.COMMAND-HELP"))
                    Common.sendMMMessage(player, line.replaceAll("%label%", label));
            }
        }
    }

    public static void run(String label, String[] args) {
        if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.TARGET-OFFLINE"));
                return;
            }

            final Group group = target.getGroup();
            if (group != null) {
                target.setUnrankedLeft(group.getUnrankedLimit());

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.RESET-DAILY-LIMIT")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%dailyUnrankedLimit%", String.valueOf(group.getUnrankedLimit())));
            } else
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.NO-GROUP").replaceAll("%target%", target.getPlayer().getName()));
        } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.TARGET-OFFLINE"));
                return;
            }

            final int extraUnranked = Integer.parseInt(args[3]);
            if (extraUnranked <= 0) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.INVALID-NUMBER"));
                return;
            }

            target.setUnrankedLeft(target.getUnrankedLeft() + extraUnranked);

            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.ADD-EXTRA-UNRANKED")
                    .replaceAll("%extraUnranked%", String.valueOf(extraUnranked))
                    .replaceAll("%target%", target.getPlayer().getName())
                    .replaceAll("%newUnranked%", String.valueOf(target.getUnrankedLeft())));
        } else {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.UNRANKED.COMMAND-HELP"))
                Common.sendConsoleMMMessage(line.replaceAll("%label%", label));
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (args.length == 2) {
            if (player.hasPermission("zpp.practice.unranked.default")) arguments.add("reset");
            if (player.hasPermission("zpp.practice.unranked.add")) arguments.add("add");

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        } else if (args.length == 3) {
            if (player.hasPermission("zpp.practice.unranked.default") || player.hasPermission("zpp.practice.unranked.add")) {
                for (Player online : Bukkit.getOnlinePlayers())
                    arguments.add(online.getName());
            }
        }

        return arguments;
    }

}
