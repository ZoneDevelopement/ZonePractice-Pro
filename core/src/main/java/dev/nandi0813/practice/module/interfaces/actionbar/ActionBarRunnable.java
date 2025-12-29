package dev.nandi0813.practice.module.interfaces.actionbar;

import dev.nandi0813.practice.util.interfaces.Runnable;

public class ActionBarRunnable extends Runnable {

    private final ActionBar actionBar;

    public ActionBarRunnable(final ActionBar actionBar) {
        super(0L, 20L, false);

        this.actionBar = actionBar;
    }

    @Override
    public void run() {
        if (!this.actionBar.getProfile().getPlayer().isOnline()) {
            this.actionBar.cancelActionBar();
            return;
        }

        if (this.seconds == this.actionBar.getDuration()) {
            this.actionBar.cancelActionBar();
            return;
        }

        this.seconds++;
        this.actionBar.send();
    }

}
