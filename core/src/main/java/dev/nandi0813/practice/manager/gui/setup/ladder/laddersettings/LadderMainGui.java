package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.confirmgui.ConfirmGuiType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class LadderMainGui extends GUI {

    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.BACK-TO").get();
    private static final ItemStack DELETE_ITEM = GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.DELETE").get();
    private static final ItemStack DESTROYABLE_BLOCK_ITEM = GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.DESTROYABLE-BLOCKS").get();
    private static final ItemStack INVENTORY_ITEM = GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.INVENTORY").get();
    private static final ItemStack SETTINGS_ITEM = GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.SETTINGS").get();
    private static final ItemStack MATCH_TYPE_ITEM = GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.MATCH-TYPE").get();

    private final NormalLadder ladder;

    public LadderMainGui(NormalLadder ladder) {
        super(GUIType.Ladder_Main);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.LADDER.LADDER-MAIN.TITLE").replace("%ladder%", ladder.getName()), 4));

        this.ladder = ladder;

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        // Navigation
        inventory.setItem(27, BACK_TO_ITEM);
        inventory.setItem(35, DELETE_ITEM);

        for (int i : new int[]{28, 29, 30, 31, 32, 33})
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        if (ladder.isBuild())
            inventory.setItem(13, DESTROYABLE_BLOCK_ITEM);
        inventory.setItem(14, INVENTORY_ITEM);
        inventory.setItem(15, SETTINGS_ITEM);
        inventory.setItem(16, MATCH_TYPE_ITEM);

        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        inventory.setItem(10, getNameItem(ladder));
        inventory.setItem(11, getStatusItem(ladder));
        inventory.setItem(34, getFreezeItem(ladder.isFrozen()));

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (e.getCurrentItem() == null) return;

        switch (slot) {
            case 27:
                GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).open(player);
                break;
            case 11:
                if (ladder.isEnabled()) {
                    openConfirmGUI(player, ConfirmGuiType.LADDER_DISABLE, this, this);
                } else {
                    LadderUtil.changeStatus(player, ladder);
                }
                break;
            case 13:
                if (ladder.isBuild())
                    LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_DestroyableBlock).open(player);
                break;
            case 14:
                LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Inventory).open(player);
                break;
            case 15:
                LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Settings).open(player);
                break;
            case 16:
                LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_MatchType).open(player);
                break;
            case 34:
                player.performCommand("ladder freeze " + ladder.getName());
                break;
            case 35:
                if (ladder.isEnabled()) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-DELETE-ENABLED"));
                    return;
                }

                openConfirmGUI(player, ConfirmGuiType.LADDER_DELETE, GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary), this);
                break;
        }
    }

    private static ItemStack getNameItem(Ladder ladder) {
        GUIItem guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.LADDER-NAME")
                .replace("%ladder%", ladder.getName())
                .replace("%type%", ladder.getType().getName());

        if (ladder.getIcon() != null) {
            guiItem.setBaseItem(ladder.getIcon());
        }

        return guiItem.get();
    }

    private static ItemStack getStatusItem(Ladder ladder) {
        if (ladder.isEnabled())
            return GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.STATUS.ENABLED").get();
        else
            return GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.STATUS.DISABLED").get();
    }

    private static ItemStack getFreezeItem(boolean isFrozen) {
        if (isFrozen)
            return GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.FREEZE.FROZEN").get();
        else
            return GUIFile.getGuiItem("GUIS.SETUP.LADDER.LADDER-MAIN.ICONS.FREEZE.NOT-FROZEN").get();
    }

    @Override
    public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {
        switch (confirmGuiType) {
            case LADDER_DISABLE:
                LadderUtil.changeStatus(player, ladder);
                break;
            case LADDER_DELETE:
                player.performCommand("ladder delete " + ladder.getName());
                GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).open(player);
                break;
        }
    }

}
