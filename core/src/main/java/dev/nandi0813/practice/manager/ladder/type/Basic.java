package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import org.bukkit.event.Event;

public class Basic extends NormalLadder implements LadderHandle {

    public Basic(String name, LadderType type) {
        super(name, type);
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        return false;
    }

}
