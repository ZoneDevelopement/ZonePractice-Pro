package dev.nandi0813.practice.manager.fight.match.type.playersvsplayers;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class PlayersVsPlayersRound extends Round {

    protected TeamEnum roundWinner;

    protected PlayersVsPlayersRound(Match match, int roundNumber) {
        super(match, roundNumber);
    }

    @Override
    public PlayersVsPlayers getMatch() {
        return (PlayersVsPlayers) this.match;
    }

}
