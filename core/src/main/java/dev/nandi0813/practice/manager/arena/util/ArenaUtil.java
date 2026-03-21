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
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.util.BasicItem;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.NumberUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum ArenaUtil {
    ;

    public static World createEmptyWorld(String worldName) {
        WorldCreator wc = new WorldCreator(worldName);
        wc.type(WorldType.FLAT);
        wc.generateStructures(false);
        wc.generatorSettings("{\"layers\": [{\"block\": \"air\", \"height\": 1}, {\"block\": \"air\", \"height\": 1}], \"biome\":\"plains\"}");
        return wc.createWorld();
    }

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
                if (arena.getAssignedLadders().isEmpty()) {
                    Common.sendMMMessage(player, "<red>Please assign at least one ladder to the arena!");
                    return false;
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
        world.setGameRule(GameRules.SPAWN_MOBS, false);
        world.setGameRule(GameRules.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, false);
        world.setGameRule(GameRules.RAIDS, false);
        world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
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
        return assignedLadders.contains(LadderType.BEDWARS)
                || assignedLadders.contains(LadderType.FIREBALL_FIGHT)
                || assignedLadders.contains(LadderType.MLG_RUSH);
    }

    public static boolean isArenaPortalRelated(Arena arena) {
        Set<LadderType> assignedLadders = arena.getAssignedLadderTypes();
        return assignedLadders.contains(LadderType.BRIDGES) || assignedLadders.contains(LadderType.BATTLE_RUSH);
    }

        public static boolean turnsToDirt(Block block) {
        Material type = block.getType();
        return
                type.equals(Material.GRASS_BLOCK) ||
                        type.equals(Material.MYCELIUM) ||
                        type.equals(Material.DIRT_PATH) ||
                        type.equals(Material.FARMLAND) ||
                        type.equals(Material.WARPED_NYLIUM);
    }

    public static boolean containsDestroyableBlock(Ladder ladder, Block block) {
        if (!(ladder instanceof NormalLadder normalLadder)) return false;

        if (!ladder.isBuild()) return false;
        if (normalLadder.getDestroyableBlocks().isEmpty()) return false;
        if (block == null) return false;

        for (BasicItem basicItem : normalLadder.getDestroyableBlocks()) {
            if (block.getType().equals(basicItem.getMaterial()))
                return true;
        }
        return false;
    }

    public static boolean requiresSupport(Block block) {
        Material type = block.getType();
        return org.bukkit.Tag.FLOWERS.isTagged(type)
                || org.bukkit.Tag.SAPLINGS.isTagged(type)
                || org.bukkit.Tag.CROPS.isTagged(type)
                || org.bukkit.Tag.WALL_POST_OVERRIDE.isTagged(type)   // torches, signs on walls, etc.
                || type == Material.DEAD_BUSH
                || type == Material.SHORT_GRASS
                || type == Material.TALL_GRASS
                || type == Material.FERN
                || type == Material.LARGE_FERN
                || type == Material.VINE
                || type == Material.SUGAR_CANE
                || type == Material.CACTUS
                || type == Material.SNOW
                || type == Material.TORCH
                || type == Material.SOUL_TORCH
                || type == Material.REDSTONE_WIRE
                || type == Material.REDSTONE_TORCH
                || type == Material.LEVER
                || type == Material.COMPARATOR
                || type == Material.REPEATER
                || type == Material.TRIPWIRE_HOOK
                || type == Material.TRIPWIRE
                || type == Material.LILY_PAD
                || type == Material.NETHER_WART;
    }

    public static void loadArenaChunks(BasicArena arena) {
        if (arena.getCuboid() == null) return;
        org.bukkit.World world = arena.getCuboid().getWorld();
        if (world == null) return;

        // Calculate chunk coordinate range directly from the cuboid bounds
        // instead of calling getChunks() which synchronously loads all chunks.
        int minCX = arena.getCuboid().getLowerX() >> 4;
        int maxCX = arena.getCuboid().getUpperX() >> 4;
        int minCZ = arena.getCuboid().getLowerZ() >> 4;
        int maxCZ = arena.getCuboid().getUpperZ() >> 4;

        org.bukkit.plugin.Plugin plugin = dev.nandi0813.practice.ZonePractice.getInstance();
        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                // addPluginChunkTicket loads the chunk asynchronously if needed
                // and prevents it from being unloaded — no main-thread stall.
                world.addPluginChunkTicket(cx, cz, plugin);
            }
        }
    }

    public static void setMannequinItemInHand(Mannequin mannequin, ItemStack item, boolean rightHand) {
        if (mannequin == null) return;
        if (mannequin.getEquipment() == null) return;

        if (rightHand) {
            mannequin.getEquipment().setItemInMainHand(item);
        } else {
            mannequin.getEquipment().setItemInOffHand(item);
        }
    }

    public static void setMannequinInvulnerable(Mannequin mannequin) {
        if (mannequin == null) return;
        mannequin.setInvulnerable(true);
        // Keep setup markers non-persistent so they do not survive server restarts.
        mannequin.setPersistent(false);
    }

}
