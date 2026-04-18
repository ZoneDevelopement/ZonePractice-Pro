package dev.nandi0813.practice.manager.gui.guis;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.KitData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BracketsKitSelectorGui extends GUI {

    private static final int[] CUSTOM_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16};

    private final Player host;
    private final Profile hostProfile;

    private final Map<Integer, KitData> selectableKits = new HashMap<>();

    public BracketsKitSelectorGui(Player host) {
        super(GUIType.Event_BracketsKitSelector);
        this.host = host;
        this.hostProfile = ProfileManager.getInstance().getProfile(host);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.BRACKETS-KIT-SELECTOR.TITLE"), 3));
        this.build();
    }

    @Override
    public void build() {
        this.update();
    }

    @Override
    public void update() {
        Inventory inventory = this.gui.get(1);
        inventory.clear();
        this.selectableKits.clear();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, GUIManager.getFILLER_ITEM());
        }

        GUIItem defaultKitIcon = GUIFile.getGuiItem("GUIS.BRACKETS-KIT-SELECTOR.ICONS.DEFAULT-KIT").cloneItem();
        inventory.setItem(4, defaultKitIcon.get());
        this.selectableKits.put(4, null);

        List<CustomLadder> availableCustomKits = getAvailableCustomKits();
        int customIndex = 0;
        for (CustomLadder customLadder : availableCustomKits) {
            if (customIndex >= CUSTOM_SLOTS.length) {
                break;
            }

            int slot = CUSTOM_SLOTS[customIndex++];
            GUIItem customKitItem = GUIFile.getGuiItem("GUIS.BRACKETS-KIT-SELECTOR.ICONS.CUSTOM-KIT")
                    .cloneItem()
                    .replace("%kitName%", customLadder.getDisplayName());

            if (customLadder.getIcon() != null) {
                customKitItem.setBaseItem(customLadder.getIcon());
            }

            inventory.setItem(slot, customKitItem.get());
            this.selectableKits.put(slot, new KitData(customLadder.getKitData()));
        }

        if (availableCustomKits.isEmpty()) {
            inventory.setItem(13, GUIFile.getGuiItem("GUIS.BRACKETS-KIT-SELECTOR.ICONS.NO-CUSTOM-KIT").get());
        }
    }

    private List<CustomLadder> getAvailableCustomKits() {
        List<CustomLadder> available = new ArrayList<>();

        if (this.hostProfile == null) {
            return available;
        }

        for (CustomLadder customLadder : this.hostProfile.getCustomLadders()) {
            if (customLadder != null && customLadder.getKitData().isSet()) {
                available.add(customLadder);
            }
        }

        return available;
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        if (!player.equals(this.host)) {
            return;
        }

        int slot = e.getRawSlot();
        if (e.getView().getTopInventory().getSize() <= slot) {
            return;
        }

        if (!this.selectableKits.containsKey(slot)) {
            return;
        }

        KitData selectedKitData = this.selectableKits.get(slot);
        EventManager.getInstance().startEvent(this.host, EventType.BRACKETS, selectedKitData);
        this.host.closeInventory();
    }
}


