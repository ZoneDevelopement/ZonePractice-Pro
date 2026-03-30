package dev.nandi0813.practice.manager.inventory;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.BackendManager;
import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.inventory.inventories.*;
import dev.nandi0813.practice.manager.inventory.inventories.spectate.SpecEventInventory;
import dev.nandi0813.practice.manager.inventory.inventories.spectate.SpecFfaInventory;
import dev.nandi0813.practice.manager.inventory.inventories.spectate.SpecMatchInventory;
import dev.nandi0813.practice.manager.inventory.inventories.spectate.SpecModeLobbyInventory;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsData;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.ItemSerializationUtil;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

@Getter
public class InventoryManager extends ConfigFile {

    private static InventoryManager instance;

    public static InventoryManager getInstance() {
        if (instance == null)
            instance = new InventoryManager();
        return instance;
    }

    public static final boolean SPECTATOR_MODE_ENABLED = ConfigManager.getBoolean("SPECTATOR-SETTINGS.SPECTATOR-MODE");
    public static final boolean SPECTATOR_MENU_ENABLED = ConfigManager.getBoolean("SPECTATOR-SETTINGS.SPECTATOR-MENU");
    public static final double STAFF_SPECTATOR_SPEED = ConfigManager.getDouble("STAFF-MODE.STAFF-SPECTATOR-SPEED");

    private final Map<Inventory.InventoryType, Inventory> inventories = new HashMap<>();
    private final List<Player> setupModePlayers = new ArrayList<>();
    private final NamespacedKey lobbyCosmeticItemKey = new NamespacedKey(ZonePractice.getInstance(), "zpp-cosmetic-item");
    private final NamespacedKey lobbyCosmeticTypeKey = new NamespacedKey(ZonePractice.getInstance(), "zpp-cosmetic-type");
    private final Map<UUID, ItemStack> storedOffhandItems = new HashMap<>();

    private InventoryManager() {
        super("", "inventories");

        reloadFile();

        Bukkit.getPluginManager().registerEvents(new InventoryListener(), ZonePractice.getInstance());
    }

    public void loadInventories() {
        this.inventories.put(Inventory.InventoryType.LOBBY, new LobbyInventory());
        this.inventories.put(Inventory.InventoryType.PARTY, new PartyInventory());
        this.inventories.put(Inventory.InventoryType.MATCH_QUEUE, new MatchQueueInventory());
        this.inventories.put(Inventory.InventoryType.EVENT_QUEUE, new EventQueueInventory());
        this.inventories.put(Inventory.InventoryType.STAFF_MODE, new StaffInventory());
        this.inventories.put(Inventory.InventoryType.SPECTATE_EVENT, new SpecEventInventory());
        this.inventories.put(Inventory.InventoryType.SPECTATE_FFA, new SpecFfaInventory());
        this.inventories.put(Inventory.InventoryType.SPECTATE_MATCH, new SpecMatchInventory());
        this.inventories.put(Inventory.InventoryType.SPEC_MODE_LOBBY, new SpecModeLobbyInventory());

        this.getData();
    }

    public Inventory getPlayerInventory(Player player) {
        for (Inventory inventory : this.inventories.values())
            if (inventory.getPlayers().contains(player))
                return inventory;
        return null;
    }

    public void setInventory(Player player, Inventory.InventoryType inventoryType) {
        if (inventoryType == null) {
            Inventory playerInv = this.getPlayerInventory(player);
            if (playerInv != null) {
                playerInv.getPlayers().remove(player);
            }
            dev.nandi0813.practice.manager.fight.util.PlayerUtil.clearInventory(player);
            return;
        }

        Inventory inventory = this.inventories.get(inventoryType);
        if (inventory != null) {
            inventory.setInventory(player);
        }
    }

    public void setLobbyInventory(Player player, boolean teleport) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        profile.setStatus(ProfileStatus.LOBBY);
        dev.nandi0813.practice.manager.fight.util.PlayerUtil.resetAttackSpeed(player);

        PlayerUtil.clearPlayer(
                player,
                false,
                profile.isFlying(),
                true);

