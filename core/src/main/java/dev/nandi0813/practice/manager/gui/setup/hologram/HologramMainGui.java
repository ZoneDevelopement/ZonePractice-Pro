package dev.nandi0813.practice.manager.gui.setup.hologram;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.confirmgui.ConfirmGuiType;
import dev.nandi0813.practice.manager.leaderboard.hologram.Hologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramManager;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramType;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.LadderDynamicHologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.LadderStaticHologram;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public class HologramMainGui extends GUI {

    private final Hologram hologram;

    public HologramMainGui(Hologram hologram) {
        super(GUIType.Hologram_Main);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MAIN.TITLE").replace("%hologram%", hologram.getName()), 4));
        this.hologram = hologram;

        build();
    }

    @Override
    public void build() {
        // Navigation
        gui.get(1).setItem(27, GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MAIN.ICONS.BACK-TO").get());
        for (int i : new int[]{28, 29, 30, 31, 32, 33, 34})
            gui.get(1).setItem(i, GUIManager.getFILLER_ITEM());

        gui.get(1).setItem(35, GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MAIN.ICONS.DELETE").get());

        update();
    }

    @Override
    public void update() {
        gui.get(1).setItem(10, getTypeItem(hologram));
        gui.get(1).setItem(12, getLadderItem(hologram));
        gui.get(1).setItem(14, getShowItem(hologram));
        gui.get(1).setItem(16, getStatusItem(hologram));

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        ClickType click = e.getClick();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().equals(GUIManager.getFILLER_ITEM())) return;

        switch (slot) {
            case 27:
                GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).open(player);
                break;
            case 10:
                if (!hologram.isEnabled()) {
                    hologram.setLeaderboardType(HologramManager.getInstance().getNextType(hologram.getLeaderboardType()));

                    if (hologram.getLeaderboardType().isRankedRelated()) {
                        if (hologram instanceof LadderStaticHologram staticHologram) {

                            if (staticHologram.getLadder() != null && !staticHologram.getLadder().isRanked())
                                staticHologram.setLadder(null);
                        } else if (hologram instanceof LadderDynamicHologram dynamicHologram) {
                            dynamicHologram.getLadders().removeIf(ladder -> !ladder.isRanked());
                        }
                    }

                    this.update();
                    GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).update();
                    GUI ladderGUI = HologramSetupManager.getInstance().getHologramSetupGUIs().get(hologram).get(GUIType.Hologram_Ladder);
                    if (ladderGUI != null) {
                        ladderGUI.update();
                    }
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.HOLOGRAM.CANT-EDIT-ENABLED"));
                break;
            case 12:
                if (hologram.getHologramType() != null && !hologram.getHologramType().equals(HologramType.GLOBAL))
                    HologramSetupManager.getInstance().getHologramSetupGUIs().get(hologram).get(GUIType.Hologram_Ladder).open(player);
                break;
            case 14:
                if (!hologram.isEnabled()) {
                    int currentShowStat = hologram.getShowStat();

                    if (click.isLeftClick() && currentShowStat > 3)
                        hologram.setShowStat(currentShowStat - 1);
                    else if (click.isRightClick() && currentShowStat < 15)
                        hologram.setShowStat(currentShowStat + 1);

                    update();
                    GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).update();
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.HOLOGRAM.CANT-EDIT-ENABLED"));
                break;
            case 16:
                if (hologram.isEnabled()) {
                    hologram.setEnabled(false);
                } else {
                    if (!hologram.isReadyToEnable()) {
                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.HOLOGRAM.NO-REQUIREMENTS"));
                        break;
                    } else {
                        hologram.setEnabled(true);
                    }
                }

                update();
                GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).update();
                break;
            case 35:
                openConfirmGUI(player, ConfirmGuiType.HOLOGRAM_DELETE, GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary), this);
                break;
        }
    }

    private static ItemStack getTypeItem(Hologram hologram) {
        List<String> typeExtension = new ArrayList<>();
        for (LbSecondaryType hologramType : HologramManager.getInstance().getLbSecondaryTypes()) {
            typeExtension.add((hologramType.equals(hologram.getLeaderboardType()) ? "&a" : "&c") + " &l► &e" + StringUtil.getNormalizedName(hologramType.name()));
        }

        GUIItem guiItem = GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MAIN.ICONS.EVENT-TYPE");

        List<String> lore = new ArrayList<>();
        for (String line : guiItem.getLore()) {
            if (line.contains("%eventTypes%"))
                lore.addAll(typeExtension);
        }
        guiItem.setLore(lore);

        return guiItem.get();
    }

    private static ItemStack getLadderItem(Hologram hologram) {
        if (hologram.getHologramType().equals(HologramType.GLOBAL))
            return GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MAIN.ICONS.LADDER.NO-LADDER-SETTINGS").get();
        else
            return GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MAIN.ICONS.LADDER.HAS-LADDER-SETTINGS").get();
    }

    private static ItemStack getShowItem(Hologram hologram) {
        return GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MAIN.ICONS.SHOW-STATS")
                .replace("%showStats%", String.valueOf(hologram.getShowStat()))
                .get();
    }

    private static ItemStack getStatusItem(Hologram hologram) {
        if (hologram.isEnabled())
            return GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MAIN.ICONS.STATUS.ENABLED").get();
        else
            return GUIFile.getGuiItem("GUIS.SETUP.HOLOGRAM.HOLOGRAM-MAIN.ICONS.STATUS.DISABLED").get();
    }

    @Override
    public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {
        if (confirmGuiType.equals(ConfirmGuiType.HOLOGRAM_DELETE)) {
            hologram.deleteHologram(true);

            update();
            GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).update();

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.HOLOGRAM.HOLOGRAM-DELETED").replace("%hologram%", hologram.getName()));
        }
    }

}
