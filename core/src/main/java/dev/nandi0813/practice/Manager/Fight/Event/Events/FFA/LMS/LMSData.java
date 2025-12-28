package dev.nandi0813.practice.Manager.Fight.Event.Events.FFA.LMS;

import dev.nandi0813.practice.Manager.Fight.Event.Enum.EventType;
import dev.nandi0813.practice.Manager.Fight.Event.Interface.EventData;
import dev.nandi0813.practice.Module.Interfaces.KitData;
import dev.nandi0813.practice.Module.Util.ClassImport;
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
