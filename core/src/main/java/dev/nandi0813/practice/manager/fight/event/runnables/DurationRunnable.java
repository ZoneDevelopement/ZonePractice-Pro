package dev.nandi0813.practice.manager.fight.event.runnables;

import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.util.interfaces.Runnable;

public class DurationRunnable extends Runnable {

    private final Event event;

    public DurationRunnable(final Event event) {
        super(0, 20, false);
        this.event = event;
        this.seconds = event.getEventData().getDuration();
    }

    @Override
    public void run() {
        event.handleDurationRunnable(this);
    }

    public void decreaseTime() {
        this.seconds--;
    }

}
