package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings;

import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class SettingItem {

    protected final SettingsGui settingsGui;

    protected final SettingType settingType;
    @Getter
    @Setter
    protected int slot = -1;

    protected GUIItem guiItem;
    protected final NormalLadder ladder;

    protected SettingItem(final SettingsGui settingsGui, SettingType settingType, NormalLadder ladder) {
        this.settingsGui = settingsGui;
        this.settingType = settingType;
        this.ladder = ladder;
    }

    public abstract void updateItemStack();

    public void build(boolean update) {
        updateItemStack();

        if (slot == -1) return;

        if (guiItem != null)
            settingsGui.getGui().get(1).setItem(slot, guiItem.get());

        if (update)
            settingsGui.update();
    }

    public abstract void clickEvent(InventoryClickEvent e);

}
