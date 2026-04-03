package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.inventory.InventoryUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.NameFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum NametagArg {
    ;

    private static String joinArgs(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i != start) builder.append(" ");
            builder.append(args[i]);
        }
        return builder.toString();
    }

    public static void run(Player player, String label, String[] args) {
        if (!ConfigManager.getBoolean("PLAYER.LOBBY-NAMETAG.ENABLED")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.NAMETAG-DISABLED"));
            return;
        }

        if (args.length >= 4 && (args[1].equalsIgnoreCase("prefix") || args[1].equalsIgnoreCase("suffix") || args[1].equalsIgnoreCase("name"))) {
            if (!player.hasPermission("zpp.practice.nametag.set")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-OFFLINE").replace("%target%", args[2]));
                return;
            }

            if (player != target && target.hasPermission("zpp.bypass.nametag")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-BYPASS").replace("%target%", target.getName()));
                return;
            }

            Profile targetProfile = ProfileManager.getInstance().getProfile(target);
            if (args[1].equalsIgnoreCase("prefix")) {
                String prefix = joinArgs(args, 3);

                targetProfile.setPrefix(NameFormatUtil.parseConfiguredComponent(prefix));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PREFIX-SET")
                        .replace("%target%", target.getName())
                        .replace("%prefix%", Common.mmToNormal(prefix)));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-PREFIX-SET")
                        .replace("%player%", player.getName())
                        .replace("%prefix%", Common.mmToNormal(prefix)));
            } else if (args[1].equalsIgnoreCase("suffix")) {
                String suffix = joinArgs(args, 3);

                targetProfile.setSuffix(NameFormatUtil.parseConfiguredComponent(suffix));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.SUFFIX-SET")
                        .replace("%target%", target.getName())
                        .replace("%suffix%", Common.mmToNormal(suffix)));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-SUFFIX-SET")
                        .replace("%player%", player.getName())
                        .replace("%suffix%", Common.mmToNormal(suffix)));
            } else if (args[1].equalsIgnoreCase("name")) {
                String nameTemplate = joinArgs(args, 3);

                if (player != target && !player.hasPermission("zpp.practice.nametag.name.others")) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                    return;
                }

                targetProfile.setNameTemplate(NameFormatUtil.normalizePlayerNameTemplate(nameTemplate));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.NAME-SET")
                        .replace("%target%", target.getName())
                        .replace("%name%", Common.mmToNormal(nameTemplate)));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-NAME-SET")
                        .replace("%player%", player.getName())
                        .replace("%name%", Common.mmToNormal(nameTemplate)));
            }
        } else if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("zpp.practice.nametag.reset")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-OFFLINE").replace("%target%", args[2]));
                return;
            }
            Profile targetProfile = ProfileManager.getInstance().getProfile(target);

            if (player != target && target.hasPermission("zpp.bypass.nametag")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-BYPASS").replace("%target%", target.getName()));
                return;
            }

            targetProfile.setPrefix(null);
            targetProfile.setSuffix(null);
            targetProfile.setNameTemplate(null);

            InventoryUtil.setLobbyNametag(target, targetProfile);

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.RELOADED").replace("%target%", target.getName()));
        } else {
            if (player.hasPermission("zpp.practice.nametag.set") || player.hasPermission("zpp.practice.nametag.reset"))
                for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.COMMAND-HELP"))
                    Common.sendMMMessage(player, line.replace("%label%", label));
            else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
        }
    }

    public static void run(String label, String[] args) {
        if (!ConfigManager.getBoolean("PLAYER.LOBBY-NAMETAG.ENABLED")) {
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.NAMETAG-DISABLED"));
            return;
        }

        if (args.length >= 4 && (args[1].equalsIgnoreCase("prefix") || args[1].equalsIgnoreCase("suffix") || args[1].equalsIgnoreCase("name"))) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-OFFLINE").replace("%target%", args[2]));
                return;
            }

            Profile targetProfile = ProfileManager.getInstance().getProfile(target);
            if (args[1].equalsIgnoreCase("prefix")) {
                String prefix = joinArgs(args, 3);

                targetProfile.setPrefix(NameFormatUtil.parseConfiguredComponent(prefix));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PREFIX-SET")
                        .replace("%target%", target.getName())
                        .replace("%prefix%", Common.mmToNormal(prefix)));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-PREFIX-SET")
                        .replace("%player%", LanguageManager.getString("CONSOLE-NAME"))
                        .replace("%prefix%", Common.mmToNormal(prefix)));
            } else if (args[1].equalsIgnoreCase("suffix")) {
                String suffix = joinArgs(args, 3);

                targetProfile.setSuffix(NameFormatUtil.parseConfiguredComponent(suffix));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.SUFFIX-SET")
                        .replace("%target%", target.getName())
                        .replace("%suffix%", Common.mmToNormal(suffix)));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-SUFFIX-SET")
                        .replace("%player%", LanguageManager.getString("CONSOLE-NAME"))
                        .replace("%suffix%", Common.mmToNormal(suffix)));
            } else if (args[1].equalsIgnoreCase("name")) {
                String nameTemplate = joinArgs(args, 3);

                targetProfile.setNameTemplate(NameFormatUtil.normalizePlayerNameTemplate(nameTemplate));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.NAME-SET")
                        .replace("%target%", target.getName())
                        .replace("%name%", Common.mmToNormal(nameTemplate)));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-NAME-SET")
                        .replace("%player%", LanguageManager.getString("CONSOLE-NAME"))
                        .replace("%name%", Common.mmToNormal(nameTemplate)));
            }
        } else if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-OFFLINE").replace("%target%", args[2]));
                return;
            }
            Profile targetProfile = ProfileManager.getInstance().getProfile(target);

            targetProfile.setPrefix(null);
            targetProfile.setSuffix(null);
            targetProfile.setNameTemplate(null);

            InventoryUtil.setLobbyNametag(target, targetProfile);

            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.RELOADED").replace("%target%", target.getName()));
        } else {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.COMMAND-HELP"))
                Common.sendConsoleMMMessage(line.replace("%label%", label));
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (args.length == 2) {
            if (player.hasPermission("zpp.practice.nametag.reset"))
                arguments.add("reset");
            if (player.hasPermission("zpp.practice.nametag.set")) {
                arguments.add("prefix");
                arguments.add("suffix");
                arguments.add("name");
            }

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        } else if (args.length == 3) {
            if ((args[1].equalsIgnoreCase("prefix") || args[1].equalsIgnoreCase("suffix") || args[1].equalsIgnoreCase("name")) && player.hasPermission("zpp.practice.nametag.set")) {
                if (args[1].equalsIgnoreCase("name") && !player.hasPermission("zpp.practice.nametag.name.others")) {
                    arguments.add(player.getName());
                } else {
                    for (Player online : Bukkit.getOnlinePlayers())
                        arguments.add(online.getName());
                }
            } else if (args[1].equalsIgnoreCase("reset") && player.hasPermission("zpp.practice.nametag.reset")) {
                for (Player online : Bukkit.getOnlinePlayers())
                    arguments.add(online.getName());
            }

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
