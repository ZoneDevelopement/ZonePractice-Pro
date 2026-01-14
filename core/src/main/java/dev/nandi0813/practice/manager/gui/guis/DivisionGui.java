package dev.nandi0813.practice.manager.gui.guis;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.division.DivisionUtil;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StatisticUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DivisionGui extends GUI {

    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.DIVISION.ICONS.BACK-TO").get();
    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.DIVISION.ICONS.FILLER-ITEM").get();
    private static final ItemStack FILLER_ITEM2 = GUIFile.getGuiItem("GUIS.DIVISION.ICONS.FILLER-ITEM2").get();
    private static final GUIItem PAST_DIVISION_ITEM = GUIFile.getGuiItem("GUIS.DIVISION.ICONS.PAST-DIVISION");
    private static final GUIItem CURRENT_DIVISION_ITEM = GUIFile.getGuiItem("GUIS.DIVISION.ICONS.CURRENT-DIVISION");
    private static final GUIItem NEXT_DIVISION_ITEM = GUIFile.getGuiItem("GUIS.DIVISION.ICONS.NEXT-DIVISION");

    private final Profile profile;
    private final GUI backToGui;

    public DivisionGui(final Profile profile, final GUI backToGui) {
        super(GUIType.DivisionGui);

        this.profile = profile;
        this.backToGui = backToGui;

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.DIVISION.TITLE"), 6));

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

        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 27, 36, 17, 26, 35, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53})
            inventory.setItem(i, FILLER_ITEM);

        if (backToGui != null)
            inventory.setItem(45, BACK_TO_ITEM);

        for (Division division : DivisionManager.getInstance().getDivisions()) {
            inventory.addItem(getDivisionItem(division));
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, FILLER_ITEM2);
            }
        }

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (slot == 45) {
            if (backToGui != null)
                backToGui.open(player);
        }
    }

    private ItemStack getDivisionItem(final Division division) {
        GUIItem guiItem;

        if (division == profile.getStats().getDivision()) {
            guiItem = CURRENT_DIVISION_ITEM.cloneItem();
            guiItem.replace("%progress_bar%", StatisticUtil.getProgressBar(100.0));
            guiItem.replace("%progress_percent%", "100.0");
        } else if (division.getExperience() < profile.getStats().getExperience() && division.getWin() < profile.getStats().getGlobalWins()) {
            guiItem = PAST_DIVISION_ITEM.cloneItem();
            guiItem.replace("%progress_bar%", StatisticUtil.getProgressBar(100.0));
            guiItem.replace("%progress_percent%", "100.0");
        } else {
            guiItem = NEXT_DIVISION_ITEM.cloneItem();
            guiItem.replace("%current_wins%", String.valueOf(profile.getStats().getGlobalWins()));
            guiItem.replace("%current_elo%", String.valueOf(profile.getStats().getGlobalElo()));
            guiItem.replace("%current_exp%", String.valueOf(profile.getStats().getExperience()));
            guiItem.replace("%win_progress_percent%", String.valueOf(DivisionUtil.getWinProgress(profile, division)));
            guiItem.replace("%elo_progress_percent%", String.valueOf(DivisionUtil.getEloProgress(profile, division)));
            guiItem.replace("%exp_progress_percent%", String.valueOf(DivisionUtil.getExperienceProgress(profile, division)));
            guiItem.replace("%progress_bar%", StatisticUtil.getProgressBar(DivisionUtil.getDivisionProgress(profile, division)));
            guiItem.replace("%progress_percent%", String.valueOf(DivisionUtil.getDivisionProgress(profile, division)));
        }

        if (guiItem.getMaterial() == null)
            guiItem.setMaterial(division.getIconMaterial());

        if (guiItem.getDamage() != null)
            guiItem.setDamage(division.getIconDamage());

        guiItem = replacePlaceholders(guiItem, division);

        return guiItem.get();
    }

    private static GUIItem replacePlaceholders(final GUIItem guiItem, final Division division) {
        return guiItem
                .replace("%fullName%", division.getFullName())
                .replace("%shortName%", division.getShortName())
                .replace("%color%", String.valueOf(division.getColor()))
                .replace("%required_wins%", String.valueOf(division.getWin()))
                .replace("%required_elo%", String.valueOf(division.getElo()))
                .replace("%required_exp%", String.valueOf(division.getExperience()))
                .replaceMMtoNormal();
    }

}
