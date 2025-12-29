package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public enum MatchPlayerUtil {
    ;

    public static void hidePlayerPartyGames(Player hider, List<Player> matchPlayers) {
        for (Player matchPlayer : matchPlayers) {
            if (!matchPlayer.equals(hider))
                PlayerHider.getInstance().hidePlayer(matchPlayer, hider, false);
        }

        PlayerUtil.setFightPlayer(hider);

        ClassImport.getClasses().getPlayerUtil().setCollidesWithEntities(hider, false);
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
        {
            hider.setAllowFlight(true);
            hider.setFlying(true);
            hider.setFireTicks(0);
        }, 2L);
    }

}
