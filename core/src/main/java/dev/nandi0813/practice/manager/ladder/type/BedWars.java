package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.ChangedBlock;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.BedFight;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public class BedWars extends BedFight implements LadderHandle {

    public BedWars(String name, LadderType type) {
        super(name, type);
    }

    @Override
    public String getRespawnLanguagePath() {
        return "BED-WARS";
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
            onBedDestroy((BlockBreakEvent) e, match);
            return true;
        } else if (e instanceof BlockPlaceEvent) {
            onBlockPlace((BlockPlaceEvent) e, match);
            return true;
        }
        return false;
    }

    private static void onBlockPlace(final @NotNull BlockPlaceEvent e, final @NotNull Match match) {
        Block block = e.getBlockPlaced();
        if (block.getType().equals(Material.TNT)) {
            LadderUtil.placeTnt(e, match);
        } else {
            BlockUtil.setMetadata(block, PLACED_IN_FIGHT, match);
            match.addBlockChange(new ChangedBlock(e));

            Block underBlock = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
            if (ArenaUtil.turnsToDirt(underBlock))
                match.getFightChange().addArenaBlockChange(new ChangedBlock(underBlock));
        }
    }

    private static void onPlayerDamage(final @NotNull EntityDamageEvent e, final Match match) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) e.setCancelled(true);
        if (match.getCurrentStat(player).isSet()) return;

        EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause.equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelled(true);
        }
    }

}
