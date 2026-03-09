package dev.nandi0813.practice.manager.gui.guis.queue;

import dev.nandi0813.practice.ZonePractice;
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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class QueueSelectorGui extends GUI {

    /** Interval between real-time count updates, in ticks (2 seconds). */
    private static final long TICK_INTERVAL = 40L;

    private final long UPDATE_COOLDOWN_MS =
            this.getUpdateCooldownMinutes() < 0 ? 0 : this.getUpdateCooldownMinutes() * 60 * 1000L;

    // slot → ladder mapping, used for click-handling and live updates
    private final Map<Integer, NormalLadder> firstCategoryLadderSlots = new HashMap<>();
    private final Map<Integer, NormalLadder> secondCategoryLadderSlots = new HashMap<>();

    /**
     * Stores the unresolved template lore for each active ladder slot so the
     * real-time ticker can apply fresh %in_queue% / %in_fight% values without
     * rebuilding the entire inventory layout.
     *
     * key   = pageId (1 or 2)
     * value = map of slot → template ItemStack (with un-replaced placeholders)
     */
    private final Map<Integer, Map<Integer, ItemStack>> templateItems = new HashMap<>();

    private long lastUpdateTime = -1L;
    private BukkitTask tickerTask = null;

    public QueueSelectorGui(GUIType type) {
        super(type);
        build();
    }

    // -------------------------------------------------------------------------
    // Abstract API
    // -------------------------------------------------------------------------

    protected abstract long getUpdateCooldownMinutes();
    protected abstract String getQueueConfigPath();
    protected abstract String getGuiConfigPath();
    protected abstract WeightClass getWeightClass();
    protected abstract boolean isRanked();
    protected abstract boolean isValidLadder(NormalLadder ladder);
    protected abstract void onLadderClick(Player player, NormalLadder ladder);

    // -------------------------------------------------------------------------
    // Build / update lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void build() {
        doUpdate();
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();
        if (lastUpdateTime >= 0 && (now - lastUpdateTime) < UPDATE_COOLDOWN_MS) {
            return;
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
                queuePath + ".FIRST-CATEGORY.LADDER-SLOTS",
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
                    queuePath + ".SECOND-CATEGORY.LADDER-SLOTS",
                    secondCategoryLadderSlots,
                    ConfigManager.getInt(queuePath + ".SECOND-CATEGORY.BACK-TO-FIRST-CATEGORY-SLOT"),
                    GUIFile.getGuiItem(guiPath + ".SECOND-CATEGORY.ICONS.GO-BACK-TO-FIRST-CATEGORY").get()
            );
        }

        updatePlayers();
    }

    // -------------------------------------------------------------------------
    // Real-time ticker — only runs while at least one player has the GUI open
    // -------------------------------------------------------------------------

    /**
     * Called when the first player opens the GUI; starts the async ticker that
     * refreshes %in_queue% / %in_fight% counts every {@value #TICK_INTERVAL} ticks.
     */
    private void startTicker() {
        if (tickerTask != null) return;

        tickerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(ZonePractice.getInstance(), () -> {
            if (inGuiPlayers.isEmpty()) {
                stopTicker();
                return;
            }
            tickUpdate();
        }, TICK_INTERVAL, TICK_INTERVAL);
    }

    private void stopTicker() {
        if (tickerTask != null) {
            tickerTask.cancel();
            tickerTask = null;
        }
    }

    /**
     * Lightweight async tick: updates only the dynamic data (queue/fight counts,
     * item amount) for every ladder slot already placed in the inventory.
     * Does NOT rebuild the layout or re-read config.
     * The stored template already has static LB lore resolved — only
     * %in_queue% and %in_fight% are substituted here each tick.
     */
    private void tickUpdate() {
        for (Map.Entry<Integer, Map<Integer, ItemStack>> pageEntry : templateItems.entrySet()) {
            int pageId = pageEntry.getKey();
            Inventory inventory = gui.get(pageId);
            if (inventory == null) continue;

            Map<Integer, NormalLadder> slotMap = (pageId == 1) ? firstCategoryLadderSlots : secondCategoryLadderSlots;

            for (Map.Entry<Integer, ItemStack> slotEntry : pageEntry.getValue().entrySet()) {
                int slot = slotEntry.getKey();
                ItemStack template = slotEntry.getValue();
                NormalLadder ladder = slotMap.get(slot);
                if (ladder == null || template == null) continue;

                int inQueue = QueueManager.getInstance().getQueueSize(ladder, isRanked());
                int inFight = MatchManager.getInstance().getDuelMatchSize(ladder, isRanked());

                ItemStack updated = template.clone();
                ItemMeta meta = updated.getItemMeta();
                if (meta == null) continue;

                // Replace dynamic placeholders in display name
                if (meta.hasDisplayName()) {
                    meta.setDisplayName(meta.getDisplayName()
                            .replace("%in_queue%", String.valueOf(inQueue))
                            .replace("%in_fight%", String.valueOf(inFight)));
                }

                // Replace dynamic placeholders in lore
                List<String> lore = meta.getLore();
                if (lore != null) {
                    List<String> newLore = new ArrayList<>();
                    for (String line : lore) {
                        newLore.add(line
                                .replace("%in_queue%", String.valueOf(inQueue))
                                .replace("%in_fight%", String.valueOf(inFight)));
                    }
                    meta.setLore(newLore);
                }

                updated.setItemMeta(meta);
                updated.setAmount(inFight > 0 && inFight <= 64 ? inFight : 1);

                inventory.setItem(slot, updated);
            }
        }

        // Push the changes to viewers on the main thread
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), this::updatePlayers);
    }

    // -------------------------------------------------------------------------
    // Open / close — start/stop ticker accordingly
    // -------------------------------------------------------------------------

    @Override
    public void open(Player player, int page) {
        super.open(player, page);
        // Delay matches the 2L delay in GUI.open so we start only after inGuiPlayers is set
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), this::startTicker, 3L);
    }

    @Override
    public void close(Player player) {
        super.close(player);
        if (inGuiPlayers.isEmpty()) {
            stopTicker();
        }
    }

    // -------------------------------------------------------------------------
    // Inventory setup
    // -------------------------------------------------------------------------

    private void setupCategoryPage(int pageId, String title, int configSize, ItemStack filler, GUIItem ladderTemplate,
                                   String ladderConfigPath, Map<Integer, NormalLadder> slotMap, int navSlot, ItemStack navItem) {

        this.gui.put(pageId, InventoryUtil.createInventory(
                title.replace("%weight_class%", getWeightClass().getName()),
                configSize
        ));

        Inventory inventory = gui.get(pageId);
        inventory.clear();
        slotMap.clear();

        Map<Integer, ItemStack> pageTemplates = new HashMap<>();
        templateItems.put(pageId, pageTemplates);

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
                continue;
            }

            // Apply live counts for the initial render
            updateIconWithQueueData(ladder, icon, guiPath);

            ItemStack renderedItem = icon.get();
            slotMap.put(slot, ladder);
            inventory.setItem(slot, renderedItem);

            // Store a raw template (placeholders intact, no live counts) for the ticker.
            GUIItem rawTemplate = ladderTemplate.cloneItem();
            rawTemplate.replace("%ladder%", ladder.getDisplayName());
            if (ladder.getIcon() != null) rawTemplate.setBaseItem(ladder.getIcon());
            // Apply only the LB lore (which is static) but NOT %in_queue%/%in_fight%
            String lbFormat = GUIFile.getString(guiPath + ".LB-FORMAT");
            rawTemplate.setLore(QueueGuiUtil.replaceLore(lbFormat, rawTemplate.getLore(), ladder));
            rawTemplate.replace("%weight_class%", getWeightClass().getName());
            rawTemplate.setAmount(1);
            pageTemplates.put(slot, rawTemplate.get());
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

        icon.replace("%weight_class%", getWeightClass().getName());

        if (duelMatchSize > 0 && duelMatchSize <= 64) {
            icon.setAmount(duelMatchSize);
        } else {
            icon.setAmount(1);
        }
    }

    // -------------------------------------------------------------------------
    // Click handling
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<NormalLadder, Integer> getTempLadderSlots(final String path, int size) {
        final Map<NormalLadder, Integer> tempLadderSlots = new LinkedHashMap<>();

        for (String entry : ConfigManager.getList(path)) {
            String[] parts = entry.split("::");
            if (parts.length != 2) continue;

            String ladderName = parts[0].trim();
            int slot;
            try {
                slot = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                continue;
            }

            NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);

            if (ladder != null && isValidLadder(ladder) && ladder.getMatchTypes().contains(MatchType.DUEL)) {

                if (slot >= 0 && slot < size) {
                    tempLadderSlots.put(ladder, slot);
                }
            }
        }
        return tempLadderSlots;
    }
}




