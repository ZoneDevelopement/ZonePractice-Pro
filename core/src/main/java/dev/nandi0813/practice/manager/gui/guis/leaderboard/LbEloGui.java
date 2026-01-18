package dev.nandi0813.practice.manager.gui.guis.leaderboard;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUICache;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class LbEloGui extends GUI {

    private final GUI backTo;

    public LbEloGui(GUI backTo) {
        super(GUIType.Leaderboard_ELO);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.STATISTICS.ELO-LEADERBOARD.TITLE"), 6));
        this.backTo = backTo;

        build();
    }

    @Override
    public void build() {
        update(false);
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);
            inventory.clear();

            for (int i = 45; i < 54; i++)
                inventory.setItem(i, GUIManager.getFILLER_ITEM());

            for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
                if (!ladder.isEnabled()) continue;
                if (!ladder.isRanked()) continue;
                if (!ladder.getMatchTypes().contains(MatchType.DUEL)) continue;

                inventory.setItem(inventory.firstEmpty(), LbGuiUtil.createEloLbItem(ladder));
            }

            ItemStack fillerItem = GUIFile.getGuiItem("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.FILLER-ITEM").get();
            for (int i = 0; i < 45; i++) {
                ItemStack current = inventory.getItem(i);
                if (current == null || current.getType().equals(Material.AIR))
                    inventory.setItem(i, fillerItem);
            }

            inventory.setItem(45, GUIFile.getGuiItem("GUIS.STATISTICS.ELO-LEADERBOARD.ICONS.BACK-TO-HUB").get());
            inventory.setItem(49, LbGuiUtil.createGlobalEloLb());
            inventory.setItem(53, LbGuiUtil.getCacheInfoItem());

            updatePlayers();

            if (GUICache.shouldCache(type)) {
                GUICache.putCache(type, gui);
            }
        });
    }

    @Override
    public void open(Player player, int page) {
        if (GUICache.shouldCache(type) && GUICache.isCacheValid(type)) {
            Map<Integer, Inventory> cached = GUICache.getCached(type);
            if (cached != null) {
                gui.clear();
                gui.putAll(cached);
            }
        } else {
            update();
        }

        super.open(player, page);
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (slot == 45) {
            if (backTo != null) backTo.open(player);
            else player.closeInventory();
        }
    }

}
