package dev.nandi0813.practice.manager.gui.guis.queue;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.queue.QueueManager;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class RankedGui extends GUI {

    private static final boolean SHOW_DISABLED_LADDERS = ConfigManager.getBoolean("QUEUE.UNRANKED.SELECTOR-GUI.SHOW-DISABLED-LADDERS");
    private static final boolean SECOND_CATEGORY_ENABLED = ConfigManager.getBoolean("QUEUE.RANKED.SELECTOR-GUI.SECOND-CATEGORY.ENABLED");

    // First page
    private static final String FIRST_CATEGORY_TITLE = GUIFile.getString("GUIS.RANKED-GUI.FIRST-CATEGORY.TITLE");
    private static final int FIRST_CATEGORY_SIZE = ConfigManager.getInt("QUEUE.RANKED.SELECTOR-GUI.FIRST-CATEGORY.SIZE");
    private static final ItemStack FIRST_CATEGORY_FILLER_ITEM = GUIFile.getGuiItem("GUIS.RANKED-GUI.FIRST-CATEGORY.ICONS.FILLER-ITEM").get();
    private static final int GO_TO_SECOND_CATEGORY_SLOT = ConfigManager.getInt("QUEUE.RANKED.SELECTOR-GUI.FIRST-CATEGORY.GO-TO-SECOND-CATEGORY-SLOT");
    private static final ItemStack GO_TO_SECOND_CATEGORY_ITEM = GUIFile.getGuiItem("GUIS.RANKED-GUI.FIRST-CATEGORY.ICONS.GO-TO-SECOND-CATEGORY").get();
    private static final GUIItem FIRST_CATEGORY_LADDER_ITEM = GUIFile.getGuiItem("GUIS.RANKED-GUI.FIRST-CATEGORY.ICONS.LADDER");

    // Second page
    private static final String SECOND_CATEGORY_TITLE = GUIFile.getString("GUIS.RANKED-GUI.SECOND-CATEGORY.TITLE");
    private static final int SECOND_CATEGORY_SIZE = ConfigManager.getInt("QUEUE.RANKED.SELECTOR-GUI.SECOND-CATEGORY.SIZE");
    private static final ItemStack SECOND_CATEGORY_FILLER_ITEM = GUIFile.getGuiItem("GUIS.RANKED-GUI.SECOND-CATEGORY.ICONS.FILLER-ITEM").get();
    private static final int BACK_TO_FIRST_CATEGORY_SLOT = ConfigManager.getInt("QUEUE.RANKED.SELECTOR-GUI.SECOND-CATEGORY.BACK-TO-FIRST-CATEGORY-SLOT");
    private static final ItemStack BACK_TO_FIRST_CATEGORY_ITEM = GUIFile.getGuiItem("GUIS.RANKED-GUI.SECOND-CATEGORY.ICONS.GO-BACK-TO-FIRST-CATEGORY").get();
    private static final GUIItem SECOND_CATEGORY_LADDER_ITEM = GUIFile.getGuiItem("GUIS.RANKED-GUI.SECOND-CATEGORY.ICONS.LADDER");

    // Contains both first and second page
    private static final GUIItem FROZEN_LADDER_ITEM = GUIFile.getGuiItem("GUIS.RANKED-GUI.ICONS.FROZEN-LADDER-ITEM");
    private static final GUIItem DISABLED_LADDER_ITEM = GUIFile.getGuiItem("GUIS.RANKED-GUI.ICONS.DISABLED-LADDER-ITEM");

    private static final String LB_FORMAT = GUIFile.getString("GUIS.RANKED-GUI.LB-FORMAT");

    private final Map<Integer, NormalLadder> firstCategoryLadderSlots = new HashMap<>();
    private final Map<Integer, NormalLadder> secondCategoryLadderSlots = new HashMap<>();

    public RankedGui() {
        super(GUIType.Queue_Ranked);

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        createFirstCategoryGui();

        if (SECOND_CATEGORY_ENABLED)
            createSecondCategoryGui();

        updatePlayers();
    }

    private void createFirstCategoryGui() {
        this.gui.put(1, InventoryUtil.createInventory(
                FIRST_CATEGORY_TITLE.replaceAll("%weight_class%", WeightClass.RANKED.getName()),
                FIRST_CATEGORY_SIZE
        ));

        Inventory inventory = gui.get(1);
        inventory.clear();
        firstCategoryLadderSlots.clear();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, FIRST_CATEGORY_FILLER_ITEM);
        }

        if (SECOND_CATEGORY_ENABLED) {
            inventory.setItem(GO_TO_SECOND_CATEGORY_SLOT, GO_TO_SECOND_CATEGORY_ITEM);
        }

        for (Map.Entry<NormalLadder, Integer> entry : getTempLadderSlots("QUEUE.RANKED.SELECTOR-GUI.FIRST-CATEGORY.LADDERS", inventory.getSize()).entrySet()) {
            final NormalLadder ladder = entry.getKey();
            final int slot = entry.getValue();

            GUIItem icon;
            if (ladder.isEnabled() || SHOW_DISABLED_LADDERS) {
                if (!ladder.isFrozen()) {
                    icon = FIRST_CATEGORY_LADDER_ITEM.cloneItem();

                    int duelMatchSize = MatchManager.getInstance().getDuelMatchSize(ladder, true);
                    icon.replaceAll("%in_queue%", String.valueOf(QueueManager.getInstance().getQueueSize(ladder, true)))
                            .replaceAll("%in_fight%", String.valueOf(duelMatchSize))
                            .replaceAll("%weight_class%", WeightClass.RANKED.getName());

                    icon.setLore(QueueGuiUtil.replaceLore(LB_FORMAT, icon.getLore(), ladder));

                    if (duelMatchSize > 0 && duelMatchSize <= 64)
                        icon.setAmount(duelMatchSize);
                    else
                        icon.setAmount(1);
                } else {
                    icon = FROZEN_LADDER_ITEM.cloneItem();
                }
            } else {
                icon = DISABLED_LADDER_ITEM.cloneItem();
            }

            icon.replaceAll("%ladder%", ladder.getDisplayName());

            if (icon.getMaterial() == null) {
                icon.setMaterial(ladder.getIcon().getType());
                icon.setDamage(ladder.getIcon().getDurability());
            }

            firstCategoryLadderSlots.put(slot, ladder);
            inventory.setItem(slot, icon.get());
        }
    }

    private void createSecondCategoryGui() {
        this.gui.put(2, InventoryUtil.createInventory(
                SECOND_CATEGORY_TITLE.replaceAll("%weight_class%", WeightClass.RANKED.getName()),
                SECOND_CATEGORY_SIZE));

        Inventory inventory = gui.get(2);
        inventory.clear();
        secondCategoryLadderSlots.clear();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, SECOND_CATEGORY_FILLER_ITEM);
        }

        inventory.setItem(BACK_TO_FIRST_CATEGORY_SLOT, BACK_TO_FIRST_CATEGORY_ITEM);

        for (Map.Entry<NormalLadder, Integer> entry : getTempLadderSlots("QUEUE.RANKED.SELECTOR-GUI.SECOND-CATEGORY.LADDERS", inventory.getSize()).entrySet()) {
            final NormalLadder ladder = entry.getKey();
            final int slot = entry.getValue();

            GUIItem icon;
            if (ladder.isEnabled()) {
                if (!ladder.isFrozen()) {
                    icon = SECOND_CATEGORY_LADDER_ITEM.cloneItem();

                    int duelMatchSize = MatchManager.getInstance().getDuelMatchSize(ladder, true);
                    icon.replaceAll("%in_queue%", String.valueOf(QueueManager.getInstance().getQueueSize(ladder, true)))
                            .replaceAll("%in_fight%", String.valueOf(duelMatchSize))
                            .replaceAll("%weight_class%", WeightClass.RANKED.getName());

                    icon.setLore(QueueGuiUtil.replaceLore(LB_FORMAT, icon.getLore(), ladder));

                    if (duelMatchSize > 0 && duelMatchSize <= 64)
                        icon.setAmount(duelMatchSize);
                    else
                        icon.setAmount(1);
                } else {
                    icon = FROZEN_LADDER_ITEM.cloneItem();
                }
            } else {
                icon = DISABLED_LADDER_ITEM.cloneItem();
            }

            icon.replaceAll("%ladder%", ladder.getDisplayName());

            if (icon.getMaterial() == null) {
                icon.setMaterial(ladder.getIcon().getType());
                icon.setDamage(ladder.getIcon().getDurability());
            }

            secondCategoryLadderSlots.put(slot, ladder);
            inventory.setItem(slot, icon.get());
        }
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        InventoryView inventoryView = e.getView();
        ItemStack item = e.getCurrentItem();
        int slot = e.getRawSlot();
        int page = this.getInGuiPlayers().get(player);

        e.setCancelled(true);

        if (item == null || item.getType().equals(Material.AIR)) return;
        if (inventoryView.getTopInventory().getSize() <= slot) return;

        if (page == 1) {
            if (slot == GO_TO_SECOND_CATEGORY_SLOT) {
                this.open(player, 2);
                return;
            }

            if (!firstCategoryLadderSlots.containsKey(slot)) return;

            NormalLadder ladder = firstCategoryLadderSlots.get(slot);
            QueueManager.getInstance().createRankedQueue(player, ladder);
        } else if (page == 2) {
            if (slot == BACK_TO_FIRST_CATEGORY_SLOT) {
                this.open(player, 1);
                return;
            }

            if (!secondCategoryLadderSlots.containsKey(slot)) return;

            NormalLadder ladder = secondCategoryLadderSlots.get(slot);
            QueueManager.getInstance().createRankedQueue(player, ladder);
        }
    }

    private static Map<NormalLadder, Integer> getTempLadderSlots(final String path, int size) {
        final Map<NormalLadder, Integer> tempLadderSlots = new HashMap<>();

        for (String ladderName : ConfigManager.getConfigList(path)) {
            NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);
            if (ladder != null && ladder.isRanked() && ladder.getMatchTypes().contains(MatchType.DUEL)) {
                int slot = ConfigManager.getInt(path + "." + ladderName);

                if (slot > 0 || slot < size)
                    tempLadderSlots.put(ladder, slot);
            }
        }
        return tempLadderSlots;
    }

}
