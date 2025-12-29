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

    public static ItemStack createProfileStatItem(Profile profile, Player opener) {
        String playerName = profile.getPlayer().getName();
        ItemStack itemStack = ClassImport.getClasses().getItemMaterialUtil().getPlayerHead(profile.getPlayer());
        ItemMeta itemMeta = itemStack.getItemMeta();

        String displayName;
        List<String> lore = new ArrayList<>();
        if (opener.equals(profile.getPlayer())) {
            for (String line : GUIFile.getStringList("GUIS.STATISTICS.SELECTOR.ICONS.OWN-PLAYER-STATS.LORE"))
                lore.add(line.replaceAll("%player%", playerName));

            displayName = GUIFile.getString("GUIS.STATISTICS.SELECTOR.ICONS.OWN-PLAYER-STATS.NAME").replaceAll("%player%", playerName);
        } else {
            for (String line : GUIFile.getStringList("GUIS.STATISTICS.SELECTOR.ICONS.PLAYER-STATS.LORE"))
                lore.add(line.replaceAll("%target%", playerName));

            displayName = GUIFile.getString("GUIS.STATISTICS.SELECTOR.ICONS.PLAYER-STATS.NAME").replaceAll("%target%", playerName);
        }

        itemMeta.setDisplayName(StringUtil.CC(displayName));
        itemMeta.setLore(StringUtil.CC(lore));

        ItemCreateUtil.hideItemFlags(itemMeta);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static GUIItem createLadderStatItem(Profile profile, NormalLadder ladder) {
        GUIItem guiItem = null;
        switch (ladder.getWeightClass()) {
            case UNRANKED:
                guiItem = GUIFile.getGuiItem("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.UNRANKED-LADDER-STATS");
                break;
            case RANKED:
                guiItem = GUIFile.getGuiItem("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.RANKED-LADDER-STATS");
                break;
            case UNRANKED_AND_RANKED:
                guiItem = GUIFile.getGuiItem("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.UNRANKED-RANKED-STATS");
                break;
        }

        switch (ladder.getWeightClass()) {
            case RANKED:
            case UNRANKED_AND_RANKED:
                guiItem.replaceAll("%elo%", String.valueOf(profile.getStats().getLadderStat(ladder).getElo()));
                break;
        }

        guiItem
                .replaceAll("%ladder%", ladder.getDisplayName())
                .replaceAll("%unranked_wins%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins()))
                .replaceAll("%unranked_losses%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses()))
                .replaceAll("%unranked_w/l_ratio%", String.valueOf(profile.getStats().getLadderRatio(ladder, false)))
                .replaceAll("%ranked_wins%", String.valueOf(profile.getStats().getLadderStat(ladder).getRankedWins()))
                .replaceAll("%ranked_losses%", String.valueOf(profile.getStats().getLadderStat(ladder).getRankedLosses()))
                .replaceAll("%ranked_w/l_ratio%", String.valueOf(profile.getStats().getLadderRatio(ladder, true)))
                .replaceAll("%overall_w/l_ratio%", String.valueOf(profile.getStats().getOverallRatio(ladder)))
                .replaceAll("%division%", (profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "&cN/A"))
                .replaceAll("%division_short%", (profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "&cN/A"));

        guiItem.setMaterial(ladder.getIcon().getType());
        guiItem.setDamage(ladder.getIcon().getDurability());

        return guiItem;
    }

    public static ItemStack createProfileAllStatItem(Profile profile) {
        ItemStack itemStack = ClassImport.getClasses().getItemMaterialUtil().getPlayerHead(profile.getPlayer());
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>();

        for (String line : GUIFile.getStringList("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.ALL-STAT.LORE")) {
            lore.add(line
                    .replaceAll("%unranked_wins%", String.valueOf(profile.getStats().getWins(false)))
                    .replaceAll("%unranked_losses%", String.valueOf(profile.getStats().getLosses(false)))
                    .replaceAll("%unranked_w/l_ratio%", String.valueOf(profile.getStats().getRatio(false)))
                    .replaceAll("%ranked_wins%", String.valueOf(profile.getStats().getWins(true)))
                    .replaceAll("%ranked_losses%", String.valueOf(profile.getStats().getLosses(true)))
                    .replaceAll("%ranked_w/l_ratio%", String.valueOf(profile.getStats().getRatio(true)))
                    .replaceAll("%global_wins%", String.valueOf(profile.getStats().getGlobalWins()))
                    .replaceAll("%global_losses%", String.valueOf(profile.getStats().getGlobalLosses()))
                    .replaceAll("%w/l_ratio%", String.valueOf(profile.getStats().getGlobalRatio()))
                    .replaceAll("%global_elo%", String.valueOf(profile.getStats().getGlobalElo()))
                    .replaceAll("%division%", (profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "&cN/A"))
                    .replaceAll("%division_short%", profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "&cN/A")
            );
        }

        itemMeta.setDisplayName(GUIFile.getString("GUIS.STATISTICS.PLAYER-STATISTICS.ICONS.ALL-STAT.NAME").replaceAll("%player%", profile.getPlayer().getName()));
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
                if (topPlayers.size() <= showPlayers)
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
                            .replaceAll("%number%", String.valueOf(i))
                            .replaceAll("%player%", target.getName())
                            .replaceAll("%ladder_elo%", String.valueOf(stat))
                            .replaceAll("%division%", (targetProfile.getStats().getDivision() != null ? Common.mmToNormal(targetProfile.getStats().getDivision().getFullName()) : ""))
                            .replaceAll("%division_short%", (targetProfile.getStats().getDivision() != null ? Common.mmToNormal(targetProfile.getStats().getDivision().getShortName()) : ""))
                    ));
                } else
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT-NULL")
                            .replaceAll("%number%", String.valueOf(i))
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
                        .replaceAll("%ladder%", ladder.getDisplayName())
                        .replaceAll("%number%", String.valueOf(showPlayers))
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
                if (topPlayers.size() <= showPlayers)
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
                            .replaceAll("%number%", String.valueOf(i))
                            .replaceAll("%division%", (division != null ? Common.mmToNormal(division.getFullName()) : ""))
                            .replaceAll("%division_short%", (division != null ? Common.mmToNormal(division.getShortName()) : ""))
                            .replaceAll("%player%", target.getName())
                            .replaceAll("%global_elo%", String.valueOf(stat))
                    ));
                } else
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT-NULL")
                            .replaceAll("%number%", String.valueOf(i))
                    );
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%"))
                    lore.addAll(topStrings);
                else
                    lore.add(line);
            }
        }

        return ClassImport.getClasses().getItemCreateUtil().createItem(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.NAME").replaceAll("%number%", String.valueOf(showPlayers)), Material.valueOf(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.MATERIAL")), lore);
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
                if (topPlayers.size() <= showPlayers)
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
                            .replaceAll("%number%", String.valueOf(i))
                            .replaceAll("%player%", target.getName())
                            .replaceAll("%ladder_win%", String.valueOf(stat))
                            .replaceAll("%division%", (targetProfile.getStats().getDivision() != null ? Common.mmToNormal(targetProfile.getStats().getDivision().getFullName()) : ""))
                            .replaceAll("%division_short%", (targetProfile.getStats().getDivision() != null ? Common.mmToNormal(targetProfile.getStats().getDivision().getShortName()) : ""))
                    ));
                } else
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.LADDER-LEADERBOARD.LORE.FORMAT-NULL")
                            .replaceAll("%number%", String.valueOf(i))
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
                        .replaceAll("%ladder%", ladder.getDisplayName())
                        .replaceAll("%number%", String.valueOf(showPlayers))
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
                if (topPlayers.size() <= showPlayers)
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
                            .replaceAll("%number%", String.valueOf(i))
                            .replaceAll("%division%", (division != null ? Common.mmToNormal(division.getFullName()) : ""))
                            .replaceAll("%division_short%", (division != null ? Common.mmToNormal(division.getShortName()) : ""))
                            .replaceAll("%player%", target.getName())
                            .replaceAll("%global_win%", String.valueOf(stat))
                    ));
                } else
                    topStrings.add(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.FORMAT-NULL")
                            .replaceAll("%number%", String.valueOf(i))
                    );
            }

            for (String line : GUIFile.getStringList("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.LORE.LEADERBOARD")) {
                if (line.contains("%top%"))
                    lore.addAll(topStrings);
                else
                    lore.add(line);
            }
        }

        return ClassImport.getClasses().getItemCreateUtil().createItem(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.NAME").replaceAll("%number%", String.valueOf(showPlayers)), Material.valueOf(GUIFile.getString("GUIS.STATISTICS.WIN-LEADERBOARD.ICONS.GLOBAL-LEADERBOARD.MATERIAL")), lore);
    }

    public static ItemStack getRefreshItem() {
        return GUIFile.getGuiItem("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.REFRESH-ITEM").get();
    }

}
