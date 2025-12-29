package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.Items;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingItem;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.KnockbackType;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class KnockbackItem extends SettingItem {

    protected final NormalLadder ladder;

    public KnockbackItem(SettingsGui settingsGui, NormalLadder ladder) {
        super(settingsGui, SettingType.KNOCKBACK, ladder);
        this.ladder = ladder;
    }

    @Override
    public void updateItemStack() {
        List<String> extension = new ArrayList<>();
        for (KnockbackType kt : KnockbackType.values()) {
            String ktName = StringUtils.capitalize(kt.name().toLowerCase());

            if (ladder.getLadderKnockback().getKnockbackType().equals(kt)) extension.add(" &a» " + ktName);
            else extension.add(" &7» " + ktName);
        }

        GUIItem guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.SETTINGS.ICONS.KNOCKBACK");

        List<String> lore = new ArrayList<>();
        for (String line : guiItem.getLore()) {
            if (line.contains("%knockbackTypes%"))
                lore.addAll(extension);
            else
                lore.add(line);
        }

        guiItem.setLore(lore);
        this.guiItem = guiItem;
    }

    @Override
    public void clickEvent(InventoryClickEvent e) {
        switch (ladder.getLadderKnockback().getKnockbackType()) {
            case DEFAULT:
                ladder.getLadderKnockback().setKnockbackType(KnockbackType.NORMAL);
                break;
            case NORMAL:
                ladder.getLadderKnockback().setKnockbackType(KnockbackType.COMBO);
                break;
            case COMBO:
                ladder.getLadderKnockback().setKnockbackType(KnockbackType.DEFAULT);
                break;
        }

        build(true);
    }

}
