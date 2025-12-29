package dev.nandi0813.practice.manager.gui.setup.arena.arenasettings.normal;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.ArenaCopy;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.confirmgui.ConfirmGuiType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSetupManager;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSetupUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.PageUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class CopyGui extends GUI {

    private final Arena arena;
    private final int spaces = 18;
    private final Map<ItemStack, ArenaCopy> icons = new HashMap<>();

    public CopyGui(Arena arena) {
        super(GUIType.Arena_Copy);
        this.arena = arena;

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            icons.clear();
            List<ArenaCopy> copies = arena.getCopies();

            // Loop copy arena icons
            for (ArenaCopy copyArena : copies) {
                icons.put(ArenaSetupUtil.getCopyArenaItem(arena, copyArena.getNumber()), copyArena);
            }

            List<ItemStack> iconsList = new ArrayList<>(icons.keySet());
            iconsList.sort(Comparator.comparing(ItemStack::getAmount));

            for (int page = 1; page <= 4; page++) {
                if (PageUtil.isPageValid(iconsList, page, spaces) || page == 1) {
                    if (!gui.containsKey(page))
                        gui.put(page, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.ARENA.ARENA-COPY.TITLE")
                                        .replace("%arenaName%", arena.getName())
                                        .replace("%arenaDisplayName%", arena.getDisplayName())
                                        .replace("%page%", String.valueOf(page))
                                , 3));

                    Inventory inventory = gui.get(page);
                    inventory.clear();

                    // Frame
                    for (int i : new int[]{18, 19, 20, 21, 23, 24, 25, 26})
                        inventory.setItem(i, GUIManager.getFILLER_ITEM());
                    inventory.setItem(22, ArenaSetupUtil.getCopyGuiNavMainItem(arena, copies.size()));

                    // Left navigation
                    if (PageUtil.isPageValid(iconsList, page - 1, spaces))
                        inventory.setItem(18, GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-COPY.ICONS.PAGE-LEFT").replaceAll("%page%", String.valueOf(page - 1)).get());
                    else
                        inventory.setItem(18, GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-COPY.ICONS.BACK-TO").get());

                    // Right navigation
                    if (PageUtil.isPageValid(iconsList, page + 1, spaces))
                        inventory.setItem(26, GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-COPY.ICONS.PAGE-RIGHT").replaceAll("%page%", String.valueOf(page + 1)).get());

                    // Decrease copies
                    if (!copies.isEmpty())
                        inventory.setItem(21, GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-COPY.ICONS.DELETE").get());

                    // Increase copies
                    if (copies.size() < 64)
                        inventory.setItem(23, GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-COPY.ICONS.GENERATE").get());

                    // Arena icons
                    for (ItemStack icons : PageUtil.getPageItems(iconsList, page, spaces))
                        inventory.setItem(inventory.firstEmpty(), icons);
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

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack currentItem = e.getCurrentItem();
        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (e.getCurrentItem() == null) return;

        if (slot == 18 || slot == 26) {
            int page = inGuiPlayers.get(player);

            if (slot == 18) {
                if (page == 1)
                    ArenaSetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).open(player, 1);
                else
                    open(player, page - 1);
            } else {
                if (PageUtil.isPageValid(new ArrayList<>(icons.keySet()), page + 1, spaces))
                    open(player, page + 1);
            }
        } else if (currentItem.getType().equals(Material.ARROW)) {
            if (slot == 21) {
                if (arena.isEnabled()) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.ARENA.CANT-DELETE-COPIES"));
                    return;
                }

                openConfirmGUI(player, ConfirmGuiType.ARENA_COPY_DELETE, this, this);
            } else if (slot == 23) {
                if (!arena.isEnabled()) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.ARENA.ARENA-DISABLED"));
                    return;
                }

                if (arena.isCopying()) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.ARENA.ALREADY-GENERATING"));
                    return;
                }

                Location location = ClassImport.getClasses().getArenaCopyUtil().createCopy(profile, arena);
                if (!ConfigManager.getBoolean("ARENA.TELEPORT-TO-COPY")) return;

                player.teleport(location);
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        } else if (icons.containsKey(currentItem)) {
            player.closeInventory();
            icons.get(currentItem).teleport(player);
        }
    }

    @Override
    public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {
        if (confirmGuiType.equals(ConfirmGuiType.ARENA_COPY_DELETE)) {
            arena.deleteLastCopy(true);
        }
    }

}
