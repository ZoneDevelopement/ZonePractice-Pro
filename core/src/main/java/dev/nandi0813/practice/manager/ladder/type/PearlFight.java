package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempBuild;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PearlFight extends NormalLadder implements CustomConfig, LadderHandle, TempBuild {

    @Setter
    protected int buildDelay;
    protected static final String TEMPBUILD_DELAY_PATH = "tempbuild-delay";

    public PearlFight(String name, LadderType type) {
        super(name, type);
        this.setMultiRoundStartCountdown(false);
    }

    @Override
    public void setCustomConfig(YamlConfiguration config) {
        config.set(TEMPBUILD_DELAY_PATH, buildDelay);
    }

    @Override
    public void getCustomConfig(YamlConfiguration config) {
        if (config.isInt(TEMPBUILD_DELAY_PATH)) {
            buildDelay = config.getInt(TEMPBUILD_DELAY_PATH);
            if (this.buildDelay < 3 || this.buildDelay > 30)
                this.buildDelay = 6;
        } else
            this.buildDelay = 6;
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof PlayerBucketEmptyEvent) {
            TempBuild.onBucketEmpty((PlayerBucketEmptyEvent) e, match, buildDelay);
            return true;
        } else if (e instanceof BlockBreakEvent) {
            TempBuild.onBlockBreak((BlockBreakEvent) e, match);
            return true;
        } else if (e instanceof BlockPlaceEvent) {
            TempBuild.onBlockPlace((BlockPlaceEvent) e, match, buildDelay);
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
