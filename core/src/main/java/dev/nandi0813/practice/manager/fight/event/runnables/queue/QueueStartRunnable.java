package dev.nandi0813.practice.manager.fight.event.runnables.queue;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.interfaces.Runnable;
import org.bukkit.entity.Player;

public class QueueStartRunnable extends Runnable {

    private final Event event;

    public QueueStartRunnable(final QueueRunnable queueRunnable) {
        super(0, 20, false);

        this.event = queueRunnable.getEvent();
        this.seconds = this.event.getEventData().getWaitBeforeStart();
    }

    @Override
    public void run() {
        if (this.seconds == this.event.getEventData().getWaitBeforeStart() ||
                seconds == 10 ||
                seconds <= 5 &&
                        seconds != 0) {
            event.sendMessage(LanguageManager.getString("EVENT.QUEUE-START-COUNTDOWN")
                            .replace("%seconds%", String.valueOf(seconds))
                            .replace("%secondName%", (seconds == 1 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC")))
                    , false);
        }

        if (seconds == 0) {
            this.cancel();
            event.start();
            event.getQueueRunnable().cancel();

            if (event.getStarter() instanceof Player starter) {
                Profile starterProfile = ProfileManager.getInstance().getProfile(starter);
                starterProfile.setEventStartLeft(starterProfile.getEventStartLeft() - 1);
            }
        }

        this.seconds--;
    }

}
