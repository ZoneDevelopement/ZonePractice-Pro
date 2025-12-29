package dev.nandi0813.practice.manager.fight.match.interfaces;

import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import org.bukkit.entity.Player;

public interface Team {

    TeamEnum getTeam(Player player);

}
