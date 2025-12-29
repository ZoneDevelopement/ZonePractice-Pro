package dev.nandi0813.practice.manager.fight.event.runnables.queue;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.interfaces.Runnable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QueueRunnable extends Runnable {

    private static final boolean MESSAGE_ENABLED = ConfigManager.getBoolean("EVENT.BROADCAST.ENABLE");
    private static final boolean MESSAGE_IN_PARTY = ConfigManager.getBoolean("EVENT.BROADCAST.MESSAGE-TO.IN-PARTY");
    private static final boolean MESSAGE_IN_MATCH_UNRANKED = ConfigManager.getBoolean("EVENT.BROADCAST.MESSAGE-TO.IN-UNRANKED-MATCH");
    private static final boolean MESSAGE_IN_MATCH_RANKED = ConfigManager.getBoolean("EVENT.BROADCAST.MESSAGE-TO.IN-RANKED-MATCH");
    private static final boolean MESSAGE_IN_EVENT = ConfigManager.getBoolean("EVENT.BROADCAST.MESSAGE-TO.IN-EVENT");

    @Getter
    private final Event event;
    private int broadcastCounter = 0;

    private QueueStartRunnable queueStartRunnable;

    public QueueRunnable(final Event event) {
        super(0, 20, false);

        this.event = event;
        seconds = event.getEventData().getMaxQueueTime();
    }

    @Override
    public void run() {
        if (MESSAGE_ENABLED) {
            broadcast();
        }

        if (seconds == 0) {
            if (queueStartRunnable != null &&
                    queueStartRunnable.isRunning() &&
                    queueStartRunnable.getSeconds() < 20) {
                this.seconds = 20;
                return;
            }

            event.stopQueue();
            cancel();
        } else {
            this.seconds--;
        }
    }

    public void setStarting() {
        if (this.queueStartRunnable != null) {
            this.queueStartRunnable.cancel();
            this.queueStartRunnable = null;
        }

        this.queueStartRunnable = new QueueStartRunnable(this);
        this.queueStartRunnable.begin();
    }

    public void setStartingMaxPlayers() {
        if (this.queueStartRunnable.getSeconds() <= 5) {
            return;
        }

        this.queueStartRunnable.setSeconds(5);
    }

    public void playerLeave() {
        if (this.event.getPlayers().size() < this.event.getEventData().getMinPlayer()) {
            if (this.queueStartRunnable != null) {
                this.queueStartRunnable.cancel();
                this.queueStartRunnable = null;
            }
        }
    }

    private void broadcast() {
        this.broadcastCounter++;

        if (this.broadcastCounter == this.event.getEventData().getBroadcastInterval()) {
            this.broadcastCounter = 0;

            for (Player online : Bukkit.getOnlinePlayers()) {
                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);

                if (event.getPlayers().contains(online)) {
                    continue;
                }

                if (onlineProfile.isParty() && !MESSAGE_IN_PARTY) {
                    continue;
                } else if (onlineProfile.getStatus().equals(ProfileStatus.MATCH)) {
                    Match match = MatchManager.getInstance().getLiveMatchByPlayer(online);

                    if (match != null) {
                        if (match instanceof Duel && (((Duel) match).isRanked() && MESSAGE_IN_MATCH_RANKED))
                            continue;
                        else if (MESSAGE_IN_MATCH_UNRANKED) {
                            continue;
                        }
                    }
                } else if (onlineProfile.getStatus().equals(ProfileStatus.EVENT) && !MESSAGE_IN_EVENT) {
                    continue;
                }

                Common.sendMMMessage(online, event.getType().getBroadcastMSG());
            }
        }
    }

    @Override
    public void cancel() {
        if (running) {
            running = false;
            Bukkit.getScheduler().cancelTask(this.getTaskId());
        }

        if (this.queueStartRunnable != null) {
            this.queueStartRunnable.cancel();
            this.queueStartRunnable = null;
        }
    }

    @Override
    public String getFormattedTime() {
        if (queueStartRunnable != null && queueStartRunnable.isRunning()) {
            return StringUtil.formatMillisecondsToMinutes(queueStartRunnable.getSeconds() * 1000L);
        }
        return null;
    }

}
