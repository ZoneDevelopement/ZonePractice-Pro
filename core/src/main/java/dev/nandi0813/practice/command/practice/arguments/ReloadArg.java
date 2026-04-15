package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum ReloadArg {
    ;

    private static String message(String path, String fallback) {
        String value = LanguageManager.getString(path);
        return value != null ? value : fallback;
    }

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.practice.reload")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
            return;
        }

        if (args.length != 1) {
            Common.sendMMMessage(player, message("COMMAND.PRACTICE.ARGUMENTS.RELOAD.COMMAND-HELP", "<red>/%label% reload").replace("%label%", label));
            return;
        }

        if (ServerManager.getInstance().reloadFiles()) {
            Common.sendMMMessage(player, message("COMMAND.PRACTICE.ARGUMENTS.RELOAD.RELOADED", "<green>Practice config files have been reloaded."));
            return;
        }

        Common.sendMMMessage(player, message("COMMAND.PRACTICE.ARGUMENTS.RELOAD.FAILED", "<red>Reload failed. Check console for details."));
    }

    public static void run(String label, String[] args) {
        if (args.length != 1) {
            Common.sendConsoleMMMessage(message("COMMAND.PRACTICE.ARGUMENTS.RELOAD.COMMAND-HELP", "<red>/%label% reload").replace("%label%", label));
            return;
        }

        if (ServerManager.getInstance().reloadFiles()) {
            Common.sendConsoleMMMessage(message("COMMAND.PRACTICE.ARGUMENTS.RELOAD.RELOADED", "<green>Practice config files have been reloaded."));
            return;
        }

        Common.sendConsoleMMMessage(message("COMMAND.PRACTICE.ARGUMENTS.RELOAD.FAILED", "<red>Reload failed. Check console for details."));
    }
}


