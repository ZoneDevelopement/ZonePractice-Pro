package dev.nandi0813.practice.manager.fight.event.runnables;

import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.util.interfaces.Runnable;

public class StartRunnable extends Runnable {

    private final Event event;

    public StartRunnable(final Event event) {
        super(20, 20, false);
        this.event = event;
        this.seconds = this.event.getEventData().getStartTime();
    }

    @Override
    public void run() {
        this.event.handleStartRunnable(this);
    }

    public void decreaseTime() {
        this.seconds--;
    }

}
