package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.type.SkyWars;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public class SkyWarsLootGui extends GUI {

    private final SkyWars ladder;

    public SkyWarsLootGui(SkyWars ladder) {
        super(GUIType.Ladder_SkyWarsLoot);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.LADDER.SKYWARS-LOOT.TITLE").replace("%ladder%", ladder.getName()), 6));
        this.ladder = ladder;

        build();
    }

    @Override
    public void build() {
        // Set filler items
        for (int i : new int[]{46, 47, 48, 49, 50, 51, 52, 53})
            gui.get(1).setItem(i, GUIManager.getFILLER_ITEM());

        // Navigation item
        gui.get(1).setItem(45, GUIFile.getGuiItem("GUIS.SETUP.LADDER.SKYWARS-LOOT.ICONS.GO-BACK").get());

        update();
    }

    @Override
    public void update() {
        if (ladder.getSkyWarsLoot() != null) {
            List<ItemStack> content = new ArrayList<>(Arrays.asList(ladder.getSkyWarsLoot()));
            for (int i = 0; i < 45; i++)
                gui.get(1).setItem(i, content.get(i));
        }

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();

        if (inventory.getSize() > slot) {
            if (Objects.equals(e.getCurrentItem(), GUIManager.getFILLER_ITEM()) || slot == 45) {
                e.setCancelled(true);
                if (slot == 45)
                    LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Settings).open(player);
            } else {
                if (ladder.isEnabled()) {
                    e.setCancelled(true);
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-EDIT-ENABLED"));
                }
            }
        }
    }

    public void handleCloseEvent(InventoryCloseEvent e) {
        if (LadderManager.getInstance().getLadders().contains(ladder) && !ladder.isEnabled())
            this.save();
    }

    public void save() {
        List<ItemStack> content = new ArrayList<>();

        for (int i = 0; i < 45; i++)
            content.add(gui.get(1).getItem(i));

        ladder.setSkyWarsLoot(content.toArray(new ItemStack[0]));
    }

}
