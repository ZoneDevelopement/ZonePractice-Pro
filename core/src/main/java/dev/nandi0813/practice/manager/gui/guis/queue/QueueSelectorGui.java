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

public abstract class QueueSelectorGui extends GUI {

    private final long UPDATE_COOLDOWN_MS =
            this.getUpdateCooldownMinutes() < 0 ? 0 : this.getUpdateCooldownMinutes() * 60 * 1000L;

    private final Map<Integer, NormalLadder> firstCategoryLadderSlots = new HashMap<>();
    private final Map<Integer, NormalLadder> secondCategoryLadderSlots = new HashMap<>();

    private long lastUpdateTime = -1L;

    public QueueSelectorGui(GUIType type) {
        super(type);
        build();
    }

    protected abstract long getUpdateCooldownMinutes();

    protected abstract String getQueueConfigPath();

    protected abstract String getGuiConfigPath();

    protected abstract WeightClass getWeightClass();

    protected abstract boolean isRanked();

    protected abstract boolean isValidLadder(NormalLadder ladder);

    protected abstract void onLadderClick(Player player, NormalLadder ladder);

    @Override
    public void build() {
        doUpdate();
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();
        if (lastUpdateTime >= 0 && (now - lastUpdateTime) < UPDATE_COOLDOWN_MS) {
            return; // Still within the 5-minute cooldown, skip update
        }
        doUpdate();
    }

    private void doUpdate() {
        lastUpdateTime = System.currentTimeMillis();
        String queuePath = getQueueConfigPath() + ".SELECTOR-GUI";
        String guiPath = getGuiConfigPath();

        boolean secondCategoryEnabled = ConfigManager.getBoolean(queuePath + ".SECOND-CATEGORY.ENABLED");

        setupCategoryPage(
                1,
                GUIFile.getString(guiPath + ".FIRST-CATEGORY.TITLE"),
                ConfigManager.getInt(queuePath + ".FIRST-CATEGORY.SIZE"),
                GUIFile.getGuiItem(guiPath + ".FIRST-CATEGORY.ICONS.FILLER-ITEM").get(),
                GUIFile.getGuiItem(guiPath + ".FIRST-CATEGORY.ICONS.LADDER"),
                queuePath + ".FIRST-CATEGORY.LADDERS",
                firstCategoryLadderSlots,
                secondCategoryEnabled ? ConfigManager.getInt(queuePath + ".FIRST-CATEGORY.GO-TO-SECOND-CATEGORY-SLOT") : -1,
                secondCategoryEnabled ? GUIFile.getGuiItem(guiPath + ".FIRST-CATEGORY.ICONS.GO-TO-SECOND-CATEGORY").get() : null
        );

        if (secondCategoryEnabled) {
            setupCategoryPage(
                    2,
                    GUIFile.getString(guiPath + ".SECOND-CATEGORY.TITLE"),
                    ConfigManager.getInt(queuePath + ".SECOND-CATEGORY.SIZE"),
                    GUIFile.getGuiItem(guiPath + ".SECOND-CATEGORY.ICONS.FILLER-ITEM").get(),
                    GUIFile.getGuiItem(guiPath + ".SECOND-CATEGORY.ICONS.LADDER"),
                    queuePath + ".SECOND-CATEGORY.LADDERS",
                    secondCategoryLadderSlots,
                    ConfigManager.getInt(queuePath + ".SECOND-CATEGORY.BACK-TO-FIRST-CATEGORY-SLOT"),
                    GUIFile.getGuiItem(guiPath + ".SECOND-CATEGORY.ICONS.GO-BACK-TO-FIRST-CATEGORY").get()
            );
        }

        updatePlayers();
    }

    private void setupCategoryPage(int pageId, String title, int configSize, ItemStack filler, GUIItem ladderTemplate,
                                   String ladderConfigPath, Map<Integer, NormalLadder> slotMap, int navSlot, ItemStack navItem) {

        this.gui.put(pageId, InventoryUtil.createInventory(
                title.replace("%weight_class%", getWeightClass().getName()),
                configSize
        ));

        Inventory inventory = gui.get(pageId);
        inventory.clear();
        slotMap.clear();

        int actualSize = inventory.getSize();

        for (int i = 0; i < actualSize; i++) {
            inventory.setItem(i, filler);
        }

        if (navSlot >= 0 && navItem != null && navSlot < actualSize) {
            inventory.setItem(navSlot, navItem);
        }

        Map<NormalLadder, Integer> ladders = getTempLadderSlots(ladderConfigPath, actualSize);
        String guiPath = getGuiConfigPath();

        for (Map.Entry<NormalLadder, Integer> entry : ladders.entrySet()) {
            final NormalLadder ladder = entry.getKey();
            final int slot = entry.getValue();

            GUIItem icon = getLadderIcon(ladder, ladderTemplate, guiPath);

            if (icon == null) continue;

            icon.replace("%ladder%", ladder.getDisplayName());

            if (ladder.getIcon() != null) {
                icon.setBaseItem(ladder.getIcon());
            } else if (icon.getMaterial() == null) {
                // No icon set and template has no material - skip
                continue;
            }

            slotMap.put(slot, ladder);
            inventory.setItem(slot, icon.get());
        }
    }

