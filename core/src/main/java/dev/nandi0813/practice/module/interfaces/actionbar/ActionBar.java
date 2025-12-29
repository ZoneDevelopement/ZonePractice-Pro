package dev.nandi0813.practice.module.interfaces.actionbar;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

@Getter
public abstract class ActionBar {

    protected final Profile profile;

    protected Component message;
    protected int duration; // If -1 then infinite
    @Setter
    private boolean lock;
    private ActionBarRunnable actionBarRunnable;

    public ActionBar(final Profile profile) {
        this.profile = profile;
        this.lock = false;
    }

    protected abstract void send();

    protected abstract void clear();

    /**
     * Only use it for constant messages
     *
     * @param message  The message to be displayed
     * @param duration The duration of the message in seconds
     */
    public void setActionBar(final String message, final int duration) {
        if (actionBarRunnable != null && (actionBarRunnable.isRunning() || actionBarRunnable.isHasRun())) {
            actionBarRunnable.cancel();
            actionBarRunnable = new ActionBarRunnable(this);
        } else if (actionBarRunnable == null) {
            actionBarRunnable = new ActionBarRunnable(this);
        }

        this.setMessage(message);
        this.duration = duration;

        actionBarRunnable.begin();
    }

    public void createActionBar() {
        if (actionBarRunnable != null && (actionBarRunnable.isRunning() || actionBarRunnable.isHasRun())) {
            actionBarRunnable.cancel();
            actionBarRunnable = new ActionBarRunnable(this);
        } else if (actionBarRunnable == null) {
            actionBarRunnable = new ActionBarRunnable(this);
        }

        this.message = Component.empty();
        this.duration = -1;
        this.lock = true;

        actionBarRunnable.begin();
    }

    public void setDuration(final int duration) {
        this.duration = duration;

        if (actionBarRunnable != null) {
            actionBarRunnable.setSeconds(0);
        }
    }

    public void cancelActionBar() {
        if (actionBarRunnable != null && (actionBarRunnable.isRunning() || actionBarRunnable.isHasRun())) {
            actionBarRunnable.cancel();
            this.clear();
            actionBarRunnable = null;
        }
        this.lock = false;
    }

    public void setMessage(final String message) {
        this.message = ZonePractice.getMiniMessage().deserialize(message);
    }

}
