package dev.nandi0813.practice.manager.gui.guis.arena;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.ArenaType;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.arena.setup.ArenaSetupManager;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ArenaCreateGui extends GUI {

    private final String arenaName;
    @Getter
    private final Map<Integer, ArenaType> typeSlots = new HashMap<>();

    public ArenaCreateGui(String arenaName) {
        super(GUIType.Arena_Create);
        this.arenaName = arenaName;
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.ARENA-CREATE.TITLE").replace("%arena%", arenaName), 3));

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        inventory.setItem(10, null);
        inventory.setItem(13, null);
        inventory.setItem(16, null);

        for (ArenaType type : ArenaType.values()) {
            ItemStack item = ClassImport.getClasses().getItemCreateUtil().createItem("&e" + type.getName(), type.getIcon());
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setLore(type.getDescription());
            item.setItemMeta(itemMeta);

            int slot = inventory.firstEmpty();
            typeSlots.put(slot, type);

            inventory.setItem(slot, item);
        }

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (!typeSlots.containsKey(slot)) return;

        DisplayArena arena;
        if (Objects.requireNonNull(typeSlots.get(slot)) == ArenaType.FFA) {
            arena = new FFAArena(arenaName);
        } else {
            arena = new Arena(arenaName, typeSlots.get(slot));
        }

        arena.setData();

        ArenaManager.getInstance().getArenaList().add(arena);

        ArenaGUISetupManager.getInstance().buildArenaSetupGUIs(arena);
        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();

        Common.sendMMMessage(player, LanguageManager.getString("ARENA.CREATE.ARENA-CREATED").replaceAll("%arena%", arenaName));

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).open(player);
            ArenaSetupManager.getInstance().startSetup(player, arena);
        }, 3L);
    }

}
