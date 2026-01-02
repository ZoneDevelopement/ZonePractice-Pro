package dev.nandi0813.practice.manager.gui.setup.arena.arenasettings.normal;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.confirmgui.ConfirmGuiType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSetupUtil;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSummaryGui;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class ArenaMainGui extends GUI {

    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.GO-BACK").get();
    private static final ItemStack LADDER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.LADDER").get();
    private static final ItemStack DELETE_ITEM = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.DELETE").get();

    private final Arena arena;

    public ArenaMainGui(Arena arena) {
        super(GUIType.Arena_Main);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.ARENA.ARENA-MAIN.TITLE").replace("%arenaName%", arena.getName()), 4));
        this.arena = arena;

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        inventory.setItem(27, BACK_TO_ITEM);
        for (int i : new int[]{28, 29, 30, 31, 32, 33})
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        inventory.setItem(15, LADDER_ITEM);
        inventory.setItem(35, DELETE_ITEM);

        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);

            inventory.setItem(10, ArenaSetupUtil.getNameItem(arena));
            inventory.setItem(11, ArenaSetupUtil.getStatusItem(arena));
            inventory.setItem(16, ArenaSetupUtil.getLocationItem(arena));
            if (arena.isBuild())
                inventory.setItem(14, ArenaSetupUtil.getArenaCopiesItem(arena));
            inventory.setItem(34, ArenaSetupUtil.getFreezeItem(arena));

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack currentItem = e.getCurrentItem();
        ClickType clickType = e.getClick();
        e.setCancelled(true);

        if (inventory.getSize() <= slot || currentItem == null) return;

        switch (slot) {
            case 27:
                ArenaSummaryGui arenaSummaryGui = (ArenaSummaryGui) GUIManager.getInstance().searchGUI(GUIType.Arena_Summary);
                arenaSummaryGui.open(player, arenaSummaryGui.getBackToPage().getOrDefault(player, 1));
                arenaSummaryGui.getBackToPage().remove(player);
                break;
            case 10:
                if (clickType.isLeftClick())
                    player.performCommand("arena info " + arena.getName());
                else if (clickType.isRightClick())
                    arena.teleport(player);
                break;
            case 11:
                ArenaUtil.changeStatus(player, arena);
                break;
            case 14:
                if (arena.isBuild())
                    ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Copy).open(player, 1);
                break;
            case 15:
                ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Ladders_Type).open(player, 1);
                break;
            case 34:
                player.performCommand("arena freeze " + arena.getName());
                break;
            case 35:
                if (arena.isEnabled()) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.ARENA.CANT-DELETE-ENABLED"));
                    return;
                }
                openConfirmGUI(player, ConfirmGuiType.ARENA_DELETE, GUIManager.getInstance().searchGUI(GUIType.Arena_Summary), this);
                break;
        }
    }

    @Override
    public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {
        if (confirmGuiType.equals(ConfirmGuiType.ARENA_DELETE)) {
            player.performCommand("arena delete " + arena.getName());
        }
    }

}
