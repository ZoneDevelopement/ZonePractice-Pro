package dev.nandi0813.practice.manager.fight.match.type.duel;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.match.util.TempKillPlayer;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.playerdisplay.nametag.NametagManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.server.sound.SoundManager;
import dev.nandi0813.practice.manager.server.sound.SoundType;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Duel extends Match implements Team {

    private final boolean ranked;

    private final Player player1;
    private final Player player2;

    private final Map<Player, Profile> playerProfiles = new HashMap<>();

    @Getter
    private Player matchWinner;
    @Getter
    private Player loser;

    public Duel(Ladder ladder, Arena arena, List<Player> players, boolean ranked, int winsNeeded) {
        super(ladder, arena, new ArrayList<>(players), winsNeeded);

        this.type = MatchType.DUEL;
        this.ranked = ranked;

        this.player1 = players.get(0);
        this.playerProfiles.put(player1, ProfileManager.getInstance().getProfile(player1));
        NametagManager.getInstance().setNametag(player1, TeamEnum.TEAM1.getPrefix(), TeamEnum.TEAM1.getNameColor(), TeamEnum.TEAM1.getSuffix(), 20);

        if (players.size() == 2) {
            this.player2 = players.get(1);
            this.playerProfiles.put(player2, ProfileManager.getInstance().getProfile(player2));
            NametagManager.getInstance().setNametag(player2, TeamEnum.TEAM2.getPrefix(), TeamEnum.TEAM2.getNameColor(), TeamEnum.TEAM2.getSuffix(), 21);
        } else {
            this.player2 = null;
        }
    }

    @Override
    public void startNextRound() {
        DuelRound round = new DuelRound(this, this.rounds.size() + 1);
        this.rounds.put(round.getRoundNumber(), round);

        if (round.getRoundNumber() == 1) // If it's the first round
        {
            if (ranked && ladder instanceof NormalLadder normalLadder) {

                for (String line : LanguageManager.getList("MATCH.DUEL.MATCH-START-RANKED")) {
                    this.sendMessage(line
                                    .replace("%matchTypeName%", MatchType.DUEL.getName(true))
                                    .replace("%weightClassName%", WeightClass.RANKED.getMMName())
                                    .replace("%ladder%", ladder.getDisplayName())
                                    .replace("%arena%", arena.getDisplayName())
                                    .replace("%rounds%", String.valueOf(this.winsNeeded))
                                    .replace("%player1%", player1.getName())
                                    .replace("%player2%", player2.getName())
                                    .replace("%player1elo%", String.valueOf(playerProfiles.get(player1).getStats().getLadderStat(normalLadder).getElo()))
                                    .replace("%player1win%", String.valueOf(playerProfiles.get(player1).getStats().getLadderStat(normalLadder).getRankedWins()))
                                    .replace("%player2elo%", String.valueOf(playerProfiles.get(player2).getStats().getLadderStat(normalLadder).getElo()))
                                    .replace("%player2win%", String.valueOf(playerProfiles.get(player2).getStats().getLadderStat(normalLadder).getRankedWins()))
                            , false);
                }
            } else {
                for (String line : LanguageManager.getList("MATCH.DUEL.MATCH-START-UNRANKED")) {
                    this.sendMessage(line
                            .replace("%matchTypeName%", MatchType.DUEL.getName(true))
                            .replace("%weightClassName%", WeightClass.UNRANKED.getMMName())
                            .replace("%ladder%", ladder.getDisplayName())
                            .replace("%arena%", arena.getDisplayName())
                            .replace("%rounds%", String.valueOf(this.winsNeeded)), false);
                }
            }
        }

        round.startRound();
    }

    @Override
    public DuelRound getCurrentRound() {
        return (DuelRound) this.rounds.get(this.rounds.size());
    }

    @Override
    public int getWonRounds(Player player) {
        int wonRounds = 0;
        for (Round round : this.rounds.values()) {
            if (((DuelRound) round).getRoundWinner() == player)
                wonRounds++;
        }
        return wonRounds;
    }

    @Override
    public void teleportPlayer(Player player) {
        if (player.equals(player1))
            player.teleport(arena.getPosition1());
        else
            player.teleport(arena.getPosition2());
    }

    @Override
    protected void killPlayer(Player player, String deathMessage) {
        DuelRound round = this.getCurrentRound();
        Player winnerPlayer = this.getOppositePlayer(player);
        boolean endRound = false;

        // Use the Match helper method to handle ladder-specific death behavior
        DeathResult result = handleLadderDeath(player);

        switch (result) {
            case TEMPORARY_DEATH:
                // Ladder supports respawning - create temp kill
                asRespawnableLadder().ifPresent(respawnableLadder -> {
                    new TempKillPlayer(round, player, respawnableLadder.getRespawnTime());
                    SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_TEMP_DEATH).play(this.getPeople());
                });
                ClassImport.getClasses().getPlayerUtil().clearInventory(player);
                player.setHealth(20);
                break;

            case ELIMINATED:
                if (isRespawnableLadder()) {
                    // Respawnable ladder but player is eliminated (e.g., bed destroyed)
                    this.getCurrentStat(player).end(true);
                    this.teleportPlayer(player);
                    endRound = true;
                    SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());
                    ClassImport.getClasses().getPlayerUtil().clearInventory(player);
                    player.setHealth(20);
                } else if (isScoringLadder()) {
                    // Scoring ladder (like Boxing) - death doesn't end round
                    return;
                } else {
                    // Default death behavior for standard ladders
                    this.getCurrentStat(player).end(true);
                    PlayerUtil.setFightPlayer(player);
                    addEntityChange(ClassImport.getClasses().getPlayerUtil().dropPlayerInventory(player));
                    ClassImport.getClasses().getPlayerUtil().clearInventory(player);
                    player.setHealth(20);
                    SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());
                    endRound = true;
                }
                break;

            case NO_ACTION:
                // Ladder handled everything
                return;
        }

        if (endRound) {
            round.setRoundWinner(winnerPlayer);
            round.endRound();
        }
    }

    @Override
    public void removePlayer(Player player, boolean quit) {
        if (!players.contains(player)) return;

        players.remove(player);
        MatchManager.getInstance().getPlayerMatches().remove(player);

        if (quit) {
            this.getCurrentStat(player).end(true);

            this.sendMessage(
                    TeamUtil.replaceTeamNames(LanguageManager.getString("MATCH.DUEL.PLAYER-LEFT"),
                            player,
                            this.getTeam(player)),
                    true);

            DuelRound duelRound = this.getCurrentRound();
            duelRound.setRoundWinner(this.getOppositePlayer(player));
            duelRound.endRound();
        }

        this.removePlayerFromBelowName(player);

        if (ZonePractice.getInstance().isEnabled()) {
            // Remove 1 from the player's left matches
            Profile profile = ProfileManager.getInstance().getProfile(player);
            if (ranked)
                profile.setRankedLeft(profile.getRankedLeft() - 1);
            else
                profile.setUnrankedLeft(profile.getUnrankedLeft() - 1);

            // Set the player inventory to lobby inventory
            if (player.isOnline())
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

        for (Player player : this.players) {
            if (this.getWonRounds(player) == this.winsNeeded) {
                this.matchWinner = player;
                this.loser = this.getOppositePlayer(player);
                return true;
            }
        }

        return false;
    }

    @Override
    public TeamEnum getTeam(Player player) {
        if (player.equals(player1))
            return TeamEnum.TEAM1;
        else
            return TeamEnum.TEAM2;
    }

    public Player getOppositePlayer(Player player) {
        if (player1 == player)
            return player2;
        else
            return player1;
    }

}
