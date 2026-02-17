package dev.nandi0813.practice.manager.fight.match.type.partyffa;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.util.MatchPlayerUtil;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.nametag.NametagManager;
import dev.nandi0813.practice.manager.server.sound.SoundManager;
import dev.nandi0813.practice.manager.server.sound.SoundType;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class PartyFFA extends Match {

    private Player matchWinner;

    public PartyFFA(Ladder ladder, Arena arena, Party party, int winsNeeded) {
        super(ladder, arena, new ArrayList<>(party.getMembers()), winsNeeded);
        this.type = MatchType.PARTY_FFA;

        for (Player player : this.players)
            NametagManager.getInstance().setNametag(player, TeamEnum.TEAM1.getPrefix(), TeamEnum.TEAM1.getNameColor(), TeamEnum.TEAM1.getSuffix(), 20);
    }

    @Override
    public void startNextRound() {
        PartyFfaRound round = new PartyFfaRound(this, this.rounds.size() + 1);
        this.rounds.put(round.getRoundNumber(), round);

        if (round.getRoundNumber() == 1) {
            for (String line : LanguageManager.getList("MATCH.PARTY-FFA.MATCH-START")) {
                this.sendMessage(line
                        .replace("%matchTypeName%", MatchType.PARTY_FFA.getName(true))
                        .replace("%ladder%", ladder.getDisplayName())
                        .replace("%arena%", arena.getDisplayName())
                        .replace("%rounds%", String.valueOf(this.winsNeeded))
                        .replace("%players%", PlayerUtil.getPlayerNames(players).toString().replace("[", "").replace("]", "")), false);
            }
        }

        round.startRound();
    }

    @Override
    public PartyFfaRound getCurrentRound() {
        return (PartyFfaRound) this.rounds.get(this.rounds.size());
    }

    @Override
    public int getWonRounds(Player player) {
        int wonRounds = 0;
        for (Round round : this.rounds.values()) {
            if (((PartyFfaRound) round).getRoundWinner() == player)
                wonRounds++;
        }
        return wonRounds;
    }

    @Override
    public void teleportPlayer(Player player) {
        int randomNum;
        if (arena.getFfaPositions().isEmpty()) {
            randomNum = new Random().nextInt(2);
            if (randomNum == 0)
                player.teleport(arena.getPosition1());
            else
                player.teleport(arena.getPosition2());
        } else {
            randomNum = new Random().nextInt(arena.getFfaPositions().size());
            player.teleport(arena.getFfaPositions().get(randomNum));
        }
    }

    @Override
    protected void killPlayer(Player player, String deathMessage) {
        // Check if this ladder supports FFA mode using the Match helper methods
        // Respawnable ladders don't support standard FFA death mechanics
        if (isRespawnableLadder()) {
            return; // Respawnable ladders (Bridges, BedWars, etc.) don't support FFA
        }

        // Scoring ladders (like Boxing) have special win conditions
        if (isScoringLadder()) {
            return;
        }

        // Default death behavior for standard ladders in FFA
        this.getCurrentStat(player).end(true);
        SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());

        PlayerUtil.setFightPlayer(player);

        if (ladder.isDropInventoryPartyGames())
            addEntityChange(ClassImport.getClasses().getPlayerUtil().dropPlayerInventory(player));
        else
            ClassImport.getClasses().getPlayerUtil().clearInventory(player);

        PartyFfaRound round = this.getCurrentRound();
        Player winnerPlayer = this.getWinnerPlayer();
        if (winnerPlayer != null) {
            round.setRoundWinner(winnerPlayer);
            round.endRound();
        } else
            MatchPlayerUtil.hidePlayerPartyGames(player, this.players);
    }

    @Override
    public void removePlayer(Player player, boolean quit) {
        if (!players.contains(player)) return;

        players.remove(player);
        MatchManager.getInstance().getPlayerMatches().remove(player);

        // Only process quit logic if the match hasn't ended yet
        // When match status is END or OVER, players are being removed as part of cleanup
        if (quit && !this.status.equals(MatchStatus.END) && !this.status.equals(MatchStatus.OVER)) {
            this.getCurrentStat(player).end(true);

            this.sendMessage(LanguageManager.getString("MATCH.PARTY-FFA.PLAYER-LEFT")
                    .replace("%player%", player.getName()), true);

            Player winnerPlayer = this.getWinnerPlayer();
            if (winnerPlayer != null) {
                PartyFfaRound round = this.getCurrentRound();
                round.setRoundWinner(winnerPlayer);
                round.endRound();
            }
        }

        this.removePlayerFromBelowName(player);

        if (ZonePractice.getInstance().isEnabled() && player.isOnline()) {
            // Set the player inventory to lobby inventory
            InventoryManager.getInstance().setLobbyInventory(player, true);
        }
    }

    @Override
    public boolean isEndMatch() {
        if (this.getStatus().equals(MatchStatus.END))
            return true;

        if (this.players.size() == 1) {
            if (status.equals(MatchStatus.START))
                this.matchWinner = null;
            else
                this.matchWinner = this.players.stream().findAny().get();

            return true;
        }

        for (Player player : this.getPlayers()) {
            if (this.getWonRounds(player) == this.winsNeeded) {
                this.matchWinner = player;
                return true;
            }
        }

        return false;
    }

    public Player getWinnerPlayer() {
        Player winnerPlayer = null;
        for (Player player : this.players) {
            Statistic statistic = this.getCurrentStat(player);
            if (!statistic.isSet()) {
                if (winnerPlayer == null)
                    winnerPlayer = statistic.getPlayer();
                else
                    return null;
            }
        }
        return winnerPlayer;
    }

    public List<Player> getAlivePlayers() {
        List<Player> alivePlayers = new ArrayList<>();

        for (Player player : this.players)
            if (!this.getCurrentStat(player).isSet())
                alivePlayers.add(player);

        return alivePlayers;
    }

    public Player getTopPlayer(int rank) {
        Map<Player, Integer> wonRounds = new HashMap<>();
        for (Player player : this.players)
            wonRounds.put(player, this.getWonRounds(player));

        if (wonRounds.size() < rank) return null;

        return new ArrayList<>(PlayerUtil.sortByValue(wonRounds).keySet()).get(rank - 1);
    }

}
