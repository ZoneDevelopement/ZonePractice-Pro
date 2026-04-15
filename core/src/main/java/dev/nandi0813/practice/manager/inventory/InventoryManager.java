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
import dev.nandi0813.practice.manager.inventory.service.InventoryCosmeticService;
import dev.nandi0813.practice.manager.inventory.service.InventoryTransitionService;
import dev.nandi0813.practice.util.ItemSerializationUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

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
    private final InventoryCosmeticService cosmeticService = new InventoryCosmeticService();
    private final InventoryTransitionService transitionService = new InventoryTransitionService(this);

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
        transitionService.setLobbyInventory(player, teleport);
    }

    public void setMatchQueueInventory(Player player) {
        setMatchQueueInventory(player, true);
    }

    public void setMatchQueueInventory(Player player, boolean closeInventory) {
        transitionService.setMatchQueueInventory(player, closeInventory);
    }


    public void applyLobbyCosmetics(Player player) {
        cosmeticService.applyLobbyCosmetics(player);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldRenderLobbyCosmetics(dev.nandi0813.practice.manager.profile.Profile profile) {
        return cosmeticService.shouldRenderLobbyCosmetics(profile);
    }

    public boolean isLobbyCosmeticItem(org.bukkit.inventory.ItemStack itemStack) {
        return cosmeticService.isLobbyCosmeticItem(itemStack);
    }

    public dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsData.LobbyItemType getLobbyCosmeticType(org.bukkit.inventory.ItemStack itemStack) {
        return cosmeticService.getLobbyCosmeticType(itemStack);
    }

    public void setEventQueueInventory(Player player) {
        transitionService.setEventQueueInventory(player);
    }

    public void setStaffModeInventory(Player player) {
        transitionService.setStaffModeInventory(player);
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