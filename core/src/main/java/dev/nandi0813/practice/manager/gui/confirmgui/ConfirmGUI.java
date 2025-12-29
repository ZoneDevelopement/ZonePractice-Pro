package dev.nandi0813.practice.manager.gui.confirmgui;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class ConfirmGUI {

    private static final ItemStack CANCEL_ITEM = GUIFile.getGuiItem("GUIS.VALIDATION-GUI.ICONS.CANCEL").get();
    private static final ItemStack APPROVE_ITEM = GUIFile.getGuiItem("GUIS.VALIDATION-GUI.ICONS.APPROVE").get();

    private final ConfirmGuiType type;
    private final String description;
    private final GUI backToConfirm;
    private final GUI backToCancel;

    private Inventory inventory;

    public ConfirmGUI(ConfirmGuiType type, GUI backToConfirm, GUI backToCancel) {
        this.type = type;
        this.description = type.getDescription();
        this.backToConfirm = backToConfirm;
        this.backToCancel = backToCancel;

        this.buildInventory();
    }

    private void buildInventory() {
        inventory = InventoryUtil.createInventory(GUIFile.getString("GUIS.VALIDATION-GUI.TITLE"), 1);

        for (int i = 0; i < 9; i++)
            inventory.setItem(i, CANCEL_ITEM);
        inventory.setItem(4, APPROVE_ITEM);
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

}
