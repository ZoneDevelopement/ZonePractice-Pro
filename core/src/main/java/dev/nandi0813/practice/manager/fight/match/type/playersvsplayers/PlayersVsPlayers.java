package dev.nandi0813.practice.manager.fight.match.type.playersvsplayers;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.util.MatchPlayerUtil;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.match.util.TempKillPlayer;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.nandi0813.practice.manager.server.sound.SoundManager;
import dev.nandi0813.practice.manager.server.sound.SoundType;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class PlayersVsPlayers extends Match implements Team {

    protected final Map<TeamEnum, List<Player>> teams = new EnumMap<>(TeamEnum.class);
    // Track original team members (including those who left) for end messages
    protected final Map<TeamEnum, List<Player>> originalTeams = new EnumMap<>(TeamEnum.class);
    protected TeamEnum matchWinner;

    protected PlayersVsPlayers(Ladder ladder, Arena arena, List<Player> players, int winsNeeded) {
        super(ladder, arena, players, winsNeeded);

        this.teams.put(TeamEnum.TEAM1, new ArrayList<>());
        this.teams.put(TeamEnum.TEAM2, new ArrayList<>());
        this.originalTeams.put(TeamEnum.TEAM1, new ArrayList<>());
        this.originalTeams.put(TeamEnum.TEAM2, new ArrayList<>());
    }

    @Override
    public PlayersVsPlayersRound getCurrentRound() {
        return (PlayersVsPlayersRound) this.rounds.get(this.rounds.size());
    }

    @Override
    public void teleportPlayer(Player player) {
        if (this.getTeam(player) == TeamEnum.TEAM1)
            player.teleport(arena.getPosition1());
        else
            player.teleport(arena.getPosition2());
    }

    @Override
    protected void killPlayer(Player player, String deathMessage) {
        TeamEnum winnerTeam = null;
        boolean endRound = false;
        PlayersVsPlayersRound round = this.getCurrentRound();

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
                    SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());

                    winnerTeam = this.getWinnerTeam();
                    if (winnerTeam != null)
                        endRound = true;
                    else
                        MatchPlayerUtil.hidePlayerPartyGames(player, this.players);

                    ClassImport.getClasses().getPlayerUtil().clearInventory(player);
                    player.setHealth(20);
                } else if (isScoringLadder()) {
                    // Scoring ladder (like Boxing) - death doesn't end round
                    return;
                } else {
                    // Default death behavior for standard ladders
                    this.getCurrentStat(player).end(true);
                    SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());

                    PlayerUtil.setFightPlayer(player);

                    if (ladder.isDropInventoryPartyGames())
                        addEntityChange(ClassImport.getClasses().getPlayerUtil().dropPlayerInventory(player));
                    else
                        ClassImport.getClasses().getPlayerUtil().clearInventory(player);

                    if (this.getLanguagePath() != null) {
                        String teamDeathMSG = LanguageManager.getString(this.getLanguagePath() + ".PLAYER-DIE");
                        if (teamDeathMSG != null) {
                            this.sendMessage(TeamUtil.replaceTeamNames(
                                    teamDeathMSG.replace("%playerTeamLeft%", String.valueOf(this.getTeamAlivePlayers(this.getTeam(player)).size())),
                                    player,
                                    this.getTeam(player)
                            ), true);
                        }
                    }

                    winnerTeam = this.getWinnerTeam();
                    if (winnerTeam != null)
                        endRound = true;
                    else
                        MatchPlayerUtil.hidePlayerPartyGames(player, this.players);

                    ClassImport.getClasses().getPlayerUtil().clearInventory(player);
                    player.setHealth(20);
                }
                break;

            case NO_ACTION:
                // Ladder handled everything
                return;
        }

        if (endRound) {
            round.setRoundWinner(winnerTeam);
            round.endRound();
        }
    }

    @Override
    public void removePlayer(Player player, boolean quit) {
        if (!players.contains(player)) return;

        this.players.remove(player);
        this.getTeamPlayers(getTeam(player)).remove(player);
        MatchManager.getInstance().getPlayerMatches().remove(player);

        // Only process quit logic if the match hasn't ended yet
        // When match status is END or OVER, players are being removed as part of cleanup
        if (quit && !this.status.equals(MatchStatus.END) && !this.status.equals(MatchStatus.OVER)) {
            this.getCurrentStat(player).end(true);

            if (this.getLanguagePath() != null) {
                this.sendMessage(TeamUtil.replaceTeamNames(
                                LanguageManager.getString(this.getLanguagePath() + ".PLAYER-LEFT"),
                                player,
                                this.getTeam(player)),
                        true);
            }

            TeamEnum winnerTeam = this.getWinnerTeam();
            if (winnerTeam != null) {
                PlayersVsPlayersRound round = this.getCurrentRound();
                round.setRoundWinner(winnerTeam);
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

        final List<Player> team1 = this.getTeamPlayers(TeamEnum.TEAM1);
        final List<Player> team2 = this.getTeamPlayers(TeamEnum.TEAM2);

        if (team1.isEmpty() || team2.isEmpty()) {
            if (status.equals(MatchStatus.START)) {
                this.matchWinner = null;
            } else {
                if (team1.isEmpty())
                    this.matchWinner = TeamEnum.TEAM2;
                else
                    this.matchWinner = TeamEnum.TEAM1;
            }

            return true;
        }

        for (TeamEnum team : TeamEnum.values()) {
            if (this.getWonRounds(team) == this.winsNeeded) {
                this.matchWinner = team;
                return true;
            }
        }
        return false;
    }

    @Override
    public int getWonRounds(Player player) {
        TeamEnum playerTeam = this.getTeam(player);

        int wonRounds = 0;
        for (Round round : this.rounds.values()) {
            if (((PlayersVsPlayersRound) round).getRoundWinner() == playerTeam)
                wonRounds++;
        }

        return wonRounds;
    }

    public int getWonRounds(TeamEnum team) {
        int wonRounds = 0;
        for (Round round : this.rounds.values()) {
            if (((PlayersVsPlayersRound) round).getRoundWinner() == team)
                wonRounds++;
        }
        return wonRounds;
    }

    @Override
    public TeamEnum getTeam(Player player) {
        if (teams.get(TeamEnum.TEAM1).contains(player)) {
            return TeamEnum.TEAM1;
        } else {
            return TeamEnum.TEAM2;
        }
    }

    public List<Player> getTeamPlayers(TeamEnum team) {
        return teams.get(team);
    }

    /**
     * Get all players who originally started on this team, including those who left.
     * Used for end messages to show all participants.
     */
    public List<Player> getOriginalTeamPlayers(TeamEnum team) {
        return originalTeams.get(team);
    }

    public List<Player> getTeamAlivePlayers(TeamEnum team) {
        List<Player> alive = new ArrayList<>();

        for (Player player : this.getTeamPlayers(team)) {
            if (!this.getCurrentStat(player).isSet()) {
                alive.add(player);
            }
        }

        return alive;
    }

    public TeamEnum getWinnerTeam() {
        if (this.getTeamAlivePlayers(TeamEnum.TEAM1).isEmpty())
            return TeamEnum.TEAM2;
        else if (this.getTeamAlivePlayers(TeamEnum.TEAM2).isEmpty())
            return TeamEnum.TEAM1;
        return null;
    }

    private String getLanguagePath() {
        return switch (type) {
            case PARTY_SPLIT -> "MATCH.PARTY-SPLIT";
            case PARTY_VS_PARTY -> "MATCH.PARTY-VS-PARTY";
            default -> null;
        };
    }

}
