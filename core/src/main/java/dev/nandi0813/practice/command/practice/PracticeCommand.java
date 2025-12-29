package dev.nandi0813.practice.command.practice;

import dev.nandi0813.practice.command.practice.arguments.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PracticeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {

            if (args.length > 0) {
                switch (args[0]) {
                    case "arenas":
                        ArenasArg.run(player);
                        break;
                    case "lobby":
                        LobbyArg.run(player, label, args);
                        break;
                    case "rename":
                        RenameArg.run(player, label, args);
                        break;
                    case "info":
                        InfoArg.run(player, label, args);
                        break;
                    case "goldenhead":
                        GoldenHeadArg.run(player, label, args);
                        break;
                    case "elo":
                        EloArg.run(player, label, args);
                        break;
                    case "ranked":
                        RankedArg.run(player, label, args);
                        break;
                    case "unranked":
                        UnrankedArg.run(player, label, args);
                        break;
                    case "exp":
                        Exp.run(player, label, args);
                        break;
                    case "reset":
                        ResetArg.run(player, label, args);
                        break;
                    case "nametag":
                        NametagArg.run(player, label, args);
                        break;
                    case "teleport":
                        TeleportArg.run(player, label, args);
                        break;
                    /*
                    case "test":
                        Profile profile = ProfileManager.getInstance().getProfile(player);
                        List<Player> players = new ArrayList<>();
                        players.add(player);
                        Duel duel = new Duel(
                                profile.getSelectedCustomLadder(),
                                profile.getSelectedCustomLadder().getAvailableArenas().get(0),
                                players,
                                false,
                                1
                        );
                        duel.startMatch();
                        break;
                     */
                    default:
                        HelpArg.run(player, label);
                        break;
                }
            } else
                HelpArg.run(player, label);
        } else if (sender instanceof ConsoleCommandSender) {
            if (args.length > 0) {
                switch (args[0]) {
                    case "elo":
                        EloArg.run(label, args);
                        break;
                    case "ranked":
                        RankedArg.run(label, args);
                        break;
                    case "unranked":
                        UnrankedArg.run(label, args);
                        break;
                    case "exp":
                        Exp.run(label, args);
                        break;
                    case "reset":
                        ResetArg.run(label, args);
                        break;
                    case "nametag":
                        NametagArg.run(label, args);
                        break;
                    default:
                        HelpArg.run(label);
                        break;
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
            if (player.hasPermission("zpp.practice.arenas"))
                arguments.add("arenas");
            if (player.hasPermission("zpp.practice.lobby"))
                arguments.add("lobby");
            if (player.hasPermission("zpp.practice.rename"))
                arguments.add("rename");
            if (player.hasPermission("zpp.practice.info"))
                arguments.add("info");
            if (player.hasPermission("zpp.setup"))
                arguments.add("goldenhead");
            if (player.hasPermission("zpp.practice.elo.default") || player.hasPermission("zpp.practice.elo.specific"))
                arguments.add("elo");
            if (player.hasPermission("zpp.practice.ranked.default") || player.hasPermission("zpp.practice.ranked.add"))
                arguments.add("ranked");
            if (player.hasPermission("zpp.practice.unranked.default") || player.hasPermission("zpp.practice.unranked.add"))
                arguments.add("unranked");
            if (player.hasPermission("zpp.practice.exp"))
                arguments.add("exp");
            if (player.hasPermission("zpp.practice.reset"))
                arguments.add("reset");
            if (player.hasPermission("zpp.practice.nametag.set") || player.hasPermission("zpp.practice.nametag.reset"))
                arguments.add("nametag");
            if (player.hasPermission("zpp.setup"))
                arguments.add("teleport");

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        } else {
            completion = switch (args[0]) {
                case "lobby" -> LobbyArg.tabComplete(player, args);
                case "info" -> InfoArg.tabComplete(player, args);
                case "elo" -> EloArg.tabComplete(player, args);
                case "ranked" -> RankedArg.tabComplete(player, args);
                case "unranked" -> UnrankedArg.tabComplete(player, args);
                case "exp" -> Exp.tabComplete(player, args);
                case "reset" -> ResetArg.tabComplete(player, args);
                case "nametag" -> NametagArg.tabComplete(player, args);
                case "teleport" -> TeleportArg.tabComplete(player, args);
                default -> completion;
            };
        }

        Collections.sort(completion);
        return completion;
    }

}
