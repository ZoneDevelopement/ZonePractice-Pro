package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BreakAllBlocksItem extends SettingItem {

    public BreakAllBlocksItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.BREAK_ALL_BLOCKS, ladder);
    }

    @Override
    public void updateItemStack() {
        if (this.ladder.isBreakAllBlocks()) {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.BREAK-ALL-BLOCKS.ENABLED").setGlowing(true);
        } else {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.BREAK-ALL-BLOCKS.DISABLED");
        }
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        this.ladder.setBreakAllBlocks(!this.ladder.isBreakAllBlocks());
        this.build(true);
    }
}

