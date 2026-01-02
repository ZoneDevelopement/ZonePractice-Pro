package dev.nandi0813.practice.command.arena.arguments.Set;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum FfaPositionArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.NO-PERMISSION"));
            return;
        }

        if (args.length != 4 || (!args[3].equalsIgnoreCase("add") && !args[3].equalsIgnoreCase("reset"))) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        DisplayArena arena = ArenaManager.getInstance().getArena(args[2]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.ARENA-NOT-EXISTS").replace("%arena%", args[2]));
            return;
        }

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.CANT-EDIT").replace("%arena%", arena.getName()));
            return;
        }

        if (arena instanceof Arena && ((Arena) arena).hasCopies()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.CANT-EDIT2").replace("%arena%", arena.getName()));
            return;
        }

        switch (args[3].toLowerCase()) {
            case "add":
                if (arena.getCuboid() == null) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.NO-REGION").replace("%arena%", arena.getName()));
                    return;
                }

                Location position = player.getLocation();
                if (!arena.getCuboid().contains(position)) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.POS-OUTSIDE-REGION").replace("%arena%", arena.getName()));
                    return;
                }

                if (arena.getFfaPositions().size() >= 18) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.MAX-18").replaceAll("%arena%", arena.getName()));
                    return;
                }

                arena.getFfaPositions().add(position);
                ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.SET-FFAPOS").replaceAll("%arena%", arena.getName()).replaceAll("%posCount%", String.valueOf(arena.getFfaPositions().size())));
                break;
            case "reset":
                arena.getFfaPositions().clear();
                ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.RESET-FFAPOS").replaceAll("%arena%", arena.getName()));
                break;
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 3) {
            for (DisplayArena arena : ArenaManager.getInstance().getArenaList())
                arguments.add(arena.getName());

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
