package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public class CustomKitGui extends GUI {

    private final NormalLadder ladder;
    private final boolean ranked;

    public CustomKitGui(NormalLadder ladder, boolean ranked) {
        super(GUIType.Ladder_Main);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.LADDER.CUSTOM-KIT.TITLE").replace("%ladder%", ladder.getName()), 6));
        this.ladder = ladder;
        this.ranked = ranked;

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

            inventory.setItem(0, GUIFile.getGuiItem("GUIS.SETUP.LADDER.CUSTOM-KIT.ICONS.GO-BACK").get());
            inventory.setItem(6, GUIFile.getGuiItem("GUIS.SETUP.LADDER.CUSTOM-KIT.ICONS.SAVE").get());
            inventory.setItem(7, GUIFile.getGuiItem("GUIS.SETUP.LADDER.CUSTOM-KIT.ICONS.LOAD").get());
            inventory.setItem(8, GUIFile.getGuiItem("GUIS.SETUP.LADDER.CUSTOM-KIT.ICONS.CANCEL").get());
            inventory.setItem(2, getRankedItem(ladder, ranked));

            // Frame
            for (int i : new int[]{1, 3, 4, 5, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 28, 37, 46})
                inventory.setItem(i, GUIManager.getFILLER_ITEM());

            // Set Armor content
            if (ladder.getKitData().getArmor() != null) {
                List<ItemStack> armorContent = new ArrayList<>(Arrays.asList(ladder.getKitData().getArmor()));
                for (int i : new int[]{18, 27, 36, 45}) {
                    if (armorContent.get(Math.abs(i / 9 - 5)) != null)
                        inventory.setItem(i, armorContent.get(Math.abs(i / 9 - 5)));
                    else
                        inventory.setItem(i, GUIManager.getDUMMY_ITEM());
                }
            }

            // Set inventory content
            if (ladder.getCustomKitExtraItems().get(ranked) != null) {
                for (ItemStack item : ladder.getCustomKitExtraItems().get(ranked)) {
                    if (item != null)
                        inventory.setItem(inventory.firstEmpty(), item);
                    else
                        inventory.setItem(inventory.firstEmpty(), GUIManager.getDUMMY_ITEM());
                }
            }

            inventory.remove(GUIManager.getDUMMY_ITEM());

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();

        if (inventory.getSize() <= slot) return;

        if (Objects.equals(e.getCurrentItem(), GUIManager.getFILLER_ITEM()) || slot == 0 || slot == 2 || slot == 6 || slot == 7 || slot == 8 || slot == 18 || slot == 27 || slot == 36 || slot == 45) {
            e.setCancelled(true);

            if (slot == 0) {
                LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Inventory).open(player, 1);
            } else if (slot == 2) {
                switch (ladder.getWeightClass()) {
                    case UNRANKED:
                    case RANKED:
                        return;
                    case UNRANKED_AND_RANKED:
                        if (ranked)
                            LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_CustomKitExtra_unRanked).open(player);
                        else
                            LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_CustomKitExtra_Ranked).open(player);
                        break;
                }
            }
        } else {
            if (ladder.isEnabled()) {
                e.setCancelled(true);
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-EDIT-ENABLED"));
            }
        }
    }

    public void handleCloseEvent(InventoryCloseEvent e) {
        if (LadderManager.getInstance().getLadders().contains(ladder) && !ladder.isEnabled())
            this.save();
    }

    public void save() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 20; i <= 26; i++) items.add(gui.get(1).getItem(i));
        for (int i = 29; i <= 35; i++) items.add(gui.get(1).getItem(i));
        for (int i = 38; i <= 44; i++) items.add(gui.get(1).getItem(i));
        for (int i = 47; i <= 53; i++) items.add(gui.get(1).getItem(i));

        ladder.getCustomKitExtraItems().put(ranked, items.toArray(new ItemStack[0]));
    }


    private static @Nullable ItemStack getRankedItem(NormalLadder ladder, boolean ranked) {
        switch (ladder.getWeightClass()) {
            case UNRANKED:
                return GUIFile.getGuiItem("GUIS.SETUP.LADDER.CUSTOM-KIT.ICONS.SWITCH-WEIGHTCLASS.ONLY-UNRANKED")
                        .replaceAll("%weightClass%", WeightClass.UNRANKED.getName())
                        .get();
            case RANKED:
                return GUIFile.getGuiItem("GUIS.SETUP.LADDER.CUSTOM-KIT.ICONS.SWITCH-WEIGHTCLASS.ONLY-RANKED")
                        .replaceAll("%weightClass%", WeightClass.RANKED.getName())
                        .get();
            case UNRANKED_AND_RANKED:
                if (ranked)
                    return GUIFile.getGuiItem("GUIS.SETUP.LADDER.CUSTOM-KIT.ICONS.SWITCH-WEIGHTCLASS.SWITCH-TO-UNRANKED")
                            .replaceAll("%weightClass%", WeightClass.UNRANKED.getName())
                            .get();
                else
                    return GUIFile.getGuiItem("GUIS.SETUP.LADDER.CUSTOM-KIT.ICONS.SWITCH-WEIGHTCLASS.SWITCH-TO-RANKED")
                            .replaceAll("%weightClass%", WeightClass.RANKED.getName())
                            .get();
        }

        return GUIManager.getFILLER_ITEM();
    }

}
