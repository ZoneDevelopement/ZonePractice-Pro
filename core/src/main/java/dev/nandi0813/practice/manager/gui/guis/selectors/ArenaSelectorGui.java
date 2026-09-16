package dev.nandi0813.practice.manager.gui.guis.selectors;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.duel.DuelManager;
import dev.nandi0813.practice.manager.duel.DuelRequest;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.party.matchrequest.PartyRequest;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import dev.nandi0813.practice.util.PageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArenaSelectorGui extends MatchStarterGui {

    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.BACK-TO-SELECTOR").get();
    private static final ItemStack RANDOM_ARENA_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.RANDOM-ARENA").get();

    private final int spaces = 45;
    private final Map<Integer, Map<Integer, Arena>> arenaIcons = new HashMap<>();

    public ArenaSelectorGui(Ladder ladder, MatchType matchType, GUI backTo) {
        super(GUIType.Arena_Selector, matchType, ladder, backTo);

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        List<Arena> arenas = new ArrayList<>(ladder.getAvailableArenas());
        arenas.sort(Comparator.comparing(Arena::getName, String::compareToIgnoreCase));

        Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
        Map<Integer, Inventory> newGui = new HashMap<>();
        Map<Integer, Map<Integer, Arena>> newArenaIcons = new HashMap<>();

        for (int page = 1; PageUtil.isPageValid(arenas.size(), page, spaces) || page == 1; page++) {
            Inventory inventory = existingInventories.get(page);
            String title = GUIFile.getString("GUIS.SELECTORS.ARENA-SELECTOR.TITLE").replace("%matchType%", this.matchType.getName(false)).replace("%page%", String.valueOf(page));
            if (inventory == null || inventory.getSize() != 6 * 9) {
                inventory = InventoryUtil.createInventory(title, 6);
            } else {
                inventory.clear();
            }
            newGui.put(page, inventory);

            // Frame
            for (int i : new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53})
                inventory.setItem(i, GUIManager.getFILLER_ITEM());

            Map<Integer, Arena> pageSlots = new HashMap<>();
            int startIndex = (page - 1) * spaces;
            int endIndex = Math.min(startIndex + spaces, arenas.size());
            for (int i = startIndex; i < endIndex; i++) {
                int slot = inventory.firstEmpty();
                if (slot == -1 || slot >= 45) {
                    break;
                }

                Arena arena = arenas.get(i);
                List<String> lore = new ArrayList<>();
                for (String line : GUIFile.getStringList("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.ARENA-ICON.LORE"))
                    lore.add(line.replace("%arena%", arena.getDisplayName()));

                ItemStack icon = ItemCreateUtil.createItem(arena.getIcon(), GUIFile.getString("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.ARENA-ICON.NAME").replace("%arena%", arena.getDisplayName()), lore);

                inventory.setItem(slot, icon);
                pageSlots.put(slot, arena);
            }
            newArenaIcons.put(page, pageSlots);

            ItemStack left = page == 1
                    ? BACK_TO_ITEM
                    : GUIFile.getGuiItem("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.PAGE-LEFT")
                    .replace("%page%", String.valueOf(page - 1))
                    .get();
            inventory.setItem(45, left);

            inventory.setItem(49, RANDOM_ARENA_ITEM);

            ItemStack right = PageUtil.isPageValid(arenas.size(), page + 1, spaces)
                    ? GUIFile.getGuiItem("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.PAGE-RIGHT")
                    .replace("%page%", String.valueOf(page + 1))
                    .get()
                    : GUIManager.getFILLER_ITEM();
            inventory.setItem(53, right);
        }

        for (Map.Entry<Integer, Inventory> entry : new LinkedHashMap<>(gui).entrySet()) {
            if (newGui.containsKey(entry.getKey())) {
                continue;
            }

            gui.remove(entry.getKey());
            for (Player player : inGuiPlayers.keySet()) {
                if (inGuiPlayers.get(player) == entry.getKey()) {
                    open(player, entry.getKey() - 1);
                }
            }
        }

        gui.putAll(newGui);
        arenaIcons.clear();
        arenaIcons.putAll(newArenaIcons);

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Party party = PartyManager.getInstance().getParty(player);
        Inventory inventory = e.getView().getTopInventory();
        int slot = e.getRawSlot();
        ItemStack currentItem = e.getCurrentItem();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (currentItem == null) return;
        if (currentItem.equals(GUIManager.getFILLER_ITEM())) return;

        int page = inGuiPlayers.getOrDefault(player, 1);

        if (slot == 45) {
            if (page > 1 && gui.containsKey(page - 1)) {
                open(player, page - 1);
            } else if (page == 1) {
                backTo.open(player);
            }
            return;
        }

        if (slot == 53) {
            if (gui.containsKey(page + 1)) {
                open(player, page + 1);
            }
            return;
        }

        if (!ladder.isEnabled() || !ladder.getMatchTypes().contains(matchType)) {
            Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.LADDER-NOT-AVAILABLE"));
            backTo.update();
            backTo.open(player);
            return;
        }

        if (ladder instanceof NormalLadder && ((NormalLadder) ladder).isFrozen()) {
            Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.LADDER-FROZEN"));
            backTo.open(player);
            return;
        }

        /*
         * Duel games arena selector
         */
        if (party == null) {
            Player target = DuelManager.getInstance().getPendingRequestTarget().get(player);

            if (!target.isOnline()) {
                Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.DUEL.TARGET-LEFT"));
                player.closeInventory();
                return;
            }

            if (slot == 49) {
                if (player.hasPermission("zpp.duel.selectrounds")) {
                    new DuelRoundSelectorGui(matchType, ladder, null, this).open(player);
                } else {
                    // Send the duel request
                    DuelManager.getInstance().sendRequest(new DuelRequest(player, target, ladder, null, ladder.getRounds()));
                    player.closeInventory();
                }
            } else {
                if (!arenaIcons.containsKey(page) || !arenaIcons.get(page).containsKey(slot)) return;

                Arena arena = arenaIcons.get(page).get(slot);
                if (arena.getAvailableArena() == null) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.DUEL.ARENA-NOT-AVAILABLE"));
                    update();
                    return;
                }

                if (player.hasPermission("zpp.duel.selectrounds")) {
                    new DuelRoundSelectorGui(matchType, ladder, arena, this).open(player);
                } else {
                    // Send the duel request
                    DuelManager.getInstance().sendRequest(new DuelRequest(player, target, ladder, arena, ladder.getRounds()));
                }
            }
        }
        /*
         * Party games arena selector
         */
        else {
            /*
             * Own party game arena selector
             */
            if (!this.matchType.equals(MatchType.PARTY_VS_PARTY)) {
                if (party.getMembers().size() < 2) {
                    player.closeInventory();
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.PARTY.NOT-ENOUGH-PLAYERS"));
                    return;
                }

                Arena arena;
                if (slot == 49) {
                    arena = LadderUtil.getAvailableArena(ladder);

                    if (arena == null) {
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.PARTY.NO-AVAILABLE-ARENA"));
                        return;
                    }
                } else if (arenaIcons.containsKey(page) && arenaIcons.get(page).containsKey(slot)) {
                    arena = arenaIcons.get(page).get(slot);
                } else
                    return;

                if (arena.getAvailableArena() == null) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.PARTY.ARENA-NOT-AVAILABLE"));
                    update();
                    return;
                }

                if (player.hasPermission("zpp.party.selectrounds")) {
                    new DuelRoundSelectorGui(matchType, ladder, arena, this).open(player);
                } else {
                    Match match = getMatch(party, arena, ladder.getRounds());
                    if (match == null) {
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.PARTY.ERROR"));
                        return;
                    }

                    party.setMatch(match);
                    match.startMatch();
                }
            }
            /*
             * Party vs party game arena selector
             */
            else {
                Party target = PartyManager.getInstance().getRequestManager().getPendingRequestTarget().get(party);

                if (!PartyManager.getInstance().getParties().contains(target)) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.PARTY.TARGET-PARTY-DISBANDED"));
                    GUIManager.getInstance().searchGUI(GUIType.Party_OtherParties).open(player);
                    return;
                }

                if (slot == 49) {
                    player.closeInventory();

                    if (player.hasPermission("zpp.party.selectrounds")) {
                        new DuelRoundSelectorGui(matchType, ladder, null, this).open(player);
                    } else {
                        PartyRequest partyRequest = new PartyRequest(party, target, ladder, null, ladder.getRounds());
                        partyRequest.sendRequest();
                    }
                } else {
                    if (!arenaIcons.containsKey(page) || !arenaIcons.get(page).containsKey(slot)) return;

                    Arena arena = arenaIcons.get(page).get(slot);

                    if (arena.getAvailableArena() == null) {
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.PARTY.ARENA-CURRENTLY-NOT-AVAILABLE"));
                        update();
                        return;
                    }

                    if (player.hasPermission("zpp.party.selectrounds")) {
                        new DuelRoundSelectorGui(matchType, ladder, arena, this).open(player);
                    } else {
                        PartyRequest partyRequest = new PartyRequest(party, target, ladder, arena, ladder.getRounds());
                        partyRequest.sendRequest();
                    }
                }
            }
        }
    }

}