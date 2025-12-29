package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteract implements Listener {

    @EventHandler
    public void onSoup(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        Action action = e.getAction();
        ItemStack item = e.getItem();

        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
                if (profile.getStatus().equals(ProfileStatus.MATCH) || profile.getStatus().equals(ProfileStatus.EVENT)) {
                    // Soup listener
                    if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {
                        if (item != null && item.getType().equals(ClassImport.getClasses().getItemMaterialUtil().getMushroomSoup())) {
                            int food = player.getFoodLevel();
                            double health = player.getHealth();
                            double maxHealth = player.getMaxHealth();
                            double regen = 6.5;

                            if (food < 20) e.setCancelled(true);

                            if (health == maxHealth) return;

                            if ((health + regen) < maxHealth) {
                                player.getInventory().getItemInHand().setType(Material.BOWL);
                                player.setHealth(health + regen);
                            } else if ((health + regen) >= maxHealth) {
                                player.getInventory().getItemInHand().setType(Material.BOWL);
                                player.setHealth(maxHealth);
                            }
                            player.updateInventory();
                        }
                    }
                }
                break;
        }
    }

    @EventHandler
    public void onPlayerSleep(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (player.isSneaking()) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        String blockType = block.getType().toString();
        if (blockType.contains("BED_") || blockType.contains("_BED"))
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void enderPearlTpFix(PlayerTeleportEvent e) {
        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            e.getTo().setX(Math.floor(e.getTo().getX()) + 0.5f);
            e.getTo().setY(Math.floor(e.getTo().getY()) + 0.5f);
            e.getTo().setZ(Math.floor(e.getTo().getZ()) + 0.5f);
        }
    }

}
