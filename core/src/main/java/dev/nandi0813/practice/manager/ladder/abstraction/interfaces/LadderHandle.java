package dev.nandi0813.practice.manager.ladder.abstraction.interfaces;

import dev.nandi0813.practice.manager.fight.match.Match;
import org.bukkit.event.Event;

public interface LadderHandle {

    boolean handleEvents(Event e, Match match);

}
