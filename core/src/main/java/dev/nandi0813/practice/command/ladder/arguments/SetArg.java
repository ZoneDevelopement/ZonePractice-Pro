package dev.nandi0813.practice.command.ladder.arguments;

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
                case "effect":
                    EffectArg.run(player, label, args);
                    break;
                case "icon":
                    IconArg.run(player, label, args);
                    break;
                case "inventory":
                case "inv":
                    InventoryArg.run(player, label, args);
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
            arguments.add("effect");
            arguments.add("icon");
            arguments.add("inventory");

            StringUtil.copyPartialMatches(args[1], arguments, completion);
        } else if (args.length > 2) {
            switch (args[1]) {
                case "effect":
                    completion = EffectArg.tabComplete(player, args);
                    break;
                case "icon":
                    completion = IconArg.tabComplete(player, args);
                    break;
                case "inventory":
                case "inv":
                    completion = InventoryArg.tabComplete(player, args);
                    break;
            }
        }

        Collections.sort(completion);
        return completion;
    }

}
