package dev.nandi0813.practice.manager.gui.guis.ladder;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class LadderCreateGui extends GUI {

    private final String ladderName;
    @Getter
    private final Map<Integer, LadderType> typeSlots = new HashMap<>();

    public LadderCreateGui(String ladderName) {
        super(GUIType.Ladder_Create);
        this.ladderName = ladderName;
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.LADDER-CREATE.TITLE").replace("%ladder%", ladderName), 4));

        build();
    }

    @Override
    public void build() {
        this.update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 22, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35})
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        for (LadderType type : LadderType.values()) {
            ItemStack item = ClassImport.getClasses().getItemCreateUtil().createItem("&e" + type.getName(), type.getIcon());
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setLore(StringUtil.CC(type.getDescription()));
            item.setItemMeta(itemMeta);

            int slot = inventory.firstEmpty();
            typeSlots.put(slot, type);

            inventory.setItem(slot, item);
        }
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (!typeSlots.containsKey(slot)) return;

        createLadder(player, typeSlots.get(slot));
    }

    private void createLadder(Player player, LadderType type) {
        try {
            NormalLadder ladder = (NormalLadder) type.getClassInstance().getConstructor(String.class, LadderType.class).newInstance(ladderName, type);

            ladder.setData();

            LadderManager.getInstance().getLadders().add(ladder);
            LadderManager.getInstance().getLadders().sort(Comparator.comparing(Ladder::getName));

            LadderSetupManager.getInstance().buildLadderSetupGUIs(ladder);
            GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).update();
            GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();

            Common.sendMMMessage(player, LanguageManager.getString("LADDER.CREATE.LADDER-CREATED").replaceAll("%ladder%", ladderName));

            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                    LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).open(player), 3L);
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Error. Please contact us on discord.");
        }
    }

}
