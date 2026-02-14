package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class Sumo extends NormalLadder implements LadderHandle {

    public Sumo(String name, LadderType type) {
        super(name, type);
        this.startMove = false;
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof PlayerMoveEvent) {
            onPlayerMove((PlayerMoveEvent) e, match);
            return true;
        } else if (e instanceof EntityDamageEvent) {
            onPlayerDamage((EntityDamageEvent) e, match);
            return true;
        }
        return false;
    }

    private static void onPlayerMove(final @NotNull PlayerMoveEvent e, final @NotNull Match match) {
        Player player = e.getPlayer();

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        // Check both the block at player's location and the block below (feet)
        org.bukkit.Location playerLoc = player.getLocation();
        Material blockAtPlayer = playerLoc.getBlock().getType();
        Material blockBelow = playerLoc.clone().subtract(0, 1, 0).getBlock().getType();

        // Check if player is in/touching water
        if (blockAtPlayer.equals(Material.WATER) || blockAtPlayer.equals(ClassImport.getClasses().getItemMaterialUtil().getWater()) ||
            blockBelow.equals(Material.WATER) || blockBelow.equals(ClassImport.getClasses().getItemMaterialUtil().getWater())) {
            match.killPlayer(player, null, DeathCause.SUMO.getMessage());
        }
        // Check if player is in/touching lava
        else if (blockAtPlayer.equals(Material.LAVA) || blockAtPlayer.equals(ClassImport.getClasses().getItemMaterialUtil().getLava()) ||
                 blockBelow.equals(Material.LAVA) || blockBelow.equals(ClassImport.getClasses().getItemMaterialUtil().getLava())) {
            match.killPlayer(player, null, DeathCause.SUMO.getMessage());
        }
    }

    private static void onPlayerDamage(final @NotNull EntityDamageEvent e, final @NotNull Match match) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.setCancelled(true);
            return;
        }

        // Don't interfere with void damage - let it kill the player
        if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }

        // For all other damage types, nullify damage and keep player at full health
        e.setDamage(0);
        player.setHealth(20);
    }

}
