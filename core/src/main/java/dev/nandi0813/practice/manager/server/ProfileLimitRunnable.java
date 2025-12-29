package dev.nandi0813.practice.manager.server;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class ProfileLimitRunnable extends BukkitRunnable {

    public void begin() {
        ZonedDateTime zdt = LocalDate.now(TimeZone.getDefault().toZoneId()).atTime(LocalTime.of(23, 59, 59)).atZone(TimeZone.getDefault().toZoneId());

        long i2 = zdt.toInstant().toEpochMilli() - System.currentTimeMillis();
        long i3 = (i2 / 1000) * 20;

        this.runTaskTimerAsynchronously(ZonePractice.getInstance(), i3, 86400000L);
    }

    @Override
    public void run() {
        for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
            Group group = profile.getGroup();

            profile.setRankedLeft(group != null ? group.getRankedLimit() : 0);
            profile.setUnrankedLeft(group != null ? group.getUnrankedLimit() : 0);
            profile.setEventStartLeft(group != null ? group.getEventStartLimit() : 0);
        }

        for (Player player : Bukkit.getOnlinePlayers())
            Common.sendMMMessage(player, LanguageManager.getString("GAMES-RESET")
                    .replaceAll("%unranked%", WeightClass.UNRANKED.getMMName())
                    .replaceAll("%ranked%", WeightClass.RANKED.getMMName())
            );
    }

}
