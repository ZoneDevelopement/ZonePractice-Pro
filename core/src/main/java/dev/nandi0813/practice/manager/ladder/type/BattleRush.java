package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempBuild;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempDead;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.PortalFight;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class BattleRush extends PortalFight implements CustomConfig, LadderHandle, TempBuild, TempDead {

    @Getter
    @Setter
    private int respawnTime;
    protected static final String RESPAWN_TIME_PATH = "respawn-time";

    @Getter
    @Setter
    private int buildDelay;
    protected static final String TEMPBUILD_DELAY_PATH = "tempbuild-delay";

    public BattleRush(String name, LadderType type) {
        super(name, type);
    }

    @Override
    public void setCustomConfig(YamlConfiguration config) {
        config.set(RESPAWN_TIME_PATH, this.respawnTime);
        config.set(TEMPBUILD_DELAY_PATH, this.buildDelay);
    }

    @Override
    public void getCustomConfig(YamlConfiguration config) {
        if (config.isInt(RESPAWN_TIME_PATH)) {
            this.respawnTime = config.getInt(RESPAWN_TIME_PATH);
            if (this.respawnTime < 2 || this.respawnTime > 10)
                this.respawnTime = 3;
        } else
            this.respawnTime = 3;

        if (config.isInt(TEMPBUILD_DELAY_PATH)) {
            this.buildDelay = config.getInt(TEMPBUILD_DELAY_PATH);
            if (this.buildDelay < 3 || this.buildDelay > 30)
                this.buildDelay = 6;
        } else
            this.buildDelay = 6;
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
