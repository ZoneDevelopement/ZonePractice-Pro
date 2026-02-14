package dev.nandi0813.practice.manager.fight.event.setup;

import dev.nandi0813.api.Event.Event.EventEndEvent;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.event.EventSetupManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.List;


public class EventSetupListener implements Listener {

    private final EventWandSetupManager setupManager;

    public EventSetupListener(EventWandSetupManager setupManager) {
        this.setupManager = setupManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!setupManager.isSettingUp(player) || !setupManager.isSetupWand(event.getItem())) {
            return;
        }

        event.setCancelled(true);

        EventWandSetupManager.SetupSession session = setupManager.getSession(player);
        EventData eventData = session.getEventData();

        if (eventData == null) {
            player.sendMessage(Common.colorize("&cEvent not found!"));
            setupManager.stopSetup(player);
            return;
        }

        Action action = event.getAction();

        if (player.isSneaking()) {
            handleModeSwitch(player, session, eventData, action);
            return;
        }

        EventSetupMode currentMode = session.getCurrentMode();

        boolean isAirAllowed = (currentMode == EventSetupMode.TOGGLE_STATUS || currentMode == EventSetupMode.SPAWN_POINTS);

        if (!isAirAllowed) {
            if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;
        } else {
            if (action == Action.PHYSICAL) return;
        }

        switch (currentMode) {
            case CORNERS -> handleCornerSelection(player, eventData, action, event);
            case SPAWN_POINTS -> handleSpawnPoints(player, eventData, action, event);
            case TOGGLE_STATUS -> handleToggleStatus(player, eventData, action);
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

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (!setupManager.isSettingUp(player) || !setupManager.isSetupWand(player.getItemInHand())) {
            return;
        }

        EventWandSetupManager.SetupSession session = setupManager.getSession(player);
        if (session.getCurrentMode() != EventSetupMode.SPAWN_POINTS) {
            return;
        }

        if (!(event.getRightClicked() instanceof ArmorStand armorStand)) {
            return;
        }

        event.setCancelled(true);

        EventData eventData = session.getEventData();
        EventSpawnMarkerManager markerManager = EventSpawnMarkerManager.getInstance();

        if (!markerManager.isMarker(armorStand)) {
            return;
        }

        int spawnIndex = markerManager.getSpawnIndex(armorStand, eventData);
        if (spawnIndex == -1) {
            player.sendMessage(Common.colorize("&cCouldn't find spawn point for this marker."));
            return;
        }

        eventData.getSpawns().remove(spawnIndex);
        markerManager.updateMarkers(eventData);
        updateGui(eventData);

        player.sendMessage(Common.colorize("&aRemoved spawn point #" + (spawnIndex + 1) + ". Remaining: " + eventData.getSpawns().size()));
    }

    // Prevent players from manipulating marker armor stands
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        ArmorStand armorStand = event.getRightClicked();

