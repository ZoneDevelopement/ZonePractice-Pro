package dev.nandi0813.practice.manager.sidebar.adapter;

import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.type.partyffa.PartyFFA;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partyvsparty.PartyVsParty;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.sidebar.SidebarManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.NameFormatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.RegExp;

public enum AdapterUtil {
    ;

    private static String getRoundSymbol() {
        String symbol = SidebarManager.getInstance().getConfig().getString("MATCH.ROUND-SYMBOL");
        return symbol == null || symbol.isBlank() ? "|" : symbol;
    }

    private static int getRoundSymbolMaxRounds() {
        if (!SidebarManager.getInstance().getConfig().contains("MATCH.ROUND-SYMBOL-MAX-ROUNDS")) {
            return 3;
        }
        return Math.max(0, SidebarManager.getInstance().getConfig().getInt("MATCH.ROUND-SYMBOL-MAX-ROUNDS"));
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a text replacement config for a simple string replacement
     */
    private static TextReplacementConfig replace(@RegExp String placeholder, String value) {
        return TextReplacementConfig.builder().match(placeholder).replacement(value).build();
    }

    /**
     * Creates a text replacement config for a component replacement
     */
    private static TextReplacementConfig replace(@RegExp String placeholder, Component value) {
        return TextReplacementConfig.builder().match(placeholder).replacement(value).build();
    }

    /**
     * Gets player ping as string or "N/A" if offline
     */
    private static String getPingString(Player player) {
        return player.isOnline() ? String.valueOf(PlayerUtil.getPing(player)) : "N/A";
    }

    /**
     * Uses current round time when available, otherwise falls back to "0" during round transitions.
     */
    private static String getRoundDurationString(Match match) {
        Round currentRound = match.getCurrentRound();
        return currentRound != null ? currentRound.getFormattedTime() : "0";
    }

    /**
     * Replaces common match placeholders (duration, arena, ladder, ping)
     */
    private static Component replaceCommonMatchPlaceholders(Component line, Match match, Player player) {
        String roundDuration = getRoundDurationString(match);
        return line
                .replaceText(replace("%duration%", roundDuration))
                .replaceText(replace("%totalRounds%", String.valueOf(match.getLadder().getRounds())))
                .replaceText(replace("%roundDuration%", roundDuration))
                .replaceText(replace("%matchDuration%", match.getFormattedTime()))
                .replaceText(replace("%ping%", String.valueOf(PlayerUtil.getPing(player))))
                .replaceText(replace("%arena%", Common.deserializeMiniMessage(match.getArena().getDisplayName())))
                .replaceText(replace("%ladder%", Common.deserializeMiniMessage(match.getLadder().getDisplayName())));
    }

    /**
     * Replaces team placeholders for a given team prefix (team1, team2, partyTeam, enemyTeam)
     */
    private static Component replaceTeamPlaceholders(Component line, String prefix, TeamEnum team,
                                                     dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers match, int winsNeeded) {
        Component rounds = getRoundString(winsNeeded, match.getWonRounds(team));
        Component coloredRounds = team.getColor().append(getRoundString(winsNeeded, match.getWonRounds(team), team.getColor()));

        return line
                .replaceText(replace("%" + prefix + "name%", team.getNameComponent()))
                .replaceText(replace("%" + prefix + "color%", team.getColor()))
                .replaceText(replace("%" + prefix + "players%", String.valueOf(match.getTeamPlayers(team).size())))
                .replaceText(replace("%" + prefix + "alivePlayers%", String.valueOf(match.getTeamAlivePlayers(team).size())))
                .replaceText(replace("%" + prefix + "color%%" + prefix + "rounds%", coloredRounds))
                .replaceText(replace("%" + prefix + "rounds%", rounds))
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
                .replaceText(replace("%" + prefix + "%", getSidebarName(player)))
                .replaceText(replace("%" + prefix + "rounds%", getRoundString(match.getWinsNeeded(), partyFFA.getWonRounds(player))))
                .replaceText(replace("%" + prefix + "roundsNumber%", String.valueOf(partyFFA.getWonRounds(player))));
    }

    /**
     * Replaces colored player name placeholders for boxing (team color + player name)
     */
    private static Component replaceColoredPlayerName(Component line, @RegExp String placeholder, TeamEnum team, Player player) {
        if (player == null) return line.replaceText(replace(placeholder, Component.empty()));
        return line.replaceText(replace(placeholder, team.getColor().append(getSidebarName(player))));
    }

    private static Component getSidebarName(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) {
            return Component.text(player.getName());
        }

        // Sidebar placeholders follow the player's own prefix/suffix visibility setting.
        // Pass the Player instance so PlaceholderAPI expansions (e.g. %luckperms_prefix%)
        // are resolved at render time rather than left as raw placeholder text.
        return NameFormatUtil.resolveFullName(profile, player, player.getName());
    }

