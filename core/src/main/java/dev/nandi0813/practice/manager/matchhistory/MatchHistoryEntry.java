package dev.nandi0813.practice.manager.matchhistory;

import lombok.Getter;

import java.util.UUID;

@Getter
public class MatchHistoryEntry {

    private final int matchId;
    private final UUID playerUuid;
    private final UUID opponentUuid;
    private final String playerName;
    private final String opponentName;
    private final String kitName;
    private final String arenaName;
    private final int playerScore;
    private final int opponentScore;
    private final double playerFinalHealth;
    private final double opponentFinalHealth;
    private final UUID winnerUuid;
    private final int matchDuration;
    private final long playedAt;

    public MatchHistoryEntry(int matchId, UUID playerUuid, UUID opponentUuid,
                             String playerName, String opponentName,
                             String kitName, String arenaName,
                             int playerScore, int opponentScore,
                             double playerFinalHealth, double opponentFinalHealth,
                             UUID winnerUuid, int matchDuration, long playedAt) {
        this.matchId = matchId;
        this.playerUuid = playerUuid;
        this.opponentUuid = opponentUuid;
        this.playerName = playerName;
        this.opponentName = opponentName;
        this.kitName = kitName;
        this.arenaName = arenaName;
        this.playerScore = playerScore;
        this.opponentScore = opponentScore;
        this.playerFinalHealth = playerFinalHealth;
        this.opponentFinalHealth = opponentFinalHealth;
        this.winnerUuid = winnerUuid;
        this.matchDuration = matchDuration;
        this.playedAt = playedAt;
    }

    public boolean isWinner(UUID uuid) {
        return winnerUuid != null && winnerUuid.equals(uuid);
    }

    public String getFormattedDuration() {
        int minutes = matchDuration / 60;
        int seconds = matchDuration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
