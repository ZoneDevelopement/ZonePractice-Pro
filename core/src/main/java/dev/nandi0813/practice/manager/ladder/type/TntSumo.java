package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.ChangedBlock;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.BlockReturnDelay;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;
import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public class TntSumo extends NormalLadder implements LadderHandle, BlockReturnDelay {

    private static final int TNT_LIMIT = 10;

    private static final String TNT_SUMO_TNT = "ZONEPRACTICE_PRO_TNT_SUMO_TNT";
    private static final String TNT_SUMO_BLOCK_OWNER = "ZONEPRACTICE_PRO_TNT_SUMO_BLOCK_OWNER";
    private static final String TNT_SUMO_BLOCK_MATERIAL = "ZONEPRACTICE_PRO_TNT_SUMO_BLOCK_MATERIAL";
    private static final String TNT_SUMO_BLOCK_ITEM = "ZONEPRACTICE_PRO_TNT_SUMO_BLOCK_ITEM";

    @Getter
    @Setter
    // Saved by using interface and LadderFile.java
    private int blockReturnDelaySeconds;

    public TntSumo(String name, LadderType type) {
        super(name, type);
        this.startMove = false;
        this.hunger = false;
    }

    @Override
    public String getContextTargetForBlockReturn() {
        return "TNT";
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof BlockPlaceEvent blockPlaceEvent) {
            onBlockPlace(blockPlaceEvent, match);
            return true;
        }

        if (e instanceof org.bukkit.event.block.BlockBreakEvent blockBreakEvent) {
            onBlockBreak(blockBreakEvent, match);
            return true;
        }

        if (e instanceof EntityDamageEvent entityDamageEvent) {
            onPlayerDamage(entityDamageEvent, match);
            return true;
        }

        if (e instanceof EntityExplodeEvent entityExplodeEvent) {
            onEntityExplode(entityExplodeEvent, match);
            return true;
        }

        if (e instanceof org.bukkit.event.block.BlockExplodeEvent blockExplodeEvent) {
            onBlockExplode(blockExplodeEvent, match);
            return true;
        }

        if (e instanceof PlayerMoveEvent playerMoveEvent) {
            onPlayerMove(playerMoveEvent, match);
            return true;
        }

        if (e instanceof org.bukkit.event.player.PlayerDropItemEvent playerDropItemEvent) {
            onItemDrop(playerDropItemEvent);
            return true;
        }

        return false;
    }

    private static void onBlockPlace(@NotNull BlockPlaceEvent e, @NotNull Match match) {
        Block block = e.getBlockPlaced();
        Player player = e.getPlayer();

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            return;
        }

        Material blockMaterial = block.getType();
        ItemStack returnItem = createPlacedReturnItem(e, blockMaterial);
        BlockUtil.setMetadata(block, PLACED_IN_FIGHT, match);
        BlockUtil.setMetadata(block, TNT_SUMO_BLOCK_OWNER, player.getUniqueId());
        BlockUtil.setMetadata(block, TNT_SUMO_BLOCK_MATERIAL, blockMaterial);
        BlockUtil.setMetadata(block, TNT_SUMO_BLOCK_ITEM, returnItem);
        match.addBlockChange(new ChangedBlock(e));

        Block underBlock = block.getLocation().subtract(0, 1, 0).getBlock();
        if (ArenaUtil.turnsToDirt(underBlock)) {
            match.getFightChange().addArenaBlockChange(new ChangedBlock(underBlock));
        }

        if (!blockMaterial.equals(Material.TNT)) {
            return;
        }

        // Only schedule return for TNT blocks
        if (((TntSumo) match.getLadder()).getBlockReturnDelaySeconds() >= 0) {
            scheduleBlockReturn(e, match, player, returnItem);
        }

        // Spawn TNT entity for TNT blocks
        Location tntLocation = block.getLocation().clone();
        block.setType(Material.AIR, false);

        TNTPrimed tntPrimed = block.getWorld().spawn(tntLocation.clone().add(0.5, 0.0, 0.5), TNTPrimed.class);
        tntPrimed.setFuseTicks(0);
        tntPrimed.setIsIncendiary(false);

        BlockUtil.setMetadata(tntPrimed, FIGHT_ENTITY, match);
        BlockUtil.setMetadata(tntPrimed, TNT_SUMO_TNT, match);
    }

    private static void onBlockBreak(@NotNull org.bukkit.event.block.BlockBreakEvent e, @NotNull Match match) {
        Block block = e.getBlock();

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            return;
        }

        if (!BlockUtil.hasMetadata(block, TNT_SUMO_BLOCK_OWNER)) {
            return;
        }

        e.setDropItems(false);

        UUID ownerId = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_OWNER, UUID.class);
        if (ownerId == null) {
            return;
        }

        Player owner = Bukkit.getPlayer(ownerId);
        if (owner == null || !owner.isOnline()) {
            return;
        }

        if (MatchManager.getInstance().getLiveMatchByPlayer(owner) != match) {
            return;
        }

        Material material = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_MATERIAL, Material.class);
        if (material == null) {
            material = block.getType();
        }

        if (material.isAir()) {
            return;
        }

        if (material.equals(Material.TNT) && countTnt(owner) >= TNT_LIMIT) {
            return;
        }

        ItemStack returnItem = getStoredBlockItem(block, material);
        PlayerUtil.returnItemToCurrentSlotOrInventory(owner, returnItem);
        owner.updateInventory();
    }

    private static void scheduleBlockReturn(
            @NotNull BlockPlaceEvent e,
            @NotNull Match match,
            @NotNull Player player,
            @NotNull ItemStack returnItem
    ) {
        long delayTicks = ((TntSumo) match.getLadder()).getBlockReturnDelaySeconds() * 20L;
        Material material = returnItem.getType();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (e.isCancelled()) {
                return;
            }

            if (MatchManager.getInstance().getLiveMatchByPlayer(player) != match) {
                return;
            }

            if (!match.getPlayers().contains(player) || match.getCurrentStat(player).isSet()) {
                return;
            }

            // For TNT, check the limit
            if (material.equals(Material.TNT) && countTnt(player) >= TNT_LIMIT) {
                return;
            }

            PlayerUtil.returnItemToCurrentSlotOrInventory(player, returnItem.clone());
            player.updateInventory();
        }, delayTicks);
    }

    private static void onEntityExplode(@NotNull EntityExplodeEvent e, @NotNull Match match) {
        Entity entity = e.getEntity();
        if (!BlockUtil.hasMetadata(entity, TNT_SUMO_TNT)) {
            return;
        }

        Match metadataMatch = BlockUtil.getMetadata(entity, TNT_SUMO_TNT, Match.class);
        if (metadataMatch != match) {
            e.blockList().clear();
            return;
        }

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.blockList().clear();
            return;
        }

        // Keep as secondary defense, but do manual block removal for deterministic no-drop behavior.
        e.setYield(0F);

        List<Block> destroyedBlocks = destroyPlacedBlocksWithoutDrops(e.blockList());
        e.blockList().clear();

        returnDestroyedBlocksToOwners(destroyedBlocks, match);
    }

    private static void onBlockExplode(@NotNull org.bukkit.event.block.BlockExplodeEvent e, @NotNull Match match) {
        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.blockList().clear();
            return;
        }

        // Keep as secondary defense, but do manual block removal for deterministic no-drop behavior.
        e.setYield(0F);

        List<Block> destroyedBlocks = destroyPlacedBlocksWithoutDrops(e.blockList());
        e.blockList().clear();

        returnDestroyedBlocksToOwners(destroyedBlocks, match);
    }

    private static @NotNull List<Block> destroyPlacedBlocksWithoutDrops(@NotNull List<Block> explosionBlocks) {
        List<Block> destroyedBlocks = new ArrayList<>();

        for (Block block : explosionBlocks) {
            if (!BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
                continue;
            }

            destroyedBlocks.add(block);
            block.setType(Material.AIR, false);
        }

        return destroyedBlocks;
    }

    private static void returnDestroyedBlocksToOwners(@NotNull List<Block> destroyedBlocks, @NotNull Match match) {
        for (Block block : destroyedBlocks) {
            if (!BlockUtil.hasMetadata(block, TNT_SUMO_BLOCK_OWNER)) {
                continue;
            }

            UUID ownerId = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_OWNER, UUID.class);
            if (ownerId == null) {
                continue;
            }

            Player owner = Bukkit.getPlayer(ownerId);
            if (owner == null || !owner.isOnline()) {
                continue;
            }

            if (MatchManager.getInstance().getLiveMatchByPlayer(owner) != match) {
                continue;
            }

            // Get the material from metadata, fallback to current block type
            Material material = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_MATERIAL, Material.class);
            if (material == null) {
                material = block.getType();
            }

            if (material.isAir()) {
                continue;
            }

            if (material.equals(Material.TNT) && countTnt(owner) >= TNT_LIMIT) {
                continue;
            }

            ItemStack returnItem = getStoredBlockItem(block, material);
            PlayerUtil.returnItemToCurrentSlotOrInventory(owner, returnItem);
            owner.updateInventory();
        }
    }

    private static @NotNull ItemStack createPlacedReturnItem(@NotNull BlockPlaceEvent e, @NotNull Material fallbackMaterial) {
        ItemStack placedItem = e.getItemInHand();
        if (placedItem.getType().isAir()) {
            return new ItemStack(fallbackMaterial, 1);
        }

        ItemStack clone = placedItem.clone();
        clone.setAmount(1);
        return clone;
    }

    private static @NotNull ItemStack getStoredBlockItem(@NotNull Block block, @NotNull Material fallbackMaterial) {
        ItemStack storedItem = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_ITEM, ItemStack.class);
        if (storedItem == null || storedItem.getType().isAir()) {
            return new ItemStack(fallbackMaterial, 1);
        }

        ItemStack clone = storedItem.clone();
        clone.setAmount(1);
        return clone;
    }

    private static void onPlayerDamage(@NotNull EntityDamageEvent e, @NotNull Match match) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.setCancelled(true);
            return;
        }

        if (e instanceof EntityDamageByEntityEvent entityDamageByEntityEvent
                && entityDamageByEntityEvent.getDamager() instanceof TNTPrimed tnt
                && BlockUtil.hasMetadata(tnt, TNT_SUMO_TNT)) {
            Match metadataMatch = BlockUtil.getMetadata(tnt, TNT_SUMO_TNT, Match.class);
            if (metadataMatch != match) {
                e.setCancelled(true);
                return;
            }

            PlayerUtil.applyTntSumoKnockback(player, tnt);
            e.setDamage(0);
            return;
        }

        if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            match.killPlayer(player, null, DeathCause.SUMO.getMessage());
            return;
        }

        e.setDamage(0);
        player.setHealth(20);
    }

    private static void onPlayerMove(@NotNull PlayerMoveEvent e, @NotNull Match match) {
        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            return;
        }

        Player player = e.getPlayer();
        Location playerLocation = player.getLocation();

        Material blockAtPlayer = playerLocation.getBlock().getType();
        Material blockBelow = playerLocation.clone().subtract(0, 1, 0).getBlock().getType();

        if (blockAtPlayer.equals(Material.WATER) || blockBelow.equals(Material.WATER)
                || blockAtPlayer.equals(Material.LAVA) || blockBelow.equals(Material.LAVA)) {
            match.killPlayer(player, null, DeathCause.SUMO.getMessage());
        }
    }

    private static int countTnt(@NotNull Player player) {
        int amount = 0;
        for (ItemStack itemStack : player.getInventory().getStorageContents()) {
            if (itemStack != null && itemStack.getType().equals(Material.TNT)) {
                amount += itemStack.getAmount();
            }
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType().equals(Material.TNT)) {
            amount += offHand.getAmount();
        }

        return amount;
    }

    private static void onItemDrop(@NotNull org.bukkit.event.player.PlayerDropItemEvent e) {
        // Prevent item dropping in TNT Sumo
        e.setCancelled(true);
    }

}

