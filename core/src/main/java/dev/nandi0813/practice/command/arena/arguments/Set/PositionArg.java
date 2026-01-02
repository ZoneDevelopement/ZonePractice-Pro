package dev.nandi0813.practice.command.arena.arguments.Set;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PositionArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.no-permission"));
            return;
        }

        if (args.length != 4) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.COMMAND-HELP").replace("%label%", label).replace("%label2%", args[1]));
            return;
        }

        Arena arena = ArenaManager.getInstance().getNormalArena(args[2]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.ARENA-NOT-EXISTS").replace("%arena%", args[2]));
            return;
        }

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.CANT-EDIT2").replace("%arena%", arena.getName()));
            return;
        }

        if (arena.hasCopies()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.CANT-EDIT").replace("%arena%", arena.getName()));
            return;
        }

        if (arena.getCuboid() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.NO-REGION").replace("%arena%", arena.getName()));
            return;
        }

        Location position = player.getLocation();
        if (!arena.getCuboid().contains(position)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.POS-OUTSIDE-REGION").replace("%arena%", arena.getName()));
            return;
        }

        if (arena.isDeadZone() && (position.getY() - 1) <= arena.getDeadZoneValue()) {
            arena.setDeadZone(false);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.DEADZONE-TO-HIGH").replace("%arena%", arena.getName()));
            return;
        }

        int positionNumber = Integer.parseInt(args[3]);
        if (positionNumber == 1) {
            arena.setPosition1(position);
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.SAVED-POSITION").replace("%arena%", arena.getName()).replace("%position%", "1."));
        } else if (positionNumber == 2) {
            arena.setPosition2(position);
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.SAVED-POSITION").replace("%arena%", arena.getName()).replace("%position%", "2."));
        } else
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.INVALID-NUMBER"));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 3) {
            for (Arena arena : ArenaManager.getInstance().getNormalArenas())
                arguments.add(arena.getName());

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        } else if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], Arrays.asList("1", "2"), new ArrayList<>());
        }

        return arguments;
    }

}
