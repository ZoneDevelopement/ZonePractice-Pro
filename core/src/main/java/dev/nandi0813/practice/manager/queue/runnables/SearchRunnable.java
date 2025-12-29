package dev.nandi0813.practice.manager.queue.runnables;

import dev.nandi0813.practice.manager.queue.Queue;
import dev.nandi0813.practice.manager.queue.QueueManager;
import dev.nandi0813.practice.module.interfaces.actionbar.ActionBar;
import dev.nandi0813.practice.util.interfaces.Runnable;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public abstract class SearchRunnable extends Runnable {

    protected final QueueManager queueManager = QueueManager.getInstance();
    protected final Queue queue;
    protected final ActionBar actionBar;

    protected BukkitTask searching;

    public SearchRunnable(final Queue queue, long delay, long period, boolean async) {
        super(delay, period, async);
        this.queue = queue;
        this.actionBar = queue.getProfile().getActionBar();
        this.actionBar.createActionBar();
    }

    @Override
    public void cancel() {
        if (!running) return;

        running = false;
        Bukkit.getScheduler().cancelTask(this.getTaskId());

        searching.cancel();
        queue.cancel();
        queue.getSearchRunnable().cancel();
        queue.getProfile().getActionBar().cancelActionBar();
        actionBar.cancelActionBar();
    }

    public abstract void run();

}
