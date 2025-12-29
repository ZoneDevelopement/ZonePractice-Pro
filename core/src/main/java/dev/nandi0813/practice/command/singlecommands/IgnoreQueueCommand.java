package dev.nandi0813.practice.command.singlecommands;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IgnoreQueueCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        if (!player.hasPermission("zpp.ignorequeue")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.IGNORE-QUEUE.NO-PERM"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.IGNORE-QUEUE.USE"));
            return false;
        }

        Profile target = ProfileManager.getInstance().getProfile(Bukkit.getPlayer(args[0]));
        if (target == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.IGNORE-QUEUE.PLAYER-OFFLINE").replaceAll("%target%", args[0]));
            return false;
        }

        if (profile == target) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.IGNORE-QUEUE.CANT-YOURSELF"));
            return false;
        }

        if (!profile.getIgnoredPlayers().contains(target)) {
            profile.getIgnoredPlayers().add(target);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.IGNORE-QUEUE.IGNORE-ON").replaceAll("%target%", args[0]));
        } else {
            profile.getIgnoredPlayers().remove(target);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.IGNORE-QUEUE.IGNORE-OFF").replaceAll("%target%", args[0]));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();

        if (!(sender instanceof Player player)) {
            return arguments;
        }

        if (!player.hasPermission("zpp.ignorequeue")) {
            return arguments;
        }

        if (args.length == 1) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                arguments.add(online.getName());
            }

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        }

        Collections.sort(completion);
        return completion;
    }
}
