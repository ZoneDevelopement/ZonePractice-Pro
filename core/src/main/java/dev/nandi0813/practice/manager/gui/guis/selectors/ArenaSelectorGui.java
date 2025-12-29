package dev.nandi0813.practice.manager.gui.guis.selectors;

import dev.nandi0813.practice.ZonePractice;
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
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaSelectorGui extends MatchStarterGui {

    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.BACK-TO-SELECTOR").get();
    private static final ItemStack RANDOM_ARENA_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.RANDOM-ARENA").get();

    private final Map<Integer, Arena> arenaIcons = new HashMap<>();

    public ArenaSelectorGui(Ladder ladder, MatchType matchType, GUI backTo) {
        super(GUIType.Arena_Selector, matchType, ladder, backTo);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SELECTORS.ARENA-SELECTOR.TITLE").replace("%matchType%", matchType.getName(false)), 6));

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
            Inventory inventory = gui.get(1);
            arenaIcons.clear();
            inventory.clear();

            // Frame
            for (int i : new int[]{46, 47, 48, 50, 51, 52, 53})
                inventory.setItem(i, GUIManager.getFILLER_ITEM());

            inventory.setItem(45, BACK_TO_ITEM);
            inventory.setItem(49, RANDOM_ARENA_ITEM);

            for (Arena arena : ladder.getAvailableArenas()) {
                List<String> lore = new ArrayList<>();
                for (String line : GUIFile.getStringList("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.ARENA-ICON.LORE"))
                    lore.add(line.replaceAll("%arena%", arena.getDisplayName()));

                ItemStack icon = ClassImport.getClasses().getItemCreateUtil().createItem(arena.getIcon(), GUIFile.getString("GUIS.SELECTORS.ARENA-SELECTOR.ICONS.ARENA-ICON.NAME").replace("%arena%", arena.getDisplayName()), lore);

                int slot = inventory.firstEmpty();
                inventory.setItem(slot, icon);
                arenaIcons.put(slot, arena);
            }
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Party party = PartyManager.getInstance().getParty(player);
        Inventory inventory = e.getView().getTopInventory();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;

        if (slot == 45) {
            backTo.open(player);
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
                if (player.hasPermission("zpp.duel.selectrounds") && ladder instanceof NormalLadder) {
                    new DuelRoundSelectorGui(matchType, ladder, null, this).open(player);
                } else {
                    // Send the duel request
                    DuelManager.getInstance().sendRequest(new DuelRequest(player, target, ladder, null, ladder.getRounds()));
                    player.closeInventory();
                }
            } else {
                if (!arenaIcons.containsKey(slot)) return;

                Arena arena = arenaIcons.get(slot);
                if (arena.getAvailableArena() == null) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.DUEL.ARENA-NOT-AVAILABLE"));
                    update();
                    return;
                }

                if (player.hasPermission("zpp.duel.selectrounds") && ladder instanceof NormalLadder) {
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
                } else if (arenaIcons.containsKey(slot)) {
                    arena = arenaIcons.get(slot);
                } else
                    return;

                if (arena.getAvailableArena() == null) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.PARTY.ARENA-NOT-AVAILABLE"));
                    update();
                    return;
                }

                if (player.hasPermission("zpp.party.selectrounds") && ladder instanceof NormalLadder) {
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

                    if (player.hasPermission("zpp.party.selectrounds") && ladder instanceof NormalLadder) {
                        new DuelRoundSelectorGui(matchType, ladder, null, this).open(player);
                    } else {
                        PartyRequest partyRequest = new PartyRequest(party, target, ladder, null, ladder.getRounds());
                        partyRequest.sendRequest();
                    }
                } else {
                    if (!arenaIcons.containsKey(slot)) return;

                    Arena arena = arenaIcons.get(slot);

                    if (arena.getAvailableArena() == null) {
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.SELECTOR.PARTY.ARENA-CURRENTLY-NOT-AVAILABLE"));
                        update();
                        return;
                    }

                    if (player.hasPermission("zpp.party.selectrounds") && ladder instanceof NormalLadder) {
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
