package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum Exp {
    ;

    public static void run(Player player, String label, String[] args) {
        Profile target = ProfileManager.getInstance().getProfile(Bukkit.getPlayer(args[2]));

        if (args.length == 3 && args[1].equalsIgnoreCase("reset")) // /prac exp reset <player>
        {
            if (!player.hasPermission("zpp.practice.exp.reset")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            if (get(player, args, target)) return;

            target.getStats().setExperience(0);
            target.getStats().setDivision(DivisionManager.getInstance().getDivision(target));

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.TARGET-RESET")
                    .replace("%target%", target.getPlayer().getName())
            );
        } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) // /prac exp add <player> <number>
        {
            if (!player.hasPermission("zpp.practice.exp.add")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            if (get(player, args, target)) return;

            final int extraExp = Integer.parseInt(args[3]);
            if (extraExp <= 0) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.INVALID-NUMBER"));
                return;
            }

            target.getStats().setExperience(target.getStats().getExperience() + extraExp);
            target.getStats().setDivision(DivisionManager.getInstance().getDivision(target));

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.ADD-EXTRA-EXP")
                    .replace("%extraExp%", String.valueOf(extraExp))
                    .replace("%target%", target.getPlayer().getName())
                    .replace("%newExp%", String.valueOf(target.getStats().getExperience()))
            );
        } else if (args[1].equalsIgnoreCase("set")) // /prac exp set <player> <number>
        {
            if (!player.hasPermission("zpp.practice.exp.set")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return;
            }

            if (get(player, args, target)) return;

            final int setExp = Integer.parseInt(args[3]);
            if (setExp <= 0) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.INVALID-NUMBER"));
                return;
            }

            target.getStats().setExperience(setExp);
            target.getStats().setDivision(DivisionManager.getInstance().getDivision(target));

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.SET-EXP")
                    .replace("%newExp%", String.valueOf(setExp))
                    .replace("%target%", target.getPlayer().getName())
            );
        } else {
            if (player.hasPermission("zpp.practice.exp.reset") || player.hasPermission("zpp.practice.exp.set") || player.hasPermission("zpp.practice.exp.add")) {
                for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.COMMAND-HELP"))
                    Common.sendMMMessage(player, line.replace("%label%", label));
            }
        }
    }

    private static boolean get(Player player, String[] args, Profile target) {
        if (target == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.TARGET-NOT-FOUND").replace("%target%", args[2]));
            return true;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile != target && target.getPlayer().isOp() && ConfigManager.getBoolean("ADMIN-SETTINGS.OP-BYPASS-EXP-CHANGE")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.TARGET-OP").replace("%target%", args[2]));
            return true;
        }
        return false;
    }

    public static void run(String label, String[] args) {
        Profile target = ProfileManager.getInstance().getProfile(Bukkit.getPlayer(args[2]));

        if (args.length == 3 && args[1].equalsIgnoreCase("reset")) // /prac exp reset <player>
        {
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.TARGET-NOT-FOUND").replace("%target%", args[2]));
                return;
            }

            target.getStats().setExperience(0);
            target.getStats().setDivision(DivisionManager.getInstance().getDivision(target));

            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.TARGET-RESET")
                    .replace("%target%", target.getPlayer().getName())
            );
        } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) // /prac exp add <player> <number>
        {
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.TARGET-NOT-FOUND").replace("%target%", args[2]));
                return;
            }

            final int extraExp = Integer.parseInt(args[3]);
            if (extraExp <= 0) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.INVALID-NUMBER"));
                return;
            }

            target.getStats().setExperience(target.getStats().getExperience() + extraExp);
            target.getStats().setDivision(DivisionManager.getInstance().getDivision(target));

            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.ADD-EXTRA-EXP")
                    .replace("%extraExp%", String.valueOf(extraExp))
                    .replace("%target%", target.getPlayer().getName())
                    .replace("%newExp%", String.valueOf(target.getStats().getExperience()))
            );
        } else if (args[1].equalsIgnoreCase("set")) // /prac exp set <player> <number>
        {
            if (target == null) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.TARGET-NOT-FOUND").replace("%target%", args[2]));
                return;
            }

            final int setExp = Integer.parseInt(args[3]);
            if (setExp <= 0) {
                Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.INVALID-NUMBER"));
                return;
            }

            target.getStats().setExperience(setExp);
            target.getStats().setDivision(DivisionManager.getInstance().getDivision(target));

            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.SET-EXP")
                    .replace("%newExp%", String.valueOf(setExp))
                    .replace("%target%", target.getPlayer().getName())
            );
        } else {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.EXPERIENCE.COMMAND-HELP"))
                Common.sendConsoleMMMessage(line.replace("%label%", label));
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (args.length == 2) {
            if (player.hasPermission("zpp.practice.exp.reset")) arguments.add("reset");
            if (player.hasPermission("zpp.practice.exp.add")) arguments.add("add");
            if (player.hasPermission("zpp.practice.exp.set")) arguments.add("set");

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        } else if (args.length == 3) {
            if (player.hasPermission("zpp.practice.exp.reset") ||
                    player.hasPermission("zpp.practice.exp.add") ||
                    player.hasPermission("zpp.practice.exp.set")) {
                for (Player online : Bukkit.getOnlinePlayers())
                    arguments.add(online.getName());
            }

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
