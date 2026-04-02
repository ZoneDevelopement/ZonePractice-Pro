package dev.nandi0813.practice.command.singlecommands;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.guis.MatchHistoryGui;
import dev.nandi0813.practice.manager.matchhistory.MatchHistoryEntry;
import dev.nandi0813.practice.manager.matchhistory.MatchHistoryManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchHistoryCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage("<red>This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("zpp.matchhistory")) {
            Common.sendMMMessage(player, LanguageManager.getString("NO-PERMISSION"));
            return true;
        }

        // Determine target
        final UUID targetUuid;
        final String targetName;

        if (args.length == 0) {
            targetUuid = player.getUniqueId();
            targetName = player.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[0]);
            if (!offlineTarget.hasPlayedBefore() && !offlineTarget.isOnline()) {
                Common.sendMMMessage(player,
                        LanguageManager.getString("COMMAND.MATCH-HISTORY.PLAYER-NOT-FOUND"));
                return true;
            }
            targetUuid = offlineTarget.getUniqueId();
            targetName = offlineTarget.getName() != null ? offlineTarget.getName() : args[0];
        }

        // Load async, open GUI on main thread
        MatchHistoryManager.getInstance().loadHistoryAsync(targetUuid).thenAccept(entries -> {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
                if (!player.isOnline()) return;

                if (entries == null || entries.isEmpty()) {
                    Common.sendMMMessage(player,
                            LanguageManager.getString("COMMAND.MATCH-HISTORY.NO-HISTORY"));
                    return;
                }

                List<MatchHistoryEntry> display = entries.size() > 5
                        ? entries.subList(0, 5)
                        : entries;

                new MatchHistoryGui(targetUuid, targetName, display).open(player);
            });
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(partial))
                    completions.add(online.getName());
            }
        }
        return completions;
    }
}
