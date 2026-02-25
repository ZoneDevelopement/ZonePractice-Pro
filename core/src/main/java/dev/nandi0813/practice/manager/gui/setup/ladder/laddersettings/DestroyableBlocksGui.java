package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.BasicItem;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class DestroyableBlocksGui extends GUI {

    @Getter
    private final NormalLadder ladder;
    private final List<BasicItem> basicItems;

    private static final int BACK_SLOT = 45;
    private static final int INVENTORY_LAST_SLOT = 53;

    public DestroyableBlocksGui(final NormalLadder ladder) {
        super(GUIType.Ladder_DestroyableBlock);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.LADDER.DESTROYABLE-BLOCKS.TITLE").replace("%ladder%", ladder.getName()), 6));

        this.ladder = ladder;
        this.basicItems = ladder.getDestroyableBlocks();

        this.build();
    }

    @Override
    public void build() {
        gui.get(1).setItem(BACK_SLOT, GUIFile.getGuiItem("GUIS.SETUP.LADDER.DESTROYABLE-BLOCKS.ICONS.BACK-TO").get());

        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);

            // Clear all editable slots (everything except the back button)
            for (int i = 0; i <= INVENTORY_LAST_SLOT; i++) {
                if (i != BACK_SLOT)
                    inventory.setItem(i, null);
            }

            for (BasicItem block : this.basicItems) {
                // Place into the first empty editable slot
                for (int i = 0; i <= INVENTORY_LAST_SLOT; i++) {
                    if (i == BACK_SLOT) continue;
                    if (inventory.getItem(i) == null) {
                        inventory.setItem(i, new ItemStack(block.getMaterial(), 1, block.getDamage()));
                        break;
                    }
                }
            }
        });

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        ClickType click = e.getClick();

        if (slot == BACK_SLOT) {
            LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).open(player);
            e.setCancelled(true);
            return;
        }

        // Click is in the player's own inventory (bottom half)
        if (slot > INVENTORY_LAST_SLOT) {
            // Shift-click from player inventory would push item into the GUI — validate it
            if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
                if (ladder.isEnabled()) {
                    e.setCancelled(true);
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-EDIT-ENABLED"));
                    return;
                }
                checkItem(e);
            }
            // Normal clicks in the player's own inventory are always allowed
            return;
        }

        if (ladder.isEnabled()) {
            e.setCancelled(true);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-EDIT-ENABLED"));
            return;
        }

        checkItem(e);
    }

    public void handleCloseEvent(InventoryCloseEvent e) {
        if (LadderManager.getInstance().getLadders().contains(ladder) && !ladder.isEnabled())
            this.save();
    }

    @Override
    public void handleDragEvent(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    public void save() {
        basicItems.clear();

        for (int i = 0; i <= INVENTORY_LAST_SLOT; i++) {
            if (i == BACK_SLOT) continue;
            ItemStack itemStack = this.gui.get(1).getItem(i);
            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                BasicItem block = new BasicItem(itemStack.getType(), itemStack.getDurability());
                basicItems.add(block);
            }
        }
    }

    private void checkItem(InventoryClickEvent e) {
        final Player player = (Player) e.getWhoClicked();
        final Inventory inventory = e.getView().getTopInventory();
        final ClickType click = e.getClick();
        final int slot = e.getRawSlot();

        ItemStack item = null;
        if (slot < inventory.getSize() && click == ClickType.NUMBER_KEY) {
            // Hotbar key press while hovering over GUI slot
            item = player.getInventory().getItem(e.getHotbarButton());
        } else if (slot < inventory.getSize() && (click == ClickType.RIGHT || click == ClickType.LEFT)) {
            // Placing cursor item into a GUI slot
            item = player.getItemOnCursor();
        } else if (slot >= inventory.getSize() && (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT)) {
            // Shift-clicking from player inventory into GUI
            item = e.getCurrentItem();
        }

        if (item != null && !item.getType().equals(Material.AIR)) {
            if (!item.getType().isBlock()) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.ONLY-PUT-BLOCKS"));
                e.setCancelled(true);
            } else {
                Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
                    for (int i = 0; i <= INVENTORY_LAST_SLOT; i++) {
                        if (i == BACK_SLOT) continue;
                        ItemStack itemStack = gui.get(1).getItem(i);
                        if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                            itemStack.setAmount(1);
                            if (itemStack.hasItemMeta()) {
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                itemMeta.setDisplayName(itemStack.getType().getData().getSimpleName());
                                itemMeta.setLore(null);
                                itemMeta.getItemFlags().clear();
                                itemStack.setItemMeta(itemMeta);
                            }
                        }
                    }
                    updatePlayers();
                }, 2L);
            }
        }
    }

}
