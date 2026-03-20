package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.PlayerWinner;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayersRound;
import dev.nandi0813.practice.manager.fight.util.*;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.BedFight;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.manager.server.sound.SoundManager;
import dev.nandi0813.practice.manager.server.sound.SoundType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public class MLGRush extends BedFight implements LadderHandle {

    private static final int TNT_LIMIT = 10;
    private static final long TNT_RETURN_DELAY_TICKS = 60L;

    public MLGRush(String name, LadderType type) {
        super(name, type);
        this.respawnTime = 0;
    }

    @Override
    public String getRespawnLanguagePath() {
        return "MLG-RUSH";
    }

    @Override
    public DeathResult handlePlayerDeath(Player player, Match match, Round round) {
        return DeathResult.TEMPORARY_DEATH;
    }

    @Override
    public int getRespawnTime() {
        return 0;
    }

    @Override
    public void setRespawnTime(int respawnTime) {
        this.respawnTime = 0;
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof EntityDamageEvent) {
            onPlayerDamage((EntityDamageEvent) e, match);
            return true;
        } else if (e instanceof PlayerDropItemEvent) {
            onItemDrop((PlayerDropItemEvent) e);
            return true;
        } else if (e instanceof PlayerMoveEvent) {
            onPlayerMove((PlayerMoveEvent) e, match);
            return true;
        } else if (e instanceof BlockBreakEvent) {
            onBedDestroyPoint((BlockBreakEvent) e, match);
            return true;
        } else if (e instanceof BlockPlaceEvent) {
            onBlockPlace((BlockPlaceEvent) e, match);
            return true;
        }
        return false;
    }

    private static void onPlayerDamage(final @NotNull EntityDamageEvent e, final @NotNull Match match) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.setCancelled(true);
            return;
        }

        if (match.getCurrentStat(player).isSet()) {
            return;
        }

        if (e instanceof EntityDamageByEntityEvent damageByEntityEvent
                && damageByEntityEvent.getDamager() instanceof TNTPrimed tnt) {
            onTntDamage(damageByEntityEvent, player, tnt, match);
            return;
        }

        if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            e.setDamage(0);
            match.killPlayer(player, null, DeathCause.VOID.getMessage());
            return;
        }

        if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelled(true);
            return;
        }

        e.setDamage(0);
    }

    private static void onBlockPlace(final @NotNull BlockPlaceEvent e, final @NotNull Match match) {
        Block block = e.getBlockPlaced();

        if (block.getType().equals(Material.TNT)) {
            LadderUtil.placeTnt(e, match);
            scheduleTntReturn(e, match);
            return;
        }

        if (block.getType().toString().endsWith("_TERRACOTTA")) {
            block.setType(getTeamTerracotta(match, e.getPlayer()), false);
        }

        BlockUtil.setMetadata(block, PLACED_IN_FIGHT, match);
        match.addBlockChange(new ChangedBlock(e));

        Block underBlock = block.getLocation().subtract(0, 1, 0).getBlock();
        if (ArenaUtil.turnsToDirt(underBlock)) {
            match.getFightChange().addArenaBlockChange(new ChangedBlock(underBlock));
        }
    }

    private static void onTntDamage(
            final @NotNull EntityDamageByEntityEvent e,
            final @NotNull Player player,
            final @NotNull TNTPrimed tnt,
            final @NotNull Match match
    ) {
        Match metadataMatch = BlockUtil.getMetadata(tnt, dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY, Match.class);
        if (ListenerUtil.checkMetaData(metadataMatch) || metadataMatch != match) {
            return;
        }

        e.setCancelled(true);
        e.setDamage(0);
        PlayerUtil.applyMlgRushTntKnockback(player, tnt);
    }

    private static void scheduleTntReturn(final @NotNull BlockPlaceEvent e, final @NotNull Match match) {
        Player player = e.getPlayer();
        BukkitScheduler scheduler = ZonePractice.getInstance().getServer().getScheduler();

        scheduler.runTaskLater(ZonePractice.getInstance(), () -> {
            if (e.isCancelled()) {
                return;
            }

            scheduler.runTaskLater(ZonePractice.getInstance(), () -> {
                if (MatchManager.getInstance().getLiveMatchByPlayer(player) != match) {
                    return;
                }

                if (!match.getPlayers().contains(player) || match.getCurrentStat(player).isSet()) {
                    return;
                }

                if (countMaterial(player) >= TNT_LIMIT) {
                    return;
                }

                player.getInventory().addItem(new ItemStack(Material.TNT, 1));
                player.updateInventory();
            }, TNT_RETURN_DELAY_TICKS);
        }, 2L);
    }

    private static int countMaterial(@NotNull Player player) {
        int amount = 0;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null && itemStack.getType().equals(Material.TNT)) {
                amount += itemStack.getAmount();
            }
        }
        return amount;
    }

    private static void onBedDestroyPoint(final @NotNull BlockBreakEvent e, final @NotNull Match match) {
        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            return;
        }

        if (!BedUtil.onBedBreak(e, match)) {
            return;
        }

        SoundManager.getInstance().getSound(SoundType.BED_BREAK).play(match.getPlayers());

        Round round = match.getCurrentRound();
        Player scorer = e.getPlayer();

        if (round instanceof PlayerWinner playerWinner) {
            if (playerWinner.getRoundWinner() == null) {
                match.teleportPlayer(scorer);
                playerWinner.setRoundWinner(scorer);
                round.endRound();
            }
            return;
        }

        if (!(match instanceof Team teamMatch) || !(round instanceof PlayersVsPlayersRound playersVsPlayersRound)) {
            return;
        }

        if (playersVsPlayersRound.getRoundWinner() != null) {
            return;
        }

        TeamEnum scorerTeam = teamMatch.getTeam(scorer);
        if (ListenerUtil.checkMetaData(match)) {
            return;
        }

        match.teleportPlayer(scorer);
        playersVsPlayersRound.setRoundWinner(scorerTeam);
        round.endRound();
    }

    private static Material getTeamTerracotta(@NotNull Match match, @NotNull Player player) {
        if (!(match instanceof Team teamMatch)) {
            return Material.WHITE_TERRACOTTA;
        }

        return teamMatch.getTeam(player).equals(TeamEnum.TEAM1)
                ? Material.RED_TERRACOTTA
                : Material.BLUE_TERRACOTTA;
    }
}


