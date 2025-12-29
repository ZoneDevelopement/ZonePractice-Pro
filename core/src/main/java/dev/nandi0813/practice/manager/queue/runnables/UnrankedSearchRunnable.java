package dev.nandi0813.practice.manager.queue.runnables;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.queue.Queue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class UnrankedSearchRunnable extends SearchRunnable {

    private static final int SEARCH_IN_OWN_DIVISION = ConfigManager.getInt("QUEUE.UNRANKED.DIVISION-SEARCH.SETTINGS.SEARCH-IN-OWN-DIVISIONS");
    private static final int SEARCH_IN_NEXT_DIVISION = ConfigManager.getInt("QUEUE.UNRANKED.DIVISION-SEARCH.SETTINGS.EXTEND-DIVISION-SEARCH");

    private final List<Division> acceptableDivisions = new ArrayList<>();
    private final Division currentDivision;
    private Division nextDivision = null;
    private Division previousDivision = null;

    public UnrankedSearchRunnable(final Queue queue) {
        super(queue, SEARCH_IN_OWN_DIVISION * 20L, SEARCH_IN_NEXT_DIVISION * 20L, false);

        this.currentDivision = queue.getProfile().getStats().getDivision();
        this.acceptableDivisions.add(currentDivision);
        this.sendMSG();

        this.searching = new BukkitRunnable() {
            @Override
            public void run() {
                for (Queue q : queueManager.getQueues()) {
                    if (q == queue)
                        continue;

                    if (q.getPlayer() == queue.getPlayer())
                        continue;

                    if (q.getLadder() != queue.getLadder())
                        continue;

                    if (queue.getProfile().getIgnoredPlayers().contains(q.getProfile()))
                        continue;
                    else if (q.getProfile().getIgnoredPlayers().contains(queue.getProfile()))
                        continue;

                    if (q.isRanked())
                        continue;

                    Division queueDivision = q.getProfile().getStats().getDivision();
                    if (acceptableDivisions.contains(queueDivision)) {
                        this.cancel();
                        queue.startMatch(q);

                        return;
                    }
                }
            }
        }.runTaskTimer(ZonePractice.getInstance(), 0, 20L);
    }

    @Override
    public void run() {
        Division nextDivision2;
        if (nextDivision == null)
            nextDivision2 = DivisionManager.getInstance().getNextDivision(currentDivision);
        else
            nextDivision2 = DivisionManager.getInstance().getNextDivision(nextDivision);

        if (nextDivision2 != null) {
            nextDivision = nextDivision2;
            if (!acceptableDivisions.contains(nextDivision))
                acceptableDivisions.add(nextDivision);
        }

        Division previousDivision2;
        if (previousDivision == null)
            previousDivision2 = DivisionManager.getInstance().getPreviousDivision(currentDivision);
        else
            previousDivision2 = DivisionManager.getInstance().getPreviousDivision(previousDivision);

        if (previousDivision2 != null) {
            previousDivision = previousDivision2;
            if (!acceptableDivisions.contains(previousDivision))
                acceptableDivisions.add(previousDivision);
        }

        this.sendMSG();
    }

    private void sendMSG() {
        if (acceptableDivisions.size() == 1) {
            this.actionBar.setMessage(LanguageManager.getString("QUEUES.UNRANKED.SEARCHING-OWN-DIVISION")
                    .replaceAll("%division_fullName%", currentDivision.getFullName())
                    .replaceAll("%division_shortName%", currentDivision.getShortName()));
        } else if (previousDivision != null && nextDivision == null) {
            this.actionBar.setMessage(LanguageManager.getString("QUEUES.UNRANKED.SEARCHING-IN-RANGE")
                    .replaceAll("%from_fullName%", previousDivision.getFullName())
                    .replaceAll("%from_shortName%", previousDivision.getShortName())
                    .replaceAll("%to_fullName%", currentDivision.getFullName())
                    .replaceAll("%to_shortName%", currentDivision.getShortName()));
        } else if (previousDivision == null && nextDivision != null) {
            this.actionBar.setMessage(LanguageManager.getString("QUEUES.UNRANKED.SEARCHING-IN-RANGE")
                    .replaceAll("%from_fullName%", currentDivision.getFullName())
                    .replaceAll("%from_shortName%", currentDivision.getShortName())
                    .replaceAll("%to_fullName%", nextDivision.getFullName())
                    .replaceAll("%to_shortName%", nextDivision.getShortName()));
        } else if (previousDivision != null) {
            this.actionBar.setMessage(LanguageManager.getString("QUEUES.UNRANKED.SEARCHING-IN-RANGE")
                    .replaceAll("%from_fullName%", previousDivision.getFullName())
                    .replaceAll("%from_shortName%", previousDivision.getShortName())
                    .replaceAll("%to_fullName%", nextDivision.getFullName())
                    .replaceAll("%to_shortName%", nextDivision.getShortName()));
        }
    }

}
