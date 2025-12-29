package dev.nandi0813.practice.command.event;

import dev.nandi0813.practice.command.event.arguments.Events.*;
import dev.nandi0813.practice.command.event.arguments.HelpArg;
import dev.nandi0813.practice.command.event.arguments.HostArg;
import dev.nandi0813.practice.command.event.arguments.JoinArg;
import dev.nandi0813.practice.command.event.arguments.StopArg;
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

public class EventCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        if (args.length >= 1) {
            switch (args[0]) {
                case "host":
                    HostArg.run(player, label, args);
                    break;
                case "join":
                    JoinArg.run(player, label, args);
                    break;
                case "stop":
                    StopArg.run(player, label, args);
                    break;
                case "lms":
                    LMSArg.run(player, label, args);
                    break;
                case "oitc":
                    OITCArg.run(player, label, args);
                    break;
                case "tnttag":
                    TNTTagArg.run(player, label, args);
                    break;
                case "brackets":
                    BracketsArg.run(player, label, args);
                    break;
                case "sumo":
                    SumoArg.run(player, label, args);
                    break;
                case "splegg":
                    SpleggArg.run(player, label, args);
                    break;
                case "jn":
                case "juggernaut":
                    JuggernautArg.run(player, label, args);
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
            if (player.hasPermission("zpp.event.host")) arguments.add("host");
            if (player.hasPermission("zpp.event.join")) arguments.add("join");
            if (player.hasPermission("zpp.event.stop.collecting") || player.hasPermission("zpp.event.stop.live"))
                arguments.add("stop");
            if (player.hasPermission("zpp.setup")) {
                arguments.add("brackets");
                arguments.add("juggernaut");
                arguments.add("lms");
                arguments.add("oitc");
                arguments.add("splegg");
                arguments.add("sumo");
                arguments.add("tnttag");
            }

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        } else {
            completion = switch (args[0]) {
                case "stop" -> StopArg.tabComplete(player, args);
                case "brackets" -> BracketsArg.tabComplete(player, args);
                case "jn", "juggernaut" -> JuggernautArg.tabComplete(player, args);
                case "lms" -> LMSArg.tabComplete(player, args);
                case "oitc" -> OITCArg.tabComplete(player, args);
                case "splegg" -> SpleggArg.tabComplete(player, args);
                case "sumo" -> SumoArg.tabComplete(player, args);
                case "tnttag" -> TNTTagArg.tabComplete(player, args);
                default -> completion;
            };
        }

        Collections.sort(completion);
        return completion;
    }

}
