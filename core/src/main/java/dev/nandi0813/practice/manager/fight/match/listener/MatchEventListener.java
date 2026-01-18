package dev.nandi0813.practice.manager.fight.match.listener;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.util.MatchFightPlayer;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Handles match-specific events that are NOT related to ladder settings.
 * Events here are core match mechanics (teleportation, projectile tracking, kit selection, etc.)
 * <p>
 * For setting-specific events, see CentralizedSettingListener.
 */
public class MatchEventListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;
        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        Block clickedBlock = e.getClickedBlock();
        if (action.equals(Action.RIGHT_CLICK_BLOCK) && clickedBlock != null) {
            if (clickedBlock.getType().equals(Material.CHEST) || clickedBlock.getType().equals(Material.TRAPPED_CHEST)) {
                match.addBlockChange(ClassImport.createChangeBlock(clickedBlock));
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player player)) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        match.addEntityChange(e.getEntity());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(e.getPlayer());
        if (match == null) return;

        if (!match.getArena().getCuboid().contains(e.getTo()))
            e.setCancelled(true);
    }

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        match.removePlayer(player, true);
    }

    /**
     * Kit Selection Listeners
     */
    @EventHandler
    public void onPlayerChooseKit(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        MatchFightPlayer matchFightPlayer = match.getMatchPlayers().get(player);
        if (!matchFightPlayer.isHasChosenKit()) {
            e.setCancelled(true);

            TeamEnum playerTeam;
            if (match instanceof Team) {
                playerTeam = ((Team) match).getTeam(player);
            } else {
                playerTeam = TeamEnum.TEAM1;
            }

            matchFightPlayer.setChosenKit(player.getInventory().getHeldItemSlot(), playerTeam);
        }
    }

    @EventHandler
    public void onPlayerChooseKit(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        MatchFightPlayer matchFightPlayer = match.getMatchPlayers().get(player);
        if (!matchFightPlayer.isHasChosenKit()) {
            e.setCancelled(true);
        }
    }
}
