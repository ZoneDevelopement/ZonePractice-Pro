package dev.nandi0813.practice.command.arena.arguments.Set;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum DeadzoneArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.NO-PERMISSION"));
            return;
        }

        if (args.length != 3) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.COMMAND-HELP").replace("%label%", label));
            return;
        }

        DisplayArena arena = ArenaManager.getInstance().getArena(args[2]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.ARENA-NOT-EXISTS").replace("%arena%", args[2]));
            return;
        }

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.CANT-EDIT2").replace("%arena%", arena.getName()));
            return;
        }

        if (arena instanceof Arena && ((Arena) arena).hasCopies()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.CANT-EDIT").replace("%arena%", arena.getName()));
            return;
        }

        if (arena.getCuboid() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.NO-REGION").replace("%arena%", arena.getName()));
            return;
        }

        Location position = player.getLocation();
        if (!arena.getCuboid().contains(position)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.POS-OUTSIDE-REGION").replace("%arena%", arena.getName()));
            return;
        }
        int deadZoneY = position.getBlockY();

        List<Location> spawnPositions = new ArrayList<>();
        if (arena.getPosition1() != null)
            spawnPositions.add(arena.getPosition1());
        if (arena.getPosition2() != null)
            spawnPositions.add(arena.getPosition2());

        if (!arena.getFfaPositions().isEmpty())
            spawnPositions.addAll(arena.getFfaPositions());

        for (Location spawnPosition : spawnPositions) {
            if ((spawnPosition.getY() - 1) <= deadZoneY) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.LOWER-THAN-SPAWN"));
                return;
            }
        }

        arena.setDeadZoneValue(deadZoneY);
        arena.setDeadZone(true);
        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.SET-DEADZONE").replace("%arena%", arena.getName()).replace("%y-level%", String.valueOf(position.getBlockY())));
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
