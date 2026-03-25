package dev.nandi0813.practice.util.actionbar;

import dev.nandi0813.practice.util.interfaces.Runnable;

public class ActionBarRunnable extends Runnable {

    private final ActionBar actionBar;

    public ActionBarRunnable(final ActionBar actionBar) {
        // Runs every 20 ticks (1 second)
        super(0L, 20L, false);
        this.actionBar = actionBar;
    }

    @Override
    public void run() {
        this.actionBar.tick();
    }
}