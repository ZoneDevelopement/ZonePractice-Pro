package dev.nandi0813.practice.manager.queue.runnables;

import dev.nandi0813.practice.manager.queue.Queue;
import dev.nandi0813.practice.manager.queue.QueueManager;
import dev.nandi0813.practice.util.actionbar.ActionBar;
import dev.nandi0813.practice.util.actionbar.ActionBarPriority;
import dev.nandi0813.practice.util.interfaces.Runnable;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public abstract class SearchRunnable extends Runnable {

    protected final QueueManager queueManager = QueueManager.getInstance();
    protected final Queue queue;
    protected final ActionBar actionBar;

    protected BukkitTask searching;
    private static final String ACTION_BAR_ID = "queue";

    public SearchRunnable(final Queue queue, long delay, long period, boolean async) {
        super(delay, period, async);
        this.queue = queue;
        this.actionBar = queue.getProfile().getActionBar();
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

        // Clear the queue action bar message
        actionBar.removeMessage(ACTION_BAR_ID);
    }

    protected void updateQueueActionBar(String message) {
        // Sets an infinite action bar with NORMAL priority
        this.actionBar.setMessage(ACTION_BAR_ID, message, -1, ActionBarPriority.NORMAL);
    }

    public abstract void run();

}