    // ==================== Public Methods ====================

    public static Component getRoundString(int rounds, int wonRounds) {
        return getRoundString(rounds, wonRounds, null);
    }

    public static Component getRoundString(int rounds, int wonRounds, Component wonColor) {
        if (rounds <= 0) {
            return Component.empty();
        }

        int clampedWonRounds = Math.max(0, Math.min(wonRounds, rounds));
        int symbolMaxRounds = getRoundSymbolMaxRounds();
        if (symbolMaxRounds > 0 && rounds > symbolMaxRounds) {
            String counter = clampedWonRounds + "/" + rounds;
            return wonColor == null ? Component.text(counter) : wonColor.append(Component.text(counter));
        }

        Component component = Component.empty();
        String roundSymbol = getRoundSymbol();
        Component wonSymbol = wonColor == null
                ? Component.text(roundSymbol)
                : wonColor.append(Component.text(roundSymbol));
        Component notWonSymbol = Component.text(roundSymbol, NamedTextColor.GRAY);

        for (int i = 1; i <= rounds; i++) {
            if (i <= clampedWonRounds) {
                component = component.append(wonSymbol);
            } else {
                component = component.append(notWonSymbol);
            }
        }

        return component;
    }

    public static Component replaceMatchPlaceholders(Player player, Component line, Match match) {
        // Replace %player% for non-DUEL matches
        if (match.getType() != MatchType.DUEL) {
            line = line.replaceText(replace("%player%", getSidebarName(player)));
        }

        // Replace common placeholders
        line = replaceCommonMatchPlaceholders(line, match, player);

        // Handle match type specific placeholders
        return switch (match.getType()) {
            case DUEL -> handleDuelPlaceholders(line, (Duel) match, player);
            case PARTY_FFA -> handlePartyFFAPlaceholders(line, (PartyFFA) match, player);
            case PARTY_SPLIT -> handlePartySplitPlaceholders(line, (PartySplit) match);
            case PARTY_VS_PARTY -> handlePartyVsPartyPlaceholders(line, (PartyVsParty) match, player);
        };
    }

    private static Component handleDuelPlaceholders(Component line, Duel duel, Player player) {
        Player enemy = duel.getOppositePlayer(player);
        TeamEnum team = duel.getTeam(player);
        TeamEnum enemyTeam = TeamUtil.getOppositeTeam(team);

        // Replace combined placeholders for colored player names (boxing)
        line = replaceColoredPlayerName(line, "%playerTeamColor%%player%", team, player)
                .replaceText(replace("%playerTeamColor%%rounds%",
                        team.getColor().append(getRoundString(duel.getWinsNeeded(), duel.getWonRounds(player), team.getColor()))))
                .replaceText(replace("%enemyTeamColor%%enemyName%",
                        enemy == null ? Component.empty() : enemyTeam.getColor().append(getSidebarName(enemy))))
                .replaceText(replace("%enemyTeamColor%%enemyRounds%",
                        enemy == null ? Component.empty() : enemyTeam.getColor().append(getRoundString(duel.getWinsNeeded(), duel.getWonRounds(enemy), enemyTeam.getColor()))));

        // Replace individual placeholders
        line = line
                .replaceText(replace("%player%", getSidebarName(player)))
                .replaceText(replace("%enemyName%", enemy == null ? Component.empty() : getSidebarName(enemy)));

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
                .replaceText(replace("%ping%", String.valueOf(PlayerUtil.getPing(player))))
                .replaceText(replace("%ladder%", Common.deserializeMiniMessage(ffa.getPlayers().get(player).getDisplayName())))
                .replaceText(replace("%arena%", Common.deserializeMiniMessage(ffa.getArena().getDisplayName())))
                .replaceText(replace("%kills%", String.valueOf(statistic.getKills())))
                .replaceText(replace("%deaths%", String.valueOf(statistic.getDeaths())));
    }

