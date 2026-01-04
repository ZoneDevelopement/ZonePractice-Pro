package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
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

public enum ResetArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.practice.reset")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
            return;
        }

        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RESET.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[1]));
        if (target == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RESET.TARGET-OFFLINE").replace("%target%", args[1]));
            return;
        }

        if (player != target.getPlayer() && target.getPlayer().isOp() && ConfigManager.getBoolean("ADMIN-SETTINGS.OP-BYPASS-FULL-RESET")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RESET.TARGET-OP").replace("%target%", target.getPlayer().getName()));
            return;
        }

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            target.getStats().loadDefaultStats(ladder);

            if (target.getUnrankedCustomKits().containsKey(ladder))
                target.getUnrankedCustomKits().get(ladder).clear();

            if (target.getRankedCustomKits().containsKey(ladder))
                target.getRankedCustomKits().get(ladder).clear();
        }
        target.getStats().setExperience(0);

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RESET.RESET-PLAYER").replace("%target%", target.getPlayer().getName()));
    }

    public static void run(String label, String[] args) {
        if (args.length != 2) {
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RESET.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[1]));
        if (target == null) {
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RESET.TARGET-OFFLINE").replace("%target%", args[1]));
            return;
        }

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            target.getStats().loadDefaultStats(ladder);
            if (target.getUnrankedCustomKits().containsKey(ladder))
                target.getUnrankedCustomKits().get(ladder).clear();
            if (target.getRankedCustomKits().containsKey(ladder))
                target.getRankedCustomKits().get(ladder).clear();
        }
        target.getStats().setExperience(0);

        Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RESET.RESET-PLAYER").replace("%target%", target.getPlayer().getName()));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (!player.hasPermission("zpp.practice.reset")) return arguments;

        if (args.length == 2) {
            for (Player online : Bukkit.getOnlinePlayers())
                arguments.add(online.getName());

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
