package dev.nandi0813.practice.manager.playerkit.guis;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.playerkit.StaticItems;
import dev.nandi0813.practice.manager.playerkit.items.KitItem;
import dev.nandi0813.practice.util.InventoryUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AmountChangeGUI extends GUI {

    private final KitItem kitItem;
    private final int maxStackSize;
    private final MainGUI backTo;

    private static final Map<Integer, Integer> AMOUNTS = Map.of(19, 1, 20, 8, 21, 16, 22, 32, 23, 64);

    public AmountChangeGUI(KitItem kitItem, MainGUI backTo) {
        super(GUIType.PlayerCustom_ChangeAmount);
        this.kitItem = kitItem;
        this.maxStackSize = kitItem.getMaterial().getMaxStackSize();
        this.backTo = backTo;
        this.gui.put(1, InventoryUtil.createInventory(StaticItems.CHANGE_AMOUNT_TITLE, 6));
        this.build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);
        Material material = kitItem.get().getType();

        inventory.setItem(45, StaticItems.CHANGE_AMOUNT_BACK_ICON);
        inventory.setItem(18, StaticItems.CHANGE_CUSTOM_AMOUNT_ICON);

        for (Map.Entry<Integer, Integer> entry : AMOUNTS.entrySet()) {
            if (material.getMaxStackSize() >= entry.getValue()) {
                inventory.setItem(entry.getKey(), new ItemStack(material, entry.getValue()));
            }
        }

        update();
    }

    @Override
    public void update() {
        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        switch (slot) {
            case 45:
                backTo.open(player);
                break;
            case 18:
                new AnvilGUI.Builder()
                        .plugin(ZonePractice.getInstance())
                        .onClose(stateSnapshot ->
                                Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                                        backTo.open(player), 2L))
                        .onClick((anvilSlot, stateSnapshot) -> {
                            if (anvilSlot != AnvilGUI.Slot.OUTPUT) {
                                return Collections.emptyList();
                            }

                            try {
                                String text = stateSnapshot.getText();
                                if (text.startsWith("#"))
                                    text = text.replaceFirst("#", "");

                                int number = Integer.parseInt(text);
                                if (number < 1) {
                                    return List.of(AnvilGUI.ResponseAction.replaceInputText("Invalid number"));
                                } else {
                                    kitItem.setAmount(Math.min(number, maxStackSize));
                                    backTo.update();
                                }

                                return List.of(AnvilGUI.ResponseAction.close());
                            } catch (Exception exception) {
                                return List.of(AnvilGUI.ResponseAction.replaceInputText("Invalid number"));
                            }
                        })
                        .text("#")
                        .title(StaticItems.CHANGE_CUSTOM_AMOUNT_TITLE.replace("%max%", String.valueOf(maxStackSize)))
                        .open(player);
                break;
            default:
                if (AMOUNTS.containsKey(slot)) {
                    if (maxStackSize >= AMOUNTS.get(slot)) {
                        kitItem.setAmount(AMOUNTS.get(slot));
                        backTo.update();
                        backTo.open(player);
                    }
                }
                break;
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (player.getOpenInventory() != null && player.getOpenInventory().getType().equals(InventoryType.ANVIL)) {
                return;
            } else if (GUIManager.getInstance().getOpenGUI().containsKey(player)) {
                return;
            }

            backTo.open(player);
        }, 5L);
    }

}