    public static Component replaceFFASpecPlaceholders(Component line, FFA ffa) {
        return line
                .replaceText(replace("%players%", String.valueOf(ffa.getPlayers().size())))
                .replaceText(replace("%spectators%", String.valueOf(ffa.getSpectators().size())))
                .replaceText(replace("%nextReset%", ffa.getBuildRollback() != null ? ffa.getBuildRollback().getFormattedTime() : "N/A"))
                .replaceText(replace("%arena%", Common.deserializeMiniMessage(ffa.getArena().getDisplayName())));
    }

    public static Component replaceMatchSpectatePlaceholders(Component line, Match match) {
        String roundDuration = getRoundDurationString(match);
        // Replace common placeholders
        line = line
                .replaceText(replace("%duration%", roundDuration))
                .replaceText(replace("%totalRounds%", String.valueOf(match.getLadder().getRounds())))
                .replaceText(replace("%roundDuration%", roundDuration))
                .replaceText(replace("%matchDuration%", match.getFormattedTime()))
                .replaceText(replace("%arena%", Common.deserializeMiniMessage(match.getArena().getDisplayName())))
                .replaceText(replace("%ladder%", Common.deserializeMiniMessage(match.getLadder().getDisplayName())));

        // Replace team colors for non-FFA matches
        if (!match.getType().equals(MatchType.PARTY_FFA)) {
            line = line
                    .replaceText(replace("%team1color%", TeamEnum.TEAM1.getColor()))
                    .replaceText(replace("%team1name%", TeamEnum.TEAM1.getNameComponent()))
                    .replaceText(replace("%team2color%", TeamEnum.TEAM2.getColor()))
                    .replaceText(replace("%team2name%", TeamEnum.TEAM2.getNameComponent()));
        }

        // Handle match type specific placeholders
        return switch (match.getType()) {
            case DUEL -> handleSpectatorDuelPlaceholders(line, (Duel) match);
            case PARTY_FFA -> handleSpectatorPartyFFAPlaceholders(line, (PartyFFA) match);
            case PARTY_SPLIT -> handleSpectatorPartySplitPlaceholders(line, (PartySplit) match);
            case PARTY_VS_PARTY -> handleSpectatorPartyVsPartyPlaceholders(line, (PartyVsParty) match);
        };
    }

    private static Component handleSpectatorDuelPlaceholders(Component line, Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();

        // Replace colored player names for boxing
        line = replaceColoredPlayerName(line, "%team1color%%player1%", TeamEnum.TEAM1, player1)
                .replaceText(replace("%team2color%%player2%", TeamEnum.TEAM2.getColor().append(getSidebarName(player2))))
                .replaceText(replace("%team1color%%player1rounds%",
                        TeamEnum.TEAM1.getColor().append(getRoundString(duel.getWinsNeeded(), duel.getWonRounds(player1), TeamEnum.TEAM1.getColor()))))
                .replaceText(replace("%team2color%%player2rounds%",
                        TeamEnum.TEAM2.getColor().append(getRoundString(duel.getWinsNeeded(), duel.getWonRounds(player2), TeamEnum.TEAM2.getColor()))));

        // Replace individual player info
        return line
                .replaceText(replace("%player1%", getSidebarName(player1)))
                .replaceText(replace("%player1ping%", getPingString(player1)))
                .replaceText(replace("%player1rounds%", getRoundString(duel.getWinsNeeded(), duel.getWonRounds(player1))))
                .replaceText(replace("%player1roundsNumber%", String.valueOf(duel.getWonRounds(player1))))
                .replaceText(replace("%player2%", getSidebarName(player2)))
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
