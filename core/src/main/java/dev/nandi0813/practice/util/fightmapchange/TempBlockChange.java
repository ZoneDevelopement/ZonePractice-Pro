package dev.nandi0813.practice.util.fightmapchange;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.module.interfaces.ChangedBlock;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class TempBlockChange {

    private final FightChange fightChange;
    private final ChangedBlock changedBlock;

    private final Location location;
    @Getter
    @Setter
    private boolean removed = false;

    @Getter
    private final Player player;
    @Getter
    @Setter
    private boolean returnItem = true;

    public TempBlockChange(final FightChange fightChange, final ChangedBlock changedBlock, final Player setBlockPlayer, final int destroyTime) {
        this.fightChange = fightChange;
        this.changedBlock = changedBlock;

        this.location = changedBlock.getLocation();
        this.player = setBlockPlayer;

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), this::reset, destroyTime * 20L);
    }

    public void reset() {
        if (returnItem) {
            retrieveItemTempBuild(player, location.getBlock().getDrops());

            this.returnItem = false;
        }

        if (!removed) {
            this.changedBlock.reset();
            this.fightChange.getBlockChange().remove(location, changedBlock);

            this.removed = true;
        }

        this.fightChange.getTempBuildPlacedBlocks().remove(location);
    }

    private static void retrieveItemTempBuild(final Player player, final Collection<ItemStack> itemStacks) {
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;
        if (match.getCurrentStat(player).isSet()) return;

        for (ItemStack item : itemStacks)
            player.getInventory().addItem(item);
    }

}
