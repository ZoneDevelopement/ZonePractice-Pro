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

    public static Component getRoundString(int rounds, int wonRounds) {
        String string = "";

        boolean firstNotWon = true;
        for (int i = 1; i <= rounds; i++) {
            if (i <= wonRounds)
                string = string.concat(ROUND_SYMBOL);
            else {
                if (firstNotWon) {
                    string = string.concat("<gray>" + ROUND_SYMBOL);
                    firstNotWon = false;
                } else
                    string = string.concat(ROUND_SYMBOL);
            }
        }
        return ZonePractice.getMiniMessage().deserialize(string);
    }

    public static Component replaceMatchPlaceholders(Player player, Component line, Match match) {
        line = line
                .replaceText(TextReplacementConfig.builder().match("%player%").replacement(player.getName()).build())
                .replaceText(TextReplacementConfig.builder().match("%duration%").replacement(match.getCurrentRound().getFormattedTime()).build())
                .replaceText(TextReplacementConfig.builder().match("%totalRounds%").replacement(String.valueOf(match.getLadder().getRounds())).build())
                .replaceText(TextReplacementConfig.builder().match("%roundDuration%").replacement(match.getCurrentRound().getFormattedTime()).build())
                .replaceText(TextReplacementConfig.builder().match("%matchDuration%").replacement(match.getFormattedTime()).build())
                .replaceText(TextReplacementConfig.builder().match("%ping%").replacement(String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(player))).build())
                .replaceText(TextReplacementConfig.builder().match("%arena%").replacement(match.getArena().getDisplayName()).build())
                .replaceText(TextReplacementConfig.builder().match("%ladder%").replacement(match.getLadder().getDisplayName()).build());

        TeamEnum team, enemyTeam;
        switch (match.getType()) {
            case DUEL:
                Duel duel = (Duel) match;
                Player enemy = duel.getOppositePlayer(player);
                team = duel.getTeam(player);
                enemyTeam = TeamUtil.getOppositeTeam(team);
                return line
                        .replaceText(TextReplacementConfig.builder().match("%playerTeamName%").replacement(team.getNameComponent()).build())
                        .replaceText(TextReplacementConfig.builder().match("%playerTeamColor%").replacement(team.getColor()).build())
                        .replaceText(TextReplacementConfig.builder().match("%rounds%").replacement(getRoundString(match.getWinsNeeded(), duel.getWonRounds(player))).build())
                        .replaceText(TextReplacementConfig.builder().match("%roundsNumber%").replacement(String.valueOf(duel.getWonRounds(player))).build())
                        .replaceText(TextReplacementConfig.builder()
                                .match("%enemyRoundsNumber%")
                                .replacement(enemy == null ? "" : String.valueOf(duel.getWonRounds(enemy)))
                                .build())
                        .replaceText(TextReplacementConfig.builder()
                                .match("%enemyName%")
                                .replacement(enemy == null ? "" : enemy.getName())
                                .build())
                        .replaceText(TextReplacementConfig.builder()
                                .match("%enemyPing%")
                                .replacement(enemy == null ? "" : (enemy.isOnline() ? String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(enemy)) : "N/A"))
                                .build())
                        .replaceText(TextReplacementConfig.builder()
                                .match("%enemyTeamName%")
                                .replacement(enemy == null ? Component.empty() : enemyTeam.getNameComponent())
                                .build())
                        .replaceText(TextReplacementConfig.builder()
                                .match("%enemyTeamColor%")
                                .replacement(enemy == null ? Component.empty() : enemyTeam.getColor())
                                .build())
                        .replaceText(TextReplacementConfig.builder()
                                .match("%enemyRounds%")
                                .replacement(enemy == null ? Component.empty() : getRoundString(match.getWinsNeeded(), duel.getWonRounds(enemy)))
                                .build());
            case PARTY_FFA:
                PartyFFA partyFFA = (PartyFFA) match;
                Player player1 = partyFFA.getTopPlayer(1);
                if (player1 != null) {
                    line = line
                            .replaceText(TextReplacementConfig.builder().match("%player1%").replacement(player1.getName()).build())
                            .replaceText(TextReplacementConfig.builder().match("%player1rounds%").replacement(AdapterUtil.getRoundString(match.getWinsNeeded(), partyFFA.getWonRounds(player1))).build())
                            .replaceText(TextReplacementConfig.builder().match("%player1roundsNumber%").replacement(String.valueOf(partyFFA.getWonRounds(player1))).build());
                }
                Player player2 = partyFFA.getTopPlayer(2);
                if (player2 != null) {
                    line = line
                            .replaceText(TextReplacementConfig.builder().match("%player2%").replacement(player2.getName()).build())
                            .replaceText(TextReplacementConfig.builder().match("%player2rounds%").replacement(AdapterUtil.getRoundString(match.getWinsNeeded(), partyFFA.getWonRounds(player2))).build())
                            .replaceText(TextReplacementConfig.builder().match("%player2roundsNumber%").replacement(String.valueOf(partyFFA.getWonRounds(player2))).build());
                }
                Player player3 = partyFFA.getTopPlayer(3);
                if (player3 != null) {
                    line = line
                            .replaceText(TextReplacementConfig.builder().match("%player3%").replacement(player3.getName()).build())
                            .replaceText(TextReplacementConfig.builder().match("%player3rounds%").replacement(AdapterUtil.getRoundString(match.getWinsNeeded(), partyFFA.getWonRounds(player3))).build())
                            .replaceText(TextReplacementConfig.builder().match("%player3roundsNumber%").replacement(String.valueOf(partyFFA.getWonRounds(player3))).build());
                }
                return line
                        .replaceText(TextReplacementConfig.builder().match("%players%").replacement(String.valueOf(partyFFA.getPlayers().size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%alivePlayers%").replacement(String.valueOf(partyFFA.getAlivePlayers().size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%rounds%").replacement(AdapterUtil.getRoundString(match.getWinsNeeded(), partyFFA.getWonRounds(player))).build())
                        .replaceText(TextReplacementConfig.builder().match("%roundsNumber%").replacement(String.valueOf(partyFFA.getWonRounds(player))).build());
            case PARTY_SPLIT:
                PartySplit partySplit = (PartySplit) match;
                return line
                        .replaceText(TextReplacementConfig.builder().match("%team1name%").replacement(TeamEnum.TEAM1.getNameComponent()).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1color%").replacement(TeamEnum.TEAM1.getColor()).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1players%").replacement(String.valueOf(partySplit.getTeamPlayers(TeamEnum.TEAM1).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1alivePlayers%").replacement(String.valueOf(partySplit.getTeamAlivePlayers(TeamEnum.TEAM1).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1rounds%").replacement(getRoundString(match.getWinsNeeded(), partySplit.getWonRounds(TeamEnum.TEAM1))).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1roundsNumber%").replacement(String.valueOf(partySplit.getWonRounds(TeamEnum.TEAM1))).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2name%").replacement(TeamEnum.TEAM2.getNameComponent()).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2color%").replacement(TeamEnum.TEAM2.getColor()).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2players%").replacement(String.valueOf(partySplit.getTeamPlayers(TeamEnum.TEAM2).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2alivePlayers%").replacement(String.valueOf(partySplit.getTeamAlivePlayers(TeamEnum.TEAM2).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2rounds%").replacement(getRoundString(match.getWinsNeeded(), partySplit.getWonRounds(TeamEnum.TEAM2))).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2roundsNumber%").replacement(String.valueOf(partySplit.getWonRounds(TeamEnum.TEAM2))).build());
            case PARTY_VS_PARTY:
                PartyVsParty partyVsParty = (PartyVsParty) match;
                team = partyVsParty.getTeam(player);
                enemyTeam = TeamUtil.getOppositeTeam(team);
                return line
                        .replaceText(TextReplacementConfig.builder().match("%partyTeamName%").replacement(team.getNameComponent()).build())
                        .replaceText(TextReplacementConfig.builder().match("%partyTeamColor%").replacement(team.getColor()).build())
                        .replaceText(TextReplacementConfig.builder().match("%partyTeamPlayers%").replacement(String.valueOf(partyVsParty.getTeamPlayers(team).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%partyTeamAlivePlayers%").replacement(String.valueOf(partyVsParty.getTeamAlivePlayers(team).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%partyTeamRounds%").replacement(getRoundString(match.getWinsNeeded(), partyVsParty.getWonRounds(team))).build())
                        .replaceText(TextReplacementConfig.builder().match("%partyTeamRoundsNumber%").replacement(String.valueOf(partyVsParty.getWonRounds(team))).build())
                        .replaceText(TextReplacementConfig.builder().match("%enemyTeamName%").replacement(enemyTeam.getNameComponent()).build())
                        .replaceText(TextReplacementConfig.builder().match("%enemyTeamColor%").replacement(enemyTeam.getColor()).build())
                        .replaceText(TextReplacementConfig.builder().match("%enemyTeamPlayers%").replacement(String.valueOf(partyVsParty.getTeamPlayers(enemyTeam).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%enemyTeamAlivePlayers%").replacement(String.valueOf(partyVsParty.getTeamAlivePlayers(enemyTeam).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%enemyTeamRounds%").replacement(getRoundString(match.getWinsNeeded(), partyVsParty.getWonRounds(enemyTeam))).build())
                        .replaceText(TextReplacementConfig.builder().match("%enemyTeamRoundsNumber%").replacement(String.valueOf(partyVsParty.getWonRounds(enemyTeam))).build());
        }
        return line;
    }

    public static Component replaceFFAPlaceholders(Player player, Component line, FFA ffa) {
        Statistic statistic = ffa.getStatistics().get(player);

        return line
                .replaceText(TextReplacementConfig.builder().match("%players%").replacement(String.valueOf(ffa.getPlayers().size())).build())
                .replaceText(TextReplacementConfig.builder().match("%spectators%").replacement(String.valueOf(ffa.getSpectators().size())).build())
                .replaceText(TextReplacementConfig.builder().match("%nextReset%").replacement(ffa.getBuildRollback() != null ? ffa.getBuildRollback().getFormattedTime() : "N/A").build())
                .replaceText(TextReplacementConfig.builder().match("%ping%").replacement(String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(player))).build())
                .replaceText(TextReplacementConfig.builder().match("%ladder%").replacement(ffa.getPlayers().get(player).getDisplayName()).build())
                .replaceText(TextReplacementConfig.builder().match("%arena%").replacement(ffa.getArena().getDisplayName()).build())
                .replaceText(TextReplacementConfig.builder().match("%kills%").replacement(String.valueOf(statistic.getKills())).build())
                .replaceText(TextReplacementConfig.builder().match("%deaths%").replacement(String.valueOf(statistic.getDeaths())).build());
    }

    public static Component replaceFFASpecPlaceholders(Component line, FFA ffa) {
        return line
                .replaceText(TextReplacementConfig.builder().match("%players%").replacement(String.valueOf(ffa.getPlayers().size())).build())
                .replaceText(TextReplacementConfig.builder().match("%spectators%").replacement(String.valueOf(ffa.getSpectators().size())).build())
                .replaceText(TextReplacementConfig.builder().match("%nextReset%").replacement(ffa.getBuildRollback() != null ? ffa.getBuildRollback().getFormattedTime() : "N/A").build())
                .replaceText(TextReplacementConfig.builder().match("%arena%").replacement(ffa.getArena().getDisplayName()).build());
    }

    public static Component replaceMatchSpectatePlaceholders(Component line, Match match) {
        line = line
                .replaceText(TextReplacementConfig.builder().match("%duration%").replacement(match.getCurrentRound().getFormattedTime()).build())
                .replaceText(TextReplacementConfig.builder().match("%totalRounds%").replacement(String.valueOf(match.getLadder().getRounds())).build())
                .replaceText(TextReplacementConfig.builder().match("%roundDuration%").replacement(match.getCurrentRound().getFormattedTime()).build())
                .replaceText(TextReplacementConfig.builder().match("%matchDuration%").replacement(match.getFormattedTime()).build())
                .replaceText(TextReplacementConfig.builder().match("%arena%").replacement(match.getArena().getDisplayName()).build())
                .replaceText(TextReplacementConfig.builder().match("%ladder%").replacement(match.getLadder().getDisplayName()).build());

        if (!match.getType().equals(MatchType.PARTY_FFA)) {
            line = line
                    .replaceText(TextReplacementConfig.builder().match("%team1color%").replacement(TeamEnum.TEAM1.getColor()).build())
                    .replaceText(TextReplacementConfig.builder().match("%team1name%").replacement(TeamEnum.TEAM1.getNameComponent()).build())
                    .replaceText(TextReplacementConfig.builder().match("%team2color%").replacement(TeamEnum.TEAM2.getColor()).build())
                    .replaceText(TextReplacementConfig.builder().match("%team2name%").replacement(TeamEnum.TEAM2.getNameComponent()).build());
        }

        switch (match.getType()) {
            case DUEL:
                Duel duel = (Duel) match;
                return line
                        .replaceText(TextReplacementConfig.builder().match("%player1%").replacement(duel.getPlayer1().getName()).build())
                        .replaceText(TextReplacementConfig.builder().match("%player1ping%").replacement(duel.getPlayer1().isOnline() ? String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(duel.getPlayer1())) : "N/A").build())
                        .replaceText(TextReplacementConfig.builder().match("%player1rounds%").replacement(AdapterUtil.getRoundString(match.getWinsNeeded(), duel.getWonRounds(duel.getPlayer1()))).build())
                        .replaceText(TextReplacementConfig.builder().match("%player1roundsNumber%").replacement(String.valueOf(duel.getWonRounds(duel.getPlayer1()))).build())
                        .replaceText(TextReplacementConfig.builder().match("%player2%").replacement(duel.getPlayer2().getName()).build())
                        .replaceText(TextReplacementConfig.builder().match("%player2ping%").replacement(duel.getPlayer2().isOnline() ? String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(duel.getPlayer2())) : "N/A").build())
                        .replaceText(TextReplacementConfig.builder().match("%player2rounds%").replacement(AdapterUtil.getRoundString(match.getWinsNeeded(), duel.getWonRounds(duel.getPlayer2()))).build())
                        .replaceText(TextReplacementConfig.builder().match("%player2roundsNumber%").replacement(String.valueOf(duel.getWonRounds(duel.getPlayer2()))).build());
            case PARTY_FFA:
                PartyFFA partyFFA = (PartyFFA) match;
                Player player1 = partyFFA.getTopPlayer(1);
                if (player1 != null) {
                    line = line
                            .replaceText(TextReplacementConfig.builder().match("%player1%").replacement(player1.getName()).build())
                            .replaceText(TextReplacementConfig.builder().match("%player1rounds%").replacement(AdapterUtil.getRoundString(match.getWinsNeeded(), partyFFA.getWonRounds(player1))).build())
                            .replaceText(TextReplacementConfig.builder().match("%player1roundsNumber%").replacement(String.valueOf(partyFFA.getWonRounds(player1))).build());
                }
                Player player2 = partyFFA.getTopPlayer(2);
                if (player2 != null) {
                    line = line
                            .replaceText(TextReplacementConfig.builder().match("%player2%").replacement(player2.getName()).build())
                            .replaceText(TextReplacementConfig.builder().match("%player2rounds%").replacement(AdapterUtil.getRoundString(match.getWinsNeeded(), partyFFA.getWonRounds(player2))).build())
                            .replaceText(TextReplacementConfig.builder().match("%player2roundsNumber%").replacement(String.valueOf(partyFFA.getWonRounds(player2))).build());
                }
                Player player3 = partyFFA.getTopPlayer(3);
                if (player3 != null) {
                    line = line
                            .replaceText(TextReplacementConfig.builder().match("%player3%").replacement(player3.getName()).build())
                            .replaceText(TextReplacementConfig.builder().match("%player3rounds%").replacement(AdapterUtil.getRoundString(match.getWinsNeeded(), partyFFA.getWonRounds(player3))).build())
                            .replaceText(TextReplacementConfig.builder().match("%player3roundsNumber%").replacement(String.valueOf(partyFFA.getWonRounds(player3))).build());
                }
                return line
                        .replaceText(TextReplacementConfig.builder().match("%players%").replacement(String.valueOf(partyFFA.getPlayers().size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%alivePlayers%").replacement(String.valueOf(partyFFA.getAlivePlayers().size())).build());
            case PARTY_SPLIT:
                PartySplit partySplit = (PartySplit) match;
                return line
                        .replaceText(TextReplacementConfig.builder().match("%team1players%").replacement(String.valueOf(partySplit.getTeamPlayers(TeamEnum.TEAM1).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1alivePlayers%").replacement(String.valueOf(partySplit.getTeamAlivePlayers(TeamEnum.TEAM1).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1rounds%").replacement(getRoundString(match.getWinsNeeded(), partySplit.getWonRounds(TeamEnum.TEAM1))).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1roundsNumber%").replacement(String.valueOf(partySplit.getWonRounds(TeamEnum.TEAM1))).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2players%").replacement(String.valueOf(partySplit.getTeamPlayers(TeamEnum.TEAM2).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2alivePlayers%").replacement(String.valueOf(partySplit.getTeamAlivePlayers(TeamEnum.TEAM2).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2rounds%").replacement(getRoundString(match.getWinsNeeded(), partySplit.getWonRounds(TeamEnum.TEAM2))).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2roundsNumber%").replacement(String.valueOf(partySplit.getWonRounds(TeamEnum.TEAM2))).build());
            case PARTY_VS_PARTY:
                PartyVsParty partyVsParty = (PartyVsParty) match;
                return line
                        .replaceText(TextReplacementConfig.builder().match("%team1players%").replacement(String.valueOf(partyVsParty.getTeamPlayers(TeamEnum.TEAM1).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1alivePlayers%").replacement(String.valueOf(partyVsParty.getTeamAlivePlayers(TeamEnum.TEAM1).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1rounds%").replacement(getRoundString(match.getWinsNeeded(), partyVsParty.getWonRounds(TeamEnum.TEAM1))).build())
                        .replaceText(TextReplacementConfig.builder().match("%team1roundsNumber%").replacement(String.valueOf(partyVsParty.getWonRounds(TeamEnum.TEAM1))).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2players%").replacement(String.valueOf(partyVsParty.getTeamPlayers(TeamEnum.TEAM2).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2alivePlayers%").replacement(String.valueOf(partyVsParty.getTeamAlivePlayers(TeamEnum.TEAM2).size())).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2rounds%").replacement(getRoundString(match.getWinsNeeded(), partyVsParty.getWonRounds(TeamEnum.TEAM2))).build())
                        .replaceText(TextReplacementConfig.builder().match("%team2roundsNumber%").replacement(String.valueOf(partyVsParty.getWonRounds(TeamEnum.TEAM2))).build());
        }
        return line;
    }

}
