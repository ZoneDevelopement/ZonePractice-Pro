package dev.nandi0813.practice.manager.gui.setup;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.server.ServerHubGui;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class SetupHubGui extends GUI {

    public SetupHubGui() {
        super(GUIType.Setup_Hub);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.HUB.TITLE"), 4));

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        inventory.setItem(11, GUIFile.getGuiItem("GUIS.SETUP.HUB.ICONS.ARENA-MANAGER").get());
        inventory.setItem(13, GUIFile.getGuiItem("GUIS.SETUP.HUB.ICONS.LADDER-MANAGER").get());
        inventory.setItem(15, GUIFile.getGuiItem("GUIS.SETUP.HUB.ICONS.HOLOGRAM-MANAGER").get());
        inventory.setItem(21, GUIFile.getGuiItem("GUIS.SETUP.HUB.ICONS.EVENT-MANAGER").get());
        inventory.setItem(23, GUIFile.getGuiItem("GUIS.SETUP.HUB.ICONS.SERVER-MANAGER").get());

        for (int i = 0; i < inventory.getSize(); i++)
            if (inventory.getItem(i) == null)
                inventory.setItem(i, GUIFile.getGuiItem("GUIS.SETUP.HUB.ICONS.GENERAL-FILLER-ITEM").get());
    }

    @Override
    public void update() {
        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;

        switch (slot) {
            case 11:
                GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).open(player);
                break;
            case 13:
                GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).open(player);
                break;
            case 15:
                GUI hologramSummaryGUI = GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary);
                if (hologramSummaryGUI != null)
                    hologramSummaryGUI.open(player);
                else
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.GUI-LOADING"));
                break;
            case 21:
                GUIManager.getInstance().searchGUI(GUIType.Event_Summary).open(player);
                break;
            case 23:
                new ServerHubGui().open(player);
                break;
        }
    }

}
