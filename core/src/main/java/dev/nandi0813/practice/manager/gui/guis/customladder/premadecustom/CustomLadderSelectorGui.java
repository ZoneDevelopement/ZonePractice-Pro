package dev.nandi0813.practice.manager.gui.guis.customladder.premadecustom;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import dev.nandi0813.practice.util.PageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomLadderSelectorGui extends GUI {

    private final int spaces = 45;
    private final Map<Integer, Map<Integer, NormalLadder>> ladderSlots = new HashMap<>();

    public CustomLadderSelectorGui() {
        super(GUIType.CustomLadder_Selector);
        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        List<NormalLadder> ladders = new ArrayList<>();
        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            if (ladder.isEnabled() && ladder.isEditable()) {
                ladders.add(ladder);
            }
        }
        ladders.sort(Comparator.comparing(Ladder::getName, String::compareToIgnoreCase));

        Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
        Map<Integer, Inventory> newGui = new HashMap<>();
        Map<Integer, Map<Integer, NormalLadder>> newLadderSlots = new HashMap<>();

        for (int page = 1; PageUtil.isPageValid(ladders.size(), page, spaces) || page == 1; page++) {
            Inventory inventory = existingInventories.get(page);
            String title = GUIFile.getString("GUIS.KIT-EDITOR.LADDER-SELECTOR.TITLE").replace("%page%", String.valueOf(page));
            if (inventory == null || inventory.getSize() != 6 * 9) {
                inventory = InventoryUtil.createInventory(title, 6);
            } else {
                inventory.clear();
            }
            newGui.put(page, inventory);

            for (int i = 45; i < 54; i++) {
                inventory.setItem(i, GUIManager.getFILLER_ITEM());
            }
            for (int i = 0; i < 45; i++) {
                inventory.setItem(i, null);
            }

            Map<Integer, NormalLadder> pageSlots = new HashMap<>();
            int startIndex = (page - 1) * spaces;
            int endIndex = Math.min(startIndex + spaces, ladders.size());
            for (int i = startIndex; i < endIndex; i++) {
                int slot = inventory.firstEmpty();
                if (slot == -1 || slot >= 45) {
                    break;
                }

                NormalLadder ladder = ladders.get(i);
                inventory.setItem(slot, getLadderItem(ladder));
                pageSlots.put(slot, ladder);
            }
            newLadderSlots.put(page, pageSlots);

            ItemStack left = page == 1
                    ? GUIManager.getFILLER_ITEM()
                    : GUIFile.getGuiItem("GUIS.KIT-EDITOR.LADDER-SELECTOR.ICONS.PAGE-LEFT")
                    .replace("%page%", String.valueOf(page - 1))
                    .get();
            inventory.setItem(45, left);

            ItemStack right = PageUtil.isPageValid(ladders.size(), page + 1, spaces)
                    ? GUIFile.getGuiItem("GUIS.KIT-EDITOR.LADDER-SELECTOR.ICONS.PAGE-RIGHT")
                    .replace("%page%", String.valueOf(page + 1))
                    .get()
                    : GUIManager.getFILLER_ITEM();
            inventory.setItem(53, right);
        }

        for (Map.Entry<Integer, Inventory> entry : new LinkedHashMap<>(gui).entrySet()) {
            if (newGui.containsKey(entry.getKey())) {
                continue;
            }

            gui.remove(entry.getKey());
            for (Player player : inGuiPlayers.keySet()) {
                if (inGuiPlayers.get(player) == entry.getKey()) {
                    open(player, entry.getKey() - 1);
                }
            }
        }

        gui.putAll(newGui);
        ladderSlots.clear();
        ladderSlots.putAll(newLadderSlots);

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.LOBBY)) return;

        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack currentItem = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (currentItem == null) return;
        if (currentItem.equals(GUIManager.getFILLER_ITEM())) return;

        int page = inGuiPlayers.getOrDefault(player, 1);
        if (slot == 45) {
            if (gui.containsKey(page - 1)) {
                open(player, page - 1);
            }
        } else if (slot == 53) {
            if (gui.containsKey(page + 1)) {
                open(player, page + 1);
            }
        } else if (ladderSlots.containsKey(page) && ladderSlots.get(page).containsKey(slot)) {
            NormalLadder ladder = ladderSlots.get(page).get(slot);

            if (!ladder.isEnabled() || !ladder.isEditable()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.KIT-EDITOR.LADDER-SELECTOR.NOT-AVAILABLE").replace("%ladder%", ladder.getDisplayName()));
                update();
                return;
            }

            if (ladder.isFrozen()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.KIT-EDITOR.LADDER-SELECTOR.LADDER-FROZEN").replace("%ladder%", ladder.getDisplayName()));
                return;
            }

            new CustomLadderSumGui(profile, ladder).open(player);
        }
    }

    private ItemStack getLadderItem(NormalLadder ladder) {
        ItemStack icon = ladder.getIcon();
        ItemMeta iconMeta = icon.getItemMeta();
        if (iconMeta != null) {
            iconMeta.displayName(Common.legacyToComponent(GUIFile.getString("GUIS.KIT-EDITOR.LADDER-SELECTOR.ICONS.NAME")
                    .replace("%ladder%", ladder.getDisplayName())
                    .replace("%ladderOriginal%", ladder.getName())));
            ItemCreateUtil.hideItemFlags(iconMeta);

            List<String> lore = new ArrayList<>();
            for (String line : GUIFile.getStringList("GUIS.KIT-EDITOR.LADDER-SELECTOR.ICONS.LORE")) {
                lore.add(line
                        .replace("%ladder%", ladder.getDisplayName())
                        .replace("%ladderOriginal%", ladder.getName()))
                ;
            }
            iconMeta.lore(lore.stream().map(Common::legacyToComponent).toList());
            icon.setItemMeta(iconMeta);
        }
        return icon;
    }

}