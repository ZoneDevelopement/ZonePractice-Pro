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
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class QueueSelectorGui extends GUI {

    /** Interval between real-time count updates, in ticks (2 seconds). */
    private static final long TICK_INTERVAL = 40L;
    private static final int DEFAULT_QUICK_MATCH_SLOT = 45;

    private final long UPDATE_COOLDOWN_MS =
            this.getUpdateCooldownMinutes() < 0 ? 0 : this.getUpdateCooldownMinutes() * 60 * 1000L;

    // page -> slot -> ladder mapping, used for click-handling and live updates
    private final Map<Integer, Map<Integer, NormalLadder>> pageLadderSlots = new HashMap<>();
    private final Map<Integer, Map<Integer, ItemStack>> templateItems = new HashMap<>();
    private final Map<Integer, Integer> selectorSlotToPage = new HashMap<>();

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

    protected void decoratePage(int pageId, Inventory inventory) {
    }

    protected boolean handleCustomTopInventoryClick(Player player, int rawSlot, InventoryView inventoryView, ItemStack item) {
        return false;
    }

    // -------------------------------------------------------------------------
    // Build / update lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void build() {
        doUpdate();
    }

    @Override
    public void update() {
        update(false);
    }

    @Override
    public boolean update(boolean forceRefresh) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> update(forceRefresh));
            return false;
        }

        long now = System.currentTimeMillis();
        if (!forceRefresh && lastUpdateTime >= 0 && (now - lastUpdateTime) < UPDATE_COOLDOWN_MS) {
            return false;
        }

        doUpdate();
        return true;
    }

    private void doUpdate() {
        lastUpdateTime = System.currentTimeMillis();
        String queuePath = getQueueConfigPath() + ".SELECTOR-GUI";
        String guiPath = getGuiConfigPath();

        Map<Integer, Inventory> existingInventories = new HashMap<>(this.gui);

        this.gui.clear();
        this.pageLadderSlots.clear();
        this.templateItems.clear();
        this.selectorSlotToPage.clear();

        List<CategoryConfig> categories = loadCategories(queuePath);
        if (categories.isEmpty()) {
            return;
        }

        int rows = Math.clamp(ConfigManager.getInt(queuePath + ".SIZE"), 1, 6);
        ItemStack filler = GUIFile.getGuiItem(guiPath + ".ICONS.FILLER-ITEM").get();
        if (filler == null) {
            filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        GUIItem ladderTemplate = GUIFile.getGuiItem(guiPath + ".ICONS.LADDER");

        for (int i = 0; i < categories.size(); i++) {
            int pageId = i + 1;
            CategoryConfig category = categories.get(i);
            selectorSlotToPage.put(category.selectorSlot(), pageId);
        }

        for (int i = 0; i < categories.size(); i++) {
            int pageId = i + 1;
            setupCategoryPage(pageId, rows, filler, ladderTemplate, categories.get(i), categories, guiPath, existingInventories);
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

        tickerTask = Bukkit.getScheduler().runTaskTimer(ZonePractice.getInstance(), () -> {
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
            Map<Integer, NormalLadder> slotMap = pageLadderSlots.get(pageId);
            if (slotMap == null) continue;

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
                    String replaced = Common.serializeComponentToLegacyString(meta.displayName())
                            .replace("%in_queue%", String.valueOf(inQueue))
                            .replace("%in_fight%", String.valueOf(inFight));
                    meta.displayName(Common.legacyToComponent(replaced));
                }

                // Replace dynamic placeholders in lore
                List<net.kyori.adventure.text.Component> lore = meta.lore();
                if (lore != null) {
                    List<String> newLore = new ArrayList<>();
                    for (net.kyori.adventure.text.Component lineComponent : lore) {
                        String line = Common.serializeComponentToLegacyString(lineComponent);
                        newLore.add(line
                                .replace("%in_queue%", String.valueOf(inQueue))
                                .replace("%in_fight%", String.valueOf(inFight)));
                    }
                    meta.lore(newLore.stream().map(Common::legacyToComponent).toList());
                }

                updated.setItemMeta(meta);
                setStackAmount(updated, inFight > 0 ? inFight : 1);

                inventory.setItem(slot, updated);
            }
        }

        updatePlayers();
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

    private void setupCategoryPage(int pageId, int rows, ItemStack filler, GUIItem ladderTemplate,
                                   CategoryConfig currentCategory, List<CategoryConfig> allCategories,
                                   String guiPath, Map<Integer, Inventory> existingInventories) {

        String title = GUIFile.getString(guiPath + ".TITLE")
                .replace("%weight_class%", getWeightClass().getName())
                .replace("%category%", currentCategory.displayName());

        int size = rows * 9;
        Inventory inventory = existingInventories.get(pageId);
        if (inventory == null || inventory.getSize() != size) {
            inventory = InventoryUtil.createInventory(title, rows);
        } else {
            inventory.clear();
        }

        this.gui.put(pageId, inventory);
        inventory.clear();
        Map<Integer, NormalLadder> slotMap = new HashMap<>();
        pageLadderSlots.put(pageId, slotMap);

        Map<Integer, ItemStack> pageTemplates = new HashMap<>();
        templateItems.put(pageId, pageTemplates);

        int actualSize = inventory.getSize();

        applyLayoutFillers(inventory, filler, actualSize);

        placeCategorySelectors(inventory, currentCategory, allCategories, actualSize, guiPath);
        placeQuickMatchItem(inventory, filler, actualSize, guiPath);

        int ladderAmount = Math.min(currentCategory.ladderNames().size(), currentCategory.ladderSlots().size());
        for (int index = 0; index < ladderAmount; index++) {
            String ladderName = currentCategory.ladderNames().get(index).trim();
            int slot = currentCategory.ladderSlots().get(index);

            if (slot < 0 || slot >= actualSize) {
                continue;
            }

            if (slot == getQuickMatchSlot()) {
                continue;
            }

            NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);
            if (ladder == null || !isValidLadder(ladder) || !ladder.getMatchTypes().contains(MatchType.DUEL)) {
                continue;
            }

            GUIItem icon = getLadderIcon(ladder, ladderTemplate, guiPath);
            if (icon == null) continue;

            icon.replace("%ladder%", ladder.getDisplayName());

            boolean activeLadder = ladder.isEnabled() && !ladder.isFrozen();

            // Disabled/frozen templates may only define lore/name; fallback to ladder icon in that case.
            if (icon.getMaterial() == null && icon.getBaseItemStack() == null && ladder.getIcon() != null) {
                icon.setBaseItem(ladder.getIcon());
            }

            if (activeLadder && ladder.getIcon() != null) {
                icon.setBaseItem(ladder.getIcon());
            }

            if (icon.getMaterial() == null && icon.getBaseItemStack() == null) {
                continue;
            }

            // Apply live queue counters only to active ladders.
            if (activeLadder) {
                updateIconWithQueueData(ladder, icon, guiPath);
            }

            ItemStack renderedItem = icon.get();
            slotMap.put(slot, ladder);
            inventory.setItem(slot, renderedItem);

            if (activeLadder) {
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

        decoratePage(pageId, inventory);
    }

    private void applyLayoutFillers(Inventory inventory, ItemStack filler, int actualSize) {
        if (filler == null || actualSize <= 0) {
            return;
        }

        int rows = actualSize / 9;
        for (int slot = 0; slot < 9 && slot < actualSize; slot++) {
            inventory.setItem(slot, filler);
        }

        for (int row = 0; row < rows; row++) {
            int leftSlot = row * 9;
            int rightSlot = leftSlot + 8;
            inventory.setItem(leftSlot, filler);
            inventory.setItem(rightSlot, filler);
            if (row == 5) {
                inventory.setItem(leftSlot + 1, filler);
                inventory.setItem(rightSlot - 1, filler);
            }
        }
    }

    private void placeQuickMatchItem(Inventory inventory, ItemStack filler, int actualSize, String guiPath) {
        int quickMatchSlot = getQuickMatchSlot();
        if (quickMatchSlot < 0 || quickMatchSlot >= actualSize) {
            return;
        }

        if (!isQuickMatchEnabled()) {
            inventory.setItem(quickMatchSlot, filler);
            return;
        }

        GUIItem quickMatch = GUIFile.getGuiItem(guiPath + ".ICONS.QUICK-MATCH");
        if (!isTemplateConfigured(quickMatch)) {
            return;
        }

        GUIItem icon = quickMatch.cloneItem().replace("%weight_class%", getWeightClass().getName());
        ItemStack renderedItem = icon.get();
        if (renderedItem != null) {
            inventory.setItem(quickMatchSlot, renderedItem);
        }
    }

    private void placeCategorySelectors(Inventory inventory, CategoryConfig currentCategory,
                                        List<CategoryConfig> allCategories, int actualSize, String guiPath) {
        for (CategoryConfig category : allCategories) {
            int selectorSlot = category.selectorSlot();
            if (selectorSlot < 0 || selectorSlot >= actualSize) {
                continue;
            }

            boolean selected = currentCategory.key().equalsIgnoreCase(category.key());
            GUIItem selectorTemplate = getCategorySelectorTemplate(guiPath, selected);
            if (selectorTemplate == null) {
                continue;
            }

            GUIItem selectorIcon = selectorTemplate.cloneItem()
                    .replace("%category%", category.displayName())
                    .replace("%category_ladders%", getCategoryLadderPreview(category))
                    .replace("%weight_class%", getWeightClass().getName());

            Material categoryMaterial = getCategorySelectorMaterial(category, selected);
            if (categoryMaterial != null) {
                selectorIcon.setMaterial(categoryMaterial);
            }

            ItemStack selectorItem = selectorIcon.get();
            if (selectorItem != null) {
                inventory.setItem(selectorSlot, selectorItem);
            }
        }
    }

    private GUIItem getCategorySelectorTemplate(String guiPath, boolean selected) {
        GUIItem normalTemplate = GUIFile.getGuiItem(guiPath + ".ICONS.CATEGORY-SELECTOR");
        if (!isTemplateConfigured(normalTemplate)) {
            return null;
        }

        if (!selected) {
            return normalTemplate;
        }

        GUIItem selectedTemplate = GUIFile.getGuiItem(guiPath + ".ICONS.SELECTED-CATEGORY-SELECTOR");
        if (isTemplateConfigured(selectedTemplate)) {
            return selectedTemplate;
        }

        return normalTemplate.cloneItem().setGlowing(true);
    }

    private boolean isTemplateConfigured(GUIItem item) {
        return item != null && (item.getMaterial() != null || item.getBaseItemStack() != null);
    }

    private Material getCategorySelectorMaterial(CategoryConfig category, boolean selected) {
        String materialName = selected && !category.selectedIconMaterial().isBlank()
                ? category.selectedIconMaterial()
                : category.iconMaterial();

        if (materialName == null || materialName.isBlank()) {
            return null;
        }

        try {
            return Material.valueOf(materialName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String getCategoryLadderPreview(CategoryConfig category) {
        if (category.ladderNames().isEmpty()) {
            return "&7No ladders";
        }

        List<String> preview = new ArrayList<>();
        int limit = Math.min(4, category.ladderNames().size());
        for (int i = 0; i < limit; i++) {
            String ladderName = category.ladderNames().get(i);
            NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);
            preview.add(ladder != null ? ladder.getDisplayName() : ladderName);
        }

        return String.join("&7, &f", preview);
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

        ItemStack item = icon.get();
        setStackAmount(item, duelMatchSize > 0 ? duelMatchSize : 1);
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

        if (handleCustomTopInventoryClick(player, rawSlot, inventoryView, item)) {
            return;
        }

        if (selectorSlotToPage.containsKey(rawSlot)) {
            int targetPage = selectorSlotToPage.get(rawSlot);
            if (this.gui.containsKey(targetPage)) {
                this.open(player, targetPage);
            }
            return;
        }

        if (rawSlot == getQuickMatchSlot() && isQuickMatchEnabled()) {
            Map<Integer, NormalLadder> ladders = pageLadderSlots.get(page);
            if (ladders == null || ladders.isEmpty()) {
                return;
            }

            NormalLadder randomLadder = getRandomQuickMatchLadder(ladders);
            if (randomLadder != null) {
                onLadderClick(player, randomLadder);
            }
            return;
        }

        Map<Integer, NormalLadder> currentMap = pageLadderSlots.get(page);
        if (currentMap == null) {
            return;
        }

        if (currentMap.containsKey(rawSlot)) {
            onLadderClick(player, currentMap.get(rawSlot));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Allows non-stackable items to display with a specific amount by overriding
     * their max stack size. This enables items like swords to show multiple amounts
     * in inventory displays.
     */
    private void setStackAmount(ItemStack item, int amount) {
        if (item == null || amount <= 0) return;
        
        item.setAmount(Math.min(amount, 64));
        
        if (amount > 1) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setMaxStackSize(64);
                item.setItemMeta(meta);
            }
        }
    }

    private List<CategoryConfig> loadCategories(String queuePath) {
        List<Integer> sharedLadderSlots = getConfiguredIntList(queuePath + ".LADDER-SLOTS");
        ConfigurationSection categoriesSection = ConfigManager.getConfig().getConfigurationSection(queuePath + ".CATEGORIES");
        ConfigurationSection selectorSection = ConfigManager.getConfig().getConfigurationSection(queuePath + ".CATEGORY-SELECTOR-SLOTS");

        if (categoriesSection == null || selectorSection == null) {
            return Collections.emptyList();
        }

        List<CategoryConfig> categories = new ArrayList<>();

        for (String categoryKey : categoriesSection.getKeys(false)) {
            String categoryPath = queuePath + ".CATEGORIES." + categoryKey;

            String displayName = ConfigManager.getString(categoryPath + ".DISPLAY-NAME");
            if (displayName.isEmpty()) {
                displayName = categoryKey;
            }

            List<String> ladders = ConfigManager.getList(categoryPath + ".LADDERS");
            List<Integer> ladderSlots = getConfiguredIntList(categoryPath + ".LADDER-SLOTS");
            if (ladderSlots.isEmpty()) {
                ladderSlots = sharedLadderSlots;
            }

            int selectorSlot = selectorSection.getInt(categoryKey, -1);
            if (selectorSlot < 0 || ladderSlots.isEmpty()) {
                continue;
            }

            String iconMaterial = ConfigManager.getString(categoryPath + ".ICON-MATERIAL");
            String selectedIconMaterial = ConfigManager.getString(categoryPath + ".SELECTED-ICON-MATERIAL");

            categories.add(new CategoryConfig(
                    categoryKey,
                    displayName,
                    selectorSlot,
                    iconMaterial,
                    selectedIconMaterial,
                    ladders,
                    new ArrayList<>(ladderSlots)
            ));
        }

        return categories;
    }

    private List<Integer> getConfiguredIntList(String path) {
        List<Integer> slots = new ArrayList<>();
        List<?> rawEntries = ConfigManager.getConfig().getList(path);
        if (rawEntries == null) {
            return slots;
        }

        for (Object entry : rawEntries) {
            if (entry instanceof Number number) {
                slots.add(number.intValue());
                continue;
            }

            if (entry instanceof String string) {
                try {
                    slots.add(Integer.parseInt(string.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return slots;
    }

    private int getQuickMatchSlot() {
        String path = getQueueConfigPath() + ".SELECTOR-GUI.QUICK-MATCH-SLOT";
        if (!ConfigManager.getConfig().isInt(path)) {
            return DEFAULT_QUICK_MATCH_SLOT;
        }

        int configured = ConfigManager.getInt(path);
        return configured >= 0 ? configured : DEFAULT_QUICK_MATCH_SLOT;
    }

    private boolean isQuickMatchEnabled() {
        String path = getQueueConfigPath() + ".SELECTOR-GUI.QUICK-MATCH-ENABLED";
        if (!ConfigManager.getConfig().isBoolean(path)) {
            return true;
        }

        return ConfigManager.getBoolean(path);
    }

    private NormalLadder getRandomQuickMatchLadder(Map<Integer, NormalLadder> laddersInCategory) {
        List<NormalLadder> candidates = new ArrayList<>();
        for (NormalLadder ladder : new LinkedHashSet<>(laddersInCategory.values())) {
            if (ladder != null && ladder.isEnabled() && !ladder.isFrozen()) {
                candidates.add(ladder);
            }
        }

        if (candidates.isEmpty()) {
            candidates.addAll(new LinkedHashSet<>(laddersInCategory.values()));
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private record CategoryConfig(String key, String displayName, int selectorSlot,
                                  String iconMaterial, String selectedIconMaterial,
                                  List<String> ladderNames, List<Integer> ladderSlots) {
    }
}




