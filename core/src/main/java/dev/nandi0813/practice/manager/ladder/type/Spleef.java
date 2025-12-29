package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class Spleef extends NormalLadder implements LadderHandle {

    public Spleef(String name, LadderType type) {
        super(name, type);
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof BlockBreakEvent) {
            onSpleefBlockBreak((BlockBreakEvent) e, match);
            return true;
        } else if (e instanceof PlayerMoveEvent) {
            whenFall((PlayerMoveEvent) e, match);
            return true;
        } else if (e instanceof EntityDamageEvent) {
            onPlayerDamage((EntityDamageEvent) e);
        }
        return false;
    }

    private static void onSpleefBlockBreak(final @NotNull BlockBreakEvent e, final @NotNull Match match) {
        Player player = e.getPlayer();

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;
        if (match.getCurrentStat(player).isSet()) return;

        Block snow = e.getBlock();
        if (snow == null) return;
        if (!match.getArena().getCuboid().contains(snow)) return;

        e.setCancelled(true);
        if (snow.getType().equals(Material.SNOW_BLOCK)) {
            match.addBlockChange(ClassImport.createChangeBlock(e.getBlock()));
            snow.setType(Material.AIR);
            // player.getInventory().addItem(new ItemStack(ClassImport.getClasses().getItemMaterialUtil().getSnowball()));
        }
    }

    private static void whenFall(final @NotNull PlayerMoveEvent e, final @NotNull Match match) {
        Player player = e.getPlayer();

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        Material block = player.getLocation().getBlock().getType();
        if (block.equals(Material.WATER) || block.equals(ClassImport.getClasses().getItemMaterialUtil().getWater())) {
            match.killPlayer(player, null, DeathCause.SPLEEF.getMessage());
        } else if (block.equals(Material.LAVA) || block.equals(ClassImport.getClasses().getItemMaterialUtil().getLava())) {
            match.killPlayer(player, null, DeathCause.SPLEEF.getMessage());
        }
    }

    private static void onPlayerDamage(final @NotNull EntityDamageEvent e) {
        if (e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
            e.setDamage(0);
        else
            e.setCancelled(true);
    }

}
