package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.PlayerWinner;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.ScoringLadder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Setter
@Getter
public class Boxing extends NormalLadder implements CustomConfig, LadderHandle, ScoringLadder {

    private int boxingWinHit;
    private static final String BOXING_WINHIT_PATH = "boxing-winhit";

    public Boxing(String name, LadderType type) {
        super(name, type);
    }

    @Override
    public boolean shouldEndRound(Match match, Round round, Player player) {
        // Check if the player has reached the required hits
        int requiredStrokes = boxingWinHit - 1;
        return match.getCurrentStat(player).getHit() == requiredStrokes;
    }

    @Override
    public String getWinConditionMessage() {
        return "reached " + boxingWinHit + " hits";
    }

    @Override
    public String getScoringDisplayName() {
        return "Hits";
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof EntityDamageEvent) {
            onPlayerDamage((EntityDamageEvent) e, match);

            if (e instanceof EntityDamageByEntityEvent)
                onPlayerDamagePlayer((EntityDamageByEntityEvent) e, match, this);

            return true;
        }
        return false;
    }

    @Override
    public void setCustomConfig(YamlConfiguration config) {
        config.set(BOXING_WINHIT_PATH, this.boxingWinHit);
    }

    @Override
    public void getCustomConfig(YamlConfiguration config) {
        if (config.isInt(BOXING_WINHIT_PATH)) {
            this.boxingWinHit = config.getInt(BOXING_WINHIT_PATH);
            if (this.boxingWinHit < 40 || this.boxingWinHit > 600)
                this.boxingWinHit = 100;
        } else
            this.boxingWinHit = 100;
    }

    private static void onPlayerDamagePlayer(final @NotNull EntityDamageByEntityEvent e, final @NotNull Match match, final @NotNull Boxing ladder) {
        Player attacker = (Player) e.getDamager();
        int requiredStrokes = ladder.getBoxingWinHit();
        requiredStrokes--;

        MatchType matchType = match.getType();
        TeamEnum attackerTeam;

        Round round = match.getCurrentRound();

        switch (matchType) {
            case DUEL:
            case PARTY_FFA:
                if (match.getCurrentStat(attacker).getHit() == requiredStrokes && round instanceof PlayerWinner) {
                    PlayerWinner playerWinner = (PlayerWinner) match.getCurrentRound();

                    if (!match.getCurrentStat(attacker).isSet()) {
                        playerWinner.setRoundWinner(attacker);
                        round.endRound();
                    }
                }
                break;
            case PARTY_SPLIT:
            case PARTY_VS_PARTY:
                if (match instanceof PlayersVsPlayers playersVsPlayers) {
                    attackerTeam = playersVsPlayers.getTeam(attacker);

                    if (getTeamBoxingStrokes(match, playersVsPlayers.getTeamPlayers(attackerTeam)) == requiredStrokes) {
                        if (!match.getCurrentStat(attacker).isSet()) {
                            playersVsPlayers.getCurrentRound().setRoundWinner(attackerTeam);
                            round.endRound();
                        }
                    }
                }
                break;
        }
    }

    private static void onPlayerDamage(final @NotNull EntityDamageEvent e, final @NotNull Match match) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.setDamage(0);
            player.setHealth(20);
        }
    }

    public static int getTeamBoxingStrokes(Match match, List<Player> team) {
        int strokes = 0;
        for (Player player : team)
            strokes += match.getCurrentStat(player).getHit();
        return strokes;
    }

}
