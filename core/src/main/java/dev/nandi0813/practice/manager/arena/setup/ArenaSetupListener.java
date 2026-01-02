package dev.nandi0813.practice.manager.arena.setup;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ArenaSetupListener implements Listener {

    private final ArenaSetupManager setupManager;

    public ArenaSetupListener(ArenaSetupManager setupManager) {
        this.setupManager = setupManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!setupManager.isSettingUp(player)) return;
        if (!setupManager.isSetupWand(event.getItem())) return;

        event.setCancelled(true);

        ArenaSetupManager.SetupSession session = setupManager.getSession(player);
        Action action = event.getAction();

        if (player.isSneaking()) {
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                session.setCurrentMode(session.getCurrentMode().next());
                setupManager.updateWand(player);
                player.sendMessage(ChatColor.YELLOW + "Switched to: " + session.getCurrentMode().getDisplayName());
                return;
            } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                session.setCurrentMode(session.getCurrentMode().previous());
                setupManager.updateWand(player);
                player.sendMessage(ChatColor.YELLOW + "Switched to: " + session.getCurrentMode().getDisplayName());
                return;
            }
        }

        // 2. HANDLE FUNCTIONALITY BASED ON CURRENT MODE
        // (If not sneaking, or sneaking but handled above)

        SetupMode currentMode = session.getCurrentMode();
        Arena arena = session.getArena();

        switch (currentMode) {
            case CORNERS:
                handleCornerSelection(player, arena, action, event);
                break;
            case SPAWN_POINTS:
                handleSpawnPoints(player, arena, action);
                break;
            case BED_LOCATIONS:
                handleBedLocations(player, arena, action, event);
                break;
            case PORTALS:
                handlePortals(player, arena, action);
                break;
            case TOGGLE_STATUS:
                handleToggleStatus(player, arena, action);
                break;
        }
    }

    private void handleCornerSelection(Player player, Arena arena, Action action, PlayerInteractEvent event) {
        if (arena == null) return;

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.CANT-EDIT2").replace("%arena%", arena.getName()));
            return;
        }

        if (arena.hasCopies()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.CANT-EDIT").replace("%arena%", arena.getName()));
            return;
        }

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
        if (cornerId == 1) {
            arena.setCorner1(cornerLocation);
        } else {
            arena.setCorner2(cornerLocation);
        }

        arena.createCuboid();

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CORNER.SAVED-CORNER")
                .replace("%arena%", arena.getName())
                .replace("%corner%", cornerId + "."));

        Cuboid cuboid = arena.getCuboid();

        if (cuboid == null) {
            if (ArenaGUISetupManager.getInstance().getArenaSetupGUIs().containsKey(arena)) {
                ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();
            }
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

        if (arena.getAssignedLadderTypes().contains(LadderType.BEDWARS)) {
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

    private void handleSpawnPoints(Player player, Arena arena, Action action) {
        if (action.name().contains("RIGHT")) {
            player.sendMessage(ChatColor.GREEN + "Added spawn point for " + arena);
            // TODO: Add spawn logic (player.getLocation())
        } else if (action.name().contains("LEFT")) {
            player.sendMessage(ChatColor.RED + "Removed last spawn point.");
            // TODO: Remove spawn logic
        }
    }

    private void handleBedLocations(Player player, Arena arena, Action action, PlayerInteractEvent event) {
        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType().name().contains("BED")) {
                player.sendMessage(ChatColor.GREEN + "Bed location set for " + arena);
                // TODO: Save bed logic
            } else {
                player.sendMessage(ChatColor.RED + "You must click on a bed block!");
            }
        }
    }

    private void handlePortals(Player player, Arena arena, Action action) {
        if (action.name().contains("RIGHT")) {
            player.sendMessage(ChatColor.GREEN + "Portal added at your location.");
            // TODO: Add portal logic
        }
    }

    private void handleToggleStatus(Player player, Arena arena, Action action) {
        if (action.name().contains("RIGHT")) {
            player.sendMessage(ChatColor.GREEN + "Arena " + arena + " ENABLED!");
            // TODO: arena.setEnabled(true)
        } else if (action.name().contains("LEFT")) {
            player.sendMessage(ChatColor.RED + "Arena " + arena + " DISABLED!");
            // TODO: arena.setEnabled(false)
        }
    }

    private String formatLoc(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
}