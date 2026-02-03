package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.type.partyffa.PartyFFA;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partyvsparty.PartyVsParty;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum EndMessageUtil {
    ;

    public static List<String> getEndMessage(Duel duel, List<String> rankedExtension) {
        Player winner = duel.getMatchWinner();
        Player loser = duel.getOppositePlayer(winner);

        List<String> message = new ArrayList<>();

        for (String line : LanguageManager.getList("MATCH.DUEL.MATCH-END.MESSAGE")) {
            if (line.contains("%spectatorExtension%")) {
                List<String> spectatorNames = new ArrayList<>();
                for (Player spectator : duel.getSpectators()) {
                    Profile spectatorProfile = ProfileManager.getInstance().getProfile(spectator);
                    if (!spectatorProfile.isHideFromPlayers())
                        spectatorNames.add(spectator.getName());
                }

                if (!spectatorNames.isEmpty()) {
                    for (String line2 : LanguageManager.getList("MATCH.DUEL.MATCH-END.SPECTATOR-EXTENSION")) {
                        message.add(line2
                                .replace("%size%", String.valueOf(spectatorNames.size()))
                                .replace("%spectators%", spectatorNames.toString().replace("[", "").replace("]", "")));
                    }
                }
            } else if (line.contains("%rankedExtension%")) {
                if (duel.isRanked())
                    message.addAll(rankedExtension);
            } else {
                message.add(line
                        .replace("%matchId%", duel.getId())
                        .replace("%winner%", winner.getName())
                        .replace("%winner_uuid%", ProfileManager.getInstance().getUuids().get(winner).toString())
                        .replace("%loser%", loser.getName())
                        .replace("%loser_uuid%", ProfileManager.getInstance().getUuids().get(loser).toString())
                );
            }
        }

        return message;
    }

    public static List<String> getEndMessage(PartyFFA partyFFA, List<Player> losers) {
        Player winner = partyFFA.getMatchWinner();
        List<String> message = new ArrayList<>();

        String losersString = "";
        for (Player loser : losers) {
            String loserString;

            if (partyFFA.getPlayers().contains(loser)) {
                loserString = LanguageManager.getString("MATCH.PARTY-FFA.MATCH-END.LOSER-PLAYER-FORMAT")
                        .replace("%matchId%", partyFFA.getId())
                        .replace("%player%", loser.getName())
                        .replace("%player_uuid%", ProfileManager.getInstance().getUuids().get(loser).toString());
            } else {
                loserString = LanguageManager.getString("MATCH.PARTY-FFA.MATCH-END.LEFT-PLAYER-FORMAT")
                        .replace("%player%", loser.getName());
            }

            losersString = losersString.concat(loserString);
            if (!losers.get(losers.size() - 1).equals(loser))
                losersString = losersString.concat(LanguageManager.getString("MATCH.PARTY-FFA.MATCH-END.SEPARATOR-FORMAT"));
        }

        for (String line : LanguageManager.getList("MATCH.PARTY-FFA.MATCH-END.MESSAGE")) {
            if (line.contains("%spectatorExtension%")) {
                List<String> spectators = new ArrayList<>();
                for (Player spectator : partyFFA.getSpectators()) {
                    Profile spectatorProfile = ProfileManager.getInstance().getProfile(spectator);
                    if (!spectatorProfile.isHideFromPlayers())
                        spectators.add(spectator.getName());
                }

                if (!spectators.isEmpty()) {
                    for (String line2 : LanguageManager.getList("MATCH.PARTY-FFA.MATCH-END.SPECTATOR-EXTENSION")) {
                        message.add(line2
                                .replace("%size%", String.valueOf(spectators.size()))
                                .replace("%spectators%", spectators.toString().replace("[", "").replace("]", "")));
                    }
                }
            } else {
                message.add(line
                        .replace("%matchId%", partyFFA.getId())
                        .replace("%losers%", losersString)
                        .replace("%winner%", winner.getName())
                        .replace("%winner_uuid%", ProfileManager.getInstance().getUuids().get(winner).toString())
                );
            }
        }

        return message;
    }

    public static List<String> getEndMessage(PartySplit partySplit, TeamEnum winnerTeam, List<Player> winners, List<Player> losers) {
        List<String> message = new ArrayList<>();
        TeamEnum loserTeam = TeamUtil.getOppositeTeam(winnerTeam);

        String winnersString = "";
        for (Player winner : winners) {
            String winnerString;
            // Check if player is still in the match (not left)
            if (partySplit.getPlayers().contains(winner)) {
                winnerString = LanguageManager.getString("MATCH.PARTY-SPLIT.MATCH-END.WINNER-PLAYER-FORMAT")
                        .replace("%matchId%", partySplit.getId())
                        .replace("%player%", winner.getName())
                        .replace("%player_uuid%", ProfileManager.getInstance().getUuids().get(winner) != null
                                ? ProfileManager.getInstance().getUuids().get(winner).toString() : "");
            } else {
                // Player left the match - use left player format if available, otherwise just show name
                String leftFormat = LanguageManager.getString("MATCH.PARTY-SPLIT.MATCH-END.LEFT-PLAYER-FORMAT");
                if (leftFormat != null && !leftFormat.isEmpty()) {
                    winnerString = leftFormat.replace("%player%", winner.getName());
                } else {
                    winnerString = winner.getName();
                }
            }

            winnersString = winnersString.concat(winnerString);
            if (!winners.get(winners.size() - 1).equals(winner))
                winnersString = winnersString.concat(", ");
        }

        String losersString = "";
        for (Player loser : losers) {
            String loserString;
            // Check if player is still in the match (not left)
            if (partySplit.getPlayers().contains(loser)) {
                loserString = LanguageManager.getString("MATCH.PARTY-SPLIT.MATCH-END.LOSER-PLAYER-FORMAT")
                        .replace("%matchId%", partySplit.getId())
                        .replace("%player%", loser.getName())
                        .replace("%player_uuid%", ProfileManager.getInstance().getUuids().get(loser) != null
                                ? ProfileManager.getInstance().getUuids().get(loser).toString() : "");
            } else {
                // Player left the match - use left player format if available, otherwise just show name
                String leftFormat = LanguageManager.getString("MATCH.PARTY-SPLIT.MATCH-END.LEFT-PLAYER-FORMAT");
                if (leftFormat != null && !leftFormat.isEmpty()) {
                    loserString = leftFormat.replace("%player%", loser.getName());
                } else {
                    loserString = loser.getName();
                }
            }

            losersString = losersString.concat(loserString);
            if (!losers.get(losers.size() - 1).equals(loser))
                losersString = losersString.concat(", ");
        }

        for (String line : LanguageManager.getList("MATCH.PARTY-SPLIT.MATCH-END.MESSAGE")) {
            if (line.contains("%spectatorExtension%")) {
                List<String> spectators = new ArrayList<>();
                for (Player spectator : partySplit.getSpectators()) {
                    Profile spectatorProfile = ProfileManager.getInstance().getProfile(spectator);
                    if (!spectatorProfile.isHideFromPlayers())
                        spectators.add(spectator.getName());
                }

                if (!spectators.isEmpty()) {
                    for (String line2 : LanguageManager.getList("MATCH.PARTY-SPLIT.MATCH-END.SPECTATOR-EXTENSION")) {
                        message.add(line2
                                .replace("%size%", String.valueOf(spectators.size()))
                                .replace("%spectators%", spectators.toString().replace("[", "").replace("]", "")));
                    }
                }
            } else {
                message.add(line
                        .replace("%matchId%", partySplit.getId())
                        .replace("%winnerTeam%", winnerTeam.getNameMM())
                        .replace("%winners%", winnersString)
                        .replace("%loserTeam%", loserTeam.getNameMM())
                        .replace("%losers%", losersString)
                );
            }
        }

        return message;
    }

    public static List<String> getEndMessage(PartyVsParty partyVsParty, TeamEnum winnerTeam, List<Player> winners, List<Player> losers) {
        List<String> message = new ArrayList<>();
        TeamEnum loserTeam = TeamUtil.getOppositeTeam(winnerTeam);

        String winnersString = "";
        for (Player winner : winners) {
            String winnerString;
            // Check if player is still in the match (not left)
            if (partyVsParty.getPlayers().contains(winner)) {
                winnerString = LanguageManager.getString("MATCH.PARTY-VS-PARTY.MATCH-END.WINNER-PLAYER-FORMAT")
                        .replace("%matchId%", partyVsParty.getId())
                        .replace("%player%", winner.getName())
                        .replace("%player_uuid%", ProfileManager.getInstance().getUuids().get(winner) != null
                                ? ProfileManager.getInstance().getUuids().get(winner).toString() : "");
            } else {
                // Player left the match - use left player format if available, otherwise just show name
                String leftFormat = LanguageManager.getString("MATCH.PARTY-VS-PARTY.MATCH-END.LEFT-PLAYER-FORMAT");
                if (leftFormat != null && !leftFormat.isEmpty()) {
                    winnerString = leftFormat.replace("%player%", winner.getName());
                } else {
                    winnerString = winner.getName();
                }
            }

            winnersString = winnersString.concat(winnerString);
            if (!winners.get(winners.size() - 1).equals(winner))
                winnersString = winnersString.concat(", ");
        }

        String losersString = "";
        for (Player loser : losers) {
            String loserString;
            // Check if player is still in the match (not left)
            if (partyVsParty.getPlayers().contains(loser)) {
                loserString = LanguageManager.getString("MATCH.PARTY-VS-PARTY.MATCH-END.LOSER-PLAYER-FORMAT")
                        .replace("%matchId%", partyVsParty.getId())
                        .replace("%player%", loser.getName())
                        .replace("%player_uuid%", ProfileManager.getInstance().getUuids().get(loser) != null
                                ? ProfileManager.getInstance().getUuids().get(loser).toString() : "");
            } else {
                // Player left the match - use left player format if available, otherwise just show name
                String leftFormat = LanguageManager.getString("MATCH.PARTY-VS-PARTY.MATCH-END.LEFT-PLAYER-FORMAT");
                if (leftFormat != null && !leftFormat.isEmpty()) {
                    loserString = leftFormat.replace("%player%", loser.getName());
                } else {
                    loserString = loser.getName();
                }
            }

            losersString = losersString.concat(loserString);
            if (!losers.get(losers.size() - 1).equals(loser))
                losersString = losersString.concat(", ");
        }

        for (String line : LanguageManager.getList("MATCH.PARTY-VS-PARTY.MATCH-END.MESSAGE")) {
            if (line.contains("%spectatorExtension%")) {
                List<String> spectators = new ArrayList<>();
                for (Player spectator : partyVsParty.getSpectators()) {
                    Profile spectatorProfile = ProfileManager.getInstance().getProfile(spectator);
                    if (!spectatorProfile.isHideFromPlayers())
                        spectators.add(spectator.getName());
                }

                if (!spectators.isEmpty()) {
                    for (String line2 : LanguageManager.getList("MATCH.PARTY-VS-PARTY.MATCH-END.SPECTATOR-EXTENSION")) {
                        message.add(line2
                                .replace("%size%", String.valueOf(spectators.size()))
                                .replace("%spectators%", spectators.toString().replace("[", "").replace("]", "")));
                    }
                }
            } else {
                message.add(line
                        .replace("%matchId%", partyVsParty.getId())
                        .replace("%winnerTeam%", winnerTeam.getNameMM())
                        .replace("%winners%", winnersString)
                        .replace("%loserTeam%", loserTeam.getNameMM())
                        .replace("%losers%", losersString)
                );
            }
        }

        return message;
    }

}
