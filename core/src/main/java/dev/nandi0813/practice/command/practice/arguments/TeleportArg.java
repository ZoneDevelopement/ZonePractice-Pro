package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum TeleportArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
            return;
        }

        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.TELEPORT.COMMAND-HELP").replace("%label%", label));
            return;
        }

        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.TELEPORT.WORLD-DONT-EXISTS").replace("%world%", args[1]));
            return;
        }

        player.teleport(world.getSpawnLocation());
        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.TELEPORT.TELEPORTED").replace("%world%", world.getName()));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (args.length == 2) {
            if (player.hasPermission("zpp.setup")) {
                for (World world : Bukkit.getWorlds())
                    arguments.add(world.getName());
            }

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
