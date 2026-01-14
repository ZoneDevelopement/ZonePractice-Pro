package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempBuild;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempDead;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.PortalFight;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class BattleRush extends PortalFight implements LadderHandle, TempBuild, TempDead {

    @Getter
    @Setter
    private int respawnTime;

    @Getter
    @Setter
    private int buildDelay;

    public BattleRush(String name, LadderType type) {
        super(name, type);
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof BlockBreakEvent) {
            onBlockBreak((BlockBreakEvent) e, match);
            TempBuild.onBlockBreak((BlockBreakEvent) e, match);
            return true;
        } else if (e instanceof BlockPlaceEvent) {
            onBlockPlace((BlockPlaceEvent) e, match);
            TempBuild.onBlockPlace((BlockPlaceEvent) e, match, buildDelay);
            return true;
        } else if (e instanceof PlayerBucketEmptyEvent) {
            onBucketEmpty((PlayerBucketEmptyEvent) e, match);
            TempBuild.onBucketEmpty((PlayerBucketEmptyEvent) e, match, buildDelay);
            return true;
        } else if (e instanceof BlockFromToEvent) {
            onLiquidFlow((BlockFromToEvent) e);
            return true;
        } else if (e instanceof PlayerMoveEvent) {
            onPlayerMove((PlayerMoveEvent) e, match);
            return true;
        } else if (e instanceof EntityDamageEvent) {
            onPlayerDamage((EntityDamageEvent) e, match);
            return true;
        }
        return false;
    }

    private static void onPlayerDamage(final @NotNull EntityDamageEvent e, final @NotNull Match match) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.setDamage(0);
            player.setHealth(20);
        }
    }

}
