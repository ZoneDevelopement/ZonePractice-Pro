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
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class LadderSummaryGui extends GUI {

    private final Map<Integer, NormalLadder> ladderSlots = new HashMap<>();

    public LadderSummaryGui() {
        super(GUIType.Ladder_Summary);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.LADDER.LADDER-MANAGER.TITLE"), 6));

        build();
    }

    @Override
    public void build() {
        // Frame
        for (int i : new int[]{46, 47, 48, 49, 50, 51, 52, 53})
            gui.get(1).setItem(i, GUIManager.getFILLER_ITEM());

        // Back to Manager Icon
        gui.get(1).setItem(45, GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MANAGER.ICONS.BACK-TO").get());

        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        ladderSlots.clear();
        for (int i = 0; i < 45; i++)
            inventory.setItem(i, null);

        // Sort ladders alphanumerically by internal name for consistent ordering
        LadderManager.getInstance().getLadders()
                .stream()
                .sorted(java.util.Comparator.comparing(Ladder::getName, String::compareToIgnoreCase))
                .forEach(ladder -> {
                    int slot = inventory.firstEmpty();

                    inventory.setItem(slot, getLadderItem(ladder));
                    ladderSlots.put(slot, ladder);
                });

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

        if (slot == 45) {
            GUIManager.getInstance().searchGUI(GUIType.Setup_Hub).open(player);
        } else if (ladderSlots.containsKey(slot)) {
            Ladder ladder = ladderSlots.get(slot);
            if (ladder == null) {
                this.update();
                return;
            }

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
