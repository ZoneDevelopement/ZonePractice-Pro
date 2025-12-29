package dev.nandi0813.practice.manager.fight.match.interfaces;

import org.bukkit.entity.Player;

public interface PlayerWinner {

    void setRoundWinner(Player roundWinner);

    Player getRoundWinner();

}
