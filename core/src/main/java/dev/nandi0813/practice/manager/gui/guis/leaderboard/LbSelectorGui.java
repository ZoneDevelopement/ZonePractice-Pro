package dev.nandi0813.practice.manager.gui.guis.leaderboard;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.division.DivisionUtil;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.guis.DivisionGui;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StatisticUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LbSelectorGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.STATISTICS.SELECTOR.ICONS.FILLER-ITEM").get();

    @Getter
    private static LbEloGui sharedLbEloGui;
    @Getter
    private static LbWinGui sharedLbWinGui;

    private final Player opener;
    private final Profile profile;

    private final LbProfileStatGui lbProfileStatGui;

    public LbSelectorGui(Player opener, Profile profile) {
        super(GUIType.Leaderboard_Selector);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.STATISTICS.SELECTOR.TITLE"), 4));
        this.opener = opener;
        this.profile = profile;

        this.lbProfileStatGui = new LbProfileStatGui(profile, this);

        if (sharedLbEloGui == null) {
            sharedLbEloGui = new LbEloGui(this);
        }
        if (sharedLbWinGui == null) {
            sharedLbWinGui = new LbWinGui(this);
        }

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, FILLER_ITEM);
        }

        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);

            inventory.setItem(13, LbGuiUtil.createProfileStatItem(profile, opener));
            inventory.setItem(20, GUIFile.getGuiItem("GUIS.STATISTICS.SELECTOR.ICONS.ELO-LEADERBOARD").get());
            inventory.setItem(22, getDivisionItem(profile));
            inventory.setItem(24, GUIFile.getGuiItem("GUIS.STATISTICS.SELECTOR.ICONS.TOP-WIN-LEADERBOARD").get());

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        switch (slot) {
            case 13:
                lbProfileStatGui.open(player);
                break;
            case 20:
                sharedLbEloGui.open(player);
                break;
            case 22:
                new DivisionGui(profile, this).open(player);
                break;
            case 24:
                sharedLbWinGui.open(player);
                break;
        }
    }

    private static ItemStack getDivisionItem(Profile profile) {
        Division division = profile.getStats().getDivision();
        Division nextDivision = DivisionManager.getInstance().getNextDivision(profile);

        GUIItem guiItem;
        if (nextDivision != null) {
            guiItem = GUIFile.getGuiItem("GUIS.STATISTICS.SELECTOR.ICONS.VIEW-DIVISIONS.HAS-NEXT");

            guiItem
                    .replace("%nextDivision_fullName%", Common.mmToNormal(nextDivision.getFullName()))
                    .replace("%nextDivision_shortName%", Common.mmToNormal(nextDivision.getShortName()))
                    .replace("%nextDivision_exp%", String.valueOf(nextDivision.getExperience()))
                    .replace("%nextDivision_wins%", String.valueOf(nextDivision.getWin()))
                    .replace("%progress_bar%", StatisticUtil.getProgressBar(DivisionUtil.getDivisionProgress(profile, nextDivision)))
                    .replace("%progress_percent%", String.valueOf(DivisionUtil.getDivisionProgress(profile, nextDivision)));
        } else {
            guiItem = GUIFile.getGuiItem("GUIS.STATISTICS.SELECTOR.ICONS.VIEW-DIVISIONS.NO-NEXT");
        }

        guiItem
                .replace("%division_fullName%", Common.mmToNormal(division.getFullName()))
                .replace("%division_shortName%", Common.mmToNormal(division.getShortName()))
                .replace("%exp%", String.valueOf(profile.getStats().getExperience()))
                .replace("%elo%", String.valueOf(profile.getStats().getGlobalElo()))
                .replace("%wins%", String.valueOf(profile.getStats().getGlobalWins()));

        return guiItem.get();
    }

}
