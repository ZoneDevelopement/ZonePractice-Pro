package dev.nandi0813.practice.manager.sidebar.adapter;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.type.partyffa.PartyFFA;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partyvsparty.PartyVsParty;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.sidebar.SidebarManager;
import dev.nandi0813.practice.module.util.ClassImport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;

public enum AdapterUtil {
    ;

    private static final String ROUND_SYMBOL = SidebarManager.getInstance().getConfig().getString("MATCH.ROUND-SYMBOL");

    // ==================== Helper Methods ====================

    /**
     * Creates a text replacement config for a simple string replacement
     */
    private static TextReplacementConfig replace(String placeholder, String value) {
        return TextReplacementConfig.builder().match(placeholder).replacement(value).build();
    }

    /**
     * Creates a text replacement config for a component replacement
     */
    private static TextReplacementConfig replace(String placeholder, Component value) {
        return TextReplacementConfig.builder().match(placeholder).replacement(value).build();
    }

    /**
     * Gets player ping as string or "N/A" if offline
     */
    private static String getPingString(Player player) {
        return player.isOnline() ? String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(player)) : "N/A";
    }

    /**
     * Replaces common match placeholders (duration, arena, ladder, ping)
     */
    private static Component replaceCommonMatchPlaceholders(Component line, Match match, Player player) {
        return line
                .replaceText(replace("%duration%", match.getCurrentRound().getFormattedTime()))
                .replaceText(replace("%totalRounds%", String.valueOf(match.getLadder().getRounds())))
                .replaceText(replace("%roundDuration%", match.getCurrentRound().getFormattedTime()))
                .replaceText(replace("%matchDuration%", match.getFormattedTime()))
                .replaceText(replace("%ping%", String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(player))))
                .replaceText(replace("%arena%", match.getArena().getDisplayName()))
                .replaceText(replace("%ladder%", match.getLadder().getDisplayName()));
    }

    /**
     * Replaces team placeholders for a given team prefix (team1, team2, partyTeam, enemyTeam)
     */
    private static Component replaceTeamPlaceholders(Component line, String prefix, TeamEnum team,
                                                     dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers match, int winsNeeded) {
        return line
                .replaceText(replace("%" + prefix + "name%", team.getNameComponent()))
                .replaceText(replace("%" + prefix + "color%", team.getColor()))
                .replaceText(replace("%" + prefix + "players%", String.valueOf(match.getTeamPlayers(team).size())))
                .replaceText(replace("%" + prefix + "alivePlayers%", String.valueOf(match.getTeamAlivePlayers(team).size())))
                .replaceText(replace("%" + prefix + "rounds%", getRoundString(winsNeeded, match.getWonRounds(team))))
                .replaceText(replace("%" + prefix + "roundsNumber%", String.valueOf(match.getWonRounds(team))));
    }

    /**
     * Replaces player placeholders for top players in party FFA
     */
    private static Component replacePartyFFAPlayerPlaceholders(Component line, PartyFFA partyFFA, int rank, Match match) {
        Player player = partyFFA.getTopPlayer(rank);
        if (player == null) return line;

        String prefix = "player" + rank;
        return line
                .replaceText(replace("%" + prefix + "%", player.getName()))
                .replaceText(replace("%" + prefix + "rounds%", getRoundString(match.getWinsNeeded(), partyFFA.getWonRounds(player))))
                .replaceText(replace("%" + prefix + "roundsNumber%", String.valueOf(partyFFA.getWonRounds(player))));
    }

    /**
     * Replaces colored player name placeholders for boxing (team color + player name)
     */
    private static Component replaceColoredPlayerName(Component line, String placeholder, TeamEnum team, Player player) {
        if (player == null) return line.replaceText(replace(placeholder, Component.empty()));
        return line.replaceText(replace(placeholder, team.getColor().append(Component.text(player.getName()))));
    }

    // ==================== Public Methods ====================

    public static Component getRoundString(int rounds, int wonRounds) {
        StringBuilder string = new StringBuilder();
        boolean firstNotWon = true;

        for (int i = 1; i <= rounds; i++) {
            if (i <= wonRounds) {
                string.append(ROUND_SYMBOL);
            } else {
                if (firstNotWon) {
                    string.append("<gray>").append(ROUND_SYMBOL);
                    firstNotWon = false;
                } else {
                    string.append(ROUND_SYMBOL);
                }
            }
        }
        return ZonePractice.getMiniMessage().deserialize(string.toString());
    }

    public static Component replaceMatchPlaceholders(Player player, Component line, Match match) {
        // Replace %player% for non-DUEL matches
        if (match.getType() != MatchType.DUEL) {
            line = line.replaceText(replace("%player%", player.getName()));
        }

        // Replace common placeholders
        line = replaceCommonMatchPlaceholders(line, match, player);

        // Handle match type specific placeholders
        switch (match.getType()) {
            case DUEL:
                return handleDuelPlaceholders(line, (Duel) match, player);
            case PARTY_FFA:
                return handlePartyFFAPlaceholders(line, (PartyFFA) match, player);
            case PARTY_SPLIT:
                return handlePartySplitPlaceholders(line, (PartySplit) match);
            case PARTY_VS_PARTY:
                return handlePartyVsPartyPlaceholders(line, (PartyVsParty) match, player);
        }
        return line;
    }

    private static Component handleDuelPlaceholders(Component line, Duel duel, Player player) {
        Player enemy = duel.getOppositePlayer(player);
        TeamEnum team = duel.getTeam(player);
        TeamEnum enemyTeam = TeamUtil.getOppositeTeam(team);

        // Replace combined placeholders for colored player names (boxing)
        line = replaceColoredPlayerName(line, "%playerTeamColor%%player%", team, player)
                .replaceText(replace("%enemyTeamColor%%enemyName%",
                        enemy == null ? Component.empty() : enemyTeam.getColor().append(Component.text(enemy.getName()))));

        // Replace individual placeholders
        line = line
                .replaceText(replace("%player%", player.getName()))
                .replaceText(replace("%enemyName%", enemy == null ? "" : enemy.getName()));

        // Replace team and round info
        return line
                .replaceText(replace("%playerTeamName%", team.getNameComponent()))
                .replaceText(replace("%playerTeamColor%", team.getColor()))
                .replaceText(replace("%rounds%", getRoundString(duel.getWinsNeeded(), duel.getWonRounds(player))))
                .replaceText(replace("%roundsNumber%", String.valueOf(duel.getWonRounds(player))))
                .replaceText(replace("%enemyRoundsNumber%", enemy == null ? "" : String.valueOf(duel.getWonRounds(enemy))))
                .replaceText(replace("%enemyPing%", enemy == null ? "" : getPingString(enemy)))
                .replaceText(replace("%enemyTeamName%", enemy == null ? Component.empty() : enemyTeam.getNameComponent()))
                .replaceText(replace("%enemyTeamColor%", enemyTeam.getColor()))
                .replaceText(replace("%enemyRounds%", enemy == null ? Component.empty() : getRoundString(duel.getWinsNeeded(), duel.getWonRounds(enemy))));
    }

    private static Component handlePartyFFAPlaceholders(Component line, PartyFFA partyFFA, Player player) {
        // Replace top 3 players
        line = replacePartyFFAPlayerPlaceholders(line, partyFFA, 1, partyFFA);
        line = replacePartyFFAPlayerPlaceholders(line, partyFFA, 2, partyFFA);
        line = replacePartyFFAPlayerPlaceholders(line, partyFFA, 3, partyFFA);

        // Replace general party FFA info
        return line
                .replaceText(replace("%players%", String.valueOf(partyFFA.getPlayers().size())))
                .replaceText(replace("%alivePlayers%", String.valueOf(partyFFA.getAlivePlayers().size())))
                .replaceText(replace("%rounds%", getRoundString(partyFFA.getWinsNeeded(), partyFFA.getWonRounds(player))))
                .replaceText(replace("%roundsNumber%", String.valueOf(partyFFA.getWonRounds(player))));
    }

    private static Component handlePartySplitPlaceholders(Component line, PartySplit partySplit) {
        line = replaceTeamPlaceholders(line, "team1", TeamEnum.TEAM1, partySplit, partySplit.getWinsNeeded());
        line = replaceTeamPlaceholders(line, "team2", TeamEnum.TEAM2, partySplit, partySplit.getWinsNeeded());
        return line;
    }

    private static Component handlePartyVsPartyPlaceholders(Component line, PartyVsParty partyVsParty, Player player) {
        TeamEnum team = partyVsParty.getTeam(player);
        TeamEnum enemyTeam = TeamUtil.getOppositeTeam(team);

        line = replaceTeamPlaceholders(line, "partyTeam", team, partyVsParty, partyVsParty.getWinsNeeded());
        line = replaceTeamPlaceholders(line, "enemyTeam", enemyTeam, partyVsParty, partyVsParty.getWinsNeeded());
        return line;
    }

    public static Component replaceFFAPlaceholders(Player player, Component line, FFA ffa) {
        Statistic statistic = ffa.getStatistics().get(player);

        return line
                .replaceText(replace("%players%", String.valueOf(ffa.getPlayers().size())))
                .replaceText(replace("%spectators%", String.valueOf(ffa.getSpectators().size())))
                .replaceText(replace("%nextReset%", ffa.getBuildRollback() != null ? ffa.getBuildRollback().getFormattedTime() : "N/A"))
                .replaceText(replace("%ping%", String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(player))))
                .replaceText(replace("%ladder%", ffa.getPlayers().get(player).getDisplayName()))
                .replaceText(replace("%arena%", ffa.getArena().getDisplayName()))
                .replaceText(replace("%kills%", String.valueOf(statistic.getKills())))
                .replaceText(replace("%deaths%", String.valueOf(statistic.getDeaths())));
    }

    public static Component replaceFFASpecPlaceholders(Component line, FFA ffa) {
        return line
                .replaceText(replace("%players%", String.valueOf(ffa.getPlayers().size())))
                .replaceText(replace("%spectators%", String.valueOf(ffa.getSpectators().size())))
                .replaceText(replace("%nextReset%", ffa.getBuildRollback() != null ? ffa.getBuildRollback().getFormattedTime() : "N/A"))
                .replaceText(replace("%arena%", ffa.getArena().getDisplayName()));
    }

    public static Component replaceMatchSpectatePlaceholders(Component line, Match match) {
        // Replace common placeholders
        line = line
                .replaceText(replace("%duration%", match.getCurrentRound().getFormattedTime()))
                .replaceText(replace("%totalRounds%", String.valueOf(match.getLadder().getRounds())))
                .replaceText(replace("%roundDuration%", match.getCurrentRound().getFormattedTime()))
                .replaceText(replace("%matchDuration%", match.getFormattedTime()))
                .replaceText(replace("%arena%", match.getArena().getDisplayName()))
                .replaceText(replace("%ladder%", match.getLadder().getDisplayName()));

        // Replace team colors for non-FFA matches
        if (!match.getType().equals(MatchType.PARTY_FFA)) {
            line = line
                    .replaceText(replace("%team1color%", TeamEnum.TEAM1.getColor()))
                    .replaceText(replace("%team1name%", TeamEnum.TEAM1.getNameComponent()))
                    .replaceText(replace("%team2color%", TeamEnum.TEAM2.getColor()))
                    .replaceText(replace("%team2name%", TeamEnum.TEAM2.getNameComponent()));
        }

        // Handle match type specific placeholders
        switch (match.getType()) {
            case DUEL:
                return handleSpectatorDuelPlaceholders(line, (Duel) match);
            case PARTY_FFA:
                return handleSpectatorPartyFFAPlaceholders(line, (PartyFFA) match);
            case PARTY_SPLIT:
                return handleSpectatorPartySplitPlaceholders(line, (PartySplit) match);
            case PARTY_VS_PARTY:
                return handleSpectatorPartyVsPartyPlaceholders(line, (PartyVsParty) match);
        }
        return line;
    }

    private static Component handleSpectatorDuelPlaceholders(Component line, Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();

        // Replace colored player names for boxing
        line = replaceColoredPlayerName(line, "%team1color%%player1%", TeamEnum.TEAM1, player1)
                .replaceText(replace("%team2color%%player2%", TeamEnum.TEAM2.getColor().append(Component.text(player2.getName()))));

        // Replace individual player info
        return line
                .replaceText(replace("%player1%", player1.getName()))
                .replaceText(replace("%player1ping%", getPingString(player1)))
                .replaceText(replace("%player1rounds%", getRoundString(duel.getWinsNeeded(), duel.getWonRounds(player1))))
                .replaceText(replace("%player1roundsNumber%", String.valueOf(duel.getWonRounds(player1))))
                .replaceText(replace("%player2%", player2.getName()))
                .replaceText(replace("%player2ping%", getPingString(player2)))
                .replaceText(replace("%player2rounds%", getRoundString(duel.getWinsNeeded(), duel.getWonRounds(player2))))
                .replaceText(replace("%player2roundsNumber%", String.valueOf(duel.getWonRounds(player2))));
    }

    private static Component handleSpectatorPartyFFAPlaceholders(Component line, PartyFFA partyFFA) {
        // Replace top 3 players
        line = replacePartyFFAPlayerPlaceholders(line, partyFFA, 1, partyFFA);
        line = replacePartyFFAPlayerPlaceholders(line, partyFFA, 2, partyFFA);
        line = replacePartyFFAPlayerPlaceholders(line, partyFFA, 3, partyFFA);

        // Replace general info
        return line
                .replaceText(replace("%players%", String.valueOf(partyFFA.getPlayers().size())))
                .replaceText(replace("%alivePlayers%", String.valueOf(partyFFA.getAlivePlayers().size())));
    }

    private static Component handleSpectatorPartySplitPlaceholders(Component line, PartySplit partySplit) {
        line = replaceTeamPlaceholders(line, "team1", TeamEnum.TEAM1, partySplit, partySplit.getWinsNeeded());
        line = replaceTeamPlaceholders(line, "team2", TeamEnum.TEAM2, partySplit, partySplit.getWinsNeeded());
        return line;
    }

    private static Component handleSpectatorPartyVsPartyPlaceholders(Component line, PartyVsParty partyVsParty) {
        line = replaceTeamPlaceholders(line, "team1", TeamEnum.TEAM1, partyVsParty, partyVsParty.getWinsNeeded());
        line = replaceTeamPlaceholders(line, "team2", TeamEnum.TEAM2, partyVsParty, partyVsParty.getWinsNeeded());
        return line;
    }

}
