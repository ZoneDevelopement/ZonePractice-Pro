package dev.nandi0813.practice.manager.arena.setup;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaSetupManager {

    @Getter
    private static ArenaSetupManager instance;
    private final Map<Player, SetupSession> setupSessions = new HashMap<>();

    public ArenaSetupManager() {
        instance = this;
    }

    @Getter
    @Setter
    public static class SetupSession {
        private final DisplayArena arena;
        private SetupMode currentMode;

        public SetupSession(DisplayArena arenaName) {
            this.arena = arenaName;
            this.currentMode = SetupMode.CORNERS;
        }
    }

    public void startSetup(Player player, DisplayArena arena) {
        setupSessions.put(player, new SetupSession(arena));
        updateWand(player);
        player.sendMessage(Common.colorize("&aSetup mode started for arena: " + arena));
    }

    public void stopSetup(Player player) {
        setupSessions.remove(player);
        player.getInventory().remove(Material.BLAZE_ROD);
        player.sendMessage(Common.colorize("&cSetup mode ended."));
    }

    public SetupSession getSession(Player player) {
        return setupSessions.get(player);
    }

    public boolean isSettingUp(Player player) {
        return setupSessions.containsKey(player);
    }

    public boolean isSetupWand(ItemStack item) {
        return item != null && item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains("Arena Wand");
    }

    public SetupMode getNextMode(DisplayArena arena, SetupMode current) {
        List<SetupMode> validModes = getValidModes(arena);
        int currentIndex = validModes.indexOf(current);
        if (currentIndex == -1) return validModes.get(0);

        int nextIndex = (currentIndex + 1) % validModes.size();
        return validModes.get(nextIndex);
    }

    public SetupMode getPreviousMode(DisplayArena arena, SetupMode current) {
        List<SetupMode> validModes = getValidModes(arena);
        int currentIndex = validModes.indexOf(current);
        if (currentIndex == -1) return validModes.get(0);

        int prevIndex = (currentIndex - 1 + validModes.size()) % validModes.size();
        return validModes.get(prevIndex);
    }

    private List<SetupMode> getValidModes(DisplayArena arena) {
        List<SetupMode> modes = new ArrayList<>();

        modes.add(SetupMode.CORNERS);

        if (arena instanceof FFAArena) {
            modes.add(SetupMode.FFA_POSITIONS);
        } else if (arena instanceof Arena a) {
            modes.add(SetupMode.POSITIONS);

            if (a.isBuild()) {
                modes.add(SetupMode.BUILD_MAX);
                modes.add(SetupMode.DEAD_ZONE);
                modes.add(SetupMode.BED_LOCATIONS);
                modes.add(SetupMode.PORTALS);
            }
        }

        modes.add(SetupMode.TOGGLE_STATUS);

        return modes;
    }

    public void updateWand(Player player) {
        SetupSession session = getSession(player);
        if (session == null) return;

        DisplayArena arena = session.getArena();
        if (arena == null) return;

        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        SetupMode mode = session.getCurrentMode();

        meta.setDisplayName(Common.colorize("&6Arena Wand &7(&e" + mode.getDisplayName() + "&7)"));

        List<String> lore = new ArrayList<>();
        lore.add(Common.colorize("&7Editing: &a" + arena.getName()));
        lore.add(Common.colorize("&7Type: &b" + (arena instanceof FFAArena ? "FFA" : "Standard")));
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

        meta.setLore(lore);
        wand.setItemMeta(meta);

        player.getInventory().setItemInHand(wand);
    }
}