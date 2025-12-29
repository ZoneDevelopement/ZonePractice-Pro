package dev.nandi0813.practice.manager.inventory;

import dev.nandi0813.practice.manager.inventory.inventories.StaffInventory;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.staffitems.CheckInventoryInvItem;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.manager.server.WorldEnum;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    @EventHandler
    public void onPlayerInteractWithInvItem(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        switch (profile.getStatus()) {
            case EVENT:
            case MATCH:
            case FFA:
            case CUSTOM_EDITOR:
            case EDITOR:
                return;
        }

        Inventory inventory = InventoryManager.getInstance().getPlayerInventory(player);
        if (inventory == null) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType().equals(Material.AIR)) return;

        Action action = e.getAction();
        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        if (ServerManager.getLobby() == null) {
            Common.sendConsoleMMMessage("<red>Please set the lobby (/prac lobby set) and restart the server before doing anything.");
            return;
        }

        InvItem invItem;
        if (ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY) && !player.hasPermission("zpp.admin")) {
            invItem = inventory.getHoldItem(item.getItemMeta().getDisplayName(), item.getType(), e.getPlayer().getInventory().getHeldItemSlot());
        } else {
            int slot = -1;
            if (profile.getStatus().equals(ProfileStatus.QUEUE)) {
                slot = e.getPlayer().getInventory().getHeldItemSlot();
            }

            invItem = inventory.getHoldItem(item.getItemMeta().getDisplayName(), item.getType(), slot);
        }

        if (invItem != null) {
            e.setCancelled(true);
            invItem.handleClickEvent(player);
        }
    }

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        switch (profile.getStatus()) {
            case EVENT:
            case MATCH:
            case FFA:
            case CUSTOM_EDITOR:
            case EDITOR:
                return;
        }

        if (!profile.isStaffMode()) return;
        if (!(e.getRightClicked() instanceof Player)) return;

        Inventory inventory = InventoryManager.getInstance().getPlayerInventory(player);
        if (inventory == null) return;

        if (inventory instanceof StaffInventory) {
            ItemStack itemInHand = ClassImport.getClasses().getPlayerUtil().getPlayerMainHand(player);
            InvItem invItem = inventory.getInvItem(itemInHand.getItemMeta().getDisplayName(), itemInHand.getType());

            if (invItem instanceof CheckInventoryInvItem checkInventoryInvItem) {
                checkInventoryInvItem.handleClickEvent(player, (Player) e.getRightClicked());
            }
        }
    }

    /*
     * Disable player from interacting with the inventory
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        if (ServerManager.getInstance().getInWorld().containsKey(player) && ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY)) {
            e.setCancelled(!player.hasPermission("zpp.admin"));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        if (ServerManager.getInstance().getInWorld().containsKey(player) && ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY)) {
            e.setCancelled(!player.hasPermission("zpp.admin"));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        if (!player.hasPermission("zpp.admin") && profileStatus.equals(ProfileStatus.LOBBY))
            e.setCancelled(true);
        else {
            switch (profileStatus) {
                case QUEUE:
                case STAFF_MODE:
                    e.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        switch (profileStatus) {
            case LOBBY:
            case QUEUE:
            case STAFF_MODE:
                e.setCancelled(!player.hasPermission("zpp.admin"));
                break;
            case CUSTOM_EDITOR:
            case EDITOR:
                e.getItemDrop().remove();
                break;
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        if (ServerManager.getInstance().getInWorld().containsKey(player) && ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.OTHER)) {
            return;
        }

        if (!player.hasPermission("zpp.admin") && profileStatus.equals(ProfileStatus.LOBBY))
            e.setCancelled(true);
        else {
            switch (profileStatus) {
                case QUEUE:
                case STAFF_MODE:
                case CUSTOM_EDITOR:
                case EDITOR:
                    e.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        switch (profileStatus) {
            case LOBBY:
            case QUEUE:
            case STAFF_MODE:
            case CUSTOM_EDITOR:
            case EDITOR:
                e.setCancelled(true);
                e.setFoodLevel(20);
                break;
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;
        ProfileStatus profileStatus = profile.getStatus();

        switch (profileStatus) {
            case LOBBY:
            case QUEUE:
            case STAFF_MODE:
            case CUSTOM_EDITOR:
            case EDITOR:
                e.setCancelled(true);
                break;
        }
    }

}
