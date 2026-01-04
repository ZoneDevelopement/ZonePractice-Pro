package dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.enums.WeightClassType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MatchTypeGui extends GUI {

    private final NormalLadder ladder;
    private final Map<Integer, MatchType> matchTypeSlots = new HashMap<>();

    public MatchTypeGui(NormalLadder ladder) {
        super(GUIType.Ladder_MatchType);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.LADDER.MATCH-TYPE.TITLE").replace("%ladder%", ladder.getName()), 1));
        this.ladder = ladder;

        build();
    }

    @Override
    public void build() {
        LadderType ladderType = ladder.getType();
        Inventory inventory = gui.get(1);

        inventory.setItem(0, GUIFile.getGuiItem("GUIS.SETUP.LADDER.MATCH-TYPE.ICONS.GO-BACK").get());

        for (int i : new int[]{1, 2, 3, 4})
            inventory.setItem(i, GUIManager.getDUMMY_ITEM());

        if (!ladderType.isPartyFFASupported())
            inventory.setItem(5, GUIManager.getDUMMY_ITEM());


        for (MatchType matchType : MatchType.values()) {
            if (!ladderType.isPartyFFASupported() && (matchType.equals(MatchType.PARTY_FFA)))
                continue;

            int slot = inventory.firstEmpty();
            inventory.setItem(slot, GUIManager.getDUMMY_ITEM());
            matchTypeSlots.put(slot, matchType);
        }

        inventory.remove(GUIManager.getDUMMY_ITEM());

        update();
    }

    @Override
    public void update() {
        for (Map.Entry<Integer, MatchType> slot : matchTypeSlots.entrySet())
            gui.get(1).setItem(slot.getKey(), getMatchTypeItem(ladder, slot.getValue()));

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (e.getCurrentItem() == null) return;

        if (slot == 0) {
            LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).open(player);
            return;
        }

        if (ladder.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-EDIT-ENABLED"));
            return;
        }

        MatchType matchType = matchTypeSlots.get(slot);
        if (matchType == null) return;

        if (ladder.getWeightClass().equals(WeightClassType.RANKED)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-EDIT-RANKED-WEIGHTCLASS-MATCHTYPE"));
            return;
        }

        if (ladder.getMatchTypes().contains(matchType))
            ladder.getMatchTypes().remove(matchType);
        else
            ladder.getMatchTypes().add(matchType);

        update();
    }


    private ItemStack getMatchTypeItem(Ladder ladder, MatchType matchType) {
        if (ladder.getMatchTypes().contains(matchType)) {
            return GUIFile.getGuiItem("GUIS.SETUP.LADDER.MATCH-TYPE.ICONS.ENABLED")
                    .replace("%matchType%", matchType.getName(false))
                    .get();
        } else {
            return GUIFile.getGuiItem("GUIS.SETUP.LADDER.MATCH-TYPE.ICONS.DISABLED")
                    .replace("%matchType%", matchType.getName(false))
                    .get();
        }
    }

}
