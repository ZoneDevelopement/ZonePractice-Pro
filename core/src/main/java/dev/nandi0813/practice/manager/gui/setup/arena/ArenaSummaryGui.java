package dev.nandi0813.practice.manager.gui.setup.arena;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.PageUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class ArenaSummaryGui extends GUI {

    private static final String STATUS_ENABLED = GUIFile.getString("GUIS.SETUP.ARENA.ARENA-MANAGER.ICONS.ARENA-ICON.STATUS-NAMES.ENABLED");
    private static final String STATUS_DISABLED = GUIFile.getString("GUIS.SETUP.ARENA.ARENA-MANAGER.ICONS.ARENA-ICON.STATUS-NAMES.DISABLED");

    private final int spaces = 27;
    private final Map<Integer, Map<Integer, DisplayArena>> slots = new HashMap<>();
    // private final Map<ItemStack, DisplayArena> icons = new HashMap<>();
    private final Map<Player, Integer> backToPage = new HashMap<>();

    public ArenaSummaryGui() {
        super(GUIType.Arena_Summary);

        this.build();
    }

    @Override
    public void build() {
        slots.clear();
        Map<DisplayArena, ItemStack> displayIcons = new HashMap<>();

        for (Arena arena : ArenaManager.getInstance().getNormalArenas())
            displayIcons.put(arena, getArenaItem(arena));

        for (FFAArena ffaArena : ArenaManager.getInstance().getFFAArenas())
            displayIcons.put(ffaArena, getArenaItem(ffaArena));

        for (int page = 1; page < 10; page++) {
            if (PageUtil.isPageValid(displayIcons.size(), page, spaces) || page == 1) {
                if (!gui.containsKey(page))
                    gui.put(page, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.ARENA.ARENA-MANAGER.TITLE").replace("%page%", String.valueOf(page)), 5));

                gui.get(page).clear();

                // Frame
                for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44})
                    gui.get(page).setItem(i, GUIManager.getFILLER_ITEM());

                // Arena icons
                Map<Integer, DisplayArena> pageSlots = new HashMap<>();
                for (Map.Entry<DisplayArena, ItemStack> entry : getPageItems(displayIcons, page).entrySet()) {
                    int slot = gui.get(page).firstEmpty();
                    if (slot != -1 && slot < gui.get(page).getSize()) {
                        gui.get(page).setItem(slot, entry.getValue());
                        pageSlots.put(slot, entry.getKey());
                    }
                }
                slots.put(page, pageSlots);

                // Left navigation
                ItemStack left;
                if (PageUtil.isPageValid(displayIcons.size(), page - 1, spaces))
                    left = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MANAGER.ICONS.PAGE-LEFT").replace("%page%", String.valueOf(page - 1)).get();
                else
                    left = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MANAGER.ICONS.BACK-TO").get();
                gui.get(page).setItem(36, left);

                // Right navigation
                ItemStack right;
                if (PageUtil.isPageValid(displayIcons.size(), page + 1, spaces))
                    right = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MANAGER.ICONS.PAGE-RIGHT").replace("%page%", String.valueOf(page + 1)).get();
                else
                    right = GUIManager.getFILLER_ITEM();
                gui.get(page).setItem(44, right);
            } else {
                if (gui.containsKey(page)) {
                    int finalPage = page;
                    Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () ->
                    {
                        gui.remove(finalPage);
                        for (Player player : inGuiPlayers.keySet()) {
                            if (inGuiPlayers.get(player) == finalPage)
                                open(player, finalPage - 1);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            build();
            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        ClickType click = e.getClick();
        int slot = e.getRawSlot();
        e.setCancelled(true);

        if (inventory.getSize() > slot && e.getCurrentItem() != null) {
            int page = inGuiPlayers.get(player);

            if (slot == 36 || slot == 44) {
                if (slot == 36) {
                    if (page == 1) {
                        GUIManager.getInstance().searchGUI(GUIType.Setup_Hub).open(player);
                    } else {
                        this.open(player, page - 1);
                    }
                } else {
                    if (this.gui.containsKey(page + 1)) {
                        this.open(player, page + 1);
                    }
                }
            } else if (slots.containsKey(page)) {
                if (slots.get(page).containsKey(slot)) {
                    DisplayArena arena = slots.get(page).get(slot);

                    if (click.isLeftClick()) {
                        if (ArenaGUISetupManager.getInstance().getArenaSetupGUIs().containsKey(arena)) {
                            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).open(player);
                            backToPage.put(player, page);
                        }
                    } else if (click.isRightClick())
                        arena.teleport(player);
                }
            }
        }
    }

    private static ItemStack getArenaItem(DisplayArena arena) {
        if (arena instanceof Arena)
            return getArenaItem((Arena) arena);

        GUIItem guiItem = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MANAGER.ICONS.ARENA-ICON.FFA");

        if (arena.getIcon() != null) {
            guiItem.setMaterial(arena.getIcon().getType());
            guiItem.setDamage(arena.getIcon().getDurability());
        }

        guiItem
                .replace("%arenaName%", arena.getName())
                .replace("%type%", arena.getType().getName())
                .replace("%state%", (arena.isEnabled() ? STATUS_ENABLED : STATUS_DISABLED))
                .replace("%build%", (arena.isBuild() ? STATUS_ENABLED : STATUS_DISABLED));

        return guiItem.get();
    }

    private static ItemStack getArenaItem(Arena arena) {
        GUIItem guiItem;
        if (arena.isBuild()) guiItem = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MANAGER.ICONS.ARENA-ICON.BUILD");
        else guiItem = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MANAGER.ICONS.ARENA-ICON.NON-BUILD");

        if (arena.getIcon() != null) {
            guiItem.setMaterial(arena.getIcon().getType());
            guiItem.setDamage(arena.getIcon().getDurability());
        }

        guiItem
                .replace("%arenaName%", arena.getName())
                .replace("%type%", arena.getType().getName())
                .replace("%state%", (arena.isEnabled() ? STATUS_ENABLED : STATUS_DISABLED))
                .replace("%assigned_ladders%", String.valueOf(arena.getAssignedLadders().size()))
                .replace("%assignable_ladders%", String.valueOf(arena.getAssignableLadders().size()));

        if (arena.isBuild())
            guiItem.replace("%copies%", String.valueOf(arena.getCopies().size()));

        return guiItem.get();
    }

    private static Map<DisplayArena, ItemStack> getPageItems(Map<DisplayArena, ItemStack> items, int page) {
        Map<DisplayArena, ItemStack> sortedItems = sortByValue(items);

        int upperBound = page * 27;
        int lowerBound = upperBound - 27;

        // IMPORTANT: Use LinkedHashMap to preserve the sorted order!
        Map<DisplayArena, ItemStack> newItems = new LinkedHashMap<>();
        int index = 0;

        for (Map.Entry<DisplayArena, ItemStack> entry : sortedItems.entrySet()) {
            if (index >= lowerBound && index < upperBound) {
                newItems.put(entry.getKey(), entry.getValue());
            }
            index++;
            if (index >= upperBound) {
                break;
            }
        }

        return newItems;
    }

    public static Map<DisplayArena, ItemStack> sortByValue(Map<DisplayArena, ItemStack> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey((arena1, arena2) -> {
                    // Proper lexicographic comparison (case-insensitive, character by character)
                    String name1 = arena1.getName();
                    String name2 = arena2.getName();
                    return String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
                }))
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

}
