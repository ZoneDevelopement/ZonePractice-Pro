package dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.nametag.NametagManager;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PartySplit extends PlayersVsPlayers {

    /**
     * Creates a party split match with randomly shuffled teams.
     */
    public PartySplit(Ladder ladder, Arena arena, Party party, int winsNeeded) {
        this(ladder, arena, party, winsNeeded, null);
    }

    /**
     * Creates a party split match.
     *
     * @param presetTeams If non-null, uses this manual team assignment (e.g. chosen by the
     *                    party owner through the team selector GUI) instead of randomly
     *                    shuffling the party members into teams.
     */
    public PartySplit(Ladder ladder, Arena arena, Party party, int winsNeeded, Map<TeamEnum, List<Player>> presetTeams) {
        super(ladder, arena, new ArrayList<>(party.getMembers()), winsNeeded);

        this.type = MatchType.PARTY_SPLIT;

        if (presetTeams != null) {
            /*
             * Use the manual team assignment chosen by the party owner
             */
            for (Player player : presetTeams.getOrDefault(TeamEnum.TEAM1, List.of())) {
                this.teams.get(TeamEnum.TEAM1).add(player);
                this.originalTeams.get(TeamEnum.TEAM1).add(player);
                NametagManager.getInstance().setNametag(player, TeamEnum.TEAM1.getPrefix(), TeamEnum.TEAM1.getNameColor(), TeamEnum.TEAM1.getSuffix(), 20);
            }
            for (Player player : presetTeams.getOrDefault(TeamEnum.TEAM2, List.of())) {
                this.teams.get(TeamEnum.TEAM2).add(player);
                this.originalTeams.get(TeamEnum.TEAM2).add(player);
                NametagManager.getInstance().setNametag(player, TeamEnum.TEAM2.getPrefix(), TeamEnum.TEAM2.getNameColor(), TeamEnum.TEAM2.getSuffix(), 21);
            }
            return;
        }

        /*
         * Split the players into teams randomly
         */
        Collections.shuffle(this.players);
        int team1PlayerCount = 0;
        int team2PlayerCount = 0;
        for (Player player : players) {
            if (team2PlayerCount > team1PlayerCount) {
                this.teams.get(TeamEnum.TEAM1).add(player);
                this.originalTeams.get(TeamEnum.TEAM1).add(player); // Track original team members
                NametagManager.getInstance().setNametag(player, TeamEnum.TEAM1.getPrefix(), TeamEnum.TEAM1.getNameColor(), TeamEnum.TEAM1.getSuffix(), 20);

                team1PlayerCount++;
            } else {
                this.teams.get(TeamEnum.TEAM2).add(player);
                this.originalTeams.get(TeamEnum.TEAM2).add(player); // Track original team members
                NametagManager.getInstance().setNametag(player, TeamEnum.TEAM2.getPrefix(), TeamEnum.TEAM2.getNameColor(), TeamEnum.TEAM2.getSuffix(), 21);

                team2PlayerCount++;
            }
        }
    }

    @Override
    public void startNextRound() {
        PartySplitRound round = new PartySplitRound(this, this.rounds.size() + 1);
        this.rounds.put(round.getRoundNumber(), round);

        if (round.getRoundNumber() == 1) {
            for (String line : LanguageManager.getList("MATCH.PARTY-SPLIT.MATCH-START")) {
                this.sendMessage(line
                        .replace("%matchTypeName%", MatchType.PARTY_SPLIT.getName(false))
                        .replace("%ladder%", ladder.getDisplayName())
                        .replace("%map%", arena.getDisplayName())
                        .replace("%rounds%", String.valueOf(this.winsNeeded))
                        .replace("%team1name%", TeamEnum.TEAM1.getNameMM())
                        .replace("%team2name%", TeamEnum.TEAM2.getNameMM())
                        .replace("%team1players%", PlayerUtil.getPlayerNames(teams.get(TeamEnum.TEAM1)).toString().replace("[", "").replace("]", ""))
                        .replace("%team2players%", PlayerUtil.getPlayerNames(teams.get(TeamEnum.TEAM2)).toString().replace("[", "").replace("]", "")), false);
            }
        }

        round.startRound();
    }

}
