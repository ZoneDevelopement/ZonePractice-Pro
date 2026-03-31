package dev.nandi0813.practice.manager.gui.guis;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.matchhistory.MatchHistoryEntry;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MatchHistoryGui extends GUI {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final UUID viewerUuid;
    private final String targetName;
    private final List<MatchHistoryEntry> entries;

    public MatchHistoryGui(UUID viewerUuid, String targetName, List<MatchHistoryEntry> entries) {
        super(GUIType.MatchHistory_Gui);
        this.viewerUuid = viewerUuid;
        this.targetName = targetName;
        this.entries = entries;
        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        String rawTitle = ConfigManager.getString("MATCH-HISTORY.GUI.TITLE");
        if (rawTitle == null || rawTitle.isEmpty()) rawTitle = "&6Match History";
        rawTitle = rawTitle.replace("%player%", targetName);

        int size = ConfigManager.getInt("MATCH-HISTORY.GUI.SIZE");
        if (size < 9 || size > 54 || size % 9 != 0) size = 27;

        Inventory inventory = InventoryUtil.createInventory(rawTitle, size / 9);

        // Place match items
        String materialStr = ConfigManager.getString("MATCH-HISTORY.MATCH-ITEM.MATERIAL");
        Material material;
        try {
            material = Material.valueOf(materialStr);
        } catch (Exception e) {
            material = Material.PAPER;
        }

        int slot = 0;
        for (MatchHistoryEntry entry : entries) {
            if (slot >= size) break;
            inventory.setItem(slot, buildMatchItem(entry, material));
            slot++;
        }

        gui.put(1, inventory);
    }

    private ItemStack buildMatchItem(MatchHistoryEntry entry, Material material) {
        String opponentName = entry.getOpponentNameFor(viewerUuid);
        boolean won = entry.isWinner(viewerUuid);
        String result = won
                ? StringUtil.CC("&aWin")
                : StringUtil.CC("&cLoss");

        String score = entry.getScoreFor(viewerUuid);
        double playerHealth = entry.getPlayerHealthFor(viewerUuid);
        double opponentHealth = entry.getOpponentHealthFor(viewerUuid);
        String duration = entry.getFormattedDuration();
        String date = DATE_FORMAT.format(new Date(entry.getPlayedAt()));

        // --- item name ---
        String rawName = ConfigManager.getString("MATCH-HISTORY.MATCH-ITEM.NAME");
        if (rawName == null || rawName.isEmpty()) rawName = "&eMatch vs %opponent%";
        String displayName = StringUtil.CC(rawName.replace("%opponent%", opponentName));

        // --- lore ---
        List<String> loreCfg = ConfigManager.getList("MATCH-HISTORY.MATCH-ITEM.LORE");
        if (loreCfg == null || loreCfg.isEmpty()) {
            loreCfg = new ArrayList<>();
            loreCfg.add("&7Result: %result%");
            loreCfg.add("&7Score: %score%");
            loreCfg.add("&7Kit: %kit%");
            loreCfg.add("&7Arena: %arena%");
            loreCfg.add("&7Your Health: %player_health%");
            loreCfg.add("&7Opponent Health: %opponent_health%");
            loreCfg.add("&7Duration: %duration%");
            loreCfg.add("&7Played: %date%");
        }

        List<Component> lore = new ArrayList<>();
        for (String line : loreCfg) {
            String processed = line
                    .replace("%result%", result)
                    .replace("%score%", score)
                    .replace("%kit%", entry.getKitName())
                    .replace("%arena%", entry.getArenaName())
                    .replace("%player_health%", formatHealth(playerHealth))
                    .replace("%opponent_health%", formatHealth(opponentHealth))
                    .replace("%duration%", duration)
                    .replace("%date%", date)
                    .replace("%opponent%", opponentName);
            lore.add(Component.text(StringUtil.CC(processed)));
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatHealth(double health) {
        // health is in half-hearts; divide by 2 to get hearts
        double hearts = health / 2.0;
        return String.format("%.1f❤", hearts);
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
        // No interactive slots — read-only display
    }
}
