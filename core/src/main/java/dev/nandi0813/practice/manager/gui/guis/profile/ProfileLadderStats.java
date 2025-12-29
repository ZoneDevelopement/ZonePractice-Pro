package dev.nandi0813.practice.manager.gui.guis.profile;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.statistics.LadderStats;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileLadderStats extends GUI {

    private final Profile profile;
    private final GUI backTo;
    private final Map<Integer, NormalLadder> ladderSlots = new HashMap<>();

    public ProfileLadderStats(Profile profile, GUI backTo) {
        super(GUIType.Profile_LadderStats);
        this.profile = profile;
        this.backTo = backTo;

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.TITLE").replace("%player%", profile.getPlayer().getName()), 6));

        build();
    }

    @Override
    public void build() {
        update();
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
                int slot = inventory.firstEmpty();

                inventory.setItem(slot, getLadderStatItem(ladder));
                ladderSlots.put(slot, ladder);
            }

            inventory.setItem(45, GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.BACK-TO-HUB").get());
            inventory.setItem(49, GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.REFRESH").get());
            inventory.setItem(53, GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.RESET-ALL-STATS").get());

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        Inventory inventory = e.getView().getTopInventory();
        ItemStack item = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() > slot && item != null) {
            switch (slot) {
                case 45:
                    backTo.open(player);
                    break;
                case 49:
                    update();
                    break;
                case 53:
                    if (!player.hasPermission("zpp.practice.info.resetstats")) {
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                        return;
                    }

                    for (NormalLadder ladder : LadderManager.getInstance().getLadders())
                        profile.getStats().loadDefaultStats(ladder);
                    update();
                    break;
                default:
                    if (!ladderSlots.containsKey(slot)) return;

                    if (!player.hasPermission("zpp.practice.info.resetstats")) {
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                        return;
                    }

                    NormalLadder ladder = ladderSlots.get(slot);
                    profile.getStats().loadDefaultStats(ladder);
                    update();
                    break;
            }
        }
    }

    private ItemStack getLadderStatItem(NormalLadder ladder) {
        List<String> lore = new ArrayList<>();
        String nullString = GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.NULL-STAT");

        if (!ladder.isRanked()) {
            for (String line : GUIFile.getStringList("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.UNRANKED-LADDER-STATS.LORE")) {
                lore.add(line
                        .replaceAll("%ladder%", ladder.getDisplayName())
                        .replaceAll("%wins%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins()))
                        .replaceAll("%losses%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses()))
                        .replaceAll("%custom_kits%", profile.getUnrankedCustomKits().containsKey(ladder) ? String.valueOf(profile.getUnrankedCustomKits().get(ladder).size()) : nullString)
                );
            }

            if (ladder.getIcon() != null)
                return ClassImport.getClasses().getItemCreateUtil().createItem(
                        ladder.getIcon(),
                        GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.UNRANKED-LADDER-STATS.NAME").replace("%ladder%", ladder.getDisplayName()),
                        lore);
            else
                return ClassImport.getClasses().getItemCreateUtil().createItem(
                        GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.UNRANKED-LADDER-STATS.NAME").replace("%ladder%", ladder.getDisplayName()),
                        Material.valueOf(GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.UNRANKED-LADDER-STATS.DEFAULT-MATERIAL")),
                        lore);
        } else {
            LadderStats ladderStats = profile.getStats().getLadderStat(ladder);
            for (String line : GUIFile.getStringList("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.RANKED-LADDER-STATS.LORE")) {
                lore.add(line
                        .replaceAll("%unranked_wins%", String.valueOf(ladderStats.getUnRankedWins()))
                        .replaceAll("%unranked_losses%", String.valueOf(ladderStats.getUnRankedLosses()))
                        .replaceAll("%unranked_custom_kits%", String.valueOf(profile.getUnrankedCustomKits().containsKey(ladder) ? profile.getUnrankedCustomKits().get(ladder).size() : nullString))
                        .replaceAll("%ranked_wins%", String.valueOf(ladderStats.getRankedWins()))
                        .replaceAll("%ranked_losses%", String.valueOf(ladderStats.getRankedLosses()))
                        .replaceAll("%ranked_custom_kits%", String.valueOf(profile.getRankedCustomKits().containsKey(ladder) ? profile.getRankedCustomKits().get(ladder).size() : nullString))
                        .replaceAll("%elo%", String.valueOf(ladderStats.getElo()))
                );
            }

            if (ladder.getIcon() != null)
                return ClassImport.getClasses().getItemCreateUtil().createItem(
                        ladder.getIcon(),
                        GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.RANKED-LADDER-STATS.NAME").replace("%ladder%", ladder.getDisplayName()),
                        lore);
            else
                return ClassImport.getClasses().getItemCreateUtil().createItem(
                        GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.RANKED-LADDER-STATS.NAME").replace("%ladder%", ladder.getDisplayName()),
                        Material.valueOf(GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.RANKED-LADDER-STATS.DEFAULT-MATERIAL")),
                        lore);
        }
    }

}
