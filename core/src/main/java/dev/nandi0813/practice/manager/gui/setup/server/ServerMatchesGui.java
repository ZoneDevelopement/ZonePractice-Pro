package dev.nandi0813.practice.manager.gui.setup.server;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.PageUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerMatchesGui extends GUI {

    private final GUI backTo;
    private static final int spaces = 27;
    private final Map<ItemStack, Match> matchIcons = new HashMap<>();

    public ServerMatchesGui(GUI backTo) {
        super(GUIType.Server_Matches);
        this.backTo = backTo;

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
            matchIcons.clear();

            for (Match match : MatchManager.getInstance().getLiveMatches())
                matchIcons.put(getMatchItem(match), match);

            List<ItemStack> iconsList = new ArrayList<>(matchIcons.keySet());

            int page = 1;
            while (PageUtil.isPageValid(iconsList, page, spaces) || page == 1) {
                if (!gui.containsKey(page))
                    gui.put(page, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.SERVER.MATCHES.TITLE").replace("%page%", String.valueOf(page)), 6));

                Inventory inventory = gui.get(page);
                inventory.clear();

                // Frame
                for (int i : new int[]{46, 47, 48, 49, 50, 51, 52})
                    inventory.setItem(i, GUIManager.getFILLER_ITEM());

                // Match icons
                for (ItemStack icon : PageUtil.getPageItems(iconsList, page, spaces)) {
                    int slot = inventory.firstEmpty();
                    inventory.setItem(slot, icon);
                }

                // Left navigation
                ItemStack left;
                if (PageUtil.isPageValid(iconsList, page - 1, spaces))
                    left = GUIFile.getGuiItem("GUIS.SETUP.SERVER.MATCHES.ICONS.GO-PAGE-LEFT").replaceAll("%page%", String.valueOf(page - 1)).get();
                else
                    left = GUIFile.getGuiItem("GUIS.SETUP.SERVER.MATCHES.ICONS.BACK-TO").get();
                inventory.setItem(45, left);

                // Right navigation
                ItemStack right;
                if (PageUtil.isPageValid(iconsList, page + 1, spaces))
                    right = GUIFile.getGuiItem("GUIS.SETUP.SERVER.MATCHES.ICONS.GO-PAGE-RIGHT").replaceAll("%page%", String.valueOf(page + 1)).get();
                else
                    right = GUIManager.getFILLER_ITEM();
                inventory.setItem(53, right);

                inventory.setItem(49, GUIFile.getGuiItem("GUIS.SETUP.SERVER.MATCHES.ICONS.REFRESH").get());

                page++;
            }

            if (gui.containsKey(page)) {
                final int finalPage = page;
                Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () ->
                {
                    gui.remove(finalPage);

                    for (Player player : inGuiPlayers.keySet()) {
                        if (inGuiPlayers.get(player) == finalPage)
                            open(player, finalPage - 1);
                    }
                });
            }
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        Inventory inventory = e.getClickedInventory();
        ClickType click = e.getClick();
        ItemStack item = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory == null) return;
        if (inventory.getSize() <= slot) return;
        if (item == GUIManager.getFILLER_ITEM()) return;

        int page = inGuiPlayers.get(player);

        switch (slot) {
            case 45:
                if (page == 1)
                    backTo.open(player);
                else
                    open(player, page - 1);
                break;
            case 53:
                if (gui.containsKey(page + 1))
                    open(player, page + 1);
                break;
            case 49:
                if (PlayerCooldown.isActive(player, CooldownObject.SERVER_SETUP_MATCH_REFRESH)) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.SERVER.WAIT-BEFORE"));
                    return;
                }

                update();
                PlayerCooldown.addCooldown(player, CooldownObject.SERVER_SETUP_MATCH_REFRESH, 5);
                break;
            default:
                if (matchIcons.containsKey(item)) {
                    Match match = matchIcons.get(item);

                    if (MatchManager.getInstance().getLiveMatches().contains(match)) {
                        if (click.isLeftClick()) {
                            match.addSpectator(player, null, true, false);
                        } else if (click.isRightClick()) {
                            match.sendMessage(LanguageManager.getString("COMMAND.SETUP.SERVER.MATCH-ENDED").replaceAll("%player%", player.getName()), true);
                            match.endMatch();

                            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.SERVER.PLAYER-ENDED-MATCH").replaceAll("%matchId%", match.getId()));
                            update();
                        }
                    } else {
                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.SERVER.MATCH-ALREADY-ENDED"));
                        update();
                    }
                }
                break;
        }
    }

    private ItemStack getMatchItem(Match match) {
        GUIItem guiItem = new GUIItem(
                GUIFile.getString("GUIS.SETUP.SERVER.MATCHES.ICONS.MATCH-ICON.NAME"),
                match.getLadder().getIcon().getType(),
                match.getLadder().getIcon().getDurability(),
                GUIFile.getStringList("GUIS.SETUP.SERVER.MATCHES.ICONS.MATCH-ICON.LORE")
        );
        guiItem
                .replaceAll("%matchType%", match.getType().getName(false))
                .replaceAll("%matchId%", match.getId())
                .replaceAll("%weightClass%", (match instanceof Duel && ((Duel) match).isRanked()) ? WeightClass.RANKED.getName() : WeightClass.UNRANKED.getName())
                .replaceAll("%matchType%", match.getType().getName(false))
                .replaceAll("%ladder%", match.getLadder().getDisplayName())
                .replaceAll("%arena%", match.getArena().getDisplayName())
                .replaceAll("%duration%", match.getCurrentRound().getFormattedTime())
                .replaceAll("%roundDuration%", match.getCurrentRound().getFormattedTime())
                .replaceAll("%matchDuration%", match.getFormattedTime())
                .replaceAll("%spectators%", String.valueOf(match.getSpectators().size()));

        return guiItem.get();
    }

}
