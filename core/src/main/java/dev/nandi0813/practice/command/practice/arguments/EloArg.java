package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum EloArg {
    ;

    public static void run(Player player, String label, String[] args) {
        Profile target = ProfileManager.getInstance().getProfile(Bukkit.getPlayer(args[2]));

        if (args.length == 4 && args[1].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("zpp.practice.elo.default")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.TARGET-OFFLINE").replaceAll("%target%", args[2]));
                return;
            }

            if (player != target.getPlayer().getPlayer() && target.getPlayer().isOp() && ConfigManager.getBoolean("ADMIN-SETTINGS.OP-BYPASS-ELO-CHANGE")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.TARGET-OP").replaceAll("%target%", target.getPlayer().getName()));
                return;
            }

            int defaultElo = LadderManager.getDEFAULT_ELO();
            if (!args[3].equalsIgnoreCase("*")) {
                NormalLadder ladder = LadderManager.getInstance().getLadder(args[3]);
                if (ladder == null) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.LADDER-NOT-EXISTS").replaceAll("%ladder%", args[3]));
                    return;
                }

                target.getStats().getLadderStat(ladder).setElo(defaultElo);
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.SPECIFIC-LADDER-ELO-RESET")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%ladder%", ladder.getDisplayName())
                        .replaceAll("%defaultElo%", String.valueOf(defaultElo))
                );
            } else {
                for (NormalLadder ladder : LadderManager.getInstance().getLadders())
                    if (ladder.isRanked())
                        target.getStats().getLadderStat(ladder).setElo(defaultElo);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.ALL-LADDER-ELO-RESET")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%defaultElo%", String.valueOf(defaultElo))
                );
            }
        } else if (args.length == 5 && args[1].equalsIgnoreCase("set")) {
            if (!player.hasPermission("zpp.practice.elo.specific")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.TARGET-OFFLINE").replaceAll("%target%", args[2]));
                return;
            }

            if (player != target.getPlayer().getPlayer() && target.getPlayer().isOp() && ConfigManager.getBoolean("ADMIN-SETTINGS.OP-BYPASS-ELO-CHANGE")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.TARGET-OP").replaceAll("%target%", target.getPlayer().getName()));
                return;
            }

            int newElo = Integer.parseInt(args[4]);
            if (newElo < 0) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.INVALID-NUMBER"));
                return;
            }

            if (!args[3].equalsIgnoreCase("*")) {
                NormalLadder ladder = LadderManager.getInstance().getLadder(args[3]);
                if (ladder == null) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.LADDER-NOT-EXISTS").replaceAll("%ladder%", args[3]));
                    return;
                }

                target.getStats().getLadderStat(ladder).setElo(newElo);
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.SPECIFIC-LADDER-ELO-SET")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%ladder%", ladder.getDisplayName())
                        .replaceAll("%newElo%", String.valueOf(newElo))
                );
            } else {
                for (NormalLadder ladder : LadderManager.getInstance().getLadders())
                    if (ladder.isRanked())
                        target.getStats().getLadderStat(ladder).setElo(newElo);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.ALL-LADDER-ELO-SET")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%newElo%", String.valueOf(newElo))
                );
            }
        } else {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.ELO.COMMAND-HELP"))
                Common.sendMMMessage(player, line.replaceAll("%label%", label));
        }
    }

    public static void run(String label, String[] args) {
        if (args.length == 4 && args[1].equalsIgnoreCase("reset")) {
            Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.TARGET-OFFLINE").replaceAll("%target%", args[2]));
                return;
            }

            int defaultElo = LadderManager.getDEFAULT_ELO();

            if (!args[3].equalsIgnoreCase("*")) {
                NormalLadder ladder = LadderManager.getInstance().getLadder(args[3]);
                if (ladder == null) {
                    Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.LADDER-NOT-EXISTS").replaceAll("%ladder%", args[3]));
                    return;
                }

                target.getStats().getLadderStat(ladder).setElo(defaultElo);
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.SPECIFIC-LADDER-ELO-RESET")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%ladder%", ladder.getDisplayName())
                        .replaceAll("%defaultElo%", String.valueOf(defaultElo)));
            } else {
                for (NormalLadder ladder : LadderManager.getInstance().getLadders())
                    if (ladder.isRanked())
                        target.getStats().getLadderStat(ladder).setElo(defaultElo);

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.ALL-LADDER-ELO-RESET")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%defaultElo%", String.valueOf(defaultElo)));
            }
        } else if (args.length == 5 && args[1].equalsIgnoreCase("set")) {
            Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[2]));
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.TARGET-OFFLINE").replaceAll("%target%", args[2]));
                return;
            }

            int newElo = Integer.parseInt(args[4]);
            if (newElo < 0) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.INVALID-NUMBER"));
                return;
            }

            if (!args[3].equalsIgnoreCase("*")) {
                NormalLadder ladder = LadderManager.getInstance().getLadder(args[3]);
                if (ladder == null) {
                    Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.LADDER-NOT-EXISTS").replaceAll("%ladder%", args[3]));
                    return;
                }

                target.getStats().getLadderStat(ladder).setElo(newElo);
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.SPECIFIC-LADDER-ELO-SET")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%ladder%", ladder.getDisplayName())
                        .replaceAll("%newElo%", String.valueOf(newElo)));
            } else {
                for (NormalLadder ladder : LadderManager.getInstance().getLadders())
                    if (ladder.isRanked())
                        target.getStats().getLadderStat(ladder).setElo(newElo);

                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.ELO.ALL-LADDER-ELO-SET")
                        .replaceAll("%target%", target.getPlayer().getName())
                        .replaceAll("%newElo%", String.valueOf(newElo)));
            }
        } else {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.ELO.COMMAND-HELP"))
                Common.sendConsoleMMMessage(line.replaceAll("%label%", label));
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (args.length == 2) {
            if (player.hasPermission("zpp.practice.elo.default")) arguments.add("reset");
            if (player.hasPermission("zpp.practice.elo.specific")) arguments.add("set");

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        } else if (args.length == 3) {
            if (player.hasPermission("zpp.practice.elo.default") && args[1].equalsIgnoreCase("reset")) {
                for (Player online : Bukkit.getOnlinePlayers())
                    arguments.add(online.getName());
            } else if (player.hasPermission("zpp.practice.elo.specific") && args[1].equalsIgnoreCase("set")) {
                for (Player online : Bukkit.getOnlinePlayers())
                    arguments.add(online.getName());
            }

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        } else if (args.length == 4) {
            if (player.hasPermission("zpp.practice.elo.default") && args[1].equalsIgnoreCase("reset")) {
                for (Ladder ladder : LadderManager.getInstance().getLadders())
                    arguments.add(ladder.getName());

                arguments.add("*");
            } else if (player.hasPermission("zpp.practice.elo.specific") && args[1].equalsIgnoreCase("set")) {
                for (Ladder ladder : LadderManager.getInstance().getLadders())
                    arguments.add(ladder.getName());

                arguments.add("*");
            }

            return StringUtil.copyPartialMatches(args[3], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
