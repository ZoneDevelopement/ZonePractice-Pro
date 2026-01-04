package dev.nandi0813.practice.manager.gui.setup.server;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.util.ArmorUtil;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

import static dev.nandi0813.practice.manager.inventory.Inventory.InventoryType;

public class ServerArmorLobbyGui extends GUI {

    private final GUI backTo;

    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.SETUP.SERVER.LOBBY-ARMORS.ICONS.BACK-TO").get();
    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.SERVER.LOBBY-ARMORS.ICONS.FILLER-ITEM").get();
    private static final ItemStack FILLER_ITEM2 = GUIFile.getGuiItem("GUIS.SETUP.SERVER.LOBBY-ARMORS.ICONS.FILLER-ITEM2").get();
    private static final GUIItem NAME_ITEM = GUIFile.getGuiItem("GUIS.SETUP.SERVER.LOBBY-ARMORS.ICONS.NAME-ITEM");

    private static final List<Integer> helmetSlots = Arrays.asList(9, 11, 13, 15, 17);
    private static final List<Integer> chestplateSlots = Arrays.asList(18, 20, 22, 24, 26);
    private static final List<Integer> leggingsSlots = Arrays.asList(27, 29, 31, 33, 35);
    private static final List<Integer> bootsSlots = Arrays.asList(36, 38, 40, 42, 44);

    public ServerArmorLobbyGui(GUI backTo) {
        super(GUIType.Server_LobbyArmor);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.SERVER.LOBBY-ARMORS.TITLE"), 6));
        this.backTo = backTo;

        this.build();
    }

    @Override
    public void build() {
        org.bukkit.inventory.Inventory inventory = this.gui.get(1);

        // Filler item
        for (int x : new int[]{1, 3, 5, 7})
            for (int y = 0; y < 6; y++)
                inventory.setItem(x + (y * 9), FILLER_ITEM);

        // Back to item
        inventory.setItem(45, BACK_TO_ITEM);

        this.update();
    }

    @Override
    public void update() {
        org.bukkit.inventory.Inventory inventory = this.gui.get(1);

        for (ArmorStatusType type : ArmorStatusType.values()) {
            Inventory inv = InventoryManager.getInstance().getInventories().get(type.getInventoryType());
            int column = type.getColumn();

            for (int c = 0; c < 6; c++) {
                int slot = column + (c * 9);
                switch (c) {
                    case 0:
                        inventory.setItem(slot, NAME_ITEM.cloneItem().replace("%inventoryName%", type.getName()).get());
                        break;
                    case 1:
                        inventory.setItem(slot, inv.getInvArmor().getHelmet());
                        break;
                    case 2:
                        inventory.setItem(slot, inv.getInvArmor().getChestplate());
                        break;
                    case 3:
                        inventory.setItem(slot, inv.getInvArmor().getLeggings());
                        break;
                    case 4:
                        inventory.setItem(slot, inv.getInvArmor().getBoots());
                        break;
                    case 5:
                        if (column != 0)
                            inventory.setItem(slot, FILLER_ITEM2);
                        break;
                }
            }
        }

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ClickType clickType = e.getClick();
        int slot = e.getRawSlot();
        org.bukkit.inventory.Inventory clickedInv = e.getClickedInventory();

        if (slot == 45) {
            e.setCancelled(true);
            backTo.open(player);
        } else {
            if (!helmetSlots.contains(slot) && !chestplateSlots.contains(slot) && !leggingsSlots.contains(slot) && !bootsSlots.contains(slot)) {
                if (clickedInv == null || clickedInv.getSize() <= slot) return;

                e.setCancelled(true);
                return;
            }

            if (clickType.equals(ClickType.LEFT) || clickType.equals(ClickType.RIGHT)) {
                if (!player.getItemOnCursor().getType().equals(Material.AIR)) {
                    if (helmetSlots.contains(slot) && !ArmorUtil.isHelmet(player.getItemOnCursor()))
                        e.setCancelled(true);
                    if (chestplateSlots.contains(slot) && !ArmorUtil.isChestplate(player.getItemOnCursor()))
                        e.setCancelled(true);
                    if (leggingsSlots.contains(slot) && !ArmorUtil.isLeggings(player.getItemOnCursor()))
                        e.setCancelled(true);
                    if (bootsSlots.contains(slot) && !ArmorUtil.isBoots(player.getItemOnCursor())) e.setCancelled(true);
                }
            } else
                e.setCancelled(true);
        }
    }

    public void handleCloseEvent(InventoryCloseEvent e) {
        this.save();
    }

    @Override
    public void handleDragEvent(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    public void save() {
        org.bukkit.inventory.Inventory inventory = this.gui.get(1);

        for (ArmorStatusType type : ArmorStatusType.values()) {
            Inventory inv = InventoryManager.getInstance().getInventories().get(type.getInventoryType());
            int column = type.getColumn();

            for (int c = 0; c < 6; c++) {
                int slot = column + (c * 9);
                switch (c) {
                    case 1:
                        inv.getInvArmor().setHelmet(inventory.getItem(slot));
                        break;
                    case 2:
                        inv.getInvArmor().setChestplate(inventory.getItem(slot));
                        break;
                    case 3:
                        inv.getInvArmor().setLeggings(inventory.getItem(slot));
                        break;
                    case 4:
                        inv.getInvArmor().setBoots(inventory.getItem(slot));
                        break;
                }
            }
        }
    }

    @Getter
    private enum ArmorStatusType {
        LOBBY("Lobby", 0, InventoryType.LOBBY),
        QUEUE("Queue", 2, InventoryType.MATCH_QUEUE),
        PARTY("Party", 4, InventoryType.PARTY),
        SPECTATE_MODE("Spectate Mode", 6, InventoryType.SPEC_MODE_LOBBY),
        STAFF_MODE("Staff Mode", 8, InventoryType.STAFF_MODE);

        private final String name;
        private final int column;
        private final Inventory.InventoryType inventoryType;

        ArmorStatusType(final String name, final int column, final Inventory.InventoryType inventoryType) {
            this.name = name;
            this.column = column;
            this.inventoryType = inventoryType;
        }
    }

}
