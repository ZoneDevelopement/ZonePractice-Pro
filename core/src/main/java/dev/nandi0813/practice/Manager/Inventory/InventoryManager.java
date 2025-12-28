package dev.nandi0813.practice.Manager.Inventory;

import dev.nandi0813.practice.Manager.Backend.BackendManager;
import dev.nandi0813.practice.Manager.Backend.ConfigFile;
import dev.nandi0813.practice.Manager.Backend.ConfigManager;
import dev.nandi0813.practice.Manager.Inventory.Inventories.*;
import dev.nandi0813.practice.Manager.Inventory.Inventories.Spectate.SpecEventInventory;
import dev.nandi0813.practice.Manager.Inventory.Inventories.Spectate.SpecFfaInventory;
import dev.nandi0813.practice.Manager.Inventory.Inventories.Spectate.SpecMatchInventory;
import dev.nandi0813.practice.Manager.Inventory.Inventories.Spectate.SpecModeLobbyInventory;
import dev.nandi0813.practice.Manager.Profile.Enum.ProfileStatus;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileManager;
import dev.nandi0813.practice.Manager.Server.ServerManager;
import dev.nandi0813.practice.Module.Util.ClassImport;
import dev.nandi0813.practice.Util.ItemSerializationUtil;
import dev.nandi0813.practice.Util.PlayerUtil.PlayerUtil;
import dev.nandi0813.practice.ZonePractice;
import lombok.Getter;
import org.bukkit.Bukkit;
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
            ClassImport.getClasses().getPlayerUtil().clearInventory(player);
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

        PlayerUtil.clearPlayer(
                player,
                false,
                profile.isFlying(),
                true);

        InventoryUtil.setLobbyNametag(player, profile);

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
    }

    public void setMatchQueueInventory(Player player) {
        ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.QUEUE);
        player.closeInventory();

        this.setInventory(player, Inventory.InventoryType.MATCH_QUEUE);
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

        for (String key : BackendManager.getConfig().getConfigurationSection("INV-ARMORS").getKeys(false)) {
            Inventory inventory = this.inventories.get(Inventory.InventoryType.valueOf(key));
            if (inventory == null) continue;

            inventory.getInvArmor().setArmorContent(
                    Objects.requireNonNull(ItemSerializationUtil.itemStackArrayFromBase64(BackendManager.getConfig().getString("INV-ARMORS." + key)))
            );
        }
    }

}
