package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.BlockReturnDelay;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempBuild;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.jetbrains.annotations.NotNull;

public class PearlFight extends NormalLadder implements LadderHandle, TempBuild, BlockReturnDelay {

    private static final int MIN_BUILD_DELAY_SECONDS = -1;
    private static final int MAX_BUILD_DELAY_SECONDS = 30;

    // Saved by using interface and LadderFile.java
    @Getter
    protected int blockReturnDelaySeconds;

    public PearlFight(String name, LadderType type) {
        super(name, type);
        this.setMultiRoundStartCountdown(false);
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof PlayerBucketEmptyEvent) {
            TempBuild.onBucketEmpty((PlayerBucketEmptyEvent) e, match, blockReturnDelaySeconds);
            return true;
        } else if (e instanceof BlockBreakEvent) {
            TempBuild.onBlockBreak((BlockBreakEvent) e, match);
            return true;
        } else if (e instanceof BlockPlaceEvent) {
            TempBuild.onBlockPlace((BlockPlaceEvent) e, match, blockReturnDelaySeconds);
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

    @Override
    public String getContextTargetForBlockReturn() {
        return "Placed Blocks";
    }

    @Override
    public void setBlockReturnDelaySeconds(int blockReturnDelaySeconds) {
        this.blockReturnDelaySeconds = Math.max(MIN_BUILD_DELAY_SECONDS,
                Math.min(MAX_BUILD_DELAY_SECONDS, blockReturnDelaySeconds));
    }
}
