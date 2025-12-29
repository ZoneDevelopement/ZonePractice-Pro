package dev.nandi0813.practice.manager.fight.match.type.partyffa;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.interfaces.PlayerWinner;
import dev.nandi0813.practice.manager.fight.match.util.EndMessageUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PartyFfaRound extends Round implements PlayerWinner {

    private Player roundWinner;

    protected PartyFfaRound(Match match, int roundNumber) {
        super(match, roundNumber);
    }

    @Override
    public void sendEndMessage(boolean endMatch) {
        PartyFFA partyFFA = (PartyFFA) match;

        if (endMatch) {
            Player matchWinner = this.getMatch().getMatchWinner();
            if (matchWinner != null) {
                for (String message : EndMessageUtil.getEndMessage(partyFFA, this.getLosers()))
                    this.match.sendMessage(message, true);
            } else {
                for (String line : LanguageManager.getList("MATCH.PARTY-FFA.MATCH-END-DRAW"))
                    this.match.sendMessage(line, true);
            }
        } else {
            if (roundWinner != null) {
                for (String line : LanguageManager.getList("MATCH.PARTY-FFA.MATCH-END-ROUND"))
                    this.match.sendMessage(line
                            .replaceAll("%player%", roundWinner.getName())
                            .replaceAll("%round%", String.valueOf((match.getWinsNeeded() - partyFFA.getWonRounds(roundWinner)))), true);
            } else {
                for (String line : LanguageManager.getList("MATCH.PARTY-FFA.MATCH-END-ROUND-DRAW"))
                    this.match.sendMessage(line, true);
            }
        }
    }

    private List<Player> getLosers() {
        List<Player> losers = new ArrayList<>();
        for (Player player : this.match.getMatchPlayers().keySet())
            if (!player.equals(roundWinner))
                losers.add(player);
        return losers;
    }

    @Override
    public PartyFFA getMatch() {
        return (PartyFFA) this.match;
    }

}
