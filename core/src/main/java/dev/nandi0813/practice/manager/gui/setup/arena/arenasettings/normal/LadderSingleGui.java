package dev.nandi0813.practice.manager.gui.setup.arena.arenasettings.normal;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSetupUtil;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LadderSingleGui extends GUI {

    private final Map<Integer, String> ladderSlots = new HashMap<>();
    private final Arena arena;

    public LadderSingleGui(Arena arena) {
        super(GUIType.Arena_Ladders_Single);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.ARENA.ARENA-LADDERS-SINGLE.TITLE").replace("%arenaName%", arena.getName()), 6));
        this.arena = arena;

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        for (int i = 45; i < 54; i++)
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        // Navigation item
        gui.get(1).setItem(45, GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-SINGLE.ICONS.BACK-TO").get());

        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            ladderSlots.clear();
            for (int i = 0; i < 45; i++) gui.get(1).setItem(i, null);
            Set<NormalLadder> assignableLadders = arena.getAssignableLadders();

            for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
                ItemStack ladderItem;

                if (assignableLadders.contains(ladder)) {
                    if (arena.getAssignedLadderTypes().contains(ladder.getType()) && ladder.isEnabled()) {
                        if (arena.getAssignedLadders().contains(ladder))
                            ladderItem = ArenaSetupUtil.getAssignedLadderItem(ladder);
                        else
                            ladderItem = ArenaSetupUtil.getNotAssignedLadderItem(ladder);
                    } else
                        ladderItem = ArenaSetupUtil.getDisabledLadderItem(ladder);
                } else
                    ladderItem = ArenaSetupUtil.getNonCompatibleLadderItem(ladder);

                int slot = gui.get(1).firstEmpty();
                gui.get(1).setItem(slot, ladderItem);
                ladderSlots.put(slot, ladder.getName());
            }

            updatePlayers();
        });
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
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Ladders_Type).open(player);
        } else if (ladderSlots.containsKey(slot)) {
            NormalLadder ladder = LadderManager.getInstance().getLadder(ladderSlots.get(slot));
            if (ladder != null) {
                if (arena.getAssignedLadders().contains(ladder)) {
                    if (!arena.isEnabled()) {
                        arena.getAssignedLadders().remove(ladder);

                        update();
                        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.ARENA.CANT-REMOVE-LADDER"));
                } else {
                    if (arena.getAssignedLadderTypes().contains(ladder.getType()) && ladder.isEnabled()) {
                        arena.getAssignedLadders().add(ladder);

                        update();
                        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
                    }
                }
            } else
                update();
        }
    }
}
