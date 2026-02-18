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
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class LadderGui extends GUI {

    private final Map<Integer, String> ladderSlots = new HashMap<>();
    private final Hologram hologram;

    public LadderGui(Hologram hologram) {
        super(GUIType.Hologram_Ladder);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.HOLOGRAM.HOLOGRAM-LADDERS.TITLE").replace("%hologram%", hologram.getName()), 6));
        this.hologram = hologram;

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        for (int i = 45; i < 54; i++)
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        // Navigation item
        gui.get(1).setItem(45, GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-LADDERS.ICONS.GO-BACK").get());

        update();
    }

    @Override
    public void update() {
        if (hologram.getHologramType() == null) return;

        ladderSlots.clear();
        for (int i = 0; i < 45; i++)
            gui.get(1).setItem(i, null);

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            if (ladder.isEnabled() && ladder.getMatchTypes().contains(MatchType.DUEL)) {
                ItemStack ladderItem = null;
                if (hologram instanceof LadderStaticHologram staticHologram) {

                    if (staticHologram.getLadder() != null && staticHologram.getLadder() == ladder)
                        ladderItem = getLadderItem(ladder, true);
                    else
                        ladderItem = getLadderItem(ladder, false);
                } else if (hologram instanceof LadderDynamicHologram dynamicHologram) {

                    if (dynamicHologram.getLeaderboardType().isRankedRelated() && !ladder.isRanked())
                        continue;

                    if (dynamicHologram.getLadders().contains(ladder))
                        ladderItem = getLadderItem(ladder, true);
                    else
                        ladderItem = getLadderItem(ladder, false);
                }

                if (ladderItem != null) {
                    int slot = gui.get(1).firstEmpty();

                    gui.get(1).setItem(slot, ladderItem);
                    ladderSlots.put(slot, ladder.getName());
                }
            }
        }

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack currentItem = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() > slot && currentItem != null && !currentItem.equals(GUIManager.getFILLER_ITEM())) {
            if (slot == 45) {
                HologramSetupManager.getInstance().getHologramSetupGUIs().get(hologram).get(GUIType.Hologram_Main).open(player);
            } else if (ladderSlots.containsKey(slot)) {
                if (!hologram.isEnabled()) {
                    NormalLadder ladder = LadderManager.getInstance().getLadder(ladderSlots.get(slot));
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
