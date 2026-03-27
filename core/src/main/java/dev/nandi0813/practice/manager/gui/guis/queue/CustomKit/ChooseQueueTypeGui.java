package dev.nandi0813.practice.manager.gui.guis.queue.CustomKit;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChooseQueueTypeGui extends GUI {

    private static final String GUI_PATH = "GUIS.CUSTOM-KIT-QUEUE.CHOOSE-TYPE";
    private final Map<UUID, GUIType> backTargets = new HashMap<>();

    public ChooseQueueTypeGui() {
        super(GUIType.Queue_CustomKitChooseType);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString(GUI_PATH + ".TITLE"), getRows()));
        build();
    }

    private int getRows() {
        int rows = GUIFile.getInt(GUI_PATH + ".SIZE");
        return Math.clamp(rows <= 0 ? 3 : rows, 1, 6);
    }

    private int hostOwnSlot() {
        return GUIFile.getInt(GUI_PATH + ".SLOTS.HOST-OWN");
    }

    private int joinOthersSlot() {
        return GUIFile.getInt(GUI_PATH + ".SLOTS.JOIN-OTHERS");
    }

    private int backSlot() {
        return GUIFile.getInt(GUI_PATH + ".SLOTS.BACK");
    }

    public void openFor(Player player, GUIType backTarget) {
        if (player == null) {
            return;
        }

        if (backTarget != null) {
            backTargets.put(player.getUniqueId(), backTarget);
        }

        open(player);
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inventory = this.gui.get(1);
        inventory.clear();

        ItemStack filler = GUIFile.getGuiItem(GUI_PATH + ".ICONS.FILLER-ITEM").get();
        if (filler == null) {
            filler = ItemCreateUtil.createItem(" ", Material.BLACK_STAINED_GLASS_PANE);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        ItemStack hostItem = GUIFile.getGuiItem(GUI_PATH + ".ICONS.QUEUE-OWN-KIT").get();
        if (hostItem == null) {
            hostItem = ItemCreateUtil.createItem("&aQueue Your Own Kit", Material.WRITABLE_BOOK);
        }

        ItemStack joinOthersItem = GUIFile.getGuiItem(GUI_PATH + ".ICONS.QUEUE-OTHERS-KITS").get();
        if (joinOthersItem == null) {
            joinOthersItem = ItemCreateUtil.createItem("&eQueue Others Kits", Material.BOOK);
        }

        ItemStack backItem = GUIFile.getGuiItem(GUI_PATH + ".ICONS.BACK").get();
        if (backItem == null) {
            backItem = ItemCreateUtil.createItem("&cBack", Material.ARROW);
        }

        if (hostOwnSlot() >= 0 && hostOwnSlot() < inventory.getSize()) {
            inventory.setItem(hostOwnSlot(), hostItem);
        }
        if (joinOthersSlot() >= 0 && joinOthersSlot() < inventory.getSize()) {
            inventory.setItem(joinOthersSlot(), joinOthersItem);
        }
        if (backSlot() >= 0 && backSlot() < inventory.getSize()) {
            inventory.setItem(backSlot(), backItem);
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

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null || profile.getStatus() != ProfileStatus.LOBBY) {
            Common.sendMMMessage(player, LanguageManager.getString("CANT-USE-COMMAND"));
            player.closeInventory();
            return;
        }

        if (slot == hostOwnSlot()) {
            if (CustomKitQueueManager.getInstance().getHostableKits(profile).isEmpty()) {
                Common.sendMMMessage(player, LanguageManager.getString("QUEUES.CUSTOM.NO-SAVED-KITS"));
                return;
            }

            if (!player.hasPermission("zpp.playerkit.queue.host")) {
                Common.sendMMMessage(player, LanguageManager.getString("QUEUES.CUSTOM.NO-PERMISSION"));
                return;
            }

            new CustomKitHostSelectorGui(profile).open(player);
            return;
        }

        if (slot == joinOthersSlot()) {
            if (CustomKitQueueManager.getInstance().startJoinSearch(player)) {
                player.closeInventory();
            }
            return;
        }

        if (slot == backSlot()) {
            GUIType backTarget = backTargets.getOrDefault(player.getUniqueId(), GUIType.Queue_Unranked);
            GUI backGui = GUIManager.getInstance().searchGUI(backTarget);
            if (backGui != null) {
                backGui.open(player);
            }
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            backTargets.remove(player.getUniqueId());
        }
    }
}

