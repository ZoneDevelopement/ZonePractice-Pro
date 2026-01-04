package dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partyvsparty;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayersRound;
import dev.nandi0813.practice.manager.fight.match.util.EndMessageUtil;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;

public class PartyVsPartyRound extends PlayersVsPlayersRound {

    protected PartyVsPartyRound(Match match, int roundNumber) {
        super(match, roundNumber);
    }

    @Override
    public void sendEndMessage(boolean endMatch) {
        PartyVsParty partyVsParty = (PartyVsParty) match;
        if (endMatch) {
            TeamEnum matchWinner = this.getMatch().getMatchWinner();
            if (matchWinner != null) {
                for (String message : EndMessageUtil.getEndMessage(partyVsParty, matchWinner, partyVsParty.getTeamPlayers(matchWinner), partyVsParty.getTeamPlayers(TeamUtil.getOppositeTeam(matchWinner))))
                    this.match.sendMessage(message, true);
            } else {
                for (String line : LanguageManager.getList("MATCH.PARTY-VS-PARTY.MATCH-END-DRAW"))
                    this.match.sendMessage(line, true);
            }
        } else {
            if (roundWinner != null) {
                for (String line : LanguageManager.getList("MATCH.PARTY-VS-PARTY.MATCH-END-ROUND"))
                    this.match.sendMessage(line
                            .replace("%team%", roundWinner.getNameMM())
                            .replace("%round%", String.valueOf((match.getWinsNeeded() - partyVsParty.getWonRounds(roundWinner)))), true);
            } else {
                for (String line : LanguageManager.getList("MATCH.PARTY-VS-PARTY.match-end-round-draw"))
                    this.match.sendMessage(line, true);
            }
        }
    }

}
