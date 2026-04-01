package dev.nandi0813.practice.manager.fight.match.runnable.round;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.PlayerWinner;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayersRound;
import dev.nandi0813.practice.manager.fight.match.util.TitleUtil;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class RoundEndRunnable extends BukkitRunnable {

    private final Match match;
    private final Round round;

    @Getter
    private int seconds;
    @Getter
    private boolean running = false;

    @Getter
    private final boolean ended;

    /**
     * When {@code true} the runnable will NOT call {@link Match#startNextRound()} when
     * it reaches zero — used for non-ending rounds where the rollback callback is
     * responsible for starting the next round.
     */
    private final boolean suppressNextRound;
    private final boolean rollbackBeforeNextRound;

    /**
     * Tracks whether we have already sent the round-end titles and messages.
     * This prevents duplicate title sends if the runnable is reused.
     */
    private boolean titlesSent = false;

    public RoundEndRunnable(Round round, boolean ended) {
        this(round, ended, false, false);
    }

    public RoundEndRunnable(Round round, boolean ended, boolean suppressNextRound, boolean rollbackBeforeNextRound) {
        this.round = round;
        this.match = round.getMatch();
        this.ended = ended;
        this.suppressNextRound = suppressNextRound;
        this.rollbackBeforeNextRound = rollbackBeforeNextRound;

        // Use the ladder's ROUND_END_DELAY setting
        if (!ended) {
            this.seconds = round.getMatch().getLadder().getRoundEndDelay();
        } else {
            this.seconds = ConfigManager.getConfig().getInt("MATCH-SETTINGS.AFTER-COUNTDOWN");
        }
    }

    public RoundEndRunnable begin() {
        this.round.setRoundStatus(RoundStatus.END);
        if (this.ended) match.setStatus(MatchStatus.END);

        running = true;
        
        // Send titles and victory/defeat messages immediately (before delay)
        sendRoundEndTitles();
        
        this.runTaskTimer(ZonePractice.getInstance(), 0, 20L);

        return this;
    }

    /**
     * Sends victory/defeat titles to players if enabled in ladder settings
     */
    private void sendRoundEndTitles() {
        if (titlesSent) return;
        titlesSent = true;

        if (!match.getLadder().isRoundStatusTitles()) {
            return;
        }

        // If MULTI_ROUND_START_COUNTDOWN is disabled, don't show victory/defeat titles
        // The "FIGHT!" title from RoundStartRunnable will be displayed instead
        if (match.getLadder().getRoundEndDelay() <= 0 && !this.ended) {
            return;
        }

        if (this.ended) {
            // Match is ending - show match winner/loser titles
            Object matchWinnerObj = match.getMatchWinner();
            if (matchWinnerObj instanceof Player winner) {

                // Show green "VICTORY!" to winner
                TitleUtil.sendTitle(
                        winner,
                        Common.deserializeMiniMessage(
                                LanguageManager.getString("MATCH.END-TITLES.MATCH.VICTORY")
                        ),
                        Common.deserializeMiniMessage(
                                getMatchSubtitle(winner, true)
                        ),
                        200,
                        1500,
                        300
                );
                
                // Show red "DEFEAT" to losers
                for (Player player : match.getPlayers()) {
                    if (player != winner) {
                        TitleUtil.sendTitle(
                                player,
                                Common.deserializeMiniMessage(
                                        LanguageManager.getString("MATCH.END-TITLES.MATCH.DEFEAT")
                                ),
                                Common.deserializeMiniMessage(
                                        getMatchSubtitle(winner, false)
                                ),
                                200,
                                1500,
                                300
                        );
                    }
                }
            }
        } else {
            // Round is ending - show round winner title
            if (round instanceof PlayerWinner) {
                Player roundWinner = ((PlayerWinner) round).getRoundWinner();
                if (roundWinner != null) {
                    String subtitle = getRoundSubtitle(roundWinner, null);

                    // Show green "VICTORY!" to round winner
                        TitleUtil.sendTitle(
                                roundWinner,
                                Common.deserializeMiniMessage(
                                        LanguageManager.getString("MATCH.END-TITLES.ROUND.VICTORY")
                                ),
                                Common.deserializeMiniMessage(subtitle),
                                200,
                                1500,
                                300
                        );
                    
                    // Show red "DEFEAT" to losers
                    for (Player player : match.getPlayers()) {
                        if (player != roundWinner) {
                            TitleUtil.sendTitle(
                                    player,
                                    Common.deserializeMiniMessage(
                                            LanguageManager.getString("MATCH.END-TITLES.ROUND.DEFEAT")
                                    ),
                                    Common.deserializeMiniMessage(subtitle),
                                    200,
                                    1500,
                                    300
                            );
                        }
                    }
                }
            } else if (round instanceof PlayersVsPlayersRound playersVsPlayersRound) {
                TeamEnum winnerTeam = playersVsPlayersRound.getRoundWinner();
                if (winnerTeam == null || !(match instanceof PlayersVsPlayers playersVsPlayers)) {
                    return;
                }

                String subtitle = getRoundSubtitle(null, winnerTeam);
                for (Player player : match.getPlayers()) {
                    boolean wonRound = playersVsPlayers.getTeam(player) == winnerTeam;
                    String titlePath = wonRound ? "MATCH.END-TITLES.ROUND.VICTORY" : "MATCH.END-TITLES.ROUND.DEFEAT";

                    TitleUtil.sendTitle(
                            player,
                            Common.deserializeMiniMessage(LanguageManager.getString(titlePath)),
                            Common.deserializeMiniMessage(subtitle),
                            200,
                            1500,
                            300
                    );
                }
            }
        }
    }

    private String getRoundSubtitle(Player roundWinner, TeamEnum winnerTeam) {
        if (!isMultiRoundMatch()) {
            return "";
        }

        String subtitlePath = winnerTeam != null
                ? "MATCH.END-TITLES.ROUND.SUBTITLE-TEAM"
                : "MATCH.END-TITLES.ROUND.SUBTITLE-PLAYER";

        return applyPlaceholders(
                LanguageManager.getString(subtitlePath),
                getRoundScorePlaceholders(roundWinner, winnerTeam)
        );
    }

    private Map<String, String> getRoundScorePlaceholders(Player roundWinner, TeamEnum winnerTeam) {
        Map<String, String> placeholders = new HashMap<>();
        Player losingPlayer = null;

        if (roundWinner != null) {
            for (Player player : match.getPlayers()) {
                if (player != roundWinner) {
                    losingPlayer = player;
                    break;
                }
            }

            int winnerScore = match.getWonRounds(roundWinner);
            int loserScore = losingPlayer != null ? match.getWonRounds(losingPlayer) : 0;

            placeholders.put("%winner%", roundWinner.getName());
            placeholders.put("%loser%", losingPlayer != null ? losingPlayer.getName() : "-");
            placeholders.put("%winnerScore%", String.valueOf(winnerScore));
            placeholders.put("%loserScore%", String.valueOf(loserScore));
            if (match instanceof Team teamMatch) {
                placeholders.put("%scoredTeam%", teamMatch.getTeam(roundWinner).getNameMM());
            } else {
                placeholders.put("%scoredTeam%", "-");
            }
        } else if (winnerTeam != null && match instanceof PlayersVsPlayers playersVsPlayers) {
            TeamEnum loserTeam = winnerTeam == TeamEnum.TEAM1 ? TeamEnum.TEAM2 : TeamEnum.TEAM1;
            int winnerScore = playersVsPlayers.getWonRounds(winnerTeam);
            int loserScore = playersVsPlayers.getWonRounds(loserTeam);

            placeholders.put("%winnerScore%", String.valueOf(winnerScore));
            placeholders.put("%loserScore%", String.valueOf(loserScore));
            placeholders.put("%scoredTeam%", winnerTeam.getNameMM());
            placeholders.put("%team1Name%", TeamEnum.TEAM1.getNameMM());
            placeholders.put("%team2Name%", TeamEnum.TEAM2.getNameMM());
            placeholders.put("%team1Score%", String.valueOf(playersVsPlayers.getWonRounds(TeamEnum.TEAM1)));
            placeholders.put("%team2Score%", String.valueOf(playersVsPlayers.getWonRounds(TeamEnum.TEAM2)));
        }

        placeholders.put("%roundNumber%", String.valueOf(round.getRoundNumber()));
        placeholders.put("%winsNeeded%", String.valueOf(match.getWinsNeeded()));
        return placeholders;
    }

    private String getMatchSubtitle(Player winner, boolean victory) {
        if (!isMultiRoundMatch()) {
            return "";
        }

        String path = victory
                ? "MATCH.END-TITLES.MATCH.SUBTITLE-VICTORY"
                : "MATCH.END-TITLES.MATCH.SUBTITLE-DEFEAT";

        return applyPlaceholders(LanguageManager.getString(path), getMatchScorePlaceholders(winner));
    }

    private boolean isMultiRoundMatch() {
        return match.getWinsNeeded() > 1;
    }

    private Map<String, String> getMatchScorePlaceholders(Player winner) {
        Map<String, String> placeholders = new HashMap<>();
        Player losingPlayer = null;

        for (Player player : match.getPlayers()) {
            if (player != winner) {
                losingPlayer = player;
                break;
            }
        }

        int winnerScore = match.getWonRounds(winner);
        int loserScore = losingPlayer != null ? match.getWonRounds(losingPlayer) : 0;

        placeholders.put("%winner%", winner.getName());
        placeholders.put("%loser%", losingPlayer != null ? losingPlayer.getName() : "-");
        placeholders.put("%winnerScore%", String.valueOf(winnerScore));
        placeholders.put("%loserScore%", String.valueOf(loserScore));
        placeholders.put("%roundNumber%", String.valueOf(round.getRoundNumber()));
        placeholders.put("%winsNeeded%", String.valueOf(match.getWinsNeeded()));
        return placeholders;
    }

    private String applyPlaceholders(String template, Map<String, String> placeholders) {
        String formatted = template == null ? "" : template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            formatted = formatted.replace(entry.getKey(), entry.getValue());
        }
        return formatted;
    }

    @Override
    public void cancel() {
        if (running) {
            Bukkit.getScheduler().cancelTask(this.getTaskId());
            running = false;
        }

        this.round.setRoundEndRunnable(null);
    }

    @Override
    public void run() {
        if (seconds == 0) {
            this.cancel();

            if (ended)
                match.endMatch();
            else if (rollbackBeforeNextRound)
                match.resetMap(match::startNextRound);
            else if (!suppressNextRound)
                match.startNextRound();
        } else
            seconds--;
    }
}



