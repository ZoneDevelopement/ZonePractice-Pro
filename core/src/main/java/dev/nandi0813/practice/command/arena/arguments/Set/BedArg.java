package dev.nandi0813.practice.command.arena.arguments.Set;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.util.BedLocation;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSetupManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public enum BedArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.NO-PERMISSION"));
            return;
        }

        if (args.length != 4) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Arena arena = ArenaManager.getInstance().getNormalArena(args[2]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.ARENA-NOT-EXISTS").replace("%arena%", args[2]));
            return;
        }

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.ARENA-ENABLED").replace("%arena%", arena.getName()));
            return;
        }

        if (!arena.isBuild()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.NOT-BUILD").replace("%arena%", arena.getName()));
            return;
        }

        if (arena.getCuboid() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.NO-REGION").replace("%arena%", arena.getName()));
            return;
        }

        Block block = player.getTargetBlock((Set<Material>) null, 5);
        if (block == null || !block.getType().toString().contains("_BED") && !block.getType().toString().contains("BED_")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.NO-BED").replace("%arena%", arena.getName()));
            return;
        }

        Location bedLoc = block.getLocation();
        if (!arena.getCuboid().contains(bedLoc)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.NOT-IN-REGION").replace("%arena%", arena.getName()));
            return;
        }

        int number = Integer.parseInt(args[3]);
        if (number != 1 && number != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.INVALID-NUMBER").replace("%arena%", arena.getName()));
            return;
        }

        BedLocation bedLocation = ClassImport.getClasses().getBedUtil().getBedLocation(block);
        if (number == 1) {
            arena.setBedLoc1(bedLocation);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.SET-BED1").replace("%arena%", arena.getName()));
        } else {
            arena.setBedLoc2(bedLocation);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.SET-BED2").replace("%arena%", arena.getName()));
        }

        ArenaSetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();
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
