package dev.nandi0813.practice.manager.gui.setup.arena.arenasettings.ffa;

import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.confirmgui.ConfirmGuiType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArenaMainGui extends GUI {

    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.GO-BACK").get();
    private static final ItemStack DELETE_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.DELETE").get();
    private static final ItemStack STATUS_ENABLED_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.STATUS.ENABLED").get();
    private static final ItemStack STATUS_DISABLED_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.STATUS.DISABLED").get();
    private static final ItemStack LADDER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.LADDER").get();
    private static final ItemStack SETTINGS_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.SETTINGS").get();
    private static final ItemStack FFA_OPEN_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.OPEN-STATUS.OPEN").get();
    private static final ItemStack FFA_CLOSE_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.OPEN-STATUS.CLOSE").get();

    private static final GUIItem NAME_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.ARENA-NAME");
    private static final GUIItem LOCATION_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.MAIN.ICONS.LOCATION");

    private final FFAArena ffaArena;
    private final FFASettingsGui ffaSettingsGui;

    public ArenaMainGui(FFAArena ffaArena) {
        super(GUIType.Arena_Main);

        this.ffaArena = ffaArena;
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.FFA-ARENA.MAIN.TITLE").replaceAll("%arenaName%", ffaArena.getName()), 4));
        this.ffaSettingsGui = new FFASettingsGui(ffaArena, this);

        this.build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        for (int i : new int[]{28, 29, 30, 32, 33, 34})
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        inventory.setItem(14, SETTINGS_ITEM);
        inventory.setItem(15, LADDER_ITEM);
        inventory.setItem(27, BACK_TO_ITEM);
        inventory.setItem(35, DELETE_ITEM);

        update();

        this.update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        inventory.setItem(10, NAME_ITEM.cloneItem()
                .replaceAll("%arenaDisplayName%", ffaArena.getDisplayName())
                .replaceAll("%arenaName%", ffaArena.getName())
                .replaceAll("%arenaType%", ffaArena.getType().toString())
                .setMaterial(ffaArena.getIcon() != null && ffaArena.getIcon().getType() != null ? ffaArena.getIcon().getType() : null)
                .setDamage(ffaArena.getIcon() != null && ffaArena.getIcon().getDurability() != 0 ? ffaArena.getIcon().getDurability() : -1)
                .get());

        inventory.setItem(11, ffaArena.isEnabled() ? STATUS_ENABLED_ITEM : STATUS_DISABLED_ITEM);

        inventory.setItem(15, LADDER_ITEM);

        inventory.setItem(16, LOCATION_ITEM.cloneItem()
                .replaceAll("%arenaName%", ffaArena.getName())
                .replaceAll("%arenaDisplayName%", ffaArena.getDisplayName())
                .replaceAll("%corner1%", Common.mmToNormal(ArenaUtil.convertLocation(ffaArena.getCorner1())))
                .replaceAll("%corner2%", Common.mmToNormal(ArenaUtil.convertLocation(ffaArena.getCorner2())))
                .replaceAll("%ffa_pos_num%", String.valueOf(ffaArena.getFfaPositions().size()))
                .get());

        inventory.setItem(31, ffaArena.getFfa().isOpen() ? FFA_OPEN_ITEM : FFA_CLOSE_ITEM);

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        ClickType clickType = e.getClick();
        Inventory inventory = e.getClickedInventory();

        if (inventory == null) return;
        if (inventory.getSize() <= slot) return;

        switch (slot) {
            case 10:
                if (clickType.isLeftClick())
                    player.performCommand("arena info " + ffaArena.getName());
                else if (clickType.isRightClick())
                    ffaArena.teleport(player);
                break;
            case 11:
                if (ffaArena.isEnabled()) {
                    FFA ffa = ffaArena.getFfa();

                    if (ffa != null && ffa.isOpen()) {
                        openConfirmGUI(player, ConfirmGuiType.FFA_ARENA_DISABLE, this, this);
                    } else {
                        ArenaUtil.changeStatus(player, ffaArena);
                    }
                } else {
                    ArenaUtil.changeStatus(player, ffaArena);
                }
                break;
            case 14:
                this.ffaSettingsGui.open(player);
                break;
            case 15:
                ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(ffaArena).get(GUIType.Arena_Ladders_Single).open(player);
                break;
            case 27:
                GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).open(player);
                break;
            case 31:
                if (ffaArena.isEnabled()) {
                    FFA ffa = ffaArena.getFfa();

                    if (ffa.isOpen()) {
                        openConfirmGUI(player, ConfirmGuiType.FFA_ARENA_CLOSE, this, this);
                    } else {
                        ffa.open();
                        this.update();
                    }
                }
                break;
            case 35:
                if (ffaArena.isEnabled()) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.ARENA.CANT-DELETE-ENABLED"));
                    return;
                }
                openConfirmGUI(player, ConfirmGuiType.FFA_ARENA_DELETE, GUIManager.getInstance().searchGUI(GUIType.Arena_Summary), this);
                break;
        }
    }

    @Override
    public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {
        switch (confirmGuiType) {
            case FFA_ARENA_DISABLE:
                ArenaUtil.changeStatus(player, ffaArena);
                break;
            case FFA_ARENA_CLOSE:
                ffaArena.getFfa().close("");
                this.update();
                break;
            case FFA_ARENA_DELETE:
                player.performCommand("arena delete " + ffaArena.getName());
                break;
        }
    }

}
