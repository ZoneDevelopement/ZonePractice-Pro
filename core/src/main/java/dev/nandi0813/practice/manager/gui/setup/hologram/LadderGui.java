package dev.nandi0813.practice.manager.gui.setup.hologram;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.leaderboard.hologram.Hologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.LadderDynamicHologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.LadderStaticHologram;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.PageUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class LadderGui extends GUI {

    private final int spaces = 45;
    private final Map<Integer, Map<Integer, String>> ladderSlots = new HashMap<>();
    private final Hologram hologram;

    public LadderGui(Hologram hologram) {
        super(GUIType.Hologram_Ladder);
        this.hologram = hologram;

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        if (hologram.getHologramType() == null) return;

        List<NormalLadder> ladders = new ArrayList<>();
        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            if (ladder.isEnabled() && ladder.getMatchTypes().contains(MatchType.DUEL)) {
                if (hologram instanceof LadderDynamicHologram dynamicHologram) {
                    if (dynamicHologram.getLeaderboardType().isRankedRelated() && !ladder.isRanked()) {
                        continue;
                    }
                }
                ladders.add(ladder);
            }
        }
        ladders.sort(Comparator.comparing(Ladder::getName, String::compareToIgnoreCase));

        Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
        Map<Integer, Inventory> newGui = new HashMap<>();
        Map<Integer, Map<Integer, String>> newLadderSlots = new HashMap<>();

        for (int page = 1; PageUtil.isPageValid(ladders.size(), page, spaces) || page == 1; page++) {
            Inventory inventory = existingInventories.get(page);
            String title = GUIFile.getString("GUIS.SETUP.HOLOGRAM.HOLOGRAM-LADDERS.TITLE").replace("%hologram%", hologram.getName()).replace("%page%", String.valueOf(page));
            if (inventory == null || inventory.getSize() != 6 * 9) {
                inventory = InventoryUtil.createInventory(title, 6);
            } else {
                inventory.clear();
            }
            newGui.put(page, inventory);

            for (int i = 45; i < 54; i++) {
                inventory.setItem(i, GUIManager.getFILLER_ITEM());
            }

            Map<Integer, String> pageSlots = new HashMap<>();
            int startIndex = (page - 1) * spaces;
            int endIndex = Math.min(startIndex + spaces, ladders.size());
            for (int i = startIndex; i < endIndex; i++) {
                int slot = inventory.firstEmpty();
                if (slot == -1 || slot >= 45) {
                    break;
                }

                NormalLadder ladder = ladders.get(i);
                boolean enabled = false;
                if (hologram instanceof LadderStaticHologram staticHologram) {
                    enabled = staticHologram.getLadder() == ladder;
                } else if (hologram instanceof LadderDynamicHologram dynamicHologram) {
                    enabled = dynamicHologram.getLadders().contains(ladder);
                }

                inventory.setItem(slot, getLadderItem(ladder, enabled));
                pageSlots.put(slot, ladder.getName());
            }
            newLadderSlots.put(page, pageSlots);

            ItemStack left = page == 1
                    ? GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-LADDERS.ICONS.GO-BACK").get()
                    : GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-LADDERS.ICONS.PAGE-LEFT").replace("%page%", String.valueOf(page - 1)).get();
            inventory.setItem(45, left);

            ItemStack right = PageUtil.isPageValid(ladders.size(), page + 1, spaces)
                    ? GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-LADDERS.ICONS.PAGE-RIGHT").replace("%page%", String.valueOf(page + 1)).get()
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
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack currentItem = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (currentItem == null) return;
        if (currentItem.equals(GUIManager.getFILLER_ITEM())) return;

        int page = inGuiPlayers.getOrDefault(player, 1);
        if (slot == 45) {
            if (page == 1) {
                HologramSetupManager.getInstance().getHologramSetupGUIs().get(hologram).get(GUIType.Hologram_Main).open(player);
            } else if (gui.containsKey(page - 1)) {
                open(player, page - 1);
            }
        } else if (slot == 53) {
            if (gui.containsKey(page + 1)) {
                open(player, page + 1);
            }
        } else if (ladderSlots.containsKey(page) && ladderSlots.get(page).containsKey(slot)) {
            if (!hologram.isEnabled()) {
                NormalLadder ladder = LadderManager.getInstance().getLadder(ladderSlots.get(page).get(slot));
                if (ladder == null) return;

                if (hologram instanceof LadderStaticHologram staticHologram) {
                    staticHologram.setLadder(ladder);
                    staticHologram.setData();
                } else if (hologram instanceof LadderDynamicHologram dynamicHologram) {
                    if (dynamicHologram.getLadders().contains(ladder))
                        dynamicHologram.getLadders().remove(ladder);
                    else
                        dynamicHologram.getLadders().add(ladder);
                    dynamicHologram.setData();
                }

                this.update();
            } else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.HOLOGRAM.CANT-EDIT-ENABLED"));
        }
    }

    private static ItemStack getLadderItem(Ladder ladder, boolean enabled) {
        if (enabled) {
            return GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-LADDERS.ICONS.ENABLED-LADDER")
                    .replace("%ladder%", ladder.getName())
                    .replace("%ladderDisplayName%", ladder.getDisplayName())
                    .get();
        } else {
            return GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-LADDERS.ICONS.DISABLED-LADDER")
                    .replace("%ladder%", ladder.getName())
                    .replace("%ladderDisplayName%", ladder.getDisplayName())
                    .get();
        }
    }

}