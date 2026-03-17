package dev.nandi0813.practice.manager.ladder.abstraction.normal;

import dev.nandi0813.practice.manager.arena.arenas.interfaces.NormalArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.arena.util.PortalLocation;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.PlayerWinner;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayersRound;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.ChangedBlock;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public abstract class PortalFight extends NormalLadder {

    protected PortalFight(String name, LadderType type) {
        super(name, type);
        this.startMove = false;
    }

    protected static void onPlayerMove(final @NotNull PlayerMoveEvent e, final @NotNull Match match) {
        Player player = e.getPlayer();
        Round round = match.getCurrentRound();
        RoundStatus roundStatus = match.getCurrentRound().getRoundStatus();

        if (roundStatus.equals(RoundStatus.LIVE)) {
            if (match.getCurrentRound().getTempKill(player) == null) {
                for (PortalLocation portalLocation : match.getArena().getPortalLocations()) {
                    if (portalLocation.isIn(player)) {
                        TeamEnum team = ((Team) match).getTeam(player);

                        if ((portalLocation == match.getArena().getPortalLoc1() && team == TeamEnum.TEAM1) || (portalLocation == match.getArena().getPortalLoc2() && team == TeamEnum.TEAM2)) {
                            match.killPlayer(player, null, DeathCause.PORTAL_OWN_JUMP.getMessage());
                        } else {
                            if (round instanceof PlayerWinner playerWinner) {
                                if (playerWinner.getRoundWinner() == null) {
                                    match.teleportPlayer(player);
                                    playerWinner.setRoundWinner(player);
                                    round.endRound();
                                }
                            } else if (round instanceof PlayersVsPlayersRound playersVsPlayersRound) {
                                if (playersVsPlayersRound.getRoundWinner() == null) {
                                    match.teleportPlayer(player);
                                    playersVsPlayersRound.setRoundWinner(team);
                                    round.endRound();
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    protected static void onBucketEmpty(final @NotNull PlayerBucketEmptyEvent e, final @NotNull Match match) {
        for (PortalLocation portalLocation : match.getArena().getPortalLocations()) {
            if (portalLocation.isInsidePortalProtection(e.getBlockClicked(), match.getArena().getPortalProtectionValue())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    protected static void onBlockPlace(final @NotNull BlockPlaceEvent e, final @NotNull Match match) {
        for (PortalLocation portalLocation : match.getArena().getPortalLocations()) {
            if (portalLocation.isInsidePortalProtection(e.getBlockPlaced(), match.getArena().getPortalProtectionValue())) {
                e.setCancelled(true);
                break;
            }
        }

        if (!e.isCancelled()) {
            BlockUtil.setMetadata(e.getBlockPlaced(), PLACED_IN_FIGHT, match);
            match.addBlockChange(new ChangedBlock(e));

            Block underBlock = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
            if (ArenaUtil.turnsToDirt(underBlock))
                match.getFightChange().addArenaBlockChange(new ChangedBlock(underBlock));
        }
    }

    protected static void onBlockBreak(final @NotNull BlockBreakEvent e, final @NotNull Match match) {
        for (PortalLocation portalLocation : match.getArena().getPortalLocations()) {
            if (portalLocation.isInsidePortalProtection(e.getBlock(), match.getArena().getPortalProtectionValue())) {
                e.setCancelled(true);
                break;
            }
        }

        if (!e.isCancelled()) {
            // Use addArenaBlockChange (no PLACED_IN_FIGHT metadata) so the block is tracked
            // for rollback but LadderTypeListener's breakAllBlocks gate still controls
            // whether the actual break is permitted. addBlockChange would tag the block with
            // PLACED_IN_FIGHT, causing LadderTypeListener to treat it as a player-placed block
            // and allow the break even when breakAllBlocks is disabled.
            match.getFightChange().addArenaBlockChange(new ChangedBlock(e.getBlock()));

            Block underBlock = e.getBlock().getLocation().subtract(0, 1, 0).getBlock();
            if (underBlock.getType() == Material.DIRT) {
                match.getFightChange().addArenaBlockChange(new ChangedBlock(underBlock));
            }
        }
    }

    protected static void onLiquidFlow(final @NotNull BlockFromToEvent e) {
        Block block = e.getBlock();
        if (!BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) return;

        Match match = BlockUtil.getMetadata(block, PLACED_IN_FIGHT, Match.class);
        if (match == null) return;

        NormalArena arena = match.getArena();

        for (PortalLocation portalLocation : arena.getPortalLocations()) {
            if (portalLocation.isInsidePortalProtection(block, arena.getPortalProtectionValue())) {
                e.setCancelled(true);
                break;
            }
        }
    }

}
