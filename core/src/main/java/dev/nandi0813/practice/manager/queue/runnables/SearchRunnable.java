package dev.nandi0813.practice.manager.queue.runnables;

import dev.nandi0813.practice.manager.queue.Queue;
import dev.nandi0813.practice.manager.queue.QueueManager;
import dev.nandi0813.practice.util.actionbar.ActionBar;
import dev.nandi0813.practice.util.interfaces.Runnable;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public abstract class SearchRunnable extends Runnable {

    protected final QueueManager queueManager = QueueManager.getInstance();
    protected final Queue queue;
    protected final ActionBar actionBar;
    protected final boolean ownsActionBar;

    protected BukkitTask searching;

    public SearchRunnable(final Queue queue, long delay, long period, boolean async) {
        super(delay, period, async);
        this.queue = queue;
        this.actionBar = queue.getProfile().getActionBar();
        this.ownsActionBar = !this.actionBar.isLock();
        if (this.ownsActionBar) {
            this.actionBar.createActionBar();
        }
    }

    @Override
    public void cancel() {
        if (!running) return;

        running = false;
        Bukkit.getScheduler().cancelTask(this.getTaskId());

        if (searching != null) {
            searching.cancel();
        }
        queue.cancel();
        queue.getSearchRunnable().cancel();
        if (this.ownsActionBar) {
            actionBar.cancelActionBar();
        }
    }

    protected void updateQueueActionBar(String message) {
        if (!this.ownsActionBar) {
            return;
        }
        this.actionBar.setMessage(message);
    }

    public abstract void run();

}
