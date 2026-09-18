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
import dev.nandi0813.practice.util.PageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LadderSingleGui extends GUI {

    private static final GUIItem ASSIGNED_LADDER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.ASSIGNED");
    private static final GUIItem NOT_ASSIGNED_LADDER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.NOT-ASSIGNED");
    private static final GUIItem DISABLED_LADDER_ITEM = GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.DISABLED");

    private final int spaces = 45;
    private final Map<Integer, Map<Integer, String>> ladderSlots = new HashMap<>();
    private final FFAArena ffaArena;

    public LadderSingleGui(FFAArena ffaArena) {
        super(GUIType.Arena_Ladders_Single);

        this.ffaArena = ffaArena;

        this.build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        List<NormalLadder> ladders = new ArrayList<>(ffaArena.getAssignableLadders());
        ladders.sort(Comparator.comparing(NormalLadder::getName, String::compareToIgnoreCase));

        Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
        Map<Integer, Inventory> newGui = new HashMap<>();
        Map<Integer, Map<Integer, String>> newLadderSlots = new HashMap<>();

        for (int page = 1; PageUtil.isPageValid(ladders.size(), page, spaces) || page == 1; page++) {
            Inventory inventory = existingInventories.get(page);
            String title = GUIFile.getString("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.TITLE")
                    .replace("%arenaName%", ffaArena.getName())
                    .replace("%page%", String.valueOf(page));
            if (inventory == null || inventory.getSize() != 6 * 9) {
                inventory = InventoryUtil.createInventory(title, 6);
            } else {
                inventory.clear();
            }
            newGui.put(page, inventory);

            for (int i = 45; i < 54; i++)
                inventory.setItem(i, GUIManager.getFILLER_ITEM());

            Map<Integer, String> pageSlots = new HashMap<>();
            int startIndex = (page - 1) * spaces;
            int endIndex = Math.min(startIndex + spaces, ladders.size());
            for (int i = startIndex; i < endIndex; i++) {
                NormalLadder ladder = ladders.get(i);
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

                int slot = inventory.firstEmpty();
                if (slot == -1 || slot >= 45) {
                    break;
                }

                inventory.setItem(slot, ladderItem);
                pageSlots.put(slot, ladder.getName());
            }
            newLadderSlots.put(page, pageSlots);

            ItemStack left = page == 1
                    ? GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.BACK-TO").get()
                    : GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.PAGE-LEFT").replace("%page%", String.valueOf(page - 1)).get();
            inventory.setItem(45, left);

            ItemStack right = PageUtil.isPageValid(ladders.size(), page + 1, spaces)
                    ? GUIFile.getGuiItem("GUIS.SETUP.FFA-ARENA.ARENA-LADDERS-SINGLE.ICONS.PAGE-RIGHT").replace("%page%", String.valueOf(page + 1)).get()
                    : GUIManager.getFILLER_ITEM();
            inventory.setItem(53, right);
        }

        for (Map.Entry<Integer, Inventory> entry : new LinkedHashMap<>(gui).entrySet()) {
            if (newGui.containsKey(entry.getKey())) {
                continue;
            }

            gui.remove(entry.getKey());
            for (Player player : inGuiPlayers.keySet()) {
                if (inGuiPlayers.get(player).equals(entry.getKey())) {
                    open(player, entry.getKey() - 1);
                }
            }
        }

        gui.putAll(newGui);
        ladderSlots.clear();
        ladderSlots.putAll(newLadderSlots);

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

        int page = inGuiPlayers.getOrDefault(player, 1);

        if (slot == 45) {
            if (page == 1) {
                ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(ffaArena).get(GUIType.Arena_Main).open(player);
            } else if (gui.containsKey(page - 1)) {
                open(player, page - 1);
            }
        } else if (slot == 53) {
            if (gui.containsKey(page + 1)) {
                open(player, page + 1);
            }
        } else if (ladderSlots.containsKey(page) && ladderSlots.get(page).containsKey(slot)) {
            NormalLadder ladder = LadderManager.getInstance().getLadder(ladderSlots.get(page).get(slot));
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
