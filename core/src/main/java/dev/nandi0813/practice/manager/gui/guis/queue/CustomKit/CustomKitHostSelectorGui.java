package dev.nandi0813.practice.manager.gui.guis.queue.CustomKit;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.queue.CustomKitQueueManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomKitHostSelectorGui extends GUI {

    private static final String GUI_PATH = "GUIS.CUSTOM-KIT-QUEUE.HOST-SELECTOR";

    private final Profile profile;
    private final Map<Integer, CustomLadder> slotToKit = new HashMap<>();

    public CustomKitHostSelectorGui(Profile profile) {
        super(GUIType.Queue_CustomKitHostSelector);
        this.profile = profile;

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString(GUI_PATH + ".TITLE"), getRows()));
        build();
    }

    private int getRows() {
        int rows = GUIFile.getInt(GUI_PATH + ".SIZE");
        return Math.clamp(rows <= 0 ? 3 : rows, 1, 6);
    }

    private int backSlot() {
        return GUIFile.getInt(GUI_PATH + ".SLOTS.BACK");
    }

    private int noKitsSlot() {
        return GUIFile.getInt(GUI_PATH + ".SLOTS.NO-KITS");
    }

    private List<Integer> kitSlots() {
        List<Integer> slots = GUIFile.getConfig().getIntegerList((GUI_PATH + ".SLOTS.KIT-SLOTS").toUpperCase());
        if (slots.isEmpty()) {
            return List.of(10, 11, 12, 13, 14, 15, 16);
        }
        return slots;
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);
        inventory.clear();
        slotToKit.clear();

        ItemStack filler = GUIFile.getGuiItem(GUI_PATH + ".ICONS.FILLER-ITEM").get();
        if (filler == null) {
            filler = ItemCreateUtil.createItem(" ", Material.BLACK_STAINED_GLASS_PANE);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        if (!CustomKitQueueManager.getInstance().isCustomKitQueueEnabled()) {
            updatePlayers();
            return;
        }

        ItemStack backItem = GUIFile.getGuiItem(GUI_PATH + ".ICONS.BACK").get();
        if (backItem == null) {
            backItem = ItemCreateUtil.createItem("&cBack", Material.ARROW);
        }
        if (backSlot() >= 0 && backSlot() < inventory.getSize()) {
            inventory.setItem(backSlot(), backItem);
        }

        List<CustomLadder> hostableKits = new ArrayList<>(CustomKitQueueManager.getInstance().getHostableKits(profile));
        if (hostableKits.isEmpty()) {
            ItemStack noKitsItem = GUIFile.getGuiItem(GUI_PATH + ".ICONS.NO-KITS").get();
            if (noKitsItem == null) {
                noKitsItem = ItemCreateUtil.createItem("&cNo Saved Custom Kits", Material.BARRIER);
            }

            if (noKitsSlot() >= 0 && noKitsSlot() < inventory.getSize()) {
                inventory.setItem(noKitsSlot(), noKitsItem);
            }
            updatePlayers();
            return;
        }

        List<Integer> kitSlots = kitSlots();
        for (int i = 0; i < hostableKits.size() && i < kitSlots.size(); i++) {
            CustomLadder customLadder = hostableKits.get(i);

            ItemStack icon = customLadder.getIcon();
            if (icon == null || icon.getType() == Material.AIR) {
                icon = ItemCreateUtil.createItem("&e" + customLadder.getDisplayName(), Material.WRITABLE_BOOK);
            } else {
                icon = icon.clone();
                ItemMeta itemMeta = icon.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.displayName(Common.legacyToComponent("&e" + customLadder.getDisplayName()));
                    icon.setItemMeta(itemMeta);
                }
            }

            int targetSlot = kitSlots.get(i);
            if (targetSlot < 0 || targetSlot >= inventory.getSize()) {
                continue;
            }
            inventory.setItem(targetSlot, icon);
            slotToKit.put(targetSlot, customLadder);
        }

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot >= e.getView().getTopInventory().getSize()) {
            return;
        }

        Profile currentProfile = ProfileManager.getInstance().getProfile(player);
        if (currentProfile == null || currentProfile.getStatus() != ProfileStatus.LOBBY) {
            Common.sendMMMessage(player, LanguageManager.getString("CANT-USE-COMMAND"));
            player.closeInventory();
            return;
        }

        if (slot == backSlot()) {
            GUI backGui = GUIManager.getInstance().searchGUI(GUIType.Queue_CustomKitChooseType);
            if (backGui != null) {
                backGui.open(player);
            }
            return;
        }

        if (!slotToKit.containsKey(slot)) {
            return;
        }

        CustomLadder selectedKit = slotToKit.get(slot);
        if (CustomKitQueueManager.getInstance().hostQueue(player, selectedKit)) {
            player.closeInventory();
        }
    }
}





