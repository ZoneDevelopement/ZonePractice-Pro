package dev.nandi0813.practice.util.cooldown;

import dev.nandi0813.practice.ZonePractice;
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
public class GoldenAppleRunnable extends BukkitRunnable {

    private final Player player;
    private final Profile profile;
    private boolean running;
    private final int seconds;

    public GoldenAppleRunnable(Player player, int seconds) {
        this.player = player;
        this.seconds = seconds;
        profile = ProfileManager.getInstance().getProfile(player);
    }

    public void begin() {
        running = true;
        PlayerCooldown.addCooldown(player, CooldownObject.GOLDEN_APPLE, seconds);
        this.runTaskTimerAsynchronously(ZonePractice.getInstance(), 0, 10L);
    }

    @Override
    public void cancel() {
        if (running) {
            Bukkit.getScheduler().cancelTask(this.getTaskId());
            running = false;
            PlayerCooldown.removeCooldown(player, CooldownObject.GOLDEN_APPLE);
        }
    }

    @Override
    public void run() {
        if (PlayerCooldown.isActive(player, CooldownObject.GOLDEN_APPLE)) {
            if (profile.getStatus().equals(ProfileStatus.MATCH) || profile.getStatus().equals(ProfileStatus.FFA)) {
                Statistic roundStatistic = MatchManager.getInstance().getLiveMatchByPlayer(player).getCurrentStat(player);

                if (roundStatistic.isSet())
                    cancel();
            } else
                cancel();
        } else
            cancel();
    }
}
