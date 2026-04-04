package dev.nandi0813.practice.manager.gui.guis;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.matchhistory.MatchHistoryEntry;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Match History GUI — fully configurable through guis.yml.
 *
 * guis.yml path:  GUIS.MATCH-HISTORY.*
 *
 * Layout strategy
 * ─────────────────────────────────────────────────────────────────
 * The GUI is filled with a configurable filler item.
 * Match items are placed in the CENTER of the inventory:
 *   • For a 3-row (27-slot) GUI with 5 matches the items land
 *     in the middle row (slots 9-17), centred within that row.
 *   • If CENTER-ITEMS is false the items start at slot 0.
 * Every slot is configurable: START-SLOT overrides the automatic
 * centering when set to a non-negative value.
 */
public class MatchHistoryGui extends GUI {

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final UUID viewerUuid;
    private final String targetName;
    private final List<MatchHistoryEntry> entries;

    public MatchHistoryGui(UUID viewerUuid, String targetName,
                           List<MatchHistoryEntry> entries) {
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
        // ── Read config ────────────────────────────────────────
        String rawTitle = GUIFile.getString("GUIS.MATCH-HISTORY.TITLE");
        if (rawTitle == null || rawTitle.isEmpty())
            rawTitle = "&8Match History &7- &6%player%";
        rawTitle = rawTitle.replace("%player%", targetName);

        int size = GUIFile.getInt("GUIS.MATCH-HISTORY.SIZE");
        if (size < 9 || size > 54 || size % 9 != 0) size = 27;

        boolean centerItems = getBooleanOrDefault("GUIS.MATCH-HISTORY.CENTER-ITEMS", true);
        int configuredStart = GUIFile.getInt("GUIS.MATCH-HISTORY.START-SLOT"); // -1 means auto

        // ── Build inventory ────────────────────────────────────
        Inventory inventory = InventoryUtil.createInventory(rawTitle, size / 9);

        // Fill with configurable glass pane
        ItemStack filler = buildFillerItem();
        for (int i = 0; i < size; i++) inventory.setItem(i, filler);

        // ── Determine where to place the match items ───────────
        int startSlot;
        if (configuredStart >= 0) {
            startSlot = configuredStart;
        } else if (centerItems) {
            startSlot = computeCenterStart(size, entries.size());
        } else {
            startSlot = 0;
        }

        // ── Place match items ──────────────────────────────────
        String materialStr = GUIFile.getString("GUIS.MATCH-HISTORY.MATCH-ITEM.MATERIAL");
        Material material = Material.PAPER;
        if (materialStr != null && !materialStr.isBlank()) {
            try { material = Material.valueOf(materialStr.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        boolean usePlayerHead = getBooleanOrDefault(
                "GUIS.MATCH-HISTORY.MATCH-ITEM.USE-PLAYER-HEAD", true);

        for (int i = 0; i < entries.size(); i++) {
            int slot = startSlot + i;
            if (slot >= size) break;
            inventory.setItem(slot, buildMatchItem(entries.get(i), material, usePlayerHead));
        }

        gui.put(1, inventory);
    }

    // ── Item builders ────────────────────────────────────────────

    private ItemStack buildMatchItem(MatchHistoryEntry entry,
                                     Material fallbackMaterial,
                                     boolean usePlayerHead) {

        boolean won  = entry.isWinner(viewerUuid);
        boolean draw = entry.getWinnerUuid() == null;

        String oppName   = getOpponentName(entry);
        UUID   oppUuid   = getOpponentUuid(entry);

        // Resolve result label — must be effectively final for lambda capture
        String rawResult = won ? StringUtil.CC(GUIFile.getString(
                "GUIS.MATCH-HISTORY.MESSAGES.WIN"))
                : draw ? StringUtil.CC(GUIFile.getString(
                "GUIS.MATCH-HISTORY.MESSAGES.DRAW"))
                : StringUtil.CC(GUIFile.getString(
                "GUIS.MATCH-HISTORY.MESSAGES.LOSS"));
        final String result = (rawResult == null || rawResult.isBlank())
                ? (won ? "§aWin" : draw ? "§eEquality" : "§cLoss")
                : rawResult;

        double myHealth  = getMyHealth(entry);
        double oppHealth = getOpponentHealth(entry);
        int    myScore   = getMyScore(entry);
        int    oppScore  = getOpponentScore(entry);

        // ── Name ──────────────────────────────────────────────
        String rawName = GUIFile.getString("GUIS.MATCH-HISTORY.MATCH-ITEM.NAME");
        if (rawName == null || rawName.isBlank())
            rawName = "&eMatch vs &f%opponent%";
        String displayName = applyPlaceholders(rawName, entry, oppName,
                result, myScore, oppScore, myHealth, oppHealth, won, draw);

        // ── Lore ──────────────────────────────────────────────
        List<String> loreCfg = GUIFile.getStringList("GUIS.MATCH-HISTORY.MATCH-ITEM.LORE");
        if (loreCfg == null || loreCfg.isEmpty()) {
            loreCfg = defaultLore();
        }
        List<Component> lore = loreCfg.stream()
                .map(line -> applyPlaceholders(line, entry, oppName,
                        result, myScore, oppScore, myHealth, oppHealth, won, draw))
                .map(line -> Common.legacyToComponent(StringUtil.CC(line)))
                .collect(Collectors.toList());

        // ── ItemStack ─────────────────────────────────────────
        ItemStack item;
        if (usePlayerHead) {
            item = buildSkull(oppUuid, oppName);
        } else {
            item = new ItemStack(fallbackMaterial);
        }

        // Apply win/loss enchant glow if configured
        boolean glowOnWin = getBooleanOrDefault(
                "GUIS.MATCH-HISTORY.MATCH-ITEM.GLOW-ON-WIN", true);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Common.legacyToComponent(StringUtil.CC(displayName)));
            meta.lore(lore);
            if (glowOnWin && won) {
                org.bukkit.NamespacedKey glowKey = org.bukkit.NamespacedKey.minecraft("unbreaking");
                org.bukkit.enchantments.Enchantment glowEnchant =
                        org.bukkit.Registry.ENCHANTMENT.get(glowKey);
                if (glowEnchant != null) {
                    meta.addEnchant(glowEnchant, 1, true);
                }
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(
                    org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES,
                    org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
            );
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildSkull(UUID uuid, String name) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(uuid);
            skullMeta.setOwningPlayer(op);
            skull.setItemMeta(skullMeta);
        }
        return skull;
    }

    private ItemStack buildFillerItem() {
        GUIItem fillerCfg = GUIFile.getGuiItem("GUIS.MATCH-HISTORY.FILLER-ITEM");
        if (fillerCfg != null) {
            ItemStack built = fillerCfg.get();
            if (built != null) return built;
        }
        // Fallback
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }
        return item;
    }

    // ── Centering logic ──────────────────────────────────────────

    /**
     * Computes the first slot so that {@code count} items are centred
     * inside the inventory. Works row-by-row:
     *  1. Pick the middle row (or slightly above centre for even row counts).
     *  2. Within that row, centre the items horizontally.
     * If there are more items than fit in one row the items span multiple
     * rows, still centred vertically within the whole GUI.
     */
    private int computeCenterStart(int size, int count) {
        int rows = size / 9;
        int itemRows = (int) Math.ceil(count / 9.0);

        // Vertical: start on the row that centres itemRows within rows
        int startRow = Math.max(0, (rows - itemRows) / 2);

        // Horizontal: for the first (possibly partial) row
        int itemsInFirstRow = Math.min(count, 9);
        int startCol = Math.max(0, (9 - itemsInFirstRow) / 2);

        return startRow * 9 + startCol;
    }

    // ── Placeholder helpers ──────────────────────────────────────

    private String applyPlaceholders(String text, MatchHistoryEntry entry,
                                     String oppName, String result,
                                     int myScore, int oppScore,
                                     double myHealth, double oppHealth,
                                     boolean won, boolean draw) {
        return text
                .replace("%opponent%",        oppName)
                .replace("%result%",          result)
                .replace("%score%",           myScore + " - " + oppScore)
                .replace("%kit%",             entry.getKitName())
                .replace("%arena%",           entry.getArenaName())
                .replace("%player_health%",   formatHealth(myHealth))
                .replace("%opponent_health%", formatHealth(oppHealth))
                .replace("%duration%",        entry.getFormattedDuration())
                .replace("%date%",            DATE_FMT.format(
                        new Date(entry.getPlayedAt())));
    }

    private String formatHealth(double raw) {
        return String.format("%.1f❤", raw / 2.0);
    }

    // ── Perspective helpers ──────────────────────────────────────

    private boolean isViewer(MatchHistoryEntry e) {
        return viewerUuid != null && e.getPlayerUuid().equals(viewerUuid);
    }

    private String getOpponentName(MatchHistoryEntry e) {
        return isViewer(e) ? e.getOpponentName() : e.getPlayerName();
    }

    private UUID getOpponentUuid(MatchHistoryEntry e) {
        return isViewer(e) ? e.getOpponentUuid() : e.getPlayerUuid();
    }

    private double getMyHealth(MatchHistoryEntry e) {
        return isViewer(e) ? e.getPlayerFinalHealth() : e.getOpponentFinalHealth();
    }

    private double getOpponentHealth(MatchHistoryEntry e) {
        return isViewer(e) ? e.getOpponentFinalHealth() : e.getPlayerFinalHealth();
    }

    private int getMyScore(MatchHistoryEntry e) {
        return isViewer(e) ? e.getPlayerScore() : e.getOpponentScore();
    }

    private int getOpponentScore(MatchHistoryEntry e) {
        return isViewer(e) ? e.getOpponentScore() : e.getPlayerScore();
    }

    // ── Default lore ─────────────────────────────────────────────

    private List<String> defaultLore() {
        List<String> lore = new ArrayList<>();
        lore.add("&8&m--------------------");
        lore.add("&7Result: %result%");
        lore.add("&7Score: %score%");
        lore.add("&7Kit: &f%kit%");
        lore.add("&7Arena: &f%arena%");
        lore.add("&7Your Health: %player_health%");
        lore.add("&7Opponent Health: %opponent_health%");
        lore.add("&7Duration: &f%duration%");
        lore.add("&7Played: &f%date%");
        lore.add("&8&m--------------------");
        return lore;
    }

    // ── Utility ─────────────────────────────────────────────────

    private boolean getBooleanOrDefault(String path, boolean def) {
        if (GUIFile.getConfig().isSet(path.toUpperCase()))
            return GUIFile.getConfig().getBoolean(path.toUpperCase());
        return def;
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
    }
}
