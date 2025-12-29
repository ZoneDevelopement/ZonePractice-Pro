package dev.nandi0813.practice.manager.gui.guis.customladder;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.playerkit.PlayerKitManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerCustomKitSelector extends GUI {

    private static final Map<Integer, List<Integer>> slotsPerKitLimit = Map.of(
            1, List.of(13),
            2, List.of(12, 14),
            3, List.of(11, 13, 15),
            4, List.of(10, 12, 14, 16),
            5, List.of(9, 11, 13, 15, 17)
    );

    private static final GUIItem KIT_ITEM = GUIFile.getGuiItem("GUIS.KIT-EDITOR.PLAYER-CUSTOM-KIT-SELECTOR.ICONS.KIT");
    private static final ItemStack SELECTED_KIT_ITEM = GUIFile.getGuiItem("GUIS.KIT-EDITOR.PLAYER-CUSTOM-KIT-SELECTOR.ICONS.SELECTED").get();
    private static final ItemStack UNSELECTED_KIT_ITEM = GUIFile.getGuiItem("GUIS.KIT-EDITOR.PLAYER-CUSTOM-KIT-SELECTOR.ICONS.UNSELECTED").get();
    private static final ItemStack GUIDE_ITEM = GUIFile.getGuiItem("GUIS.KIT-EDITOR.PLAYER-CUSTOM-KIT-SELECTOR.ICONS.GUIDE").get();

    private final Profile profile;
    private final int kitLimit;
    private final Map<Integer, CustomLadder> slots = new HashMap<>();
    private final Map<Integer, CustomLadder> selectSlots = new HashMap<>();

    public PlayerCustomKitSelector(final Profile profile) {
        super(GUIType.CustomLadder_CustomKitSelector);
        this.profile = profile;
        this.kitLimit = profile.getGroup().getCustomKitLimit();

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.KIT-EDITOR.PLAYER-CUSTOM-KIT-SELECTOR.TITLE"), 6));

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
        this.slots.clear();

        inventory.setItem(49, GUIDE_ITEM);

        for (int i = 0; i < this.kitLimit; i++) {
            CustomLadder customLadder = this.getCustomLadder(i);
            if (customLadder == null) {
                continue;
            }

            GUIItem kitItem = KIT_ITEM.cloneItem();
            kitItem.replaceAll("%kit%", customLadder.getDisplayName() != null ? customLadder.getDisplayName() : "Kit " + i);

            int slot = slotsPerKitLimit.get(kitLimit).get(i);
            inventory.setItem(slot, kitItem.get());

            if (profile.getSelectedCustomLadder() == customLadder) {
                inventory.setItem(slot + 9, SELECTED_KIT_ITEM);
            } else {
                inventory.setItem(slot + 9, UNSELECTED_KIT_ITEM);
            }

            this.slots.put(slot, customLadder);
            this.selectSlots.put(slot + 9, customLadder);
        }

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        CustomLadder customLadder = slots.get(slot);
        if (customLadder != null) {
            if (PlayerKitManager.getInstance().getCopying().containsKey(player)) {
                int index = profile.getCustomLadders().indexOf(customLadder);
                if (index != -1) {
                    CustomLadder copyCustomLadder = PlayerKitManager.getInstance().getCopying().get(player);
                    CustomLadder newLadder = new CustomLadder(
                            copyCustomLadder,
                            this.profile,
                            customLadder.getMapPath()
                    );

                    this.profile.getCustomLadders().set(index, newLadder);
                    newLadder.getMainGUI().open(player);
                    Common.sendMMMessage(player, LanguageManager.getString("CUSTOM-PLAYER-KIT.COPYKIT.COPY-SUCCESS").replaceAll("%name%", copyCustomLadder.getDisplayName()));
                }
            } else {
                customLadder.getMainGUI().open(player);
            }
        } else {
            customLadder = selectSlots.get(slot);
            if (customLadder != null) {
                if (customLadder.isEnabled()) {
                    if (this.profile.getSelectedCustomLadder() != customLadder) {
                        this.profile.setSelectedCustomLadder(customLadder);
                        this.update();
                    }
                } else {
                    Common.sendMMMessage(player, LanguageManager.getString("CUSTOM-PLAYER-KIT.ONLY-SELECT-ENABLED-CUSTOMS"));
                }
            }
        }
    }

    private CustomLadder getCustomLadder(int i) {
        if (i < 0 || i >= profile.getCustomLadders().size()) {
            return null;
        }

        return profile.getCustomLadders().get(i);
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        PlayerKitManager.getInstance().getCopying().remove((Player) e.getPlayer());
    }

}
