package dev.nandi0813.practice.command.arena.arguments;

import dev.nandi0813.practice.command.arena.arguments.Set.*;
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
                case "corner":
                    CornerArg.run(player, label, args);
                    break;
                case "pos":
                case "position":
                    PositionArg.run(player, label, args);
                    break;
                case "ffaposition":
                case "ffapositions":
                case "ffapos":
                    FfaPositionArg.run(player, label, args);
                    break;
                case "buildmax":
                    BuildmaxArg.run(player, label, args);
                    break;
                case "deadzone":
                    DeadzoneArg.run(player, label, args);
                    break;
                case "bed":
                    BedArg.run(player, label, args);
                    break;
                case "portal":
                    PortalArg.run(player, label, args);
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
            arguments.add("corner");
            arguments.add("position");
            arguments.add("ffapositions");
            arguments.add("buildmax");
            arguments.add("deadzone");
            arguments.add("bed");
            arguments.add("portal");
            arguments.add("portalprot");
            arguments.add("sidebuildlimit");

            StringUtil.copyPartialMatches(args[1], arguments, completion);
        } else if (args.length > 2) {
            switch (args[1]) {
                case "corner":
                    completion = CornerArg.tabComplete(player, args);
                    break;
                case "pos":
                case "position":
                    completion = PositionArg.tabComplete(player, args);
                    break;
                case "ffaposition":
                case "ffapositions":
                case "ffapos":
                    completion = FfaPositionArg.tabComplete(player, args);
                    break;
                case "buildmax":
                    completion = BuildmaxArg.tabComplete(player, args);
                    break;
                case "deadzone":
                    completion = DeadzoneArg.tabComplete(player, args);
                    break;
                case "icon":
                    completion = IconArg.tabComplete(player, args);
                    break;
                case "bed":
                    completion = BedArg.tabComplete(player, args);
                    break;
                case "portal":
                    completion = PortalArg.tabComplete(player, args);
                    break;
                case "portalprot":
                    completion = PortalProtArg.tabComplete(player, args);
                    break;
                case "sidebuildlimit":
                    completion = SideBuildLimitArg.tabComplete(player, args);
                    break;
            }
        }

        Collections.sort(completion);
        return completion;
    }

}
