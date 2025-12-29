package dev.nandi0813.practice.manager.fight.ffa.game;

import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LadderSelector extends GUI {

    private static final GUIItem LADDER_ITEM = GUIFile.getGuiItem("GUIS.FFA.LADDER-SELECTOR.ICONS.LADDER");
    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.FFA.LADDER-SELECTOR.ICONS.FILLER").get();

    private final FFA ffa;
    private final FFAArena ffaArena;

    private final Map<Integer, NormalLadder> ladderSlots = new HashMap<>();

    public LadderSelector(FFA ffa) {
        super(GUIType.FFA_Ladder_Selector);
        this.ffa = ffa;
        this.ffaArena = ffa.getArena();

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.FFA.LADDER-SELECTOR.TITLE"), 3));
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);
        inventory.clear();
        ladderSlots.clear();

        for (NormalLadder ladder : ffaArena.getAssignedLadders()) {
            ItemStack ladderItem = LADDER_ITEM.cloneItem()
                    .setMaterial(ladder.getIcon().getType())
                    .setDamage(ladder.getIcon().getDurability())
                    .replaceAll("%ladder%", ladder.getDisplayName())
                    .get();

            int slot = inventory.firstEmpty();
            inventory.setItem(slot, ladderItem);
            ladderSlots.put(slot, ladder);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, FILLER_ITEM);
            }
        }

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        if (ladderSlots.isEmpty()) {
            player.closeInventory();
            return;
        }

        if (!ladderSlots.containsKey(slot))
            return;

        NormalLadder ladder = ladderSlots.get(slot);
        if (ladder != null) {
            if (!ladder.isEnabled() || !ffaArena.getAssignedLadders().contains(ladder)) {
                update();
                return;
            } else if (ladder.isFrozen()) {
                update();
                return;
            }
        }

        if (!ffa.isOpen()) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.FFA-CLOSED").replaceAll("%arena%", ffaArena.getDisplayName()));
            player.closeInventory();
            return;
        }

        ffa.addPlayer(player, ladder);
    }

}
