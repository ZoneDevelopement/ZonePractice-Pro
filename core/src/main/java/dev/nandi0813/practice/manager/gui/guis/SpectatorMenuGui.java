package dev.nandi0813.practice.manager.gui.guis;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.util.FightUtil;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.PageUtil;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class SpectatorMenuGui extends GUI {

    private static final int spaces = 27;
    private final Map<ItemStack, Spectatable> icons = new HashMap<>();

    public SpectatorMenuGui() {
        super(GUIType.Spectator_Menu);

        build();
    }

    @Override
    public void build() {
        icons.clear();

        for (Spectatable spectatable : FightUtil.getSpectatables()) {
            if (spectatable.canDisplay())
                icons.put(spectatable.getSpectatorMenuItem().get(), spectatable);
        }

        List<ItemStack> iconsList = new ArrayList<>(icons.keySet());
        ItemStack fillerItem = GUIFile.getGuiItem("GUIS.SPECTATOR-MENU.ICONS.FILLER-ITEM").get();

        for (int page = 1; page < 20; page++) {
            if (PageUtil.isPageValid(iconsList, page, spaces) || page == 1) {
                if (!gui.containsKey(page))
                    gui.put(page, InventoryUtil.createInventory(GUIFile.getString("GUIS.SPECTATOR-MENU.TITLE").replace("%page%", String.valueOf(page)), 5));

                Inventory inventory = gui.get(page);
                inventory.clear();

                // Frame
                for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44})
                    inventory.setItem(i, fillerItem);

                // Match icons
                for (ItemStack icon : PageUtil.getPageItems(iconsList, page, spaces)) {
                    int slot = inventory.firstEmpty();
                    if (slot != -1 && slot < inventory.getSize()) {
                        inventory.setItem(slot, icon);
                    }
                }

                // Left navigation
                ItemStack left;
                if (PageUtil.isPageValid(iconsList, page - 1, spaces)) {
                    left = GUIFile.getGuiItem("GUIS.SPECTATOR-MENU.ICONS.PAGE-LEFT")
                            .replace("%page%", String.valueOf(page - 1))
                            .get();
                } else
                    left = GUIFile.getGuiItem("GUIS.SPECTATOR-MENU.ICONS.CLOSE").get();
                inventory.setItem(36, left);

                // Right navigation
                ItemStack right;
                if (PageUtil.isPageValid(iconsList, page + 1, spaces)) {
                    right = GUIFile.getGuiItem("GUIS.SPECTATOR-MENU.ICONS.PAGE-RIGHT")
                            .replace("%page%", String.valueOf(page + 1))
                            .get();
                } else
                    right = fillerItem;
                inventory.setItem(44, right);
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
        if (!ZonePractice.getInstance().isEnabled()) return;

        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            build();
            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        Inventory inventory = e.getView().getTopInventory();
        ItemStack item = e.getCurrentItem();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (item == null) return;
        if (item.getType().equals(Material.AIR)) return;

        if (inventory.getSize() > slot) {
            int page = inGuiPlayers.get(player);

            switch (slot) {
                case 36:
                    if (page == 1)
                        player.closeInventory();
                    else
                        open(player, page - 1);
                    break;
                case 44:
                    if (PageUtil.isPageValid(new ArrayList<>(icons.keySet()), page + 1, spaces))
                        open(player, page + 1);
                    break;
                default:
                    if (!icons.containsKey(item)) return;
                    Spectatable spectatable = icons.get(item);
                    if (spectatable == null) {
                        return;
                    }

                    if (!spectatable.canDisplay()) {
                        this.update();
                        return;
                    }
                    spectatable.addSpectator(player, null, true, !profile.isStaffMode());
                    break;
            }
        }
    }

}
