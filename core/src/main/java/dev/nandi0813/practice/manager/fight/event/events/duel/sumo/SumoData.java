package dev.nandi0813.practice.manager.fight.event.events.duel.sumo;

import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelEventData;

import java.io.IOException;

public class SumoData extends DuelEventData {

    public SumoData() {
        super(EventType.SUMO);
    }

    @Override
    protected void setCustomData() {
    }

    @Override
    protected void getCustomData() {
    }

    @Override
    protected void enable() throws IOException {
        if (spawns.size() != 2) {
            throw new IOException("Spawn positions are not set. Or not equal to 2. Current size: " + spawns.size());
        }
    }
}
