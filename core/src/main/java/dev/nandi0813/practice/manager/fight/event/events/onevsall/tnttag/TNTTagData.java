package dev.nandi0813.practice.manager.fight.event.events.onevsall.tnttag;

import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;

import java.io.IOException;

public class TNTTagData extends EventData {

    public TNTTagData() {
        super(EventType.TNTTAG);
    }

    @Override
    protected void setCustomData() {
    }

    @Override
    protected void getCustomData() {
    }

    @Override
    protected void enable() throws IOException {
    }

}