    private GUIItem getLadderIcon(NormalLadder ladder, GUIItem template, String guiPath) {
        boolean showDisabled = ConfigManager.getBoolean(getQueueConfigPath() + ".SELECTOR-GUI.SHOW-DISABLED-LADDERS");

        if (ladder.isEnabled()) {
            if (!ladder.isFrozen()) {
                GUIItem activeIcon = template.cloneItem();
                updateIconWithQueueData(ladder, activeIcon, guiPath);
                return activeIcon;
            } else {
                return GUIFile.getGuiItem(guiPath + ".ICONS.FROZEN-LADDER-ITEM").cloneItem();
            }
        }

        if (showDisabled) {
            return GUIFile.getGuiItem(guiPath + ".ICONS.DISABLED-LADDER-ITEM").cloneItem();
        }

        return null;
    }

    private void updateIconWithQueueData(NormalLadder ladder, GUIItem icon, String guiPath) {
        int duelMatchSize = MatchManager.getInstance().getDuelMatchSize(ladder, isRanked());

        icon.replace("%in_queue%", String.valueOf(QueueManager.getInstance().getQueueSize(ladder, isRanked())))
                .replace("%in_fight%", String.valueOf(duelMatchSize))
                .replace("%weight_class%", getWeightClass().getName());

        String lbFormat = GUIFile.getString(guiPath + ".LB-FORMAT");
        icon.setLore(QueueGuiUtil.replaceLore(lbFormat, icon.getLore(), ladder));

        if (duelMatchSize > 0 && duelMatchSize <= 64) {
            icon.setAmount(duelMatchSize);
        } else {
            icon.setAmount(1);
        }
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        InventoryView inventoryView = e.getView();
        ItemStack item = e.getCurrentItem();
        int rawSlot = e.getRawSlot();

        if (!this.getInGuiPlayers().containsKey(player)) return;
        int page = this.getInGuiPlayers().get(player);

        e.setCancelled(true);

        if (item == null || item.getType() == Material.AIR) return;
        if (rawSlot >= inventoryView.getTopInventory().getSize()) return;

        String queuePath = getQueueConfigPath() + ".SELECTOR-GUI";
        boolean secondCategoryEnabled = ConfigManager.getBoolean(queuePath + ".SECOND-CATEGORY.ENABLED");
        int nextSlot = ConfigManager.getInt(queuePath + ".FIRST-CATEGORY.GO-TO-SECOND-CATEGORY-SLOT");
        int prevSlot = ConfigManager.getInt(queuePath + ".SECOND-CATEGORY.BACK-TO-FIRST-CATEGORY-SLOT");

        if (page == 1 && rawSlot == nextSlot && secondCategoryEnabled) {
            this.open(player, 2);
            return;
        }
        if (page == 2 && rawSlot == prevSlot) {
            this.open(player, 1);
            return;
        }

        Map<Integer, NormalLadder> currentMap = (page == 1) ? firstCategoryLadderSlots : secondCategoryLadderSlots;

        if (currentMap.containsKey(rawSlot)) {
            onLadderClick(player, currentMap.get(rawSlot));
        }
    }

    private Map<NormalLadder, Integer> getTempLadderSlots(final String path, int size) {
        final Map<NormalLadder, Integer> tempLadderSlots = new HashMap<>();

        for (String ladderName : ConfigManager.getConfigList(path)) {
            NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);

            if (ladder != null && isValidLadder(ladder) && ladder.getMatchTypes().contains(MatchType.DUEL)) {
                int slot = ConfigManager.getInt(path + "." + ladderName);

                if (slot >= 0 && slot < size) {
                    tempLadderSlots.put(ladder, slot);
                }
            }
        }
        return tempLadderSlots;
    }
}