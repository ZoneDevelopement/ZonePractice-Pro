package dev.nandi0813.practice.manager.inventory.service;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.inventory.InventoryUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class InventoryTransitionService {

    private final InventoryManager inventoryManager;

    public InventoryTransitionService(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    public void setLobbyInventory(Player player, boolean teleport) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        profile.setStatus(ProfileStatus.LOBBY);
        dev.nandi0813.practice.manager.fight.util.PlayerUtil.resetAttackSpeed(player);

        PlayerUtil.clearPlayer(player, false, profile.isFlying(), true);

        if (ZonePractice.getInstance().isEnabled()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> InventoryUtil.setLobbyNametag(player, profile));
        } else {
            InventoryUtil.setLobbyNametag(player, profile);
        }

        if (teleport) {
            player.closeInventory();
        }

        if (profile.isStaffMode()) {
            inventoryManager.setStaffModeInventory(player);
        } else if (profile.isSpectatorMode()) {
            inventoryManager.setInventory(player, Inventory.InventoryType.SPEC_MODE_LOBBY);
        } else if (profile.isParty()) {
            inventoryManager.setInventory(player, Inventory.InventoryType.PARTY);
        } else {
            inventoryManager.setInventory(player, Inventory.InventoryType.LOBBY);
        }

        if (teleport && ServerManager.getLobby() != null) {
            player.teleport(ServerManager.getLobby());
        }

        player.updateInventory();
        inventoryManager.applyLobbyCosmetics(player);
    }

    public void setMatchQueueInventory(Player player, boolean closeInventory) {
        ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.QUEUE);

        if (closeInventory) {
            player.closeInventory();
        }

        inventoryManager.setInventory(player, Inventory.InventoryType.MATCH_QUEUE);
    }

    public void setEventQueueInventory(Player player) {
        player.closeInventory();
        inventoryManager.setInventory(player, Inventory.InventoryType.EVENT_QUEUE);
    }

    public void setStaffModeInventory(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        profile.setStatus(ProfileStatus.STAFF_MODE);
        profile.setStaffMode(true);

        PlayerUtil.clearPlayer(player, false, player.hasPermission("zpp.staffmode.fly"), false);
        inventoryManager.setInventory(player, Inventory.InventoryType.STAFF_MODE);
    }
}


