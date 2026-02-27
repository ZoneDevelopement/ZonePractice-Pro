package dev.nandi0813.practice.manager.arena.util;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.ArenaCopy;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.arena.setup.ArenaSetupManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum ArenaUtil {
    ;

    public static Arena getArena(BasicArena arena) {
        if (arena instanceof ArenaCopy)
            return ((ArenaCopy) arena).getMainArena();
        else if (arena instanceof Arena)
            return (Arena) arena;
        return null;
    }

    public static String convertLocation(final Location location) {
        if (location != null) {
            return LanguageManager.getString("ARENA.LOCATION-CONVERSION.CONVERSION")
                    .replace("%x%", String.valueOf(NumberUtil.doubleToInt(Math.ceil(location.getX()))))
                    .replace("%y%", String.valueOf(NumberUtil.doubleToInt(Math.ceil(location.getY()))))
                    .replace("%z%", String.valueOf(NumberUtil.doubleToInt(Math.ceil(location.getZ()))));
        }
        return LanguageManager.getString("ARENA.LOCATION-CONVERSION.LOCATION-NULL");
    }

    public static String convertLocation(final PortalLocation portalLocation) {
        if (portalLocation != null && portalLocation.getCenter() != null) {
            return LanguageManager.getString("ARENA.LOCATION-CONVERSION.CONVERSION")
                    .replace("%x%", String.valueOf(NumberUtil.doubleToInt(Math.ceil(portalLocation.getCenter().getX()))))
                    .replace("%y%", String.valueOf(NumberUtil.doubleToInt(Math.ceil(portalLocation.getCenter().getY()))))
                    .replace("%z%", String.valueOf(NumberUtil.doubleToInt(Math.ceil(portalLocation.getCenter().getZ()))));
        }
        return LanguageManager.getString("ARENA.LOCATION-CONVERSION.LOCATION-NULL");
    }

    public static List<String> getLadderTypeNames(Arena arena) {
        List<String> names = new ArrayList<>();
        for (LadderType ladderType : arena.getAssignedLadderTypes())
            names.add(ladderType.name());
        return names;
    }

    public static List<String> getLadderNames(DisplayArena arena) {
        List<String> ladderStrings = new ArrayList<>();
        for (Ladder ladder : arena.getAssignedLadders())
            ladderStrings.add(ladder.getName());
        return ladderStrings;
    }

    public static boolean changeStatus(Player player, DisplayArena arena) {
        boolean returnVal = true;

        if (!arena.isEnabled()) {
            if (arena.getIcon() == null || arena.getIcon().getType().equals(Material.AIR)) {
                Common.sendMMMessage(player, LanguageManager.getString("ARENA.STATUS-CHANGE.NO-ICON"));
                returnVal = false;
            }

            if (arena.getCuboid() == null) {
                Common.sendMMMessage(player, LanguageManager.getString("ARENA.STATUS-CHANGE.NO-CUBOID"));
                returnVal = false;
            }

//            if (arena.getAssignedLadders().isEmpty()) {
//                Common.sendMMMessage(player, LanguageManager.getString("ARENA.STATUS-CHANGE.NO-LADDER-ASSIGNED"));
//                returnVal = false;
//            }

            if (arena instanceof Arena) {
                if (isArenaBedRelated((Arena) arena) && !arena.isBedSet()) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.STATUS-CHANGE.NO-BED"));
                    returnVal = false;
                }

                if (isArenaPortalRelated((Arena) arena) && !arena.isPortalSet()) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.STATUS-CHANGE.NO-PORTAL"));
                    returnVal = false;
                }

                if (arena.getPosition1() == null || arena.getPosition2() == null) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.STATUS-CHANGE.NO-SPAWN-POSITIONS"));
                    returnVal = false;
                }
            } else if (arena instanceof FFAArena) {
                if (arena.getFfaPositions().isEmpty()) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.STATUS-CHANGE.NO-FFA-POSITIONS"));
                    returnVal = false;
                }
            }
        } else {
            if (!MatchManager.getInstance().getLiveMatchesByArena(arena).isEmpty()) {
                Common.sendMMMessage(player, LanguageManager.getString("ARENA.STATUS-CHANGE.CANT-DISABLE1"));
                returnVal = false;
            } else if (arena.isBuild()) {
                if (arena instanceof Arena && ((Arena) arena).isCopying()) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.STATUS-CHANGE.CANT-DISABLE2"));
                    returnVal = false;
                }
            }
        }

        if (!returnVal) {
            return false;
        }

        if (!arena.isEnabled()) {
            ArenaSetupManager setupManager = ArenaSetupManager.getInstance();

            List<Player> editors = new ArrayList<>(setupManager.getPlayersSettingUpArena(arena));
            for (Player editor : editors) {
                setupManager.stopSetup(editor);
                editor.sendMessage(Common.colorize("&cSetup mode force ended because the arena has been &aENABLED&c!"));
            }
        }

        if (arena instanceof FFAArena) {
            FFA ffa = ((FFAArena) arena).getFfa();

            if (ffa != null) {
                if (arena.isEnabled()) {
                    ffa.close("");
                } else {
                    ffa.open();
                }
            }
        }

        arena.setEnabled(!arena.isEnabled());
        return true;
    }

    public static void setGamerules(World world) {
        world.setSpawnLocation(0, 60, 0);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("showDeathMessages", "false");
        world.setGameRuleValue("doFireTick", "false");
    }

    public static void saveBedData(final YamlConfiguration config, final String path, final BedLocation bedLocation) {
        config.set(path + ".world", bedLocation.getWorld().getName());
        config.set(path + ".x", bedLocation.getX());
        config.set(path + ".y", bedLocation.getY());
        config.set(path + ".z", bedLocation.getZ());
        config.set(path + ".face", bedLocation.getFacing().name());
    }

    public static BedLocation getBedData(final YamlConfiguration config, final String path) {
        return new BedLocation(
                Bukkit.getWorld(config.getString(path + ".world")),
                config.getInt(path + ".x"),
                config.getInt(path + ".y"),
                config.getInt(path + ".z"),
                BlockFace.valueOf(config.getString(path + ".face"))
        );
    }

    public static boolean isArenaBedRelated(Arena arena) {
        Set<LadderType> assignedLadders = arena.getAssignedLadderTypes();
        return assignedLadders.contains(LadderType.BEDWARS) || assignedLadders.contains(LadderType.FIREBALL_FIGHT);
    }

    public static boolean isArenaPortalRelated(Arena arena) {
        Set<LadderType> assignedLadders = arena.getAssignedLadderTypes();
        return assignedLadders.contains(LadderType.BRIDGES) || assignedLadders.contains(LadderType.BATTLE_RUSH);
    }

}
