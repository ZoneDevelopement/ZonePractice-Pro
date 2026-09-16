package dev.nandi0813.practice.manager.gui.setup.ladder;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.PageUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class LadderSummaryGui extends GUI {

    private final int spaces = 45;
    private final Map<Integer, Map<Integer, NormalLadder>> ladderSlots = new HashMap<>();
    private final Map<Player, Integer> backToPage = new HashMap<>();

    public LadderSummaryGui() {
        super(GUIType.Ladder_Summary);
        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        List<NormalLadder> ladders = new ArrayList<>(LadderManager.getInstance().getLadders());
        ladders.sort(Comparator.comparing(Ladder::getName, String::compareToIgnoreCase));

        Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
        Map<Integer, Inventory> newGui = new HashMap<>();
        Map<Integer, Map<Integer, NormalLadder>> newLadderSlots = new HashMap<>();

        for (int page = 1; PageUtil.isPageValid(ladders.size(), page, spaces) || page == 1; page++) {
            Inventory inventory = existingInventories.get(page);
            String title = GUIFile.getString("GUIS.SETUP.LADDER.LADDER-MANAGER.TITLE").replace("%page%", String.valueOf(page));
            if (inventory == null || inventory.getSize() != 6 * 9) {
                inventory = InventoryUtil.createInventory(title, 6);
            } else {
                inventory.clear();
            }
            newGui.put(page, inventory);

            for (int i = 45; i < 54; i++) {
                inventory.setItem(i, GUIManager.getFILLER_ITEM());
            }
            for (int i = 0; i < 45; i++) {
                inventory.setItem(i, null);
            }

            Map<Integer, NormalLadder> pageSlots = new HashMap<>();
            int startIndex = (page - 1) * spaces;
            int endIndex = Math.min(startIndex + spaces, ladders.size());
            for (int i = startIndex; i < endIndex; i++) {
                int slot = inventory.firstEmpty();
                if (slot == -1 || slot >= 45) {
                    break;
                }

                NormalLadder ladder = ladders.get(i);
                inventory.setItem(slot, getLadderItem(ladder));
                pageSlots.put(slot, ladder);
            }
            newLadderSlots.put(page, pageSlots);

            ItemStack left = page == 1
                    ? GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MANAGER.ICONS.BACK-TO").get()
                    : GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MANAGER.ICONS.PAGE-LEFT")
                    .replace("%page%", String.valueOf(page - 1))
                    .get();
            inventory.setItem(45, left);

            ItemStack right = PageUtil.isPageValid(ladders.size(), page + 1, spaces)
                    ? GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MANAGER.ICONS.PAGE-RIGHT")
                    .replace("%page%", String.valueOf(page + 1))
                    .get()
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
        ItemStack currentItem = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (currentItem == null) return;
        if (currentItem.equals(GUIManager.getFILLER_ITEM())) return;

        int page = inGuiPlayers.getOrDefault(player, 1);
        if (slot == 45) {
            if (page == 1) {
                GUIManager.getInstance().searchGUI(GUIType.Setup_Hub).open(player);
            } else {
                open(player, page - 1);
            }
        } else if (slot == 53) {
            if (gui.containsKey(page + 1)) {
                open(player, page + 1);
            }
        } else if (ladderSlots.containsKey(page) && ladderSlots.get(page).containsKey(slot)) {
            Ladder ladder = ladderSlots.get(page).get(slot);
            if (ladder == null) {
                this.update();
                return;
            }

            backToPage.put(player, page);
            LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).open(player);
        }
    }


    private ItemStack getLadderItem(NormalLadder ladder) {
        String enabledStatus = GUIFile.getString("GUIS.SETUP.LADDER.LADDER-MANAGER.ICONS.LADDER-ICON.STATUS-NAMES.ENABLED");
        String disabledStatus = GUIFile.getString("GUIS.SETUP.LADDER.LADDER-MANAGER.ICONS.LADDER-ICON.STATUS-NAMES.DISABLED");

        GUIItem guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MANAGER.ICONS.LADDER-ICON")
                .replace("%ladder%", ladder.getName())
                .replace("%type%", ladder.getType().getName())
                .replace("%ladderState%", ladder.isEnabled() ? enabledStatus : disabledStatus)
                .replace("%rankedState%", ladder.isRanked() ? enabledStatus : disabledStatus)
                .replace("%freezeState%", ladder.isFrozen() ? enabledStatus : disabledStatus);

        if (ladder.getIcon() != null) {
            guiItem.setBaseItem(ladder.getIcon());
        }

        return guiItem.get();
    }

}
