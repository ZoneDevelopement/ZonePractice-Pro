package dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partyvsparty;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.playerdisplay.nametag.NametagManager;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
public class PartyVsParty extends PlayersVsPlayers {

    private final Party party1;
    private final Party party2;

    public PartyVsParty(Ladder ladder, Arena arena, Party party1, Party party2, List<Player> players, int rounds) {
        super(ladder, arena, players, rounds);

        this.type = MatchType.PARTY_VS_PARTY;

        this.party1 = party1;
        this.party2 = party2;

        for (Player party1Player : party1.getMembers()) {
            this.teams.get(TeamEnum.TEAM1).add(party1Player);
            NametagManager.getInstance().setNametag(party1Player, TeamEnum.TEAM1.getPrefix(), TeamEnum.TEAM1.getNameColor(), TeamEnum.TEAM1.getSuffix(), 20);
        }

        for (Player party2Player : party2.getMembers()) {
            this.teams.get(TeamEnum.TEAM2).add(party2Player);
            NametagManager.getInstance().setNametag(party2Player, TeamEnum.TEAM2.getPrefix(), TeamEnum.TEAM2.getNameColor(), TeamEnum.TEAM2.getSuffix(), 21);
        }

    }

    @Override
    public void startNextRound() {
        PartyVsPartyRound round = new PartyVsPartyRound(this, this.rounds.size() + 1);
        this.rounds.put(round.getRoundNumber(), round);

        if (round.getRoundNumber() == 1) {
            for (String line : LanguageManager.getList("MATCH.PARTY-VS-PARTY.MATCH-START")) {
                sendMessage(line
                        .replace("%matchTypeName%", MatchType.PARTY_VS_PARTY.getName(false))
                        .replace("%ladder%", ladder.getName())
                        .replace("%arena%", arena.getName())
                        .replace("%rounds%", String.valueOf(this.winsNeeded))
                        .replace("%party1leader%", party1.getLeader().getName())
                        .replace("%party2leader%", party2.getLeader().getName())
                        .replace("%team1name%", TeamEnum.TEAM1.getNameMM())
                        .replace("%team2name%", TeamEnum.TEAM2.getNameMM())
                        .replace("%team1players%", PlayerUtil.getPlayerNames(teams.get(TeamEnum.TEAM1)).toString().replace("[", "").replace("]", ""))
                        .replace("%team2players%", PlayerUtil.getPlayerNames(teams.get(TeamEnum.TEAM2)).toString().replace("[", "").replace("]", "")), false);
            }
        }

        round.startRound();
    }

}
