package dev.nandi0813.practice.manager.fight.event.events.duel.interfaces;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DuelFight {

    private final DuelEvent duelEvent;
    private boolean ended;

    @Getter
    private final List<Player> players;
    @Getter
    private final List<Player> spectators = new ArrayList<>();

    public DuelFight(final DuelEvent duelEvent, final List<Player> players) {
        this.duelEvent = duelEvent;
        this.players = players;
        this.ended = false;
    }

    public void endFight(final Player loser) {
        if (this.ended) {
            return;
        } else {
            this.ended = true;
        }

        this.duelEvent.getFights().remove(this);

        if (loser == null) {
            for (Player player : players) {
                this.duelEvent.sendMessage(LanguageManager.getString(duelEvent.getLANGUAGE_PATH() + ".PLAYER-OUT").replaceAll("%player%", player.getName()), true);
                this.duelEvent.getPlayers().remove(player);
                this.duelEvent.getSpectators().add(player);
            }
        } else {
            this.duelEvent.sendMessage(LanguageManager.getString(duelEvent.getLANGUAGE_PATH() + ".PLAYER-OUT").replaceAll("%player%", loser.getName()), true);
            this.duelEvent.getPlayers().remove(loser);
            this.duelEvent.getSpectators().add(loser);
            this.sendMessage(LanguageManager.getString(duelEvent.getLANGUAGE_PATH() + ".WON-FIGHT").replaceAll("%player%", getOtherPlayer(loser).getName()));
        }

        if (!this.duelEvent.checkIfEnd()) {
            if (this.duelEvent.getFights().isEmpty()) {
                this.duelEvent.startNextRound();
            } else {
                List<Player> forward = new ArrayList<>(this.players);
                forward.addAll(this.spectators);

                for (Player player : forward) {
                    this.duelEvent.addSpectator(player, this.duelEvent.getRandomFightPlayer(), true, true);
                    Common.sendMMMessage(player, LanguageManager.getString(duelEvent.getLANGUAGE_PATH() + ".SPECTATOR-FORWARDED"));
                }
            }
        } else {
            for (Player player : this.players) {
                if (!this.duelEvent.getPlayers().contains(player)) {
                    this.duelEvent.getPlayers().add(player);
                }
                this.duelEvent.getSpectators().remove(player);
            }

            this.duelEvent.endEvent();
        }
    }

    public Player getOtherPlayer(Player player) {
        for (Player fightPlayer : players)
            if (!fightPlayer.equals(player))
                return fightPlayer;
        return null;
    }

    public void sendMessage(String message) {
        List<Player> messageTo = new ArrayList<>();
        messageTo.addAll(players);
        messageTo.addAll(spectators);

        for (Player player : messageTo)
            Common.sendMMMessage(player, message);
    }

}
