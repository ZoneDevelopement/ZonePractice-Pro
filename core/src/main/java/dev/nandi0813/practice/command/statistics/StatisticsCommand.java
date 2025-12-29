package dev.nandi0813.practice.command.statistics;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.guis.leaderboard.LbSelectorGui;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!player.hasPermission("zpp.admin")) {
            switch (profile.getStatus()) {
                case MATCH:
                case FFA:
                case EVENT:
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STATISTICS.CANT-USE"));
                    return false;
            }
        }

        if (args.length == 0) {
            if (!player.hasPermission("zpp.statistics")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STATISTICS.NO-PERMISSION"));
                return false;
            }

            new LbSelectorGui(player, profile).open(player);
        } else if (args.length == 1) {
            if (!player.hasPermission("zpp.statistics.other")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STATISTICS.NO-PERMISSION"));
                return false;
            }

            Profile target = ProfileManager.getInstance().getProfile(ServerManager.getInstance().getOfflinePlayers().get(args[0]));
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STATISTICS.TARGET-NOT-EXISTS").replaceAll("%target%", args[0]));
                return false;
            }

            new LbSelectorGui(player, target).open(player);
        } else {
            for (String line : LanguageManager.getList("COMMAND.STATISTICS.COMMAND-HELP"))
                Common.sendMMMessage(player, line.replaceAll("%label%", label));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();

        if (!(sender instanceof Player player)) return arguments;

        if (!player.hasPermission("zpp.statistics.other")) return arguments;

        if (args.length == 1) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
                if (player != online && onlineProfile.isHideFromPlayers()) continue;

                arguments.add(online.getName());
            }

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        }

        Collections.sort(completion);
        return completion;
    }

}
