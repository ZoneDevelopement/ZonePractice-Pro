package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class PlayerInteract implements Listener {

    @EventHandler
    public void onFlowerPotManipulate(PlayerFlowerPotManipulateEvent e) {
        // Only block taking flowers out of pots; placing flowers remains unchanged.
        if (e.isPlacing()) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(e.getPlayer());
        if (profile == null) {
            return;
        }

        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
                e.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onSoup(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile == null) {
            return;
        }

        Action action = e.getAction();
        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        ItemStack item = e.getItem();
        if (item == null || !item.getType().equals(Material.MUSHROOM_STEW)) {
            return;
        }

        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
                int food = player.getFoodLevel();
                double health = player.getHealth();
                double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
                double regen = 6.5;

                if (food < 20) e.setCancelled(true);

                if (health == maxHealth) return;

                if ((health + regen) < maxHealth) {
                    consumeUsedSoup(player, e.getHand());
                    player.setHealth(health + regen);
                } else if ((health + regen) >= maxHealth) {
                    consumeUsedSoup(player, e.getHand());
                    player.setHealth(maxHealth);
                }
                player.updateInventory();
                break;
        }
    }

    private void consumeUsedSoup(Player player, EquipmentSlot hand) {
        if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            return;
        }

        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
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
}