package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.type.FireballFight;
import org.bukkit.event.inventory.InventoryClickEvent;

public class FireballBlockDestroyItem extends SettingItem {

    private final FireballFight fireballFight;

    public FireballBlockDestroyItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.FIREBALL_BLOCK_DESTROY, ladder);
        this.fireballFight = (FireballFight) ladder;
    }

    @Override
    public void updateItemStack() {
        if (fireballFight.isFireballBlockDestroy()) {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.FIREBALL-BLOCK-DESTROY.ENABLED").setGlowing(true);
        } else {
            this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.FIREBALL-BLOCK-DESTROY.DISABLED");
        }
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        fireballFight.setFireballBlockDestroy(!fireballFight.isFireballBlockDestroy());
        this.build(true);
    }
}

