package dev.nandi0813.practice.command.ffa;

import dev.nandi0813.practice.command.ffa.arguments.*;
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

public class FFACommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        if (args.length > 0) {
            switch (args[0]) {
                case "join":
                    JoinArg.run(player, label, args);
                    break;
                case "leave":
                    LeaveArg.run(player);
                    break;
                case "spec":
                case "spectate":
                    SpectateArg.run(player, label, args);
                    break;
                case "list":
                    ListArg.run(player);
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
            arguments.add("join");
            arguments.add("leave");
            arguments.add("list");

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        } else {
            if (args[0].equalsIgnoreCase("join")) {
                completion = JoinArg.tabComplete(player, args);
            } else if (args[0].equalsIgnoreCase("spectate"))
                completion = SpectateArg.tabComplete(player, args);
        }

        Collections.sort(completion);
        return completion;
    }

}