        if (EventSpawnMarkerManager.getInstance().isMarker(armorStand)) {
            event.setCancelled(true);
        }
    }

    // Prevent damage to marker armor stands
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;

        if (!EventSpawnMarkerManager.getInstance().isMarker(armorStand)) return;

        // Cancel damage in all cases
        event.setCancelled(true);

        // Check if the damager is a player (left-click)
        if (!(event.getDamager() instanceof Player player)) return;

        // Check if player is in setup mode
        if (!setupManager.isSettingUp(player)) return;

        EventWandSetupManager.SetupSession session = setupManager.getSession(player);
        if (session == null) return;

        EventData eventData = session.getEventData();
        if (eventData == null) return;

        // Check if in correct mode
        if (session.getCurrentMode() != EventSetupMode.SPAWN_POINTS) return;

        // Left-click on armor stand = Remove last spawn (same as left-click on block)
        if (!eventData.getSpawns().isEmpty()) {
            int index = eventData.getSpawns().size() - 1;
            eventData.getSpawns().remove(index);
            EventSpawnMarkerManager.getInstance().updateMarkers(eventData);
            updateGui(eventData);
            player.sendMessage(Common.colorize("&cRemoved last spawn point. Remaining: " + index));
        } else {
            player.sendMessage(Common.colorize("&cNo spawn points to remove."));
        }
    }

    @EventHandler
    public void onEventEnd(EventEndEvent event) {
        // When an event ends, clean up any spawn markers for that event's EventData
        // This prevents markers from persisting after the event is stopped
        if (event.getEvent() instanceof dev.nandi0813.practice.manager.fight.event.interfaces.Event practiceEvent) {
            EventData eventData = practiceEvent.getEventData();
            if (eventData != null) {
                EventSpawnMarkerManager.getInstance().clearMarkers(eventData);
            }
        }
    }

    private void handleModeSwitch(Player player, EventWandSetupManager.SetupSession session, EventData eventData, Action action) {
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            session.setCurrentMode(setupManager.getNextMode(session.getCurrentMode()));
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            session.setCurrentMode(setupManager.getPreviousMode(session.getCurrentMode()));
        }

        setupManager.updateWand(player);
        player.sendMessage(Common.colorize("&eSwitched to mode: &f" + session.getCurrentMode().getDisplayName()));
    }

    private void handleCornerSelection(Player player, EventData eventData, Action action, PlayerInteractEvent event) {
        if (eventData.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.CANT-EDIT-ENABLED"));
            return;
        }

        Block targetBlock = event.getClickedBlock();
        if (targetBlock == null || targetBlock.getType().equals(Material.AIR)) {
            player.sendMessage(Common.colorize("&cBlock location cannot be found!"));
            return;
        }

        Location cornerLocation = targetBlock.getLocation();
        if (!cornerLocation.getWorld().equals(ArenaWorldUtil.getArenasWorld())) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.ARENAS-WORLD"));
            return;
        }

        int cornerId = (action == Action.LEFT_CLICK_BLOCK) ? 1 : 2;
        if (cornerId == 1) {
            eventData.setCuboidLoc1(cornerLocation);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.SET-FIRST-CORNER")
                    .replace("%event%", eventData.getType().getName()));
        } else {
            eventData.setCuboidLoc2(cornerLocation);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.SET-SECOND-CORNER")
                    .replace("%event%", eventData.getType().getName()));
        }

        updateGui(eventData);
    }

    private void handleSpawnPoints(Player player, EventData eventData, Action action, PlayerInteractEvent event) {
        if (eventData.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.CANT-EDIT-ENABLED"));
            return;
        }

        if (eventData.getCuboid() == null) {
            player.sendMessage(Common.colorize("&cYou must set corners first!"));
            return;
        }

        // Right-click on a block to add a spawn point
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null) return;

            Location spawnLoc = block.getLocation().clone().add(0.5, 1, 0.5);
            spawnLoc.setYaw(player.getLocation().getYaw());
            spawnLoc.setPitch(player.getLocation().getPitch());

            if (!eventData.getCuboid().contains(spawnLoc)) {
                player.sendMessage(Common.colorize("&cSpawn point must be within the event cuboid!"));
                return;
            }

            eventData.addSpawn(spawnLoc);
            EventSpawnMarkerManager.getInstance().updateMarkers(eventData);
            updateGui(eventData);

            player.sendMessage(Common.colorize("&aAdded spawn point #" + eventData.getSpawns().size() + " at your location."));
        }
        // Left-click anywhere to remove the last spawn point
        else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (!eventData.getSpawns().isEmpty()) {
                int index = eventData.getSpawns().size() - 1;
                eventData.getSpawns().remove(index);
                EventSpawnMarkerManager.getInstance().updateMarkers(eventData);
                updateGui(eventData);
                player.sendMessage(Common.colorize("&cRemoved last spawn point. Remaining: " + index));
            } else {
                player.sendMessage(Common.colorize("&cNo spawn points to remove."));
            }
        }
    }

    private void handleToggleStatus(Player player, EventData eventData, Action action) {
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        try {
            if (eventData.isEnabled()) {
                eventData.setEnabled(false);
                player.sendMessage(Common.colorize("&cDisabled event: &e" + eventData.getType().getName()));
            } else {
                if (eventData.getCuboidLoc1() == null || eventData.getCuboidLoc2() == null) {
                    player.sendMessage(Common.colorize("&cYou must set both corners first!"));
                    return;
                }
                if (eventData.getSpawns().isEmpty()) {
                    player.sendMessage(Common.colorize("&cYou must set at least one spawn point!"));
                    return;
                }

                eventData.setEnabled(true);
                player.sendMessage(Common.colorize("&aEnabled event: &e" + eventData.getType().getName()));

                // When enabling an event, cleanup: clear markers and end setup mode for all players
                List<Player> playersSettingUp = new ArrayList<>(setupManager.getPlayersSettingUpEvent(eventData));

                // Clear markers first (before ending setup, to avoid duplicate clears)
                if (!playersSettingUp.isEmpty()) {
                    EventSpawnMarkerManager.getInstance().clearMarkers(eventData);
                }

                // End setup mode for all players currently setting up this event
                for (Player settingUpPlayer : playersSettingUp) {
                    setupManager.stopSetup(settingUpPlayer);
                }
            }

            updateGui(eventData);
        } catch (Exception e) {
            player.sendMessage(Common.colorize("&cError toggling status: " + e.getMessage()));
        }
    }

    private void updateGui(EventData eventData) {
        if (EventSetupManager.getInstance().getEventSetupGUIs().containsKey(eventData)) {
            if (EventSetupManager.getInstance().getEventSetupGUIs().get(eventData).containsKey(GUIType.Event_Main)) {
                EventSetupManager.getInstance().getEventSetupGUIs().get(eventData).get(GUIType.Event_Main).update();
            }
        }
    }
}
