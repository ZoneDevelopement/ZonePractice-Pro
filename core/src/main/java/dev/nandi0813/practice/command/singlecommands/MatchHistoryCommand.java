package dev.nandi0813.practice.command.singlecommands;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
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

        if (!player.hasPermission("practice.matchhistory")) {
            Common.sendMMMessage(player, "<red>You don't have permission to use this command.");
            return true;
        }

        if (!ConfigManager.getBoolean("MATCH-HISTORY.ENABLED")) {
            Common.sendMMMessage(player, "<red>Match history is currently disabled.");
            return true;
        }

        // Determine target player
        final UUID targetUuid;
        final String targetName;

        if (args.length == 0) {
            // Show sender's own history
            targetUuid = player.getUniqueId();
            targetName = player.getName();
        } else {
            // Try to find the named player (online or offline)
            @SuppressWarnings("deprecation")
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[0]);

            if (!offlineTarget.hasPlayedBefore() && !offlineTarget.isOnline()) {
                String msg = ConfigManager.getString("MATCH-HISTORY.MESSAGES.PLAYER-NOT-FOUND");
                if (msg == null || msg.isEmpty()) msg = "&cPlayer not found.";
                Common.sendMMMessage(player, msg);
                return true;
            }

            targetUuid = offlineTarget.getUniqueId();
            targetName = offlineTarget.getName() != null ? offlineTarget.getName() : args[0];
        }

        // Load history async, then open GUI on main thread
        MatchHistoryManager.getInstance().loadHistoryAsync(targetUuid).thenAccept(entries -> {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
                if (!player.isOnline()) return;

                if (entries == null || entries.isEmpty()) {
                    String msg = ConfigManager.getString("MATCH-HISTORY.MESSAGES.NO-HISTORY");
                    if (msg == null || msg.isEmpty()) msg = "&cThis player has no match history.";
                    Common.sendMMMessage(player, msg);
                    return;
                }

                // Open the history GUI from the viewer's perspective
                List<MatchHistoryEntry> display = entries.size() > 5 ? entries.subList(0, 5) : entries;
                MatchHistoryGui gui = new MatchHistoryGui(targetUuid, targetName, display);
                gui.open(player);
            });
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(partial)) {
                    completions.add(online.getName());
                }
            }
        }
        return completions;
    }
}
