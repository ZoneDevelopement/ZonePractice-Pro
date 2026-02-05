package dev.nandi0813.practice.manager.fight.event.setup;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean startSetup(Player player, EventData eventData) {
        if (eventData.isEnabled()) {
            return false;
        }

        if (isSettingUp(player)) {
            stopSetup(player);
        }

        setupSessions.put(player, new SetupSession(eventData));

        if (eventData.getCuboid() != null && !eventData.getCuboid().contains(player.getLocation())) {
            if (eventData.getCuboid().getCenter() != null) {
                player.teleport(eventData.getCuboid().getCenter());
            }
        } else if (!player.getWorld().equals(ArenaWorldUtil.getArenasWorld())) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.teleport(ArenaWorldUtil.getArenasWorld().getSpawnLocation());
        }

        updateWand(player);

        // Show spawn position markers
        EventSpawnMarkerManager.getInstance().showMarkers(eventData);

        player.sendMessage(Common.colorize("&aSetup mode started for event: &e" + eventData.getType().getName() + "&a."));
        return true;
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

        player.sendMessage(Common.colorize("&cSetup mode ended for event: &c" + eventData.getType().getName() + "."));
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
        return item != null && item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains("Event Wand");
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

        meta.setDisplayName(Common.colorize("&6Event Wand &7(&e" + mode.getDisplayName() + "&7)"));

        List<String> lore = new ArrayList<>();
        lore.add(Common.colorize("&7Editing: &a" + eventData.getType().getName()));
        lore.add("");
        lore.add(Common.colorize("&eCurrent Mode: &f" + mode.getDisplayName()));
        lore.add("");
        lore.add(Common.colorize("&7Controls:"));

        for (String line : mode.getDescription()) {
            lore.add(Common.colorize(line));
        }

        lore.add("");
        lore.add(Common.colorize("&dShift + Left: &7Next Mode"));
        lore.add(Common.colorize("&dShift + Right: &7Prev Mode"));
        lore.add("");
        lore.add(Common.colorize("&cDrop (Q): &7Exit Setup"));

        meta.setLore(lore);
        wand.setItemMeta(meta);

        player.getInventory().setItemInHand(wand);
    }
}
