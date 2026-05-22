package dev.nandi0813.practice.manager.fight.event.setup;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventWandSetupManager {

    private static EventWandSetupManager instance;

    public static EventWandSetupManager getInstance() {
        if (instance == null)
            instance = new EventWandSetupManager();
        return instance;
    }

    private EventWandSetupManager() {
        EventSetupListener eventSetupListener = new EventSetupListener(this);
        Bukkit.getPluginManager().registerEvents(eventSetupListener, ZonePractice.getInstance());
    }

    @Getter
    @Setter
    public static class SetupSession {
        private final EventData eventData;
        private EventSetupMode currentMode;

        public SetupSession(EventData eventData) {
            this.eventData = eventData;
            this.currentMode = EventSetupMode.CORNERS;
        }
    }

    private final Map<Player, SetupSession> setupSessions = new HashMap<>();

    public void startSetup(Player player, EventData eventData) {
        if (eventData.isEnabled()) {
            return;
        }

        if (isSettingUp(player)) {
            stopSetup(player);
        }

        setupSessions.put(player, new SetupSession(eventData));

        if (eventData.getCuboid() != null && !eventData.getCuboid().contains(player.getLocation())) {
            Location teleportLocation = eventData.getAvailableLocation();
            if (teleportLocation != null) {
                player.teleport(teleportLocation);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setGameMode(GameMode.CREATIVE);
            }
        } else if (!player.getWorld().equals(ArenaWorldUtil.getArenasWorld())) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setGameMode(GameMode.CREATIVE);
            player.teleport(ArenaWorldUtil.getArenasWorld().getSpawnLocation());
        }

        updateWand(player);

        // Show spawn position markers
        EventSpawnMarkerManager.getInstance().showMarkers(eventData);

        Common.sendMMMessage(player, "<green>Setup mode started for event: <yellow>" + eventData.getType().getName() + "<green>.");
    }

    public void stopSetup(Player player) {
        EventData eventData = getSession(player).getEventData();
        setupSessions.remove(player);

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (isSetupWand(item)) {
                player.getInventory().clear(i);
            }
        }

        // Clear markers if no one else is setting up this event
        if (getPlayersSettingUpEvent(eventData).isEmpty()) {
            EventSpawnMarkerManager.getInstance().clearMarkers(eventData);
        }

        Common.sendMMMessage(player, "<red>Setup mode ended for event: <red>" + eventData.getType().getName() + ".");
    }

    public SetupSession getSession(Player player) {
        return setupSessions.get(player);
    }

    public boolean isSettingUp(Player player) {
        return setupSessions.containsKey(player);
    }

    public List<Player> getPlayersSettingUpEvent(EventData eventData) {
        List<Player> players = new ArrayList<>();

        for (Map.Entry<Player, SetupSession> entry : setupSessions.entrySet()) {
            if (entry.getValue().getEventData().equals(eventData)) {
                players.add(entry.getKey());
            }
        }

        return players;
    }

    public boolean isSetupWand(ItemStack item) {
        return item != null && item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && Common.getItemDisplayName(item).contains("Event Wand");
    }

    public EventSetupMode getNextMode(EventSetupMode current) {
        EventSetupMode[] modes = EventSetupMode.values();
        int currentIndex = current.ordinal();
        int nextIndex = (currentIndex + 1) % modes.length;
        return modes[nextIndex];
    }

    public EventSetupMode getPreviousMode(EventSetupMode current) {
        EventSetupMode[] modes = EventSetupMode.values();
        int currentIndex = current.ordinal();
        int prevIndex = (currentIndex - 1 + modes.length) % modes.length;
        return modes[prevIndex];
    }

    public void updateWand(Player player) {
        SetupSession session = getSession(player);
        if (session == null) return;

        EventData eventData = session.getEventData();
        if (eventData == null) return;

        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        EventSetupMode mode = session.getCurrentMode();

        meta.displayName(Common.legacyToComponent(Common.colorize("<gold>Event Wand <gray>(<yellow>" + mode.getDisplayName() + "<gray>)")));

        List<String> lore = new ArrayList<>();
        lore.add(Common.colorize("<gray>Editing: <green>" + eventData.getType().getName()));
        lore.add("");
        lore.add(Common.colorize("<yellow>Current Mode: <white>" + mode.getDisplayName()));
        lore.add("");
        lore.add(Common.colorize("<gray>Controls:"));

        for (String line : mode.getDescription()) {
            lore.add(Common.colorize(line));
        }

        lore.add("");
        lore.add(Common.colorize("<light_purple>Shift + Left: <gray>Next Mode"));
        lore.add(Common.colorize("<light_purple>Shift + Right: <gray>Prev Mode"));
        lore.add("");
        lore.add(Common.colorize("<red>Drop (Q): <gray>Exit Setup"));

        meta.lore(lore.stream().map(Common::legacyToComponent).collect(Collectors.toList()));
        wand.setItemMeta(meta);

        player.getInventory().setItemInMainHand(wand);
    }
}
