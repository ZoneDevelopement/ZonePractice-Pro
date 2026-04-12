package dev.nandi0813.practice.manager.gui.guis.leaderboard;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.leaderboard.Leaderboard;
import dev.nandi0813.practice.manager.leaderboard.LeaderboardManager;
import dev.nandi0813.practice.manager.leaderboard.types.LbMainType;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.ItemCreateUtil;
import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public enum LbGuiUtil {
    ;

    /**
     * Parses a raw config string into a Component with full color support
     * (legacy &c, hex &#RRGGBB, MiniMessage tags) and italic explicitly disabled.
     */
    private static Component parseColor(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        return ZonePractice.getMiniMessage()
                .deserialize(StringUtil.translateColorsToMiniMessage(raw))
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private static List<Component> parseColorLore(List<String> lore) {
        return lore.stream().map(LbGuiUtil::parseColor).collect(Collectors.toList());
    }

    // if it says not used don't listen to it, buggy
    public static ItemStack createProfileStatItem(Profile profile, Player opener) {
        String playerName = profile.getPlayer().getName();
        if (playerName == null) playerName = "Unknown";

        ItemStack itemStack = ItemCreateUtil.getPlayerHead(profile.getPlayer());
        ItemMeta itemMeta = itemStack.getItemMeta();

        String displayName;
        List<String> lore = new ArrayList<>();

        if (opener.equals(profile.getPlayer())) {
            for (String line : GUIFile.getStringList("GUIS.STATISTICS.SELECTOR.ICONS.OWN-PLAYER-STATS.LORE"))
                lore.add(line.replace("%player%", playerName));
            displayName = GUIFile.getString("GUIS.STATISTICS.SELECTOR.ICONS.OWN-PLAYER-STATS.NAME")
                    .replace("%player%", playerName);
        } else {
            for (String line : GUIFile.getStringList("GUIS.STATISTICS.SELECTOR.ICONS.PLAYER-STATS.LORE"))
                lore.add(line.replace("%target%", playerName));
            displayName = GUIFile.getString("GUIS.STATISTICS.SELECTOR.ICONS.PLAYER-STATS.NAME")
                    .replace("%target%", playerName);
        }

        itemMeta.displayName(parseColor(displayName));
        itemMeta.lore(parseColorLore(lore));
        ItemCreateUtil.hideItemFlags(itemMeta);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static GUIItem createLadderStatItem(Profile profile, NormalLadder ladder) {
        GUIItem guiItem = switch (ladder.getWeightClass()) {
            case UNRANKED -> GUIFile.getGuiItem("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.UNRANKED-LADDER-STATS");
            case RANKED -> GUIFile.getGuiItem("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.RANKED-LADDER-STATS");
            case UNRANKED_AND_RANKED ->
                    GUIFile.getGuiItem("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.UNRANKED-RANKED-STATS");
        };

        switch (ladder.getWeightClass()) {
            case RANKED:
            case UNRANKED_AND_RANKED:
                guiItem.replace("%elo%", String.valueOf(profile.getStats().getLadderStat(ladder).getElo()));
                break;
        }

        String divisionName = profile.getStats().getDivision() != null
                ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "&cN/A";
        String divisionShort = profile.getStats().getDivision() != null
                ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "&cN/A";

        guiItem
                .replace("%ladder%", ladder.getDisplayName())
                .replace("%unranked_wins%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins()))
                .replace("%unranked_losses%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses()))
                .replace("%unranked_w/l_ratio%", String.valueOf(profile.getStats().getLadderRatio(ladder, false)))
                .replace("%ranked_wins%", String.valueOf(profile.getStats().getLadderStat(ladder).getRankedWins()))
                .replace("%ranked_losses%", String.valueOf(profile.getStats().getLadderStat(ladder).getRankedLosses()))
                .replace("%ranked_w/l_ratio%", String.valueOf(profile.getStats().getLadderRatio(ladder, true)))
                .replace("%overall_w/l_ratio%", String.valueOf(profile.getStats().getOverallRatio(ladder)))
                .replace("%division%", divisionName)
                .replace("%division_short%", divisionShort);

        if (ladder.getIcon() != null) guiItem.setBaseItem(ladder.getIcon());
        return guiItem;
    }

    public static ItemStack createProfileAllStatItem(Profile profile) {
        ItemStack itemStack = ItemCreateUtil.getPlayerHead(profile.getPlayer());
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>();

        String divisionName = profile.getStats().getDivision() != null
                ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "&cN/A";
        String divisionShort = profile.getStats().getDivision() != null
                ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "&cN/A";

        for (String line : GUIFile.getStringList("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.ALL-STAT.LORE")) {
            lore.add(line
                    .replace("%unranked_wins%", String.valueOf(profile.getStats().getWins(false)))
                    .replace("%unranked_losses%", String.valueOf(profile.getStats().getLosses(false)))
                    .replace("%unranked_w/l_ratio%", String.valueOf(profile.getStats().getRatio(false)))
                    .replace("%ranked_wins%", String.valueOf(profile.getStats().getWins(true)))
                    .replace("%ranked_losses%", String.valueOf(profile.getStats().getLosses(true)))
                    .replace("%ranked_w/l_ratio%", String.valueOf(profile.getStats().getRatio(true)))
                    .replace("%global_wins%", String.valueOf(profile.getStats().getGlobalWins()))
                    .replace("%global_losses%", String.valueOf(profile.getStats().getGlobalLosses()))
                    .replace("%w/l_ratio%", String.valueOf(profile.getStats().getGlobalRatio()))
                    .replace("%global_elo%", String.valueOf(profile.getStats().getGlobalElo()))
                    .replace("%division%", divisionName)
                    .replace("%division_short%", divisionShort)
            );
        }

        String name = GUIFile.getString("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.ALL-STAT.NAME")
                .replace("%player%", Objects.requireNonNull(profile.getPlayer().getName()));
        itemMeta.displayName(parseColor(name));
        itemMeta.lore(parseColorLore(lore));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createEloLbItem(NormalLadder ladder) {
        List<String> lore = new ArrayList<>();
        Leaderboard leaderboard = LeaderboardManager.getInstance().searchLB(LbMainType.LADDER, LbSecondaryType.ELO, ladder);
        int showPlayers = 10;

        if (leaderboard == null) {
            lore.addAll(GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.NO-LEADERBOARD"));
        } else {
            List<OfflinePlayer> topPlayers = new ArrayList<>();
            Map<OfflinePlayer, Integer> list = leaderboard.getList();
            for (OfflinePlayer player : list.keySet()) {
                if (topPlayers.size() < showPlayers) topPlayers.add(player);
                else break;
            }

            List<String> topStrings = new ArrayList<>();
            for (int i = 1; i <= showPlayers; i++) {
                if (topPlayers.size() > i - 1) {
                    OfflinePlayer target = topPlayers.get(i - 1);
                    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
                    Division division = targetProfile.getStats().getDivision();
                    int stat = list.get(target);
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT")
                            .replace("%number%", String.valueOf(i))
                            .replace("%player%", Objects.requireNonNull(target.getName()))
                            .replace("%ladder_elo%", String.valueOf(stat))
                            .replace("%division%", division != null ? Common.mmToNormal(division.getFullName()) : "")
                            .replace("%division_short%", division != null ? Common.mmToNormal(division.getShortName()) : ""));
                } else {
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT-NULL")
                            .replace("%number%", String.valueOf(i)));
                }
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%")) lore.addAll(topStrings);
                else lore.add(line);
            }
        }

        String name = GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.NAME")
                .replace("%ladder%", ladder.getDisplayName())
                .replace("%number%", String.valueOf(showPlayers));
        return buildItem(ladder.getIcon(), name, lore);
    }

    public static ItemStack createGlobalEloLb() {
        List<String> lore = new ArrayList<>();
        Leaderboard leaderboard = LeaderboardManager.getInstance().searchLB(LbMainType.GLOBAL, LbSecondaryType.ELO, null);
        int showPlayers = 10;

        if (leaderboard == null) {
            lore.addAll(GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.NO-LEADERBOARD"));
        } else {
            List<OfflinePlayer> topPlayers = new ArrayList<>();
            Map<OfflinePlayer, Integer> list = leaderboard.getList();
            for (OfflinePlayer player : list.keySet()) {
                if (topPlayers.size() < showPlayers) topPlayers.add(player);
                else break;
            }

            List<String> topStrings = new ArrayList<>();
            for (int i = 1; i <= showPlayers; i++) {
                if (topPlayers.size() > i - 1) {
                    OfflinePlayer target = topPlayers.get(i - 1);
                    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
                    Division division = targetProfile.getStats().getDivision();
                    int stat = list.get(target);
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT")
                            .replace("%number%", String.valueOf(i))
                            .replace("%division%", division != null ? Common.mmToNormal(division.getFullName()) : "")
                            .replace("%division_short%", division != null ? Common.mmToNormal(division.getShortName()) : "")
                            .replace("%player%", Objects.requireNonNull(target.getName()))
                            .replace("%global_elo%", String.valueOf(stat)));
                } else {
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT-NULL")
                            .replace("%number%", String.valueOf(i)));
                }
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%")) lore.addAll(topStrings);
                else lore.add(line);
            }
        }

        String name = GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.NAME")
                .replace("%number%", String.valueOf(showPlayers));
        Material mat = Material.valueOf(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.MATERIAL"));
        return buildItem(mat, name, lore);
    }

    public static ItemStack createWinLbItem(NormalLadder ladder) {
        List<String> lore = new ArrayList<>();
        Leaderboard leaderboard = LeaderboardManager.getInstance().searchLB(LbMainType.LADDER, LbSecondaryType.WIN, ladder);
        int showPlayers = 10;

        if (leaderboard == null) {
            lore.addAll(GUIFile.getStringList("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.NO-LEADERBOARD"));
        } else {
            List<OfflinePlayer> topPlayers = new ArrayList<>();
            Map<OfflinePlayer, Integer> list = leaderboard.getList();
            for (OfflinePlayer player : list.keySet()) {
                if (topPlayers.size() < showPlayers) topPlayers.add(player);
                else break;
            }

            List<String> topStrings = new ArrayList<>();
            for (int i = 1; i <= showPlayers; i++) {
                if (topPlayers.size() > i - 1) {
                    OfflinePlayer target = topPlayers.get(i - 1);
                    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
                    Division division = targetProfile.getStats().getDivision();
                    int stat = list.get(target);
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT")
                            .replace("%number%", String.valueOf(i))
                            .replace("%player%", Objects.requireNonNull(target.getName()))
                            .replace("%ladder_win%", String.valueOf(stat))
                            .replace("%division%", division != null ? Common.mmToNormal(division.getFullName()) : "")
                            .replace("%division_short%", division != null ? Common.mmToNormal(division.getShortName()) : ""));
                } else {
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT-NULL")
                            .replace("%number%", String.valueOf(i)));
                }
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%")) lore.addAll(topStrings);
                else lore.add(line);
            }
        }

        String name = GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.NAME")
                .replace("%ladder%", ladder.getDisplayName())
                .replace("%number%", String.valueOf(showPlayers));
        return buildItem(ladder.getIcon(), name, lore);
    }

    public static ItemStack createGlobalWinLb() {
        List<String> lore = new ArrayList<>();
        Leaderboard leaderboard = LeaderboardManager.getInstance().searchLB(LbMainType.GLOBAL, LbSecondaryType.WIN, null);
        int showPlayers = 10;

        if (leaderboard == null) {
            lore.addAll(GUIFile.getStringList("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.NO-LEADERBOARD"));
        } else {
            List<OfflinePlayer> topPlayers = new ArrayList<>();
            Map<OfflinePlayer, Integer> list = leaderboard.getList();
            for (OfflinePlayer player : list.keySet()) {
                if (topPlayers.size() < showPlayers) topPlayers.add(player);
                else break;
            }

            List<String> topStrings = new ArrayList<>();
            for (int i = 1; i <= showPlayers; i++) {
                if (topPlayers.size() > i - 1) {
                    OfflinePlayer target = topPlayers.get(i - 1);
                    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
                    Division division = targetProfile.getStats().getDivision();
                    int stat = list.get(target);
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT")
                            .replace("%number%", String.valueOf(i))
                            .replace("%division%", division != null ? Common.mmToNormal(division.getFullName()) : "")
                            .replace("%division_short%", division != null ? Common.mmToNormal(division.getShortName()) : "")
                            .replace("%player%", Objects.requireNonNull(target.getName()))
                            .replace("%global_win%", String.valueOf(stat)));
                } else {
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT-NULL")
                            .replace("%number%", String.valueOf(i)));
                }
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%")) lore.addAll(topStrings);
                else lore.add(line);
            }
        }

        String name = GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.NAME")
                .replace("%number%", String.valueOf(showPlayers));
        Material mat = Material.valueOf(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.MATERIAL"));
        return buildItem(mat, name, lore);
    }

    public static ItemStack getCacheInfoItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&8&m------------------------");
        lore.add("&7This leaderboard automatically");
        lore.add("&7updates every &e5 minutes&7.");
        lore.add("");
        lore.add("&7Last update: &aRecently");
        lore.add("&7Next update: &eWithin 5 minutes");
        lore.add("&8&m------------------------");
        return buildItem(Material.CLOCK, "&eAuto-Update Info", lore);
    }

    // ── Item building helpers ────────────────────────────────────────────────

    /** Builds an ItemStack from an existing icon with a parsed name and lore. */
    private static ItemStack buildItem(ItemStack icon, String name, List<String> lore) {
        ItemStack item = icon != null ? icon.clone() : new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(parseColor(name));
            meta.lore(parseColorLore(lore));
            ItemCreateUtil.hideItemFlags(meta);
            item.setItemMeta(meta);
        }
        return item;
    }

    /** Builds an ItemStack from a material with a parsed name and lore. */
    private static ItemStack buildItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(parseColor(name));
            meta.lore(parseColorLore(lore));
            ItemCreateUtil.hideItemFlags(meta);
            item.setItemMeta(meta);
        }
        return item;
    }
}
