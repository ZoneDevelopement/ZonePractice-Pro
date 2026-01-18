package dev.nandi0813.practice.manager.ladder.abstraction.normal;

import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.util.TempKillPlayer;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.RespawnableLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.server.sound.SoundManager;
import dev.nandi0813.practice.manager.server.sound.SoundType;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Cuboid;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public abstract class BedFight extends NormalLadder implements RespawnableLadder {

    @Setter
    @Getter
    protected int respawnTime;

    protected BedFight(String name, LadderType type) {
        super(name, type);
        this.startMove = false;
    }

    @Override
    public DeathResult handlePlayerDeath(Player player, Match match, Round round) {
        // Check if the player's bed is still intact
        if (match instanceof Team teamMatch) {
            TeamEnum playerTeam = teamMatch.getTeam(player);
            boolean bedIntact = round.getBedStatus().getOrDefault(playerTeam, false);
            if (bedIntact) {
                return DeathResult.TEMPORARY_DEATH;
            }
        }
        // Bed destroyed - player is eliminated
        return DeathResult.ELIMINATED;
    }

    protected static void onItemDrop(final @NotNull PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    protected static void onPlayerMove(final @NotNull PlayerMoveEvent e, final @NotNull Match match) {
        RoundStatus roundStatus = match.getCurrentRound().getRoundStatus();

        if (roundStatus.equals(RoundStatus.LIVE)) {
            Player player = e.getPlayer();
            BasicArena arena = match.getArena();
            Cuboid cuboid = arena.getCuboid();

            TempKillPlayer tempKillPlayer = match.getCurrentRound().getTempKill(player);
            if (tempKillPlayer != null && !cuboid.contains(e.getTo())) {
                player.teleport(cuboid.getCenter());
                return;
            }

            int deadZone = cuboid.getLowerY();
            if (arena.isDeadZone())
                deadZone = arena.getDeadZoneValue();

            if (e.getTo().getBlockY() <= deadZone && tempKillPlayer == null) {
                match.killPlayer(player, null, DeathCause.VOID.getMessage());
            }
        }
    }

    protected static void onBedDestroy(final @NotNull BlockBreakEvent e, final @NotNull Match match) {
        if (ClassImport.getClasses().getBedUtil().onBedBreak(e, match)) {
            SoundManager.getInstance().getSound(SoundType.BED_BREAK).play(match.getPlayers());
        }
    }

}
