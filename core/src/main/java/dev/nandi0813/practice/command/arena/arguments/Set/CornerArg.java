package dev.nandi0813.practice.command.arena.arguments.Set;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSetupManager;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public enum CornerArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.NO-PERMISSION"));
            return;
        }

        if (args.length != 4) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.COMMAND-HELP").replace("%label%", label));
            return;
        }

        DisplayArena arena = ArenaManager.getInstance().getArena(args[2]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.ARENA-NOT-EXISTS").replace("%arena%", args[2]));
            return;
        }

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.CANT-EDIT2").replace("%arena%", arena.getName()));
            return;
        }

        if (arena instanceof Arena && ((Arena) arena).hasCopies()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.CANT-EDIT").replace("%arena%", arena.getName()));
            return;
        }

        Block targetBlock = player.getTargetBlock((Set<Material>) null, 5);
        Location cornerLocation = targetBlock.getLocation();
        if (targetBlock.getType().equals(Material.AIR)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.NO-BLOCK"));
            return;
        }

        if (!cornerLocation.getWorld().equals(ArenaWorldUtil.getArenasWorld())) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.CORNER-WORLD"));
            return;
        }

        int corner = Integer.parseInt(args[3]);
        if (corner != 1 && corner != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.INVALID-NUMBER"));
            return;
        }

        if (corner == 1) {
            arena.setCorner1(cornerLocation);
            arena.createCuboid();

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.SAVED-CORNER").replace("%arena%", arena.getName()).replace("%corner%", "1."));
        } else {
            arena.setCorner2(cornerLocation);
            arena.createCuboid();

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.SAVED-CORNER").replace("%arena%", arena.getName()).replace("%corner%", "2."));
        }

        Cuboid cuboid = arena.getCuboid();
        if (cuboid == null) {
            ArenaSetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();
            return;
        }

        if (arena.getPosition1() != null && !cuboid.contains(arena.getPosition1())) {
            arena.setPosition1(null);
            arena.setEnabled(false);

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.POSITION-REMOVED").replace("%arena%", arena.getName()).replace("%position%", "1"));
        }
        if (arena.getPosition2() != null && !cuboid.contains(arena.getPosition2())) {
            arena.setPosition2(null);
            arena.setEnabled(false);

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.POSITION-REMOVED").replace("%arena%", arena.getName()).replace("%position%", "2"));
        }

        if (!arena.getFfaPositions().isEmpty()) {
            arena.getFfaPositions().removeIf(location -> !cuboid.contains(location));
        }

        if (arena.isBuildMax() && (cuboid.getLowerY() > arena.getBuildMaxValue() || arena.getBuildMaxValue() > cuboid.getUpperY())) {
            arena.setBuildMaxValue(ConfigManager.getInt("MATCH-SETTINGS.BUILD-LIMIT-DEFAULT"));
            arena.setBuildMax(false);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.BUILD-MAX-REMOVED").replaceAll("%arena%", arena.getName()));
        }

        if (arena.isDeadZone() && (cuboid.getLowerY() > arena.getDeadZoneValue() || arena.getDeadZoneValue() > cuboid.getUpperY())) {
            arena.setDeadZone(false);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.DEAD-ZONE-REMOVED").replaceAll("%arena%", arena.getName()));
        }

        if (arena instanceof Arena && ((Arena) arena).getAssignedLadderTypes().contains(LadderType.BEDWARS)) {
            if (arena.getBedLoc1() != null && !cuboid.contains(arena.getBedLoc1().getLocation())) {
                arena.setBedLoc1(null);
                arena.setEnabled(false);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.BED-REMOVED").replace("%arena%", arena.getName()).replace("%bed%", "1"));
            }

            if (arena.getBedLoc2() != null && !cuboid.contains(arena.getBedLoc2().getLocation())) {
                arena.setBedLoc2(null);
                arena.setEnabled(false);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.BED-REMOVED").replace("%arena%", arena.getName()).replace("%bed%", "2"));
            }
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 3) {
            for (DisplayArena arena : ArenaManager.getInstance().getArenaList())
                arguments.add(arena.getName());

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        } else if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], Arrays.asList("1", "2"), new ArrayList<>());
        }

        return arguments;
    }

}
