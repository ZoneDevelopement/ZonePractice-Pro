package dev.nandi0813.practice.manager.gui.setup.arena.arenasettings.ffa;

import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class FFASettingsGui extends GUI {

    private static final ItemStack GO_BACK_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.SETTINGS.ICONS.GO-BACK").get();
    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.SETTINGS.ICONS.FILLER").get();
    private static final ItemStack BUILD_ENABLED_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.SETTINGS.ICONS.BUILD.ENABLED").get();
    private static final ItemStack BUILD_DISABLED_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.SETTINGS.ICONS.BUILD.DISABLED").get();
    private static final ItemStack REKIT_ENABLED_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.SETTINGS.ICONS.RE-KIT-AFTER-KILL.ENABLED").get();
    private static final ItemStack REKIT_DISABLED_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.SETTINGS.ICONS.RE-KIT-AFTER-KILL.DISABLED").get();
    private static final ItemStack LOBBYDEATH_ENABLED_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.SETTINGS.ICONS.LOBBY-AFTER-DEATH.ENABLED").get();
    private static final ItemStack LOBBYDEATH_DISABLED_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.SETTINGS.ICONS.LOBBY-AFTER-DEATH.DISABLED").get();

    private final FFAArena ffaArena;
    private final ArenaMainGui arenaMainGui;

    public FFASettingsGui(FFAArena ffaArena, ArenaMainGui arenaMainGui) {
        super(GUIType.FFA_Arena_Settings);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.FFA-ARENA.SETTINGS.TITLE").replace("%arenaName%", ffaArena.getName()), 4));
        this.ffaArena = ffaArena;
        this.arenaMainGui = arenaMainGui;

        this.build();
    }

    @Override
    public void build() {
        Inventory inventory = this.gui.get(1);

        inventory.setItem(27, GO_BACK_ITEM);
        for (int i : new int[]{28, 29, 30, 31, 32, 33, 34, 35}) {
            inventory.setItem(i, FILLER_ITEM);
        }

        this.update();
    }

    @Override
    public void update() {
        Inventory inventory = this.gui.get(1);

        if (ffaArena.isBuild()) {
            inventory.setItem(11, BUILD_ENABLED_ITEM);
        } else {
            inventory.setItem(11, BUILD_DISABLED_ITEM);
        }

        if (ffaArena.isReKitAfterKill()) {
            inventory.setItem(13, REKIT_ENABLED_ITEM);
        } else {
            inventory.setItem(13, REKIT_DISABLED_ITEM);
        }

        if (ffaArena.isLobbyAfterDeath()) {
            inventory.setItem(15, LOBBYDEATH_ENABLED_ITEM);
        } else {
            inventory.setItem(15, LOBBYDEATH_DISABLED_ITEM);
        }

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        e.setCancelled(true);

        try {
            switch (e.getRawSlot()) {
                case 11:
                    ffaArena.setBuild(!ffaArena.isBuild());
                    this.update();
                    break;
                case 13:
                    ffaArena.setReKitAfterKill(!ffaArena.isReKitAfterKill());
                    this.update();
                    break;
                case 15:
                    ffaArena.setLobbyAfterDeath(!ffaArena.isLobbyAfterDeath());
                    this.update();
                    break;
                case 27:
                    arenaMainGui.open(player);
                    break;
            }

        } catch (IllegalStateException exception) {
            Common.sendMMMessage(player, "<red>" + exception.getMessage());
        }
    }

}
