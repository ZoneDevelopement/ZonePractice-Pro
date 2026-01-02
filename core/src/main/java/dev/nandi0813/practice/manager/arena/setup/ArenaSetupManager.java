package dev.nandi0813.practice.manager.arena.setup;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ArenaSetupManager {

    private final Map<UUID, SetupSession> setupSessions = new HashMap<>();

    @Getter
    public static class SetupSession {
        private final Arena arena;
        @Setter
        private SetupMode currentMode;

        public SetupSession(Arena arena) {
            this.arena = arena;
            this.currentMode = SetupMode.CORNERS;
        }
    }

    public void startSetup(Player player, Arena arena) {
        setupSessions.put(player.getUniqueId(), new SetupSession(arena));
        player.getInventory().addItem(getSetupWand(setupSessions.get(player.getUniqueId())));
        player.sendMessage(ChatColor.GREEN + "Setup mode started for arena: " + arena.getName());
    }

    public void stopSetup(Player player) {
        setupSessions.remove(player.getUniqueId());
        player.getInventory().remove(Material.BLAZE_ROD);
        player.sendMessage(ChatColor.RED + "Setup mode ended.");
    }

    public SetupSession getSession(Player player) {
        return setupSessions.get(player.getUniqueId());
    }

    public boolean isSettingUp(Player player) {
        return setupSessions.containsKey(player.getUniqueId());
    }

    public void updateWand(Player player) {
        SetupSession session = getSession(player);
        if (session == null) return;

        ItemStack wand = getSetupWand(session);

        // Find the wand in inventory and replace it (simplest approach is setting item in hand)
        if (isSetupWand(player.getItemInHand())) {
            player.setItemInHand(wand);
        }
    }

    private ItemStack getSetupWand(SetupSession session) {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();

        SetupMode mode = session.getCurrentMode();

        meta.setDisplayName(ChatColor.GOLD + "Arena Wand " + ChatColor.GRAY + "(" + ChatColor.YELLOW + mode.getDisplayName() + ChatColor.GRAY + ")");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Editing: " + ChatColor.GREEN + session.getArena().getName());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Current Mode: " + ChatColor.WHITE + mode.getDisplayName());
        lore.add("");
        lore.add(ChatColor.GRAY + "controls:");
        for (String desc : mode.getDescription()) {
            lore.add(ChatColor.AQUA + " * " + desc);
        }
        lore.add("");
        lore.add(ChatColor.LIGHT_PURPLE + "Shift + Left Click: " + ChatColor.GRAY + "Next Mode");
        lore.add(ChatColor.LIGHT_PURPLE + "Shift + Right Click: " + ChatColor.GRAY + "Previous Mode");

        meta.setLore(lore);
        wand.setItemMeta(meta);
        return wand;
    }

    public boolean isSetupWand(ItemStack item) {
        return item != null && item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().contains("Arena Wand");
    }
}