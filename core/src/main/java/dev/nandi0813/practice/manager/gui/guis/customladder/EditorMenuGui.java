package dev.nandi0813.practice.manager.gui.guis.customladder;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditorMenuGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.KIT-EDITOR.EDITOR-MENU.ICONS.FILLER-ITEM").get();

    public EditorMenuGui() {
        super(GUIType.CustomLadder_EditorMenu);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.KIT-EDITOR.EDITOR-MENU.TITLE"), 3));
        this.build();
    }

    @Override
    public void build() {
        this.update();
    }

    @Override
    public void update() {
        Inventory inventory = this.gui.get(1);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, FILLER_ITEM);
        }

        inventory.setItem(11, GUIFile.getGuiItem("GUIS.KIT-EDITOR.EDITOR-MENU.ICONS.PREMADE-LADDER").get());
        inventory.setItem(15, GUIFile.getGuiItem("GUIS.KIT-EDITOR.EDITOR-MENU.ICONS.PLAYER-LADDER").get());

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        int slot = e.getRawSlot();

        e.setCancelled(true);
        switch (slot) {
            case 11:
                GUIManager.getInstance().searchGUI(GUIType.CustomLadder_Selector).open(player);
                break;
            case 15:
                if (profile.getGroup() == null || profile.getGroup().getCustomKitLimit() == 0) {
                    Common.sendMMMessage(player, LanguageManager.getString("CUSTOM-PLAYER-KIT.NO-CUSTOM-KIT"));
                    return;
                }

                profile.getPlayerCustomKitSelector().update();
                profile.getPlayerCustomKitSelector().open(player);
                break;
        }
    }

}
