package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.fightmapchange.TempBlockChange;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static dev.nandi0813.practice.manager.fight.match.util.TeamUtil.replaceTeamNames;

public class TempKillPlayer extends BukkitRunnable {

    private final String languagePath;
    @Getter
    private boolean running = false;

    private final Match match;
    private final Round round;

    @Getter
    private final Player player;
    private final TeamEnum playerTeam;
    private int respawnTime;

    public TempKillPlayer(final Round round, final Player player, final int respawnTime) {
        this.round = round;
        this.match = round.getMatch();

        this.player = player;
        this.respawnTime = respawnTime;

        if (match instanceof Team)
            playerTeam = ((Team) match).getTeam(player);
        else
            playerTeam = TeamEnum.TEAM1;

        switch (match.getLadder().getType()) {
            case BEDWARS:
                languagePath = "MATCH." + match.getType().getPathName() + ".LADDER-SPECIFIC.BED-WARS";
                break;
            case FIREBALL_FIGHT:
                languagePath = "MATCH." + match.getType().getPathName() + ".LADDER-SPECIFIC.FIREBALL-FIGHT";
                break;
            case BATTLE_RUSH:
                languagePath = "MATCH." + match.getType().getPathName() + ".LADDER-SPECIFIC.BATTLE-RUSH";
                break;
            default:
                languagePath = null;
        }

        this.begin();
    }

    public void begin() {
        if (round.getTempKill(player) != null) return;
        if (running) return;

        running = true;
        this.runTaskTimer(ZonePractice.getInstance(), 0, 20L);

        /*
         * Battle rush remove blocks so the players don't get them unnecessarily
         */
        for (TempBlockChange tempBlockChange : match.getFightChange().getTempBuildPlacedBlocks().values()) {
            if (tempBlockChange.getPlayer().equals(player)) {
                tempBlockChange.setReturnItem(false);
            }
        }

        round.getTempDead().add(this);
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void cancel(boolean setPlayer) {
        if (!running) return;

        Bukkit.getScheduler().cancelTask(this.getTaskId());
        running = false;

        round.getTempDead().remove(this);

        if (!setPlayer) return;
        if (!match.getPlayers().contains(player)) return;

        if (languagePath != null)
            match.sendMessage(replaceTeamNames(LanguageManager.getString(languagePath + ".PLAYER-RESPAWNED"), player, playerTeam), true);

        match.teleportPlayer(player);
        PlayerUtil.setFightPlayer(player);
        match.getMatchPlayers().get(player).setKitChooserOrKit(playerTeam);
    }

    @Override
    public void run() {
        if (respawnTime == 0) {
            cancel(true);
            return;
        }

        if (languagePath != null)
            Common.sendMMMessage(player, StringUtil.replaceSecondString(
                    replaceTeamNames(LanguageManager.getString(languagePath + ".RESPAWN"), player, playerTeam),
                    respawnTime));

        respawnTime--;
    }

}
