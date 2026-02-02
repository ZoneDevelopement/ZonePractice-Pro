package dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayersRound;
import dev.nandi0813.practice.manager.fight.match.util.EndMessageUtil;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;

public class PartySplitRound extends PlayersVsPlayersRound {

    protected PartySplitRound(Match match, int roundNumber) {
        super(match, roundNumber);
    }

    @Override
    public void sendEndMessage(boolean endMatch) {
        PartySplit partySplit = (PartySplit) match;
        if (endMatch) {
            TeamEnum matchWinner = this.getMatch().getMatchWinner();
            if (matchWinner != null) {
                // Use getOriginalTeamPlayers to include all players who started the match (including those who left)
                for (String message : EndMessageUtil.getEndMessage(partySplit, matchWinner, partySplit.getOriginalTeamPlayers(matchWinner), partySplit.getOriginalTeamPlayers(TeamUtil.getOppositeTeam(matchWinner))))
                    this.match.sendMessage(message, true);
            } else {
                for (String line : LanguageManager.getList("MATCH.PARTY-SPLIT.MATCH-END-DRAW"))
                    this.match.sendMessage(line, true);
            }
        } else {
            if (roundWinner != null) {
                for (String line : LanguageManager.getList("MATCH.PARTY-SPLIT.MATCH-END-ROUND"))
                    this.match.sendMessage(line
                            .replace("%team%", roundWinner.getNameMM())
                            .replace("%round%", String.valueOf((match.getWinsNeeded() - partySplit.getWonRounds(roundWinner)))), true);
            } else {
                for (String line : LanguageManager.getList("MATCH.PARTY-SPLIT.MATCH-END-ROUND-DRAW"))
                    this.match.sendMessage(line, true);
            }
        }
    }

}
