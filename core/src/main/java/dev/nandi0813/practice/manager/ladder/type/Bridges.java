package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.RespawnableLadder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.PortalFight;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Bridges extends PortalFight implements LadderHandle, RespawnableLadder {

    @Getter
    @Setter
    private int respawnTime;

    public Bridges(String name, LadderType type) {
        super(name, type);
    }

    @Override
    public DeathResult handlePlayerDeath(Player player, Match match, Round round) {
        // Bridges always respawns players - they only get eliminated by portal score
        return DeathResult.TEMPORARY_DEATH;
    }

    @Override
    public String getRespawnLanguagePath() {
        return "BRIDGES";
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof BlockBreakEvent) {
            onBlockBreak((BlockBreakEvent) e, match);
            return true;
        } else if (e instanceof BlockPlaceEvent) {
            onBlockPlace((BlockPlaceEvent) e, match);
            return true;
        } else if (e instanceof PlayerBucketEmptyEvent) {
            onBucketEmpty((PlayerBucketEmptyEvent) e, match);
            return true;
        } else if (e instanceof BlockFromToEvent) {
            onLiquidFlow((BlockFromToEvent) e);
            return true;
        } else if (e instanceof PlayerMoveEvent) {
            onPlayerMove((PlayerMoveEvent) e, match);
            return true;
        } else if (e instanceof PlayerItemConsumeEvent) {
            onConsume((PlayerItemConsumeEvent) e);
            return true;
        } else if (e instanceof EntityDamageEvent) {
            onDamage((EntityDamageEvent) e);
            return true;
        }
        return false;
    }

    private static void onDamage(final @NotNull EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelled(true);
        }
    }

    private static void onConsume(final @NotNull PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item != null && item.getType().equals(Material.GOLDEN_APPLE)) {
            player.setHealth(player.getHealthScale());
        }
    }

}
