package dev.nandi0813.practice.manager.inventory;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.manager.inventory.inventories.StaffInventory;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.staffitems.CheckInventoryInvItem;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.manager.server.WorldEnum;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private static final String LOBBY_PROTECTION_PATH = "PLAYER.LOBBY-PROTECTION.";

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
        String itemDisplayName = Common.getItemDisplayName(item);
        if (ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY) && !player.hasPermission("zpp.admin")) {
            invItem = inventory.getHoldItem(itemDisplayName, item.getType(), e.getPlayer().getInventory().getHeldItemSlot());
        } else {
            int slot = -1;
            if (profile.getStatus().equals(ProfileStatus.QUEUE)) {
                slot = e.getPlayer().getInventory().getHeldItemSlot();
            }

            invItem = inventory.getHoldItem(itemDisplayName, item.getType(), slot);
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

        if (!(e.getRightClicked() instanceof Player target)) return;

        // Staff mode: check inventory item
        if (profile.isStaffMode()) {
            Inventory inventory = InventoryManager.getInstance().getPlayerInventory(player);
            if (inventory == null) return;

            if (inventory instanceof StaffInventory) {
                ItemStack itemInHand = PlayerUtil.getPlayerMainHand(player);
                InvItem invItem = inventory.getInvItem(Common.getItemDisplayName(itemInHand), itemInHand.getType());

                if (invItem instanceof CheckInventoryInvItem checkInventoryInvItem) {
                    checkInventoryInvItem.handleClickEvent(player, target);
                }
            }
        }
    }

    // Sword-to-duel: when in lobby, hitting a player with the unranked sword sends a duel request
    // Only triggers when the player is holding the unranked item
    @EventHandler
    public void onPlayerAttackEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        if (!(e.getEntity() instanceof Player target)) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (ConfigManager.getBoolean("MATCH-SETTINGS.DUEL.SWORD-TO-DUEL")
                && profile.getStatus().equals(ProfileStatus.LOBBY)
                && !profile.isParty()
                && player.hasPermission("zpp.duel")) {

            Inventory inventory = InventoryManager.getInstance().getPlayerInventory(player);
            if (inventory == null) return;
            ItemStack itemInHand = PlayerUtil.getPlayerMainHand(player);
            if (itemInHand.getType() == Material.AIR || !itemInHand.hasItemMeta()) return;

            InvItem heldInvItem = inventory.getInvItem(Common.getItemDisplayName(itemInHand), itemInHand.getType());
            if (!(heldInvItem instanceof dev.nandi0813.practice.manager.inventory.inventoryitem.lobbyitems.UnrankedInvItem))
                return;

            e.setCancelled(true);
            player.performCommand("duel " + target.getName());
        }
    }

    /*
     * Disable player from interacting with the inventory
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        if (isLobbyStatus(profileStatus)) {
            if (!isLobbyProtectionAllowed("allow-block-break") && !player.hasPermission("zpp.admin")) {
                e.setCancelled(true);
            }
            return;
        }

        switch (profileStatus) {
            case QUEUE:
            case STAFF_MODE:
            case CUSTOM_EDITOR:
            case EDITOR:
                e.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        if (isLobbyStatus(profileStatus)) {
            if (!isLobbyProtectionAllowed("allow-block-place") && !player.hasPermission("zpp.admin")) {
                e.setCancelled(true);
            }
            return;
        }

        switch (profileStatus) {
            case QUEUE:
            case STAFF_MODE:
            case CUSTOM_EDITOR:
            case EDITOR:
                e.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        if (isLobbyStatus(profileStatus)) {
            if (!isLobbyProtectionAllowed("allow-inventory-interact") && !player.hasPermission("zpp.admin")) {
                e.setCancelled(true);
            }
            return;
        }

        switch (profileStatus) {
            case QUEUE:
            case STAFF_MODE:
                e.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        switch (profileStatus) {
            case LOBBY:
                if (!isLobbyProtectionAllowed("allow-item-drop")) {
                    e.setCancelled(!player.hasPermission("zpp.admin"));
                }
                break;
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
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        if (ServerManager.getInstance().getInWorld().containsKey(player) && ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.OTHER)) {
            return;
        }

        if (isLobbyStatus(profileStatus)) {
            if (!isLobbyProtectionAllowed("allow-item-pickup") && !player.hasPermission("zpp.admin")) {
                e.setCancelled(true);
            }
            return;
        }

        switch (profileStatus) {
            case QUEUE:
            case STAFF_MODE:
            case CUSTOM_EDITOR:
            case EDITOR:
                e.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);
        ProfileStatus profileStatus = profile.getStatus();

        if (isLobbyStatus(profileStatus)) {
            if (!isLobbyProtectionAllowed("allow-hunger")) {
                e.setCancelled(true);
                e.setFoodLevel(20);
            }
            return;
        }

        switch (profileStatus) {
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

        if (isLobbyStatus(profileStatus)) {
            if (!isLobbyProtectionAllowed("allow-damage")) {
                // Keep knockback from entity hits while preventing HP loss.
                if (isLobbyProtectionAllowed("allow-velocity") && e instanceof EntityDamageByEntityEvent) {
                    e.setDamage(0.0D);
                    return;
                }

                e.setCancelled(true);
            }
            return;
        }

        switch (profileStatus) {
            case QUEUE:
            case STAFF_MODE:
            case CUSTOM_EDITOR:
            case EDITOR:
                e.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onItemSwitchHand(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (player.isOp() || player.hasPermission("*")) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile == null) return;

        switch (profile.getStatus()) {
            case LOBBY, QUEUE, EDITOR, SPECTATE -> e.setCancelled(true);
        }
    }

    private boolean isLobbyStatus(ProfileStatus profileStatus) {
        return profileStatus == ProfileStatus.LOBBY;
    }

    private boolean isLobbyProtectionAllowed(String setting) {
        return ConfigManager.getConfig().getBoolean(LOBBY_PROTECTION_PATH + setting, false);
    }

}
