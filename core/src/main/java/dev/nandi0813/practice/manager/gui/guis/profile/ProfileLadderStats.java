package dev.nandi0813.practice.manager.gui.guis.profile;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.statistics.LadderStats;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import dev.nandi0813.practice.util.PageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ProfileLadderStats extends GUI {

    private final Profile profile;
    private final GUI backTo;
    private final int spaces = 45;
    private final Map<Integer, Map<Integer, NormalLadder>> ladderSlots = new HashMap<>();

    public ProfileLadderStats(Profile profile, GUI backTo) {
        super(GUIType.Profile_LadderStats);
        this.profile = profile;
        this.backTo = backTo;

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        List<NormalLadder> ladders = new ArrayList<>(LadderManager.getInstance().getLadders());
        ladders.sort(Comparator.comparing(NormalLadder::getName, String::compareToIgnoreCase));

        Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
        Map<Integer, Inventory> newGui = new HashMap<>();
        Map<Integer, Map<Integer, NormalLadder>> newLadderSlots = new HashMap<>();

        for (int page = 1; PageUtil.isPageValid(ladders.size(), page, spaces) || page == 1; page++) {
            Inventory inventory = existingInventories.get(page);
            String title = GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.TITLE").replace("%player%", Objects.requireNonNull(profile.getPlayer().getName())).replace("%page%", String.valueOf(page));
            if (inventory == null || inventory.getSize() != 6 * 9) {
                inventory = InventoryUtil.createInventory(title, 6);
            } else {
                inventory.clear();
            }
            newGui.put(page, inventory);

            for (int i = 45; i < 54; i++) {
                inventory.setItem(i, GUIManager.getFILLER_ITEM());
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
                inventory.setItem(slot, getLadderStatItem(ladder));
                pageSlots.put(slot, ladder);
            }
            newLadderSlots.put(page, pageSlots);

            inventory.setItem(45, GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.BACK-TO-HUB").get());

            inventory.setItem(46, page > 1
                    ? GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.PAGE-LEFT").replace("%page%", String.valueOf(page - 1)).get()
                    : GUIManager.getFILLER_ITEM());
            inventory.setItem(47, PageUtil.isPageValid(ladders.size(), page + 1, spaces)
                    ? GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.PAGE-RIGHT").replace("%page%", String.valueOf(page + 1)).get()
                    : GUIManager.getFILLER_ITEM());

            inventory.setItem(49, GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.REFRESH").get());
            inventory.setItem(53, GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.RESET-ALL-STATS").get());
        }

        for (Map.Entry<Integer, Inventory> entry : new LinkedHashMap<>(gui).entrySet()) {
            if (newGui.containsKey(entry.getKey())) {
                continue;
            }

            gui.remove(entry.getKey());
            for (Player player : inGuiPlayers.keySet()) {
                if (inGuiPlayers.get(player).equals(entry.getKey())) {
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
        int slot = e.getRawSlot();
        Inventory inventory = e.getView().getTopInventory();
        ItemStack item = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (item == null) return;

        int page = inGuiPlayers.getOrDefault(player, 1);
        switch (slot) {
            case 45:
                backTo.open(player);
                break;
            case 46:
                if (page > 1 && gui.containsKey(page - 1)) {
                    open(player, page - 1);
                }
                break;
            case 47:
                if (gui.containsKey(page + 1)) {
                    open(player, page + 1);
                }
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
                if (!ladderSlots.containsKey(page) || !ladderSlots.get(page).containsKey(slot)) return;

                if (!player.hasPermission("zpp.practice.info.resetstats")) {
                    Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                    return;
                }

                NormalLadder ladder = ladderSlots.get(page).get(slot);
                profile.getStats().loadDefaultStats(ladder);
                update();
                break;
        }
    }

    private ItemStack getLadderStatItem(NormalLadder ladder) {
        List<String> lore = new ArrayList<>();
        String nullString = GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.NULL-STAT");

        if (!ladder.isRanked()) {
            for (String line : GUIFile.getStringList("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.UNRANKED-LADDER-STATS.LORE")) {
                lore.add(line
                        .replace("%ladder%", ladder.getDisplayName())
                        .replace("%wins%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins()))
                        .replace("%losses%", String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses()))
                        .replace("%custom_kits%", profile.getUnrankedCustomKits().containsKey(ladder) ? String.valueOf(profile.getUnrankedCustomKits().get(ladder).size()) : nullString)
                );
            }

            if (ladder.getIcon() != null)
                return ItemCreateUtil.createItem(
                        ladder.getIcon(),
                        GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.UNRANKED-LADDER-STATS.NAME").replace("%ladder%", ladder.getDisplayName()),
                        lore);
            else
                return ItemCreateUtil.createItem(
                        GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.UNRANKED-LADDER-STATS.NAME").replace("%ladder%", ladder.getDisplayName()),
                        Material.valueOf(GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.UNRANKED-LADDER-STATS.DEFAULT-MATERIAL")),
                        lore);
        } else {
            LadderStats ladderStats = profile.getStats().getLadderStat(ladder);
            for (String line : GUIFile.getStringList("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.RANKED-LADDER-STATS.LORE")) {
                lore.add(line
                        .replace("%unranked_wins%", String.valueOf(ladderStats.getUnRankedWins()))
                        .replace("%unranked_losses%", String.valueOf(ladderStats.getUnRankedLosses()))
                        .replace("%unranked_custom_kits%", String.valueOf(profile.getUnrankedCustomKits().containsKey(ladder) ? profile.getUnrankedCustomKits().get(ladder).size() : nullString))
                        .replace("%ranked_wins%", String.valueOf(ladderStats.getRankedWins()))
                        .replace("%ranked_losses%", String.valueOf(ladderStats.getRankedLosses()))
                        .replace("%ranked_custom_kits%", String.valueOf(profile.getRankedCustomKits().containsKey(ladder) ? profile.getRankedCustomKits().get(ladder).size() : nullString))
                        .replace("%elo%", String.valueOf(ladderStats.getElo()))
                );
            }

            if (ladder.getIcon() != null)
                return ItemCreateUtil.createItem(
                        ladder.getIcon(),
                        GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.RANKED-LADDER-STATS.NAME").replace("%ladder%", ladder.getDisplayName()),
                        lore);
            else
                return ItemCreateUtil.createItem(
                        GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.RANKED-LADDER-STATS.NAME").replace("%ladder%", ladder.getDisplayName()),
                        Material.valueOf(GUIFile.getString("GUIS.PLAYER-INFORMATION.LADDER-STATS.ICONS.LADDER.RANKED-LADDER-STATS.DEFAULT-MATERIAL")),
                        lore);
        }
    }

}