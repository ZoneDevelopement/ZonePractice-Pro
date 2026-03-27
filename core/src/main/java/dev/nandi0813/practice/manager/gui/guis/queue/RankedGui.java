package dev.nandi0813.practice.manager.gui.guis.queue;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.guis.queue.CustomKit.ChooseQueueTypeGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.queue.CustomKitQueueManager;
import dev.nandi0813.practice.manager.queue.QueueManager;
import dev.nandi0813.practice.util.ItemCreateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class RankedGui extends QueueSelectorGui {

    private static final String CUSTOM_QUEUE_ITEM_PATH = "GUIS.RANKED-GUI.ICONS.CUSTOM-KIT-QUEUE";
    private static final String SWITCH_TO_UNRANKED_ITEM_PATH = "GUIS.RANKED-GUI.ICONS.SWITCH-TO-UNRANKED";
    private static final int DEFAULT_SWITCH_SLOT = 49;

    public RankedGui() {
        super(GUIType.Queue_Ranked);
    }

    @Override
    protected long getUpdateCooldownMinutes() {
        return ConfigManager.getInt("QUEUE.RANKED.GUI-UPDATE-MINUTE");
    }

    @Override
    protected String getQueueConfigPath() {
        return "QUEUE.RANKED";
    }

    @Override
    protected String getGuiConfigPath() {
        return "GUIS.RANKED-GUI";
    }

    @Override
    protected WeightClass getWeightClass() {
        return WeightClass.RANKED;
    }

    @Override
    protected boolean isRanked() {
        return true;
    }

    @Override
    protected boolean isValidLadder(NormalLadder ladder) {
        return ladder.isRanked();
    }

    @Override
    protected void onLadderClick(Player player, NormalLadder ladder) {
        QueueManager.getInstance().createRankedQueue(player, ladder);
    }

    @Override
    protected void decoratePage(int pageId, Inventory inventory) {
        int slot = inventory.getSize() - 1;
        placeSwitchItem(inventory);

        if (!ConfigManager.getBoolean("QUEUE.RANKED.CUSTOM-KIT.ENTRY-ENABLED")
                || !CustomKitQueueManager.getInstance().isCustomKitQueueEnabled()) {
            ItemStack filler = GUIFile.getGuiItem("GUIS.RANKED-GUI.ICONS.FILLER-ITEM").get();
            if (filler != null) {
                inventory.setItem(slot, filler);
            }
            return;
        }

        ItemStack customQueueItem = GUIFile.getGuiItem(CUSTOM_QUEUE_ITEM_PATH).get();
        if (customQueueItem == null) {
            customQueueItem = ItemCreateUtil.createItem("&6Custom Kit Queue", Material.BOOK);
        }

        inventory.setItem(slot, customQueueItem);
    }

    @Override
    protected boolean handleCustomTopInventoryClick(Player player, int rawSlot, InventoryView inventoryView, ItemStack item) {
        int switchSlot = getSwitchModeSlot();
        if (isQueueGuiSwitchEnabled()
                && switchSlot >= 0
                && switchSlot < inventoryView.getTopInventory().getSize()
                && rawSlot == switchSlot) {
            GUI unrankedGui = GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked);
            if (unrankedGui != null) {
                int currentPage = this.getInGuiPlayers().getOrDefault(player, 1);
                unrankedGui.open(player, currentPage);
            }
            return true;
        }

        int customQueueSlot = inventoryView.getTopInventory().getSize() - 1;
        if (rawSlot != customQueueSlot) {
            return false;
        }

        if (!ConfigManager.getBoolean("QUEUE.RANKED.CUSTOM-KIT.ENTRY-ENABLED")
                || !CustomKitQueueManager.getInstance().isCustomKitQueueEnabled()) {
            return true;
        }

        GUI gui = GUIManager.getInstance().searchGUI(GUIType.Queue_CustomKitChooseType);
        if (gui instanceof ChooseQueueTypeGui chooseQueueTypeGui) {
            chooseQueueTypeGui.openFor(player, GUIType.Queue_Ranked);
        } else if (gui != null) {
            gui.open(player);
        }
        return true;
    }

    private void placeSwitchItem(Inventory inventory) {
        if (!isQueueGuiSwitchEnabled()) {
            return;
        }

        int switchSlot = getSwitchModeSlot();
        if (switchSlot < 0 || switchSlot >= inventory.getSize()) {
            return;
        }

        ItemStack switchItem = GUIFile.getGuiItem(SWITCH_TO_UNRANKED_ITEM_PATH).get();
        if (switchItem == null) {
            switchItem = ItemCreateUtil.createItem("&aSwitch to Unranked", Material.WOODEN_SWORD);
        }

        inventory.setItem(switchSlot, switchItem);
    }

    private int getSwitchModeSlot() {
        return ConfigManager.getInt("QUEUE.RANKED.SELECTOR-GUI.SWITCH-MODE-SLOT", DEFAULT_SWITCH_SLOT);
    }

    private boolean isQueueGuiSwitchEnabled() {
        String path = "QUEUE.COMBINED.SWITCH-ENABLED";
        if (!ConfigManager.getConfig().isBoolean(path)) {
            return true;
        }

        return ConfigManager.getBoolean(path);
    }
}