        if (ZonePractice.getInstance().isEnabled()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () ->
                    InventoryUtil.setLobbyNametag(player, profile));
        } else {
            InventoryUtil.setLobbyNametag(player, profile);
        }

        if (teleport) {
            player.closeInventory();
        }

        if (profile.isStaffMode()) {
            this.setStaffModeInventory(player);
        } else if (profile.isSpectatorMode()) {
            this.setInventory(player, Inventory.InventoryType.SPEC_MODE_LOBBY);
        } else if (profile.isParty()) {
            this.setInventory(player, Inventory.InventoryType.PARTY);
        } else {
            this.setInventory(player, Inventory.InventoryType.LOBBY);
        }

        if (teleport && ServerManager.getLobby() != null)
            player.teleport(ServerManager.getLobby());

        player.updateInventory();
        applyLobbyCosmetics(player);
    }

    public void setMatchQueueInventory(Player player) {
        setMatchQueueInventory(player, true);
    }

    public void setMatchQueueInventory(Player player, boolean closeInventory) {
        ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.QUEUE);

        if (closeInventory) {
            player.closeInventory();
        }

        this.setInventory(player, Inventory.InventoryType.MATCH_QUEUE);
    }

    public void applyLobbyCosmetics(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null || profile.getCosmeticsData() == null) {
            clearLobbyCosmeticItems(player);
            return;
        }

        if (!isLobbyCosmeticsState(profile)) {
            clearLobbyCosmeticItems(player);
            return;
        }

        CosmeticsData.LobbyItemType type = profile.getCosmeticsData().getLobbyItemType();
        if (type == null || type == CosmeticsData.LobbyItemType.NONE || !CosmeticsPermissionManager.hasLobbyItemPermission(player, type)) {
            clearLobbyCosmeticItems(player);
            return;
        }

        clearLobbyCosmeticItems(player);

        if (type == CosmeticsData.LobbyItemType.WIND_CHARGE) {
            player.getInventory().setChestplate(createLobbyElytra());
            ItemStack windChargeItem = createLobbyMovementItem(type);
            player.getInventory().setItemInOffHand(windChargeItem);
        } else {
            ItemStack movementItem = createLobbyMovementItem(type);
            setMovementItemDynamic(player, movementItem);
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

    public void restoreStoredItem(Player player) {
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

    public boolean isLobbyCosmeticsState(Profile profile) {
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
        return meta.getPersistentDataContainer().has(lobbyCosmeticItemKey, PersistentDataType.BYTE);
    }

    public CosmeticsData.LobbyItemType getLobbyCosmeticType(ItemStack itemStack) {
        if (!isLobbyCosmeticItem(itemStack)) {
            return CosmeticsData.LobbyItemType.NONE;
        }

        ItemMeta meta = itemStack.getItemMeta();
        String typeName = meta.getPersistentDataContainer().get(lobbyCosmeticTypeKey, PersistentDataType.STRING);
        if (typeName == null || typeName.isBlank()) {
            return CosmeticsData.LobbyItemType.NONE;
        }

        try {
            return CosmeticsData.LobbyItemType.valueOf(typeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return CosmeticsData.LobbyItemType.NONE;
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
        meta.getPersistentDataContainer().set(lobbyCosmeticItemKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(lobbyCosmeticTypeKey, PersistentDataType.STRING, type.name());
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

    public void setEventQueueInventory(Player player) {
        player.closeInventory();

        this.setInventory(player, Inventory.InventoryType.EVENT_QUEUE);
    }

    public void setStaffModeInventory(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        profile.setStatus(ProfileStatus.STAFF_MODE);
        profile.setStaffMode(true);

        PlayerUtil.clearPlayer(player, false, player.hasPermission("zpp.staffmode.fly"), false);

        this.setInventory(player, Inventory.InventoryType.STAFF_MODE);
    }

    @Override
    public void setData() {
        for (Inventory inventory : this.inventories.values()) {
            if (inventory.getInvArmor().isNull()) {
                BackendManager.getConfig().set("INV-ARMORS." + inventory.getType().name().toUpperCase(), null);
            } else {
                BackendManager.getConfig().set(
                        "INV-ARMORS." + inventory.getType().name().toUpperCase(),
                        ItemSerializationUtil.itemStackArrayToBase64(inventory.getInvArmor().getArmorContent())
                );
            }
        }

        BackendManager.save();
    }

    @Override
    public void getData() {
        if (!BackendManager.getConfig().isConfigurationSection("INV-ARMORS")) return;

        for (String key : Objects.requireNonNull(BackendManager.getConfig().getConfigurationSection("INV-ARMORS")).getKeys(false)) {
            Inventory inventory = this.inventories.get(Inventory.InventoryType.valueOf(key));
            if (inventory == null) continue;

            inventory.getInvArmor().setArmorContent(
                    Objects.requireNonNull(ItemSerializationUtil.itemStackArrayFromBase64(BackendManager.getConfig().getString("INV-ARMORS." + key)))
            );
        }
    }

}