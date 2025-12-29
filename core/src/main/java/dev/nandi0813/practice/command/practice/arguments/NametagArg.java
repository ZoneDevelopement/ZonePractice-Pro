package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.inventory.InventoryUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum NametagArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!ConfigManager.getBoolean("PLAYER.LOBBY-NAMETAG.ENABLED")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.NAMETAG-DISABLED"));
            return;
        }

        if (args.length >= 4 && (args[1].equalsIgnoreCase("prefix") || args[1].equalsIgnoreCase("suffix"))) {
            if (!player.hasPermission("zpp.practice.nametag.set")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-OFFLINE").replaceAll("%target%", args[2]));
                return;
            }

            if (player != target && target.hasPermission("zpp.bypass.nametag")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-BYPASS").replaceAll("%target%", target.getName()));
                return;
            }

            Profile targetProfile = ProfileManager.getInstance().getProfile(target);
            if (args[1].equalsIgnoreCase("prefix")) {
                StringBuilder message = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    message.append(args[i]);
                    if (i + 1 != args.length)
                        message.append(" ");
                }
                String prefix = message.toString();

                if (prefix.length() > 16)
                    prefix = prefix.substring(0, prefix.length() - (prefix.length() - 16));
                targetProfile.setPrefix(Component.text(prefix));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PREFIX-SET")
                        .replaceAll("%target%", target.getName())
                        .replaceAll("%prefix%", Common.mmToNormal(message.toString())));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-PREFIX-SET")
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%prefix%", Common.mmToNormal(message.toString())));
            } else if (args[1].equalsIgnoreCase("suffix")) {
                StringBuilder message = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    if (i != 3)
                        message.append(" ");
                    message.append(args[i]);
                }
                String suffix = message.toString();

                if (suffix.length() > 16)
                    suffix = suffix.substring(0, suffix.length() - (suffix.length() - 16));

                targetProfile.setSuffix(Component.text(suffix));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.SUFFIX-SET")
                        .replaceAll("%target%", target.getName())
                        .replaceAll("%suffix%", Common.mmToNormal(message.toString())));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-SUFFIX-SET")
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%suffix%", Common.mmToNormal(message.toString())));
            }
        } else if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("zpp.practice.nametag.reset")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-OFFLINE").replaceAll("%target%", args[2]));
                return;
            }
            Profile targetProfile = ProfileManager.getInstance().getProfile(target);

            if (player != target && target.hasPermission("zpp.bypass.nametag")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-BYPASS").replaceAll("%target%", target.getName()));
                return;
            }

            targetProfile.setPrefix(null);
            targetProfile.setSuffix(null);

            InventoryUtil.setLobbyNametag(target, targetProfile);

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.RELOADED").replaceAll("%target%", target.getName()));
            Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.RELOADED").replaceAll("%player%", player.getName()));
        } else {
            if (player.hasPermission("zpp.practice.nametag.set") || player.hasPermission("zpp.practice.nametag.reset"))
                for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.COMMAND-HELP"))
                    Common.sendMMMessage(player, line.replaceAll("%label%", label));
            else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
        }
    }

    public static void run(String label, String[] args) {
        if (!ConfigManager.getBoolean("PLAYER.LOBBY-NAMETAG.ENABLED")) {
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.NAMETAG-DISABLED"));
            return;
        }

        if (args.length >= 4 && (args[1].equalsIgnoreCase("prefix") || args[1].equalsIgnoreCase("suffix"))) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-OFFLINE").replaceAll("%target%", args[2]));
                return;
            }

            Profile targetProfile = ProfileManager.getInstance().getProfile(target);
            if (args[1].equalsIgnoreCase("prefix")) {
                StringBuilder message = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    message.append(args[i]);
                    if (i + 1 != args.length)
                        message.append(" ");
                }
                String prefix = message.toString();

                if (prefix.length() > 16)
                    prefix = prefix.substring(0, prefix.length() - (prefix.length() - 16));
                targetProfile.setPrefix(Component.text(prefix));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PREFIX-SET")
                        .replaceAll("%target%", target.getName())
                        .replaceAll("%prefix%", Common.mmToNormal(message.toString())));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-PREFIX-SET")
                        .replaceAll("%player%", LanguageManager.getString("CONSOLE-NAME"))
                        .replaceAll("%prefix%", Common.mmToNormal(message.toString())));
            } else if (args[1].equalsIgnoreCase("suffix")) {
                StringBuilder message = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    if (i != 3)
                        message.append(" ");
                    message.append(args[i]);
                }
                String suffix = message.toString();

                if (suffix.length() > 16)
                    suffix = suffix.substring(0, suffix.length() - (suffix.length() - 16));

                targetProfile.setSuffix(Component.text(suffix));
                InventoryUtil.setLobbyNametag(target, targetProfile);

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.SUFFIX-SET")
                        .replaceAll("%target%", target.getName())
                        .replaceAll("%suffix%", Common.mmToNormal(message.toString())));

                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.PLAYER-SUFFIX-SET")
                        .replaceAll("%player%", LanguageManager.getString("CONSOLE-NAME"))
                        .replaceAll("%suffix%", Common.mmToNormal(message.toString())));
            }
        } else if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.TARGET-OFFLINE").replaceAll("%target%", args[2]));
                return;
            }
            Profile targetProfile = ProfileManager.getInstance().getProfile(target);

            targetProfile.setPrefix(null);
            targetProfile.setSuffix(null);

            InventoryUtil.setLobbyNametag(target, targetProfile);

            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.RELOADED").replaceAll("%target%", target.getName()));
            Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.RELOADED").replaceAll("%player%", LanguageManager.getString("CONSOLE-NAME")));
        } else {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.COMMAND-HELP"))
                Common.sendConsoleMMMessage(line.replaceAll("%label%", label));
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
            }

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        } else if (args.length == 3) {
            if ((args[1].equalsIgnoreCase("prefix") || args[1].equalsIgnoreCase("suffix")) && player.hasPermission("zpp.practice.nametag.set")) {
                for (Player online : Bukkit.getOnlinePlayers())
                    arguments.add(online.getName());
            } else if (args[1].equalsIgnoreCase("reset") && player.hasPermission("zpp.practice.nametag.reset")) {
                for (Player online : Bukkit.getOnlinePlayers())
                    arguments.add(online.getName());
            }

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
