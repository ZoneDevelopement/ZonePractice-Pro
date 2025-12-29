package dev.nandi0813.practice.command.staff;

import dev.nandi0813.practice.command.staff.arguments.*;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaffCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {

            if (args.length > 0) {
                switch (args[0]) {
                    case "chat":
                        ChatArg.run(player, args);
                        break;
                    case "enable":
                        EnableArg.run(player, label, args);
                        break;
                    case "follow":
                        FollowArg.run(player, label, args);
                        break;
                    case "forceend":
                        ForceEndArg.run(player, label, args);
                        break;
                    case "stop":
                        StopArg.run(player, label, args);
                        break;
                    case "vanish":
                        VanishArg.run(player, label, args);
                        break;
                    default:
                        HelpArg.run(player, label);
                }
            } else
                HelpArg.run(player, label);
        } else if (sender instanceof ConsoleCommandSender) {
            if (args.length > 0) {
                switch (args[0]) {
                    case "stop":
                        StopArg.run(label, args);
                    case "forceend":
                        ForceEndArg.run(label, args);
                    case "chat":
                        ChatArg.run(args);
                    default:
                        HelpArg.run(label);
                }
            } else
                HelpArg.run(label);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();
        if (!(sender instanceof Player player)) return arguments;

        if (args.length == 1) {
            if (player.hasPermission("zpp.staffmode.chat") && ConfigManager.getBoolean("chat.staff-chat.enabled"))
                arguments.add("chat");
            if (player.hasPermission("zpp.staff")) {
                arguments.add("enable");
                arguments.add("vanish");
            }
            if (player.hasPermission("zpp.staffmode.forceend")) arguments.add("forceend");
            if (player.hasPermission("zpp.staffmode.stop")) arguments.add("stop");

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        } else {
            switch (args[0]) {
                case "forceend":
                    ForceEndArg.tabComplete(player, args);
                    break;
                case "stop":
                    StopArg.tabComplete(player, args);
                    break;
            }
        }

        Collections.sort(completion);
        return completion;
    }

}
