package dev.nandi0813.practice.manager.arena.setup;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.arena.util.BedLocation;
import dev.nandi0813.practice.manager.arena.util.PortalLocation;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class ArenaSetupListener implements Listener {

    private final ArenaSetupManager setupManager;

    public ArenaSetupListener(ArenaSetupManager setupManager) {
        this.setupManager = setupManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!setupManager.isSettingUp(player) || !setupManager.isSetupWand(event.getItem())) {
            return;
        }

        event.setCancelled(true);

        ArenaSetupManager.SetupSession session = setupManager.getSession(player);
        DisplayArena displayArena = session.getArena();

        if (displayArena == null) {
            player.sendMessage(Common.colorize("&cArena not found!"));
            setupManager.stopSetup(player);
            return;
        }

        Action action = event.getAction();

        if (player.isSneaking()) {
            handleModeSwitch(player, session, displayArena, action);
            return;
        }

        SetupMode currentMode = session.getCurrentMode();

        boolean isAirAllowed = (
                currentMode == SetupMode.BUILD_MAX ||
                        currentMode == SetupMode.DEAD_ZONE ||
                        currentMode == SetupMode.TOGGLE_STATUS
        );

        if (!isAirAllowed) {
            if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;
        } else {
            if (action == Action.PHYSICAL) return;
        }

        switch (currentMode) {
            case CORNERS -> handleCornerSelection(player, displayArena, action, event);
            case POSITIONS -> handleStandardPositions(player, (Arena) displayArena, action, event);
            case FFA_POSITIONS -> handleFFAPositions(player, (FFAArena) displayArena, action, event);
            case BUILD_MAX -> handleBuildMax(player, displayArena, action);
            case DEAD_ZONE -> handleDeadZone(player, displayArena, action);
            case BED_LOCATIONS -> handleBedLocations(player, (Arena) displayArena, action, event);
            case PORTALS -> handlePortals(player, (Arena) displayArena, action, event);
            case TOGGLE_STATUS -> handleToggleStatus(player, displayArena, action);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (setupManager.isSettingUp(player)) {
            setupManager.stopSetup(player);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();

        if (!setupManager.isSettingUp(player)) {
            return;
        }

        if (setupManager.isSetupWand(e.getItemDrop().getItemStack())) {
            e.getItemDrop().remove();
            setupManager.stopSetup(player);
        }
    }

    private void handleModeSwitch(Player player, ArenaSetupManager.SetupSession session, DisplayArena arena, Action action) {
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            session.setCurrentMode(setupManager.getNextMode(arena, session.getCurrentMode()));
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            session.setCurrentMode(setupManager.getPreviousMode(arena, session.getCurrentMode()));
        } else {
            return;
        }
        setupManager.updateWand(player);
        player.sendMessage(Common.colorize("&eSwitched to: &f" + session.getCurrentMode().getDisplayName()));
    }

    private void handleStandardPositions(Player player, Arena arena, Action action, PlayerInteractEvent event) {
        if (isEditBlocked(player, arena)) return;
        if (isRegionMissing(player, arena)) return;

        Location loc = getSnappedLocation(event.getClickedBlock(), player);

        if (isOutsideRegion(player, arena, loc)) return;

        // DeadZone check
        if (arena.isDeadZone() && (loc.getY() - 1) <= arena.getDeadZoneValue()) {
            arena.setDeadZone(false);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.DEADZONE-TO-HIGH").replace("%arena%", arena.getName()));
        }

        if (action == Action.LEFT_CLICK_BLOCK) {
            arena.setPosition1(loc);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.SAVED-POSITION")
                    .replace("%arena%", arena.getName()).replace("%position%", "1."));
        } else {
            arena.setPosition2(loc);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.SAVED-POSITION")
                    .replace("%arena%", arena.getName()).replace("%position%", "2."));
        }
        updateGui(arena);
    }

    private void handleFFAPositions(Player player, FFAArena ffaArena, Action action, PlayerInteractEvent event) {
        if (isEditBlocked(player, ffaArena)) return;

        if (action == Action.RIGHT_CLICK_BLOCK) {
            // Add Spawn
            if (isRegionMissing(player, ffaArena)) return;

            Location loc = getSnappedLocation(event.getClickedBlock(), player);
            if (isOutsideRegion(player, ffaArena, loc)) return;

            if (ffaArena.getFfaPositions().size() >= 18) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.MAX-18").replaceAll("%arena%", ffaArena.getName()));
                return;
            }

            ffaArena.getFfaPositions().add(loc);
            updateGui(ffaArena);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.FFA-POSITIONS.SET-FFAPOS")
                    .replaceAll("%arena%", ffaArena.getName())
                    .replaceAll("%posCount%", String.valueOf(ffaArena.getFfaPositions().size())));

        } else {
            // Remove Spawn
            if (!ffaArena.getFfaPositions().isEmpty()) {
                int index = ffaArena.getFfaPositions().size() - 1;
                ffaArena.getFfaPositions().remove(index);
                updateGui(ffaArena);
                player.sendMessage(Common.colorize("&cRemoved last FFA spawn point. Remaining: " + index));
            } else {
                player.sendMessage(Common.colorize("&cNo spawn points to remove."));
            }
        }
    }

    private void handleCornerSelection(Player player, DisplayArena arena, Action action, PlayerInteractEvent event) {
        if (isEditBlocked(player, arena)) return;

        Block targetBlock = event.getClickedBlock();
        if (targetBlock == null || targetBlock.getType().equals(Material.AIR)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.NO-BLOCK"));
            return;
        }

        Location cornerLocation = targetBlock.getLocation();
        if (!cornerLocation.getWorld().equals(ArenaWorldUtil.getArenasWorld())) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.CORNER-WORLD"));
            return;
        }

        int cornerId = (action == Action.LEFT_CLICK_BLOCK) ? 1 : 2;
        if (cornerId == 1) arena.setCorner1(cornerLocation);
        else arena.setCorner2(cornerLocation);

        arena.createCuboid();
        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.SAVED-CORNER")
                .replace("%arena%", arena.getName())
                .replace("%corner%", cornerId + "."));

        // Cleanup Logic if region changed
        cleanupAfterRegionChange(player, arena);
    }

    private void handleBedLocations(Player player, Arena arena, Action action, PlayerInteractEvent event) {
        if (isEditBlocked(player, arena)) return;
        if (isRegionMissing(player, arena)) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        String type = block.getType().toString();
        if (!type.contains("_BED") && !type.contains("BED_")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.NO-BED").replace("%arena%", arena.getName()));
            return;
        }

        if (isOutsideRegion(player, arena, block.getLocation())) return;

        BedLocation bedLocation = ClassImport.getClasses().getBedUtil().getBedLocation(block);

        if (action == Action.LEFT_CLICK_BLOCK) {
            arena.setBedLoc1(bedLocation);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.SET-BED1").replace("%arena%", arena.getName()));
        } else {
            arena.setBedLoc2(bedLocation);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.BED.SET-BED2").replace("%arena%", arena.getName()));
        }
        updateGui(arena);
    }

    private void handlePortals(Player player, Arena arena, Action action, PlayerInteractEvent event) {
        if (isEditBlocked(player, arena)) return;
        if (!arena.isBuild()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.NOT-BUILD").replace("%arena%", arena.getName()));
            return;
        }
        if (isRegionMissing(player, arena)) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType().equals(Material.AIR)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL.NO-BLOCK").replace("%arena%", arena.getName()));
            return;
        }

        Location portalLoc = block.getLocation();
        if (isOutsideRegion(player, arena, portalLoc)) return;

        PortalLocation portalLocation = new PortalLocation(portalLoc);

        if (action == Action.LEFT_CLICK_BLOCK) {
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
        updateGui(arena);
    }

    private void handleBuildMax(Player player, DisplayArena arena, Action action) {
        if (isEditBlocked(player, arena)) return;

        if (action.name().contains("LEFT")) {
            arena.setBuildMax(false);
            arena.setBuildMaxValue(ConfigManager.getInt("MATCH-SETTINGS.BUILD-LIMIT-DEFAULT"));
            player.sendMessage(Common.colorize("&cBuild Height Limit disabled for " + arena.getName()));
            return;
        }

        // Set
        if (isRegionMissing(player, arena)) return;
        Location pos = player.getLocation();
        if (isOutsideRegion(player, arena, pos)) return;

        arena.setBuildMaxValue(pos.getBlockY());
        arena.setBuildMax(true);

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETBUILDMAX.SET-BUILDMAX")
                .replace("%arena%", arena.getName())
                .replace("%y-level%", String.valueOf(pos.getBlockY())));
    }

    private void handleDeadZone(Player player, DisplayArena arena, Action action) {
        if (isEditBlocked(player, arena)) return;

        if (action.name().contains("LEFT")) {
            arena.setDeadZone(false);
            player.sendMessage(Common.colorize("&cDead Zone disabled for " + arena.getName()));
            return;
        }

        // Set
        if (isRegionMissing(player, arena)) return;
        Location pos = player.getLocation();
        if (isOutsideRegion(player, arena, pos)) return;

        int deadZoneY = pos.getBlockY();

        // Check against spawns
        List<Location> spawnPositions = new ArrayList<>();
        if (arena instanceof Arena a) {
            if (a.getPosition1() != null) spawnPositions.add(a.getPosition1());
            if (a.getPosition2() != null) spawnPositions.add(a.getPosition2());
        } else if (arena instanceof FFAArena f) {
            spawnPositions.addAll(f.getFfaPositions());
        }

        for (Location spawnPosition : spawnPositions) {
            if ((spawnPosition.getY() - 1) <= deadZoneY) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.LOWER-THAN-SPAWN"));
                return;
            }
        }

        arena.setDeadZoneValue(deadZoneY);
        arena.setDeadZone(true);

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.SETDEADZONE.SET-DEADZONE")
                .replace("%arena%", arena.getName())
                .replace("%y-level%", String.valueOf(deadZoneY)));
    }

    private void handleToggleStatus(Player player, DisplayArena arena, Action action) {
        if (action.name().contains("RIGHT")) {
            if (arena.isEnabled()) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.ENABLE.ALREADY-ENABLED").replace("%arena%", arena.getName()));
                return;
            }

            if (ArenaUtil.changeStatus(player, arena)) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.ENABLE.ARENA-ENABLED").replace("%arena%", arena.getName()));
                updateGui(arena);
            }
        }
    }

    private static boolean isEditBlocked(Player player, DisplayArena arena) {
        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.CANT-EDIT2").replace("%arena%", arena.getName()));
            return true;
        }
        if (arena instanceof Arena && ((Arena) arena).hasCopies()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.CANT-EDIT").replace("%arena%", arena.getName()));
            return true;
        }
        return false;
    }

    private static boolean isRegionMissing(Player player, DisplayArena arena) {
        if (arena.getCuboid() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.NO-REGION").replace("%arena%", arena.getName()));
            return true;
        }
        return false;
    }

    private static boolean isOutsideRegion(Player player, DisplayArena arena, Location loc) {
        if (!arena.getCuboid().contains(loc)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.POSITION.POS-OUTSIDE-REGION").replace("%arena%", arena.getName()));
            return true;
        }
        return false;
    }

    private static Location getSnappedLocation(Block clickedBlock, Player player) {
        Location loc = clickedBlock.getLocation().add(0.5, 1, 0.5);
        float snappedYaw = Math.round(player.getLocation().getYaw() / 45f) * 45f;
        loc.setYaw(snappedYaw);
        loc.setPitch(0.0f);
        return loc;
    }

    private static void updateGui(DisplayArena arena) {
        if (ArenaGUISetupManager.getInstance().getArenaSetupGUIs().containsKey(arena)) {
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();
        }
    }

    private static void cleanupAfterRegionChange(Player player, DisplayArena arena) {
        Cuboid cuboid = arena.getCuboid();
        if (cuboid == null) {
            updateGui(arena);
            return;
        }

        // Clean Position 1
        if (arena.getPosition1() != null && !cuboid.contains(arena.getPosition1())) {
            arena.setPosition1(null);
            arena.setEnabled(false);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.POSITION-REMOVED").replace("%arena%", arena.getName()).replace("%position%", "1"));
        }
        // Clean Position 2
        if (arena.getPosition2() != null && !cuboid.contains(arena.getPosition2())) {
            arena.setPosition2(null);
            arena.setEnabled(false);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.POSITION-REMOVED").replace("%arena%", arena.getName()).replace("%position%", "2"));
        }
        // Clean FFA Positions
        if (!arena.getFfaPositions().isEmpty()) {
            arena.getFfaPositions().removeIf(location -> !cuboid.contains(location));
        }
        // Clean BuildMax
        if (arena.isBuildMax() && (cuboid.getLowerY() > arena.getBuildMaxValue() || arena.getBuildMaxValue() > cuboid.getUpperY())) {
            arena.setBuildMaxValue(ConfigManager.getInt("MATCH-SETTINGS.BUILD-LIMIT-DEFAULT"));
            arena.setBuildMax(false);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.BUILD-MAX-REMOVED").replaceAll("%arena%", arena.getName()));
        }
        // Clean DeadZone
        if (arena.isDeadZone() && (cuboid.getLowerY() > arena.getDeadZoneValue() || arena.getDeadZoneValue() > cuboid.getUpperY())) {
            arena.setDeadZone(false);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.DEAD-ZONE-REMOVED").replaceAll("%arena%", arena.getName()));
        }
        // Clean Beds
        if (arena instanceof Arena a && a.getAssignedLadderTypes().contains(LadderType.BEDWARS)) {
            if (a.getBedLoc1() != null && !cuboid.contains(a.getBedLoc1().getLocation())) {
                a.setBedLoc1(null);
                a.setEnabled(false);
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.BED-REMOVED").replace("%arena%", arena.getName()).replace("%bed%", "1"));
            }
            if (a.getBedLoc2() != null && !cuboid.contains(a.getBedLoc2().getLocation())) {
                a.setBedLoc2(null);
                a.setEnabled(false);
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.BED-REMOVED").replace("%arena%", arena.getName()).replace("%bed%", "2"));
            }
        }
    }
}