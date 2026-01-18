package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.RespawnableLadder;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
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

        // Use the ladder's language path method
        this.languagePath = resolveLanguagePath(match.getLadder());

        this.begin();
    }

    /**
     * Resolves the language path for respawn messages from the ladder.
     * Uses the RespawnableLadder interface.
     */
    private String resolveLanguagePath(Ladder ladder) {
        String matchTypePath = "MATCH." + match.getType().getPathName() + ".LADDER-SPECIFIC.";

        if (ladder instanceof RespawnableLadder respawnableLadder) {
            String basePath = respawnableLadder.getRespawnLanguagePath();
            if (basePath != null && !basePath.equals("MATCH.RESPAWN")) {
                return matchTypePath + basePath;
            }
        }
        return null;
    }

    public void begin() {
        if (round.getTempKill(player) != null) return;
        if (running) return;

        /*
         * Battle rush remove blocks so the players don't get them unnecessarily
         */
        for (var entry : match.getFightChange().getBlocks().values()) {
            if (entry.getTempData() != null && entry.getTempData().getPlayer().equals(player)) {
                entry.getTempData().setReturnItem(false);
            }
        }

        round.getTempDead().add(this);
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);

        running = true;
        this.runTaskTimer(ZonePractice.getInstance(), 0, 20L);
    }

    public void cancel(boolean setPlayer) {
        if (!running) return;

        Bukkit.getScheduler().cancelTask(this.getTaskId());
        running = false;

        round.getTempDead().remove(this);

        if (!setPlayer) return;
        if (!match.getPlayers().contains(player)) return;

        if (languagePath != null && respawnTime > 0)
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
