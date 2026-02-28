package dev.nandi0813.practice.manager.gui.guis.leaderboard;

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
import dev.nandi0813.practice.module.interfaces.ItemCreateUtil;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum LbGuiUtil {
    ;

    // if it says not used don't listen to it, buggy
    public static ItemStack createProfileStatItem(Profile profile, Player opener) {
        String playerName = profile.getPlayer().getName();
        ItemStack itemStack = ClassImport.getClasses().getItemMaterialUtil().getPlayerHead(profile.getPlayer());
        ItemMeta itemMeta = itemStack.getItemMeta();

        String displayName;
        List<String> lore = new ArrayList<>();
        if (opener.equals(profile.getPlayer())) {
            for (String line : GUIFile.getStringList("GUIS.STATISTICS.SELECTOR.ICONS.OWN-PLAYER-STATS.LORE"))
                lore.add(line.replace("%player%", playerName));

            displayName = GUIFile.getString("GUIS.STATISTICS.SELECTOR.ICONS.OWN-PLAYER-STATS.NAME").replace("%player%", playerName);
        } else {
            for (String line : GUIFile.getStringList("GUIS.STATISTICS.SELECTOR.ICONS.PLAYER-STATS.LORE"))
                lore.add(line.replace("%target%", playerName));

            displayName = GUIFile.getString("GUIS.STATISTICS.SELECTOR.ICONS.PLAYER-STATS.NAME").replace("%target%", playerName);
        }

        itemMeta.setDisplayName(StringUtil.CC(displayName));
        itemMeta.setLore(StringUtil.CC(lore));

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

        guiItem
                .replace("%ladder%", ladder.getDisplayName())
                .replace("%unranked_wins%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins()))
                .replace("%unranked_losses%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses()))
                .replace("%unranked_w/l_ratio%", String.valueOf(profile.getStats().getLadderRatio(ladder, false)))
                .replace("%ranked_wins%", String.valueOf(profile.getStats().getLadderStat(ladder).getRankedWins()))
                .replace("%ranked_losses%", String.valueOf(profile.getStats().getLadderStat(ladder).getRankedLosses()))
                .replace("%ranked_w/l_ratio%", String.valueOf(profile.getStats().getLadderRatio(ladder, true)))
                .replace("%overall_w/l_ratio%", String.valueOf(profile.getStats().getOverallRatio(ladder)))
                .replace("%division%", (profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "&cN/A"))
                .replace("%division_short%", (profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "&cN/A"));

        if (ladder.getIcon() != null) {
            guiItem.setBaseItem(ladder.getIcon());
        }

        return guiItem;
    }

    public static ItemStack createProfileAllStatItem(Profile profile) {
        ItemStack itemStack = ClassImport.getClasses().getItemMaterialUtil().getPlayerHead(profile.getPlayer());
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>();

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
                    .replace("%division%", (profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "&cN/A"))
                    .replace("%division_short%", profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "&cN/A")
            );
        }

        itemMeta.setDisplayName(GUIFile.getString("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.ALL-STAT.NAME").replace("%player%", profile.getPlayer().getName()));
        itemMeta.setLore(StringUtil.CC(lore));
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
                if (topPlayers.size() < showPlayers)
                    topPlayers.add(player);
                else
                    break;
            }

            List<String> topStrings = new ArrayList<>();
            for (int i = 1; i <= showPlayers; i++) {
                if (topPlayers.size() > i - 1) {
                    OfflinePlayer target = topPlayers.get(i - 1);
                    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
                    int stat = list.get(target);

                    topStrings.add(StringUtil.CC(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT")
                            .replace("%number%", String.valueOf(i))
                            .replace("%player%", target.getName())
                            .replace("%ladder_elo%", String.valueOf(stat))
                            .replace("%division%", (targetProfile.getStats().getDivision() != null ? Common.mmToNormal(targetProfile.getStats().getDivision().getFullName()) : ""))
                            .replace("%division_short%", (targetProfile.getStats().getDivision() != null ? Common.mmToNormal(targetProfile.getStats().getDivision().getShortName()) : ""))
                    ));
                } else
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT-NULL")
                            .replace("%number%", String.valueOf(i))
                    );
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%"))
                    lore.addAll(topStrings);
                else
                    lore.add(line);
            }
        }

        return ClassImport.getClasses().getItemCreateUtil().createItem(ladder.getIcon(), GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.NAME")
                        .replace("%ladder%", ladder.getDisplayName())
                        .replace("%number%", String.valueOf(showPlayers))
                , lore);
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
                if (topPlayers.size() < showPlayers)
                    topPlayers.add(player);
                else
                    break;
            }

            List<String> topStrings = new ArrayList<>();
            for (int i = 1; i <= showPlayers; i++) {
                if (topPlayers.size() > i - 1) {
                    OfflinePlayer target = topPlayers.get(i - 1);
                    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
                    Division division = targetProfile.getStats().getDivision();
                    int stat = list.get(target);

                    topStrings.add(StringUtil.CC(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT")
                            .replace("%number%", String.valueOf(i))
                            .replace("%division%", (division != null ? Common.mmToNormal(division.getFullName()) : ""))
                            .replace("%division_short%", (division != null ? Common.mmToNormal(division.getShortName()) : ""))
                            .replace("%player%", target.getName())
                            .replace("%global_elo%", String.valueOf(stat))
                    ));
                } else
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT-NULL")
                            .replace("%number%", String.valueOf(i))
                    );
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%"))
                    lore.addAll(topStrings);
                else
                    lore.add(line);
            }
        }

        return ClassImport.getClasses().getItemCreateUtil().createItem(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.NAME").replace("%number%", String.valueOf(showPlayers)), Material.valueOf(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.MATERIAL")), lore);
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
                if (topPlayers.size() < showPlayers)
                    topPlayers.add(player);
                else
                    break;
            }

            List<String> topStrings = new ArrayList<>();
            for (int i = 1; i <= showPlayers; i++) {
                if (topPlayers.size() > i - 1) {
                    OfflinePlayer target = topPlayers.get(i - 1);
                    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
                    int stat = list.get(target);

                    topStrings.add(StringUtil.CC(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT")
                            .replace("%number%", String.valueOf(i))
                            .replace("%player%", target.getName())
                            .replace("%ladder_win%", String.valueOf(stat))
                            .replace("%division%", (targetProfile.getStats().getDivision() != null ? Common.mmToNormal(targetProfile.getStats().getDivision().getFullName()) : ""))
                            .replace("%division_short%", (targetProfile.getStats().getDivision() != null ? Common.mmToNormal(targetProfile.getStats().getDivision().getShortName()) : ""))
                    ));
                } else
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT-NULL")
                            .replace("%number%", String.valueOf(i))
                    );
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%"))
                    lore.addAll(topStrings);
                else
                    lore.add(line);
            }
        }

        return ClassImport.getClasses().getItemCreateUtil().createItem(ladder.getIcon(), GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.NAME")
                        .replace("%ladder%", ladder.getDisplayName())
                        .replace("%number%", String.valueOf(showPlayers))
                , lore);
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
                if (topPlayers.size() < showPlayers)
                    topPlayers.add(player);
                else
                    break;
            }

            List<String> topStrings = new ArrayList<>();
            for (int i = 1; i <= showPlayers; i++) {
                if (topPlayers.size() > i - 1) {
                    OfflinePlayer target = topPlayers.get(i - 1);
                    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
                    Division division = targetProfile.getStats().getDivision();
                    int stat = list.get(target);

                    topStrings.add(StringUtil.CC(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT")
                            .replace("%number%", String.valueOf(i))
                            .replace("%division%", (division != null ? Common.mmToNormal(division.getFullName()) : ""))
                            .replace("%division_short%", (division != null ? Common.mmToNormal(division.getShortName()) : ""))
                            .replace("%player%", target.getName())
                            .replace("%global_win%", String.valueOf(stat))
                    ));
                } else
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT-NULL")
                            .replace("%number%", String.valueOf(i))
                    );
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%"))
                    lore.addAll(topStrings);
                else
                    lore.add(line);
            }
        }

        return ClassImport.getClasses().getItemCreateUtil().createItem(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.NAME").replace("%number%", String.valueOf(showPlayers)), Material.valueOf(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.MATERIAL")), lore);
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

        return ClassImport.getClasses().getItemCreateUtil().createItem(
                "&eAuto-Update Info",
                ClassImport.getClasses().getItemMaterialUtil().getClock(),
                lore
        );
    }

}
