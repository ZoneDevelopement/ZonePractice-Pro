package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.BlockReturnDelay;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BlockReturnDelayItem extends SettingItem {

    private static final int MIN_DELAY_SECONDS = -1;
    private static final int MAX_DELAY_SECONDS = 30;

    private final BlockReturnDelay blockReturnDelay;

    public BlockReturnDelayItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.BLOCK_RETURN_DELAY, ladder);
        this.blockReturnDelay = (BlockReturnDelay) ladder;
    }

    @Override
    public void updateItemStack() {
        int delay = blockReturnDelay.getBlockReturnDelaySeconds();
        String delayText = delay < 0 ? "Disabled" : delay + "";

        this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.BLOCK-RETURN-DELAY")
                .replace("%blockReturnDelay%", delayText)
                .replace("%blockReturnDelayTarget%", blockReturnDelay.getContextTargetForBlockReturn());
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        int delay = blockReturnDelay.getBlockReturnDelaySeconds();

        if (e.getClick().isLeftClick() && delay > MIN_DELAY_SECONDS) {
            blockReturnDelay.setBlockReturnDelaySeconds(delay - 1);
        } else if (e.getClick().isRightClick() && delay < MAX_DELAY_SECONDS) {
            blockReturnDelay.setBlockReturnDelaySeconds(delay + 1);
        }

        build(true);
    }

}

