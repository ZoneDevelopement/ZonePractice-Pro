package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items.*;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.type.Boxing;
import dev.nandi0813.practice.manager.ladder.type.SkyWars;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsGui extends GUI {

    @Getter
    private final NormalLadder ladder;
    @Getter
    private final LadderType ladderType;
    private final GuiSlot guiSlot;

    private final List<SettingItem> settingItems = new ArrayList<>();

    public SettingsGui(@NotNull NormalLadder ladder) {
        super(GUIType.Ladder_Settings);

        this.ladder = ladder;
        this.ladderType = ladder.getType();

        if (ladderType.getSettingTypes().size() <= 7)
            guiSlot = GuiSlot.SIZE_1;
        else if (ladderType.getSettingTypes().size() <= 14)
            guiSlot = GuiSlot.SIZE_2;
        else
            guiSlot = GuiSlot.SIZE_3;

        gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.LADDER.SETTINGS.TITLE").replace("%ladder%", ladder.getName()), (guiSlot.getRows() + 1)));

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);
        inventory.clear();
        settingItems.clear();

        inventory.setItem(guiSlot.getBackSlot(), GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.GO-BACK").get());

        addItems();

        // Set the setting items
        int currentItemSlot = 0;
        for (SettingItem settingItem : settingItems) {
            int slot = guiSlot.getSlots().get(currentItemSlot);
            currentItemSlot++;
            settingItem.setSlot(slot);
            settingItem.build(false);
        }

        // Filler items
        for (int i = 0; i < inventory.getSize(); i++)
            if (inventory.getItem(i) == null)
                inventory.setItem(i, GUIManager.getFILLER_ITEM());

        update();
    }

    @Override
    public void update() {
        updatePlayers();
    }

    @Override
    public void handleClickEvent(@NotNull InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack item = e.getCurrentItem();

        e.setCancelled(true);

        if (slot == guiSlot.getBackSlot()) {
            LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).open(player);
            return;
        }

        if (inventory.getSize() <= slot) return;
        if (item == null) return;
        if (item.equals(GUIManager.getFILLER_ITEM())) return;

        if (ladder.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-EDIT-ENABLED"));
            return;
        }

        SettingItem settingItem = searchBySlot(slot);
        if (settingItem != null)
            settingItem.clickEvent(e);
    }

    @Contract ( pure = true )
    private @Nullable SettingItem searchBySlot(int slot) {
        for (SettingItem settingItem : settingItems)
            if (settingItem.getSlot() == slot)
                return settingItem;
        return null;
    }

    @Getter
    public enum GuiSlot {
        SIZE_1(2, new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16))),
        SIZE_2(3, new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25))),
        SIZE_3(4, new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 35, 36)));

        private final int rows;
        private final int backSlot;
        private final List<Integer> slots;

        GuiSlot(int rows, List<Integer> slots) {
            this.rows = rows;
            this.backSlot = rows * 9;
            this.slots = slots;
        }
    }

    private void addItems() {
        final List<SettingType> settingTypes = ladderType.getSettingTypes();

        if (settingTypes.contains(SettingType.REGENERATION))
            settingItems.add(new RegenerationItem(this, ladder));

        if (settingTypes.contains(SettingType.HUNGER))
            settingItems.add(new HungerItem(this, ladder));

        if (settingTypes.contains(SettingType.EDITABLE))
            settingItems.add(new EditableItem(this, ladder));

        if (settingTypes.contains(SettingType.ENDER_PEARL_COOLDOWN))
            settingItems.add(new EnderpearlItem(this, ladder));

        if (settingTypes.contains(SettingType.GOLDEN_APPLE_COOLDOWN))
            settingItems.add(new GoldenAppleItem(this, ladder));

        if (settingTypes.contains(SettingType.HIT_DELAY))
            settingItems.add(new HitdelayItem(this, ladder));

        if (settingTypes.contains(SettingType.KNOCKBACK))
            settingItems.add(new KnockbackItem(this, ladder));

        if (settingTypes.contains(SettingType.START_COUNTDOWN))
            settingItems.add(new StartCountdownItem(this, ladder));

        if (settingTypes.contains(SettingType.START_MOVING))
            settingItems.add(new StartMovingItem(this, ladder));

        if (settingTypes.contains(SettingType.MULTI_ROUND_START_COUNTDOWN))
            settingItems.add(new MultiRoundStartCountdownItem(this, ladder));

        if (settingTypes.contains(SettingType.DROP_INVENTORY_TEAM))
            settingItems.add(new DropInventoryItem(this, ladder));

        if (settingTypes.contains(SettingType.WEIGHT_CLASS))
            settingItems.add(new RankedItem(this, ladder));

        if (settingTypes.contains(SettingType.ROUNDS))
            settingItems.add(new RoundsItem(this, ladder));

        if (settingTypes.contains(SettingType.RESPAWN_TIME))
            settingItems.add(new TempRespawnTimeItem(this, ladder));

        if (settingTypes.contains(SettingType.BOXING_HITS))
            settingItems.add(new BoxingHitsItem(this, (Boxing) ladder));

        if (settingTypes.contains(SettingType.FIREBALL_COOLDOWN))
            settingItems.add(new FireballCooldownItem(this, ladder));

        if (settingTypes.contains(SettingType.SKYWARS_LOOT))
            settingItems.add(new SkywarsLootItem(this, (SkyWars) ladder));

        if (settingTypes.contains(SettingType.TEMP_BUILD_DELAY))
            settingItems.add(new TempbuildDelayItem(this, ladder));

        if (settingTypes.contains(SettingType.MAX_DURATION))
            settingItems.add(new MaxDurationItem(this, ladder));

        if (settingTypes.contains(SettingType.TNT_FUSE_TIME))
            settingItems.add(new TntFuseTimeItem(this, ladder));

        if (settingTypes.contains(SettingType.HEALTH_BELOW_NAME))
            settingItems.add(new HealthBelowNameItem(this, ladder));

        if (settingTypes.contains(SettingType.RESET_BUILD_AFTER_ROUND))
            settingItems.add(new ResetBuildAfterRoundItem(this, ladder));

        if (settingTypes.contains(SettingType.BREAK_ALL_BLOCKS))
            settingItems.add(new BreakAllBlocksItem(this, ladder));

        if (settingTypes.contains(SettingType.SPLEEF_SNOWBALL_MODE))
            settingItems.add(new SpleefSnowballModeItem(this, (dev.nandi0813.practice.manager.ladder.type.Spleef) ladder));
    }

}
