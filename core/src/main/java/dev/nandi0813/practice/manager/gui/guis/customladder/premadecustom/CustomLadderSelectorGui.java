package dev.nandi0813.practice.manager.gui.guis.customladder.premadecustom;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.module.interfaces.ItemCreateUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomLadderSelectorGui extends GUI {

    private final Map<Integer, NormalLadder> ladderSlots = new HashMap<>();

    public CustomLadderSelectorGui() {
        super(GUIType.CustomLadder_Selector);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.KIT-EDITOR.LADDER-SELECTOR.TITLE"), 5));

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        gui.get(1).clear();
        ladderSlots.clear();

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            if (ladder.isEnabled() && ladder.isEditable()) {
                ItemStack icon = ladder.getIcon();
                ItemMeta iconMeta = icon.getItemMeta();
                if (iconMeta != null) {
                    iconMeta.setDisplayName(GUIFile.getString("GUIS.KIT-EDITOR.LADDER-SELECTOR.ICONS.NAME")
                            .replace("%ladder%", ladder.getDisplayName())
                            .replace("%ladderOriginal%", ladder.getName())
                    );
                    ItemCreateUtil.hideItemFlags(iconMeta);

                    List<String> lore = new ArrayList<>();
                    for (String line : GUIFile.getStringList("GUIS.KIT-EDITOR.LADDER-SELECTOR.ICONS.LORE")) {
                        lore.add(line
                                .replace("%ladder%", ladder.getDisplayName())
                                .replace("%ladderOriginal%", ladder.getName()))
                        ;
                    }
                    iconMeta.setLore(lore);
                    icon.setItemMeta(iconMeta);
                }

                int slot = gui.get(1).firstEmpty();
                ladderSlots.put(slot, ladder);
                gui.get(1).setItem(slot, icon);
            }
        }

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.LOBBY)) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        int slot = e.getRawSlot();

        if (e.getView().getTopInventory().getSize() > slot) {
            if (item == null) return;
            if (!ladderSlots.containsKey(slot)) return;
            NormalLadder ladder = ladderSlots.get(slot);

            if (!ladder.isEnabled() || !ladder.isEditable()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.KIT-EDITOR.LADDER-SELECTOR.NOT-AVAILABLE").replace("%ladder%", ladder.getDisplayName()));
                update();
                return;
            }

            if (ladder.isFrozen()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.KIT-EDITOR.LADDER-SELECTOR.LADDER-FROZEN").replace("%ladder%", ladder.getDisplayName()));
                return;
            }

            new CustomLadderSumGui(profile, ladder).open(player);
        }
    }

}
