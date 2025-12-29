package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum RankedArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("zpp.practice.ranked.default")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.TARGET-NOT-FOUND").replaceAll("%target%", args[2]));
                return;
            }

            if (player != target.getPlayer() && target.getPlayer().isOp() && ConfigManager.getBoolean("ADMIN-SETTINGS.OP-BYPASS-RANKED-CHANGE")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.TARGET-OP"));
                return;
            }

            final Group group = target.getGroup();
            if (group != null) {
                target.setRankedLeft(group.getRankedLimit());

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.RESET-DAILY-LIMIT")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%dailyRankedLimit%", String.valueOf(group.getRankedLimit()))
                );
            } else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.NO-GROUP").replaceAll("%target%", target.getPlayer().getName()));
        } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
            if (!player.hasPermission("zpp.practice.ranked.add")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.TARGET-NOT-FOUND").replaceAll("%target%", args[2]));
                return;
            }

            if (player != target.getPlayer() && target.getPlayer().isOp() && ConfigManager.getBoolean("ADMIN-SETTINGS.OP-BYPASS-RANKED-CHANGE")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.TARGET-OP"));
                return;
            }

            final int extraRanked = Integer.parseInt(args[3]);
            if (extraRanked <= 0) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.INVALID-NUMBER"));
                return;
            }

            target.setRankedLeft(target.getRankedLeft() + extraRanked);

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.ADD-EXTRA-RANKED")
                    .replaceAll("%extraRanked%", String.valueOf(extraRanked))
                    .replaceAll("%target%", target.getPlayer().getName())
                    .replaceAll("%newRanked%", String.valueOf(target.getRankedLeft()))
            );
        } else if (args.length >= 3 && args[1].equalsIgnoreCase("ban")) {
            if (!player.hasPermission("zpp.practice.ranked.ban")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.TARGET-NOT-FOUND").replaceAll("%target%", args[2]));
                return;
            }

            if (target.getPlayer().isOp()) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.CANT-BAN-PLAYER").replaceAll("%target%", target.getPlayer().getName()));
                return;
            }

            String reason = null;
            if (args.length > 3) {
                StringBuilder reasonBuilder = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    reasonBuilder.append(args[i]);
                    if (i + 1 != args.length)
                        reasonBuilder.append(" ");
                }
                reason = reasonBuilder.toString();
            }

            if (target.getRankedBan().ban(ProfileManager.getInstance().getProfile(player), reason)) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.BAN.BANNED-FROM-RANKED")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%reason%", reason == null ? LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.BAN.NO-REASON") : reason));

                if (target.getPlayer().isOnline()) {
                    Common.sendMMMessage(target.getPlayer().getPlayer(), LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.BAN.BANNED-FROM-RANKED-PLAYER")
                            .replaceAll("%banner%", player.getName())
                            .replaceAll("%reason%", reason == null ? LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.BAN.NO-REASON") : reason)
                    );
                }
            } else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.BAN.PLAYER-ALREADY-BANNED").replaceAll("%target%", target.getPlayer().getName()));
        } else if (args.length == 3 && args[1].equalsIgnoreCase("unban")) {
            if (!player.hasPermission("zpp.practice.ranked.unban")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.TARGET-NOT-FOUND").replaceAll("%target%", args[2]));
                return;
            }

            if (target.getRankedBan().unban()) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNBAN.UNBANNED-FROM-RANKED").replaceAll("%target%", target.getPlayer().getName()));
                if (target.getPlayer().isOnline()) {
                    Common.sendMMMessage(target.getPlayer().getPlayer(), LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNBAN.UNBANNED-FROM-RANKED-PLAYER")
                            .replaceAll("%unbanner%", player.getName()));
                }
            } else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.UNBAN.PLAYER-NOT-BANNED").replaceAll("%target%", target.getPlayer().getName()));
        } else {
            if (player.hasPermission("zpp.practice.ranked.default") || player.hasPermission("zpp.practice.ranked.add")) {
                for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.RANKED.COMMAND-HELP"))
                    Common.sendMMMessage(player, line.replaceAll("%label%", label));
            }
        }
    }

    public static void run(String label, String[] args) {
        if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.TARGET-NOT-FOUND").replaceAll("%target%", args[2]));
                return;
            }

            final Group group = target.getGroup();
            if (group != null) {
                target.setRankedLeft(group.getRankedLimit());

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.RESET-DAILY-LIMIT")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%dailyRankedLimit%", String.valueOf(group.getRankedLimit())));
            } else
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.NO-GROUP").replaceAll("%target%", target.getPlayer().getName()));
        } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
            final Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.TARGET-NOT-FOUND").replaceAll("%target%", args[2]));
                return;
            }

            final int extraRanked = Integer.parseInt(args[3]);
            if (extraRanked <= 0) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.INVALID-NUMBER"));
                return;
            }

            target.setRankedLeft(target.getRankedLeft() + extraRanked);

            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RANKED.ADD-EXTRA-RANKED")
                    .replaceAll("%extraRanked%", String.valueOf(extraRanked))
                    .replaceAll("%target%", target.getPlayer().getName())
                    .replaceAll("%newRanked%", String.valueOf(target.getRankedLeft())));
        } else {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.RANKED.COMMAND-HELP"))
                Common.sendConsoleMMMessage(line.replaceAll("%label%", label));
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (args.length == 2) {
            if (player.hasPermission("zpp.practice.ranked.default")) arguments.add("reset");
            if (player.hasPermission("zpp.practice.ranked.add")) arguments.add("add");
            if (player.hasPermission("zpp.practice.ranked.ban")) arguments.add("ban");
            if (player.hasPermission("zpp.practice.ranked.unban")) arguments.add("unban");

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        } else if (args.length == 3) {
            if (player.hasPermission("zpp.practice.ranked.default") ||
                    player.hasPermission("zpp.practice.ranked.add") ||
                    player.hasPermission("zpp.practice.ranked.ban") ||
                    player.hasPermission("zpp.practice.ranked.unban")) {
                for (Player online : Bukkit.getOnlinePlayers())
                    arguments.add(online.getName());
            }

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
