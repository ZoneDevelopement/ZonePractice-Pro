package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
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

    public DestroyableBlocksGui(final NormalLadder ladder) {
        super(GUIType.Ladder_DestroyableBlock);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.LADDER.DESTROYABLE-BLOCKS.TITLE").replace("%ladder%", ladder.getName()), 4));

        this.ladder = ladder;
        this.basicItems = ladder.getDestroyableBlocks();

        this.build();
    }

    @Override
    public void build() {
        for (int i = 28; i <= 35; i++)
            gui.get(1).setItem(i, GUIManager.getFILLER_ITEM());

        gui.get(1).setItem(27, GUIFile.getGuiItem("GUIS.SETUP.LADDER.DESTROYABLE-BLOCKS.ICONS.BACK-TO").get());

        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);

            for (int i = 0; i <= 26; i++) {
                inventory.setItem(i, null);
            }

            for (BasicItem block : this.basicItems) {
                int slot = inventory.firstEmpty();
                if (slot != -1) {
                    ItemStack itemStack = new ItemStack(block.getMaterial(), 1, block.getDamage());
                    inventory.setItem(slot, itemStack);
                }
            }
        });

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot >= 27 && slot <= 35) {
            if (slot == 27)
                LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).open(player);

            e.setCancelled(true);
            return;
        } else {
            if (ladder.isEnabled()) {
                e.setCancelled(true);
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-EDIT-ENABLED"));
                return;
            }
        }

        checkArmor(e);
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

        for (int i = 0; i <= 26; i++) {
            ItemStack itemStack = this.gui.get(1).getItem(i);
            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                BasicItem block = new BasicItem(itemStack.getType(), itemStack.getDurability());
                basicItems.add(block);
            }
        }
    }

    private void checkArmor(InventoryClickEvent e) {
        final Player player = (Player) e.getWhoClicked();
        final Inventory inventory = e.getView().getTopInventory();
        final ClickType click = e.getClick();
        final int slot = e.getRawSlot();

        ItemStack item = null;
        if (inventory.getSize() > slot && click.equals(ClickType.NUMBER_KEY)) {
            item = player.getInventory().getItem(e.getHotbarButton());
        } else if (inventory.getSize() > slot && (click.equals(ClickType.RIGHT) || click.equals(ClickType.LEFT))) {
            item = player.getItemOnCursor();
        } else if (inventory.getSize() <= slot && click.equals(ClickType.SHIFT_LEFT) || click.equals(ClickType.SHIFT_RIGHT)) {
            item = e.getCurrentItem();
        }

        if (item != null) {
            if (!item.getType().isBlock()) {
                Common.sendMMMessage((Player) e.getWhoClicked(), LanguageManager.getString("COMMAND.SETUP.LADDER.ONLY-PUT-BLOCKS"));
                e.setCancelled(true);
            } else {
                Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                {
                    for (int i = 0; i <= 26; i++) {
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

                            updatePlayers();
                        }
                    }
                }, 2L);
            }
        }
    }

}
