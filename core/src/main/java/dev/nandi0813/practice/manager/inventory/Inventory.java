package dev.nandi0813.practice.manager.inventory;

import dev.nandi0813.practice.manager.inventory.inventoryitem.ExtraInvItem;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvArmor;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.module.util.ClassImport;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Inventory {

    @Getter
    public enum InventoryType {
        LOBBY("LOBBY-BASIC"),
        MATCH_QUEUE("QUEUE.MATCH"),
        EVENT_QUEUE("QUEUE.EVENT"),
        PARTY("PARTY"),
        SPEC_MODE_LOBBY("SPECTATOR.LOBBY"),
        SPECTATE_MATCH("SPECTATOR.MATCH"),
        SPECTATE_EVENT("SPECTATOR.EVENT"),
        SPECTATE_FFA("SPECTATOR.FFA"),
        STAFF_MODE("STAFF-MODE");

        private final String path;

        InventoryType(final String path) {
            this.path = path;
        }
    }

    protected final InventoryType type;
    protected final List<Player> players;
    protected final List<InvItem> invItems; // Contains extra as well
    protected final InvArmor invArmor = new InvArmor();

    protected Inventory(InventoryType type) {
        this.type = type;
        this.players = new ArrayList<>();
        this.invItems = new ArrayList<>();

        this.getExtraItems();
    }

    protected abstract void set(Player player);

    public void setInventory(Player player) {
        Inventory currentInventory = InventoryManager.getInstance().getPlayerInventory(player);
        if (currentInventory != null)
            currentInventory.getPlayers().remove(player);

        ClassImport.getClasses().getPlayerUtil().clearInventory(player);

        players.add(player);

        this.set(player);
        this.setArmor(player);

        player.updateInventory();
    }

    public void setArmor(Player player) {
        player.getInventory().setHelmet(invArmor.getHelmet());
        player.getInventory().setChestplate(invArmor.getChestplate());
        player.getInventory().setLeggings(invArmor.getLeggings());
        player.getInventory().setBoots(invArmor.getBoots());
    }

    public InvItem getHoldItem(String name, Material material, int slot) {
        InvItem invItem = this.getInvItem(name, material);

        if (invItem == null && slot != -1) {
            invItem = this.getInvItem(slot, material);
        }

        return invItem;
    }

    private void getExtraItems() {
        YamlConfiguration config = InventoryManager.getInstance().getConfig();

        if (config.isConfigurationSection(type.getPath() + ".EXTRA")) {
            for (String itemPath : config.getConfigurationSection(type.getPath() + ".EXTRA").getKeys(false)) {
                ExtraInvItem extraInvItem = new ExtraInvItem(type.getPath() + ".EXTRA." + itemPath);

                invItems.add(extraInvItem);
            }
        }
    }

    protected InvItem getInvItem(final int slot, final Material material) {
        return invItems.stream().filter(invItem ->
                invItem.getSlot() == slot &&
                        invItem.getItem().getType().equals(material) &&
                        invItem.getSlot() != -1
        ).findFirst().orElse(null);
    }

    protected InvItem getInvItem(final String name, final Material material) {
        return invItems.stream().filter(invItem ->
                invItem.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(name) &&
                        invItem.getItem().getType().equals(material) &&
                        invItem.getSlot() != -1
        ).findFirst().orElse(null);
    }

    protected InvItem getInvItem(Class<?> c) {
        return invItems.stream().filter(invItem ->
                invItem.getClass().equals(c) &&
                        invItem.getSlot() != -1
        ).findFirst().orElse(null);
    }

}
