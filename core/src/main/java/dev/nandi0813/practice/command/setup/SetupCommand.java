package dev.nandi0813.practice.command.setup;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.server.ServerHubGui;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
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

public class SetupCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.NO-PERMISSION"));
            return false;
        }

        if (args.length == 0) {
            GUIManager.getInstance().searchGUI(GUIType.Setup_Hub).open(player);
        } else if (args.length == 1) {
            switch (args[0]) {
                case "arena":
                    GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).open(player);
                    break;
                case "ladder":
                    GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).open(player);
                    break;
                case "hologram":
                    GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).open(player);
                    break;
                case "event":
                    GUIManager.getInstance().searchGUI(GUIType.Event_Summary).open(player);
                    break;
                case "server":
                    new ServerHubGui().open(player);
                    break;
                case "on":
                    InventoryManager.getInstance().getSetupModePlayers().add(player);
                    InventoryManager.getInstance().setInventory(player, null);
                    player.setGameMode(org.bukkit.GameMode.CREATIVE);
                    player.setFlying(true);
                    break;
                case "off":
                    InventoryManager.getInstance().getSetupModePlayers().remove(player);
                    InventoryManager.getInstance().setInventory(player, Inventory.InventoryType.LOBBY);
                    break;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();
        if (!(sender instanceof Player player)) return arguments;

        if (args.length == 1) {
            if (player.hasPermission("zpp.setup")) {
                arguments.add("arena");
                arguments.add("ladder");
                arguments.add("hologram");
                arguments.add("event");
                arguments.add("server");
                arguments.add("on");
                arguments.add("off");
            }

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        }

        Collections.sort(completion);
        return completion;
    }

}
