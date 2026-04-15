package dev.nandi0813.practice.manager.inventory.service;

import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsData;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class InventoryCosmeticService {

    private final Map<UUID, ItemStack> storedOffhandItems = new HashMap<>();

    public void applyLobbyCosmetics(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Profile profile = dev.nandi0813.practice.manager.profile.ProfileManager.getInstance().getProfile(player);
        if (profile == null || profile.getCosmeticsData() == null) {
            clearLobbyCosmeticItems(player);
            return;
        }

        if (shouldRenderLobbyCosmetics(profile)) {
            CosmeticsData.LobbyItemType type = profile.getCosmeticsData().getLobbyItemType();
            if (type == null || type == CosmeticsData.LobbyItemType.NONE || !CosmeticsPermissionManager.hasLobbyItemPermission(player, type)) {
                clearLobbyCosmeticItems(player);
                return;
            }

            clearLobbyCosmeticItems(player);

            if (type == CosmeticsData.LobbyItemType.WIND_CHARGE) {
                player.getInventory().setChestplate(createLobbyElytra());
                player.getInventory().setItemInOffHand(createLobbyMovementItem(type));
            } else {
                setMovementItemDynamic(player, createLobbyMovementItem(type));
            }
            return;
        }

        clearLobbyCosmeticItems(player);
    }

    public boolean shouldRenderLobbyCosmetics(Profile profile) {
        if (profile == null) {
            return false;
        }

        ProfileStatus status = profile.getStatus();
        return status == ProfileStatus.LOBBY || status == ProfileStatus.QUEUE;
    }

    public boolean isLobbyCosmeticItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir() || !itemStack.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = itemStack.getItemMeta();
        return meta.getPersistentDataContainer().has(InventoryManager.getInstance().getLobbyCosmeticItemKey(), PersistentDataType.BYTE);
    }

    public CosmeticsData.LobbyItemType getLobbyCosmeticType(ItemStack itemStack) {
        if (!isLobbyCosmeticItem(itemStack)) {
            return CosmeticsData.LobbyItemType.NONE;
        }

        ItemMeta meta = itemStack.getItemMeta();
        String typeName = meta.getPersistentDataContainer().get(InventoryManager.getInstance().getLobbyCosmeticTypeKey(), PersistentDataType.STRING);
        if (typeName == null || typeName.isBlank()) {
            return CosmeticsData.LobbyItemType.NONE;
        }

        try {
            return CosmeticsData.LobbyItemType.valueOf(typeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return CosmeticsData.LobbyItemType.NONE;
        }
    }

    private void setMovementItemDynamic(Player player, ItemStack movementItem) {
        if (movementItem == null || movementItem.getType().isAir()) {
            return;
        }

        PlayerInventory inv = player.getInventory();
        int firstEmpty = inv.firstEmpty();

        if (firstEmpty != -1) {
            inv.setItem(firstEmpty, movementItem);
            return;
        }

        int middleSlot = 4;
        ItemStack itemInMiddle = inv.getItem(middleSlot);

        ItemStack currentOffHand = inv.getItemInOffHand();
        if (!currentOffHand.getType().isAir()) {
            storedOffhandItems.put(player.getUniqueId(), currentOffHand);
        }

        inv.setItemInOffHand(itemInMiddle);
        inv.setItem(middleSlot, movementItem);
    }

    private void clearLobbyCosmeticItems(Player player) {
        PlayerInventory inventory = player.getInventory();

        if (isLobbyCosmeticItem(inventory.getChestplate())) {
            inventory.setChestplate(null);
        }

        if (isLobbyCosmeticItem(inventory.getItemInOffHand())) {
            inventory.setItemInOffHand(null);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack content = inventory.getItem(i);
            if (isLobbyCosmeticItem(content)) {
                inventory.setItem(i, null);
            }
        }

        restoreStoredItem(player);
    }

    private void restoreStoredItem(Player player) {
        if (player == null) {
            return;
        }

        ItemStack stored = storedOffhandItems.remove(player.getUniqueId());
        if (stored == null || stored.getType().isAir()) {
            return;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack offHand = inv.getItemInOffHand();

        if (offHand.getType().isAir() || isLobbyCosmeticItem(offHand)) {
            inv.setItemInOffHand(stored);
            return;
        }

        int firstEmpty = inv.firstEmpty();
        if (firstEmpty != -1) {
            inv.setItem(firstEmpty, stored);
        }
    }

    private ItemStack createLobbyElytra() {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        if (meta != null) {
            meta.displayName(Common.legacyToComponent("&bLobby Elytra"));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
            meta.setUnbreakable(true);
            tagAsLobbyCosmetic(meta, CosmeticsData.LobbyItemType.NONE);
            elytra.setItemMeta(meta);
        }

        return elytra;
    }

    private ItemStack createLobbyMovementItem(CosmeticsData.LobbyItemType type) {
        ItemStack itemStack = switch (type) {
            case WIND_CHARGE -> new ItemStack(Material.WIND_CHARGE, 64);
            case TRIDENT -> new ItemStack(Material.TRIDENT, 1);
            case SPEAR -> new ItemStack(Material.NETHERITE_SPEAR, 1);
            case NONE -> new ItemStack(Material.AIR);
        };

        if (type.equals(CosmeticsData.LobbyItemType.NONE)) {
            return itemStack;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            String displayName = switch (type) {
                case WIND_CHARGE -> "&bWind Charge";
                case TRIDENT -> "&3Riptide Trident";
                case SPEAR -> "&5Lunge Spear";
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };

            meta.displayName(Common.legacyToComponent(displayName));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);

            if (type == CosmeticsData.LobbyItemType.TRIDENT) {
                meta.addEnchant(Enchantment.RIPTIDE, 3, true);
            } else if (type == CosmeticsData.LobbyItemType.SPEAR) {
                meta.addEnchant(Enchantment.LUNGE, 6, true);
            }

            tagAsLobbyCosmetic(meta, type);
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private void tagAsLobbyCosmetic(ItemMeta meta, CosmeticsData.LobbyItemType type) {
        meta.getPersistentDataContainer().set(InventoryManager.getInstance().getLobbyCosmeticItemKey(), PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(InventoryManager.getInstance().getLobbyCosmeticTypeKey(), PersistentDataType.STRING, type.name());
    }
}

