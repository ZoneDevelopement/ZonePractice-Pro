package dev.nandi0813.practice.command.arena.arguments;

import dev.nandi0813.practice.command.arena.arguments.Set.IconArg;
import dev.nandi0813.practice.command.arena.arguments.Set.PortalProtArg;
import dev.nandi0813.practice.command.arena.arguments.Set.SideBuildLimitArg;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SetArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (args.length > 1) {
            switch (args[1]) {
                case "icon":
                    IconArg.run(player, label, args);
                    break;
                case "portalprot":
                    PortalProtArg.run(player, label, args);
                    break;
                case "sidebuildlimit":
                    SideBuildLimitArg.run(player, label, args);
                    break;
            }
        } else
            HelpArg.run_setCommand(player, label);
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 2) {
            arguments.add("icon");
            arguments.add("portalprot");
            arguments.add("sidebuildlimit");

            StringUtil.copyPartialMatches(args[1], arguments, completion);
        } else if (args.length > 2) {
            completion = switch (args[1]) {
                case "icon" -> IconArg.tabComplete(player, args);
                case "portalprot" -> PortalProtArg.tabComplete(player, args);
                case "sidebuildlimit" -> SideBuildLimitArg.tabComplete(player, args);
                default -> completion;
            };
        }

        Collections.sort(completion);
        return completion;
    }

}
