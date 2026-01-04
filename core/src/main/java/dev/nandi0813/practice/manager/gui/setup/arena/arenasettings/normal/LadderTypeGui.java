package dev.nandi0813.practice.manager.gui.setup.arena.arenasettings.normal;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LadderTypeGui extends GUI {

    private final Map<Integer, LadderType> ladderTypeSlots = new HashMap<>();
    private int customKitSlot;
    private final Arena arena;

    public LadderTypeGui(Arena arena) {
        super(GUIType.Arena_Ladders_Type);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.ARENA.ARENA-LADDERS-TYPE.TITLE").replace("%arenaName%", arena.getName()), 3));
        this.arena = arena;

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        // Set filler items
        for (int i : new int[]{19, 20, 21, 22, 23, 24, 25})
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        // Navigation item
        inventory.setItem(18, GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-TYPE.ICONS.BACK-TO").get());
        inventory.setItem(26, GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-TYPE.ICONS.GO-TO-LADDERS").get());

        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);
            ladderTypeSlots.clear();

            for (int i = 0; i < 18; i++) {
                inventory.setItem(i, null);
            }

            for (LadderType ladderType : LadderType.values()) {
                GUIItem item;
                if (arena.getAssignedLadderTypes().contains(ladderType)) {
                    item = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-TYPE.ICONS.LADDER-TYPE-ICONS.ENABLED");
                    item.setGlowing(true);
                } else {
                    item = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-TYPE.ICONS.LADDER-TYPE-ICONS.DISABLED");
                    item.setGlowing(false);
                }

                if (item.getMaterial() == null) {
                    item.setMaterial(ladderType.getIcon());
                    item.setDamage((short) 0);
                }

                item.replace("%ladderTypeName%", ladderType.getName());

                int slot = inventory.firstEmpty();
                inventory.setItem(slot, item.get());
                ladderTypeSlots.put(slot, ladderType);
            }

            int slot = inventory.firstEmpty();
            inventory.setItem(slot,
                    GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-TYPE.ICONS.CUSTOM-KIT-ICONS.ICON")
                            .replace("%status%", arena.isAllowCustomKitOnMap() ? "&aEnabled" : "&cDisabled")
                            .get());
            customKitSlot = slot;

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack item = e.getCurrentItem();
        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (item == null) return;

        if (slot == 18) {
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).open(player);
        } else if (slot == 26) {
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Ladders_Single).open(player);
        } else if (ladderTypeSlots.containsKey(slot)) {
            LadderType ladderType = ladderTypeSlots.get(slot);
            if (arena.getAssignedLadderTypes().contains(ladderType)) {
                if (!arena.isEnabled()) {
                    arena.getAssignedLadderTypes().remove(ladderType);
                    arena.getAssignedLadders().removeIf(ladder -> ladder.getType().equals(ladderType));

                    this.update();
                    ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Ladders_Single).update();
                    GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.ARENA.CANT-REMOVE-LADDER-TYPE"));
            } else {
                if (arena.isBuild() == ladderType.isBuild()) {
                    arena.getAssignedLadderTypes().add(ladderType);
                    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
                        if (ladder.isEnabled() && ladder.getType().equals(ladderType)) {
                            arena.getAssignedLadders().add(ladder);
                        }
                    }

                    this.update();
                    ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Ladders_Single).update();
                    GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.ARENA.CANT-ASSIGN-BUILD-DIFF"));
            }
        } else if (slot == customKitSlot) {
            if (arena.isAllowCustomKitOnMap()) {
                if (!arena.isEnabled()) {
                    arena.setAllowCustomKitOnMap(false);
                    this.update();
                } else {
                    Common.sendMMMessage(player, "<red>You can't change this setting while the arena is enabled.");
                }
            } else {
                arena.setAllowCustomKitOnMap(true);
                this.update();
            }
        }
    }

}
