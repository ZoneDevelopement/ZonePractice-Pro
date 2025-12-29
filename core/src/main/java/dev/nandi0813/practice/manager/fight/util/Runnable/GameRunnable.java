package dev.nandi0813.practice.manager.fight.util.Runnable;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class GameRunnable extends BukkitRunnable {

    protected final Player player;
    protected final Profile profile;
    protected final FightPlayer fightPlayer;

    protected final CooldownObject cooldownObject;
    protected boolean running;
    protected final int seconds;

    @Getter
    protected boolean expBarUse = false;
    protected final boolean EXP_BAR;

    public GameRunnable(Player player, FightPlayer fightPlayer, int seconds, CooldownObject cooldownObject, boolean EXP_BAR) {
        this.player = player;
        this.profile = ProfileManager.getInstance().getProfile(player);

        this.seconds = seconds;
        this.cooldownObject = cooldownObject;
        this.EXP_BAR = EXP_BAR;

        this.fightPlayer = fightPlayer;
        if (!this.fightPlayer.isExpBarUsed())
            this.expBarUse = true;
    }

    public void begin() {
        this.fightPlayer.getGameRunnables().add(this);

        running = true;
        PlayerCooldown.addCooldown(player, cooldownObject, this.seconds);
        this.runTaskTimerAsynchronously(ZonePractice.getInstance(), 0, 2L);
    }

    @Override
    public void run() {
        if (!PlayerCooldown.isActive(player, this.cooldownObject)) {
            this.cancel();
            return;
        }

        if (!profile.getStatus().equals(ProfileStatus.MATCH)) {
            this.cancel();
            return;
        }

        if (MatchManager.getInstance().getLiveMatchByPlayer(player).getCurrentStat(player).isSet()) {
            this.cancel();
            return;
        }

        if (this.EXP_BAR && this.expBarUse) {
            int level = (int) (PlayerCooldown.getLeft(player, this.cooldownObject) / 1000);
            player.setLevel(level);

            float exp = ((PlayerCooldown.getLeft(player, this.cooldownObject) / (float) seconds) / 1000);
            player.setExp(exp);
        }
    }

    @Override
    public void cancel() {
        this.cancel(false);
    }

    public void cancel(boolean died) {
        if (!running) return;

        running = false;
        Bukkit.getScheduler().cancelTask(this.getTaskId());

        if (expBarUse) {
            player.setExp(0);
            player.setLevel(0);
        }
        this.fightPlayer.getGameRunnables().remove(this);

        PlayerCooldown.removeCooldown(this.player, this.cooldownObject);

        if (!died)
            abstractCancel();
    }

    public abstract void abstractCancel();

}
