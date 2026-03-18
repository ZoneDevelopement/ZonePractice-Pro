package dev.nandi0813.practice.manager.fight.ffa.game;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
import dev.nandi0813.practice.util.interfaces.Runnable;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class BuildRollback extends Runnable {

    private static final int ROLLBACK_SECONDS = ConfigManager.getInt("FFA.ROLLBACK.SECONDS");

    private final FightChangeOptimized fightChange;
    private final java.lang.Runnable onRollbackComplete;

    public BuildRollback(FightChangeOptimized fightChange, java.lang.Runnable onRollbackComplete) {
        super(20L, 20L, false);
        this.fightChange = fightChange;
        this.onRollbackComplete = onRollbackComplete;
        this.seconds = ROLLBACK_SECONDS;
    }

    @Override
    public void run() {
        if (seconds == 0) {
            this.rollback();
        }

        seconds--;
    }

    @Override
    public void cancel() {
        if (!running) return;

        running = false;
        Bukkit.getScheduler().cancelTask(this.getTaskId());

        this.rollback();
    }

    public void rollback() {
        this.seconds = ROLLBACK_SECONDS;

        if (ZonePractice.getInstance().isEnabled()) {
            fightChange.rollback(300, 100, onRollbackComplete);
        } else {
            fightChange.quickRollback();
            if (onRollbackComplete != null) {
                onRollbackComplete.run();
            }
        }
    }

}
