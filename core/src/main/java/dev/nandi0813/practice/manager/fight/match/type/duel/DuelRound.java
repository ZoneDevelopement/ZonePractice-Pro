package dev.nandi0813.practice.manager.fight.match.type.duel;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.interfaces.PlayerWinner;
import dev.nandi0813.practice.manager.fight.match.util.EndMessageUtil;
import dev.nandi0813.practice.manager.fight.match.util.MatchUtil;
import dev.nandi0813.practice.manager.fight.match.util.RewardCommandManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.statistics.LadderStats;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DuelRound extends Round implements PlayerWinner {

    private Player roundWinner;

    public DuelRound(Match match, int round) {
        super(match, round);
    }

    @Override
    public void sendEndMessage(boolean endMatch) {
        Duel duel = (Duel) match;
        Ladder ladder = duel.getLadder();

        if (endMatch) {
            Player matchWinner = this.getMatch().getMatchWinner();
            if (matchWinner != null) {
                Profile winnerProfile = duel.getPlayerProfiles().get(matchWinner);
                Profile loserProfile = duel.getPlayerProfiles().get(duel.getOppositePlayer(matchWinner));

                List<String> rankedExtension = new ArrayList<>();
                if (ladder instanceof NormalLadder normalLadder) {

                    LadderStats wLadderStats = winnerProfile.getStats().getLadderStat(normalLadder);
                    wLadderStats.increaseWins(duel.isRanked());
                    winnerProfile.getStats().increaseWinStreak(normalLadder, duel.isRanked());

                    LadderStats lLadderStats = loserProfile.getStats().getLadderStat(normalLadder);
                    lLadderStats.increaseLosses(duel.isRanked());
                    loserProfile.getStats().increaseLoseStreak(normalLadder, duel.isRanked());

                    if (duel.isRanked()) {
                        int eloChange = MatchUtil.getRandomElo();

                        int winnerOldElo = wLadderStats.getElo();
                        wLadderStats.increaseElo(eloChange);

                        int loserOldElo = lLadderStats.getElo();
                        lLadderStats.decreaseElo(eloChange);

                        for (String reLine : LanguageManager.getList("MATCH.DUEL.MATCH-END.RANKED-EXTENSION")) {
                            rankedExtension.add(reLine
                                    .replaceAll("%winner%", matchWinner.getName())
                                    .replaceAll("%loser%", duel.getOppositePlayer(matchWinner).getName())
                                    .replaceAll("%eloChange%", String.valueOf(eloChange))
                                    .replaceAll("%winnerNewElo%", String.valueOf(wLadderStats.getElo()))
                                    .replaceAll("%loserNewElo%", String.valueOf(lLadderStats.getElo()))
                                    .replaceAll("%winnerOldElo%", String.valueOf(winnerOldElo))
                                    .replaceAll("%loserOldElo%", String.valueOf(loserOldElo)));
                        }
                    }
                }

                for (String message : EndMessageUtil.getEndMessage(duel, rankedExtension))
                    duel.sendMessage(message, true);

                RewardCommandManager.getInstance().executeCommands(duel, duel.isRanked());
            } else {
                for (String line : LanguageManager.getList("MATCH.DUEL.MATCH-END-DRAW"))
                    duel.sendMessage(line, true);
            }
        } else {
            if (roundWinner != null) {
                for (String line : LanguageManager.getList("MATCH.DUEL.MATCH-END-ROUND"))
                    duel.sendMessage(line
                            .replaceAll("%player%", roundWinner.getName())
                            .replaceAll("%round%", String.valueOf((match.getWinsNeeded() - duel.getWonRounds(roundWinner)))), true);
            } else {
                for (String line : LanguageManager.getList("MATCH.DUEL.MATCH-END-ROUND-DRAW"))
                    duel.sendMessage(line, true);
            }
        }
    }

    @Override
    public Duel getMatch() {
        return (Duel) this.match;
    }

}
