package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.type.Boxing;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BoxingHitsItem extends SettingItem {

    private final Boxing boxing;

    public BoxingHitsItem(SettingsGui settingsGui, Boxing boxing) {
        super(settingsGui, SettingType.BOXING_HITS, boxing);
        this.boxing = boxing;
    }

    @Override
    public void updateItemStack() {
        this.guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.BOXING")
                .replaceAll("%boxingWinHits%", String.valueOf(boxing.getBoxingWinHit()));
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        ClickType click = e.getClick();

        int boxingWinHit = boxing.getBoxingWinHit();

        if (click.isLeftClick() && boxingWinHit > 40)
            boxing.setBoxingWinHit(boxingWinHit - 20);
        else if (click.isRightClick() && boxingWinHit < 600)
            boxing.setBoxingWinHit(boxingWinHit + 20);

        build(true);
    }

}
