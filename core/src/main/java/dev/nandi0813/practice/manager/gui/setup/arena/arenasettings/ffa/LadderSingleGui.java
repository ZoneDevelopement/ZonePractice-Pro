package dev.nandi0813.practice.manager.gui.setup.arena.arenasettings.ffa;

import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LadderSingleGui extends GUI {

    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.BACK-TO").get();
    private static final GUIItem ASSIGNED_LADDER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.ASSIGNED");
    private static final GUIItem NOT_ASSIGNED_LADDER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.NOT-ASSIGNED");
    private static final GUIItem DISABLED_LADDER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.DISABLED");

    private final Map<Integer, String> ladderSlots = new HashMap<>();
    private final FFAArena ffaArena;

    public LadderSingleGui(FFAArena ffaArena) {
        super(GUIType.Arena_Ladders_Single);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.TITLE").replace("%arenaName%", ffaArena.getName()), 6));
        this.ffaArena = ffaArena;

        this.build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        for (int i = 45; i < 54; i++)
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        gui.get(1).setItem(45, BACK_TO_ITEM);

        update();
    }

    @Override
    public void update() {
        ladderSlots.clear();
        for (int i = 0; i < 45; i++) gui.get(1).setItem(i, null);

        for (NormalLadder ladder : ffaArena.getAssignableLadders()) {
            ItemStack ladderItem;

            if (ladder.isEnabled()) {
                if (ffaArena.getAssignedLadders().contains(ladder)) {
                    ladderItem = replacePlaceholders(ASSIGNED_LADDER_ITEM, ladder).get();
                } else {
                    ladderItem = replacePlaceholders(NOT_ASSIGNED_LADDER_ITEM, ladder).get();
                }
            } else {
                ladderItem = replacePlaceholders(DISABLED_LADDER_ITEM, ladder).get();
            }

            int slot = gui.get(1).firstEmpty();
            gui.get(1).setItem(slot, ladderItem);
            ladderSlots.put(slot, ladder.getName());
        }

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack item = e.getCurrentItem();
        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (item == null) return;

        if (slot == 45) {
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(ffaArena).get(GUIType.Arena_Main).open(player);
        } else if (ladderSlots.containsKey(slot)) {
            NormalLadder ladder = LadderManager.getInstance().getLadder(ladderSlots.get(slot));
            if (ladder != null) {
                if (ffaArena.getAssignedLadders().contains(ladder)) {
                    if (!ffaArena.isEnabled()) {
                        ffaArena.getAssignedLadders().remove(ladder);

                        update();
                        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.ARENA.CANT-REMOVE-LADDER"));
                } else {
                    if (ladder.isEnabled()) {
                        ffaArena.getAssignedLadders().add(ladder);

                        update();
                        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
                    }
                }
            } else
                update();
        }
    }

    private static GUIItem replacePlaceholders(GUIItem guiItem, NormalLadder ladder) {
        return guiItem.cloneItem()
                .replace("%ladderName%", ladder.getName())
                .replace("%ladderDisplayName%", ladder.getDisplayName())
                .replace("%ladderType%", ladder.getType().getName());
    }

}
