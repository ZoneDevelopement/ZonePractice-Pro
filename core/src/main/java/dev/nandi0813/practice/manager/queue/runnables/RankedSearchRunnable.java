package dev.nandi0813.practice.manager.queue.runnables;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.queue.Queue;
import org.bukkit.scheduler.BukkitRunnable;

public class RankedSearchRunnable extends SearchRunnable {

    private static final int RANGE_INCREASE = ConfigManager.getConfig().getInt("QUEUE.RANKED.ELO-RANGE-INCREASE");
    private static final int ELO_RANGE_TIME = ConfigManager.getInt("QUEUE.RANKED.ELO-RANGE-TIME");
    private int range = ConfigManager.getConfig().getInt("QUEUE.RANKED.ELO-RANGE-INCREASE");

    public RankedSearchRunnable(final Queue queue) {
        super(queue, 0, 20L * ELO_RANGE_TIME, false);

        this.searching = new BukkitRunnable() {
            @Override
            public void run() {
                for (Queue q : queueManager.getQueues()) {
                    if (q == queueManager.getQueue(queue.getPlayer()))
                        continue;

                    if (q.getPlayer() == queue.getPlayer())
                        continue;

                    if (!q.isRanked())
                        continue;

                    for (NormalLadder ladder : getShuffledQueuedLadders()) {
                        if (!q.isQueued(ladder)) {
                            continue;
                        }

                        int selfElo = queue.getProfile().getStats().getLadderStat(ladder).getElo();
                        int queueElo = q.getProfile().getStats().getLadderStat(ladder).getElo();

                        if ((selfElo - range) <= queueElo && (selfElo + range) >= queueElo) {
                            queue.startMatch(q, ladder);

                            this.cancel();
                            return;
                        }
                    }
                }
            }
        }.runTaskTimer(ZonePractice.getInstance(), 0, 20L);
    }

    @Override
    public void run() {
        if (queueManager.getQueue(queue.getPlayer()) == null || queue.getQueuedLadders().isEmpty()) {
            this.cancel();
            return;
        }

        this.range += RANGE_INCREASE;

        int minElo = Integer.MAX_VALUE;
        for (NormalLadder ladder : queue.getQueuedLadders()) {
            int ladderElo = queue.getProfile().getStats().getLadderStat(ladder).getElo();
            minElo = Math.min(minElo, ladderElo);
        }

        if (minElo == Integer.MAX_VALUE) {
            minElo = 0;
        }

        this.sendMSG(minElo);
    }

    private void sendMSG(int elo) {
        this.updateQueueActionBar(LanguageManager.getString("QUEUES.ELO-RANGE")
                .replace("%from%", String.valueOf(elo - range))
                .replace("%to%", String.valueOf(elo + range)));
    }

}
