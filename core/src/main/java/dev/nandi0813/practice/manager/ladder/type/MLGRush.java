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
import dev.nandi0813.practice.manager.fight.util.BedUtil;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.ChangedBlock;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.BlockReturnDelay;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.BedFight;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.server.sound.SoundManager;
import dev.nandi0813.practice.manager.server.sound.SoundType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public class MLGRush extends BedFight implements LadderHandle, CustomConfig, BlockReturnDelay {

    private static final int PLACE_BLOCK_LIMIT = 64;
    private static final String BLOCK_RETURN_DELAY_SECONDS_PATH = "block-return-delay-seconds";
    private static final int DEFAULT_BLOCK_RETURN_DELAY_SECONDS = 3;
    private static final int MIN_BLOCK_RETURN_DELAY_SECONDS = 0;
    private static final int MAX_BLOCK_RETURN_DELAY_SECONDS = 30;

    private int blockReturnDelaySeconds;

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
    public void setCustomConfig(YamlConfiguration config) {
        config.set(BLOCK_RETURN_DELAY_SECONDS_PATH, blockReturnDelaySeconds);
    }

    @Override
    public void getCustomConfig(YamlConfiguration config) {
        if (config.isInt(BLOCK_RETURN_DELAY_SECONDS_PATH)) {
            int delay = config.getInt(BLOCK_RETURN_DELAY_SECONDS_PATH);
            if (delay < MIN_BLOCK_RETURN_DELAY_SECONDS || delay > MAX_BLOCK_RETURN_DELAY_SECONDS) {
                delay = DEFAULT_BLOCK_RETURN_DELAY_SECONDS;
            }
            this.blockReturnDelaySeconds = delay;
            return;
        }

        this.blockReturnDelaySeconds = DEFAULT_BLOCK_RETURN_DELAY_SECONDS;
    }

    @Override
    public int getBlockReturnDelaySeconds() {
        return blockReturnDelaySeconds;
    }

    @Override
    public void setBlockReturnDelaySeconds(int delaySeconds) {
        if (delaySeconds < MIN_BLOCK_RETURN_DELAY_SECONDS) {
            delaySeconds = MIN_BLOCK_RETURN_DELAY_SECONDS;
        } else if (delaySeconds > MAX_BLOCK_RETURN_DELAY_SECONDS) {
            delaySeconds = MAX_BLOCK_RETURN_DELAY_SECONDS;
        }

        this.blockReturnDelaySeconds = delaySeconds;
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

        BlockUtil.setMetadata(block, PLACED_IN_FIGHT, match);
        match.addBlockChange(new ChangedBlock(e));

        Block underBlock = block.getLocation().subtract(0, 1, 0).getBlock();
        if (ArenaUtil.turnsToDirt(underBlock)) {
            match.getFightChange().addArenaBlockChange(new ChangedBlock(underBlock));
        }

        MLGRush mlgRush = (MLGRush) match.getLadder();
        long delayTicks = mlgRush.getBlockReturnDelaySeconds() * 20L;
        scheduleMaterialReturn(e, match, block.getType(), delayTicks, e.getHand());
    }

    private static void scheduleMaterialReturn(
            final @NotNull BlockPlaceEvent e,
            final @NotNull Match match,
            final @NotNull Material material,
            final long delayTicks,
            final EquipmentSlot hand
    ) {
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

                if (countMaterial(player, material) >= PLACE_BLOCK_LIMIT) {
                    return;
                }

                giveReturnedMaterial(player, material, hand);
                player.updateInventory();
            }, delayTicks);
        }, 2L);
    }

    private static void giveReturnedMaterial(@NotNull Player player, @NotNull Material material, EquipmentSlot hand) {
        PlayerInventory inventory = player.getInventory();
        ItemStack toReturn = new ItemStack(material, 1);

        if (hand == EquipmentSlot.OFF_HAND) {
            ItemStack offHand = inventory.getItemInOffHand();
            if (offHand.getType().isAir()) {
                inventory.setItemInOffHand(toReturn);
                return;
            }

            if (offHand.isSimilar(toReturn) && offHand.getAmount() < offHand.getMaxStackSize()) {
                offHand.setAmount(offHand.getAmount() + 1);
                inventory.setItemInOffHand(offHand);
                return;
            }
        }

        Map<Integer, ItemStack> overflow = inventory.addItem(toReturn);
        if (!overflow.isEmpty()) {
            overflow.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
    }

    private static int countMaterial(@NotNull Player player, @NotNull Material material) {
        int amount = 0;
        for (ItemStack itemStack : player.getInventory().getStorageContents()) {
            if (itemStack != null && itemStack.getType().equals(material)) {
                amount += itemStack.getAmount();
            }
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType().equals(material)) {
            amount += offHand.getAmount();
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

        match.teleportPlayer(scorer);
        playersVsPlayersRound.setRoundWinner(scorerTeam);
        round.endRound();
    }

}


