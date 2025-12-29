package dev.nandi0813.practice.command.ladder;

import dev.nandi0813.practice.command.ladder.arguments.*;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LadderCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        if (args.length > 0) {
            switch (args[0]) {
                case "info":
                    InfoArg.run(player, label, args);
                    break;
                case "create":
                    CreateArg.run(player, label, args);
                    break;
                case "delete":
                    DeleteArg.run(player, label, args);
                    break;
                case "set":
                    SetArg.run(player, label, args);
                    break;
                case "freeze":
                    FreezeArg.run(player, label, args);
                    break;
                case "stop":
                    StopArg.run(player, label, args);
                    break;
                default:
                    HelpArg.run(player, label);
                    break;
            }
        } else
            HelpArg.run(player, label);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();
        if (!(sender instanceof Player player)) return arguments;

        if (args.length == 1) {
            if (player.hasPermission("zpp.setup")) {
                arguments.add("info");
                arguments.add("create");
                arguments.add("delete");
                arguments.add("set");
            }

            if (player.hasPermission("zpp.ladder.stop")) arguments.add("stop");
            if (player.hasPermission("zpp.ladder.freeze")) arguments.add("freeze");

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        } else {
            switch (args[0]) {
                case "delete":
                    completion = DeleteArg.tabComplete(player, args);
                    break;
                case "freeze":
                    completion = FreezeArg.tabComplete(player, args);
                    break;
                case "stop":
                    completion = StopArg.tabComplete(player, args);
                    break;
                case "info":
                    completion = InfoArg.tabComplete(player, args);
                    break;
                case "set":
                    completion = SetArg.tabComplete(player, args);
                    break;
            }
        }

        Collections.sort(completion);
        return completion;
    }

}
