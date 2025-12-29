package dev.nandi0813.practice.util.cooldown;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class FireballRunnable extends BukkitRunnable {

    private final Player player;
    private final Profile profile;
    private boolean running;
    private final double seconds;

    public FireballRunnable(Player player, double seconds) {
        this.player = player;
        this.seconds = seconds;
        profile = ProfileManager.getInstance().getProfile(player);
    }

    public void begin() {
        running = true;
        PlayerCooldown.addCooldown(player, CooldownObject.FIREBALL_FIGHT_FIREBALL, seconds);
        this.runTaskTimerAsynchronously(ZonePractice.getInstance(), 0, 10L);
    }

    @Override
    public void cancel() {
        if (running) {
            Bukkit.getScheduler().cancelTask(this.getTaskId());
            running = false;
            PlayerCooldown.removeCooldown(player, CooldownObject.FIREBALL_FIGHT_FIREBALL);
        }
    }

    @Override
    public void run() {
        if (PlayerCooldown.isActive(player, CooldownObject.FIREBALL_FIGHT_FIREBALL)) {
            if (profile.getStatus().equals(ProfileStatus.MATCH)) {
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                Statistic roundStatistic = match.getCurrentStat(player);

                if (roundStatistic.isSet())
                    cancel();
            } else if (profile.getStatus().equals(ProfileStatus.EVENT)) {
                Event event = EventManager.getInstance().getEventByPlayer(player);

                if (event.getStatus().equals(EventStatus.END))
                    cancel();
            } else
                cancel();
        } else
            cancel();
    }
}
