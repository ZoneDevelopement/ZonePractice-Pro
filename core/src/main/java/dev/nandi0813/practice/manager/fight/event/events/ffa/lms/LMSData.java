package dev.nandi0813.practice.manager.fight.event.events.ffa.lms;

import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.module.interfaces.KitData;
import dev.nandi0813.practice.module.util.ClassImport;
import lombok.Getter;

import java.io.IOException;

@Getter
public class LMSData extends EventData {

    private final KitData kitData = ClassImport.createKitData();

    public LMSData() {
        super(EventType.LMS);
    }

    @Override
    protected void setCustomData() {
        if (kitData != null) {
            kitData.saveData(config, "kit");
        }
    }

    @Override
    protected void getCustomData() {
        if (kitData != null) {
            kitData.getData(config, "kit");
        }
    }

    @Override
    protected void enable() throws NullPointerException, IOException {
        if (kitData != null && !kitData.isSet()) {
            throw new IOException("Kit data is not set.");
        }
    }
}
