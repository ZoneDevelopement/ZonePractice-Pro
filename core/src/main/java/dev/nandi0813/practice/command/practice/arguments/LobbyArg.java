package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum LobbyArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.practice.lobby")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
            return;
        }

        if (args.length == 1) {
            if (ServerManager.getLobby() != null) {
                Location lobbyLocation = ServerManager.getLobby();
                player.teleport(lobbyLocation);
            } else {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.LOBBY.LOBBY-NOT-SET").replaceAll("%label%", label));
            }
        } else if (args.length == 2 && args[1].equalsIgnoreCase("set")) {
            Location lobbyLocation = player.getLocation();

            if (!lobbyLocation.getWorld().equals(ArenaWorldUtil.getArenasWorld()) && !lobbyLocation.getWorld().equals(ArenaWorldUtil.getArenasCopyWorld())) {
                ServerManager.getInstance().setLobby(player, lobbyLocation);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.LOBBY.LOBBY-SET"));
            } else {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.LOBBY.NO-ARENAS-WORLD"));
            }
        } else if (args.length == 2 && args[1].equalsIgnoreCase("load")) {
            InventoryManager.getInstance().setLobbyInventory(player, ServerManager.getLobby() != null);
        } else {
            for (String line : LanguageManager.getList("COMMAND.PRACTICE.ARGUMENTS.LOBBY.COMMAND-HELP"))
                Common.sendMMMessage(player, line.replaceAll("%label%", label));
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (!player.hasPermission("zpp.practice.lobby")) return arguments;

        if (args.length == 2) {
            arguments.add("set");
            arguments.add("load");

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
