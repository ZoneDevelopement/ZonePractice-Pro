package dev.nandi0813.practice.command.arena.arguments.Set;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.util.PortalLocation;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSetupManager;
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

public enum PortalArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.NO-PERMISSION"));
            return;
        }

        if (args.length != 4) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Arena arena = ArenaManager.getInstance().getNormalArena(args[2]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.ARENA-NOT-EXISTS").replace("%arena%", args[2]));
            return;
        }

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.ARENA-ENABLED").replace("%arena%", arena.getName()));
            return;
        }

        if (!arena.isBuild()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.NOT-BUILD").replace("%arena%", arena.getName()));
            return;
        }

        if (arena.getCuboid() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.NO-REGION").replace("%arena%", arena.getName()));
            return;
        }

        Block block = player.getTargetBlock((Set<Material>) null, 5);
        if (block == null || block.getType().equals(Material.AIR)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.NO-BLOCK").replace("%arena%", arena.getName()));
            return;
        }

        Location portalLoc = block.getLocation();
        if (!arena.getCuboid().contains(portalLoc)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.NOT-IN-REGION").replace("%arena%", arena.getName()));
            return;
        }

        int number = Integer.parseInt(args[3]);
        if (number != 1 && number != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.INVALID-NUMBER").replace("%arena%", arena.getName()));
            return;
        }

        PortalLocation portalLocation = new PortalLocation(portalLoc);
        if (number == 1) {
            if (arena.getPortalLoc2() != null && arena.getPortalLoc2().isOverlap(portalLocation)) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.PORTAL-OVERLAP").replace("%arena%", arena.getName()));
                return;
            }

            arena.setPortalLoc1(portalLocation);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.SET-PORTAL1").replace("%arena%", arena.getName()));
        } else {
            if (arena.getPortalLoc1() != null && arena.getPortalLoc1().isOverlap(portalLocation)) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.PORTAL-OVERLAP").replace("%arena%", arena.getName()));
                return;
            }

            arena.setPortalLoc2(portalLocation);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.SET-PORTAL2").replace("%arena%", arena.getName()));
        }
        portalLocation.setPortal();

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
