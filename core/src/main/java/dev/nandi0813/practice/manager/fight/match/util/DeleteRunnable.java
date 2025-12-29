package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import org.bukkit.Bukkit;

public enum DeleteRunnable {
    ;

    public static void start(Match match) {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                        MatchManager.getInstance().getMatches().remove(match.getId()),
                20L * ConfigManager.getInt("MATCH-SETTINGS.MATCH-STATISTIC.REMOVE-AFTER"));
    }

}
