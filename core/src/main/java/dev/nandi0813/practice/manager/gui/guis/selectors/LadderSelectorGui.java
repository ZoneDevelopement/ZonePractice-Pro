package dev.nandi0813.practice.manager.gui.guis.selectors;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.duel.DuelManager;
import dev.nandi0813.practice.manager.duel.DuelRequest;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.type.partyffa.PartyFFA;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.party.matchrequest.PartyRequest;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import dev.nandi0813.practice.util.PageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LadderSelectorGui extends GUI {

    private final Profile profile;
    private final MatchType matchType;

    private final int spaces = 45;
    private final Map<Integer, Map<Integer, Ladder>> ladderSlots = new HashMap<>();

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.LADDER-SELECTOR.ICONS.FILLER-ITEM").get();
    private static final GUIItem CUSTOM_PLAYER_KIT_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.LADDER-SELECTOR.ICONS.BASE-CUSTOM-PLAYER-KIT-ICON");
    private static final int CUSTOM_KIT_SLOT = 53;
    private static final int PAGE_LEFT_SLOT = 45;
    private static final int PAGE_RIGHT_SLOT = 49;

    public LadderSelectorGui(Profile profile, MatchType matchType) {
        super(GUIType.Ladder_Selector);
        this.profile = profile;
        this.matchType = matchType;

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        List<NormalLadder> ladders = new ArrayList<>();
        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            if (ladder.isEnabled() && ladder.isUnranked() && ladder.getMatchTypes().contains(matchType)) {
                ladders.add(ladder);
            }
        }

        Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
        Map<Integer, Inventory> newGui = new HashMap<>();
        Map<Integer, Map<Integer, Ladder>> newLadderSlots = new HashMap<>();

        for (int page = 1; PageUtil.isPageValid(ladders.size(), page, spaces) || page == 1; page++) {
            Inventory inventory = existingInventories.get(page);
            String title = GUIFile.getString("GUIS.SELECTORS.LADDER-SELECTOR.TITLE").replace("%matchType%", this.matchType.getName(false));
            if (inventory == null || inventory.getSize() != 6 * 9) {
                inventory = InventoryUtil.createInventory(title, 6);
            } else {
                inventory.clear();
            }
            newGui.put(page, inventory);

            for (int i = 45; i < 54; i++) {
                inventory.setItem(i, FILLER_ITEM);
            }

            Map<Integer, Ladder> pageSlots = new HashMap<>();
            int startIndex = (page - 1) * spaces;
            int endIndex = Math.min(startIndex + spaces, ladders.size());
            for (int i = startIndex; i < endIndex; i++) {
                int slot = inventory.firstEmpty();
                if (slot == -1 || slot >= 45) {
                    break;
                }

                NormalLadder ladder = ladders.get(i);
                List<String> lore = new ArrayList<>();
                for (String line : GUIFile.getStringList("GUIS.SELECTORS.LADDER-SELECTOR.ICONS.LADDER.LORE"))
                    lore.add(line.replace("%ladder%", ladder.getDisplayName()));

                ItemStack icon = ItemCreateUtil.createItem(ladder.getIcon(), GUIFile.getString("GUIS.SELECTORS.LADDER-SELECTOR.ICONS.LADDER.NAME").replace("%ladder%", ladder.getDisplayName()), lore);

                inventory.setItem(slot, icon);
                pageSlots.put(slot, ladder);
            }

            if (profile.getSelectedCustomLadder() != null || isPartyCustomKitSelector()) {
                GUIItem customPlayerKitItem = CUSTOM_PLAYER_KIT_ITEM.cloneItem();
                CustomLadder selectedCustomLadder = profile.getSelectedCustomLadder();
                ItemStack ladderIcon = selectedCustomLadder != null ? selectedCustomLadder.getIcon() : null;
                if (ladderIcon != null) {
                    // TODO: Custom ladder icon with name
                    if (customPlayerKitItem.getName() == null)
                        customPlayerKitItem.setName(CUSTOM_PLAYER_KIT_ITEM.getName());

                    customPlayerKitItem.setBaseItem(ladderIcon);
                }

                inventory.setItem(CUSTOM_KIT_SLOT, customPlayerKitItem.get());

                if (selectedCustomLadder != null) {
                    pageSlots.put(CUSTOM_KIT_SLOT, selectedCustomLadder);
                }
            }

            newLadderSlots.put(page, pageSlots);

            ItemStack left = page == 1
                    ? FILLER_ITEM
                    : GUIFile.getGuiItem("GUIS.SELECTORS.LADDER-SELECTOR.ICONS.PAGE-LEFT")
                    .replace("%page%", String.valueOf(page - 1))
                    .get();
            inventory.setItem(PAGE_LEFT_SLOT, left);

            ItemStack right = PageUtil.isPageValid(ladders.size(), page + 1, spaces)
                    ? GUIFile.getGuiItem("GUIS.SELECTORS.LADDER-SELECTOR.ICONS.PAGE-RIGHT")
                    .replace("%page%", String.valueOf(page + 1))
                    .get()
                    : FILLER_ITEM;
            inventory.setItem(PAGE_RIGHT_SLOT, right);
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
        ladderSlots.clear();
        ladderSlots.putAll(newLadderSlots);

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Party party = PartyManager.getInstance().getParty(player);
        Inventory inventory = e.getView().getTopInventory();

        int slot = e.getRawSlot();
        ItemStack currentItem = e.getCurrentItem();
        boolean customKitButtonClick = isPartyCustomKitButton(slot);

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (currentItem == null) return;
        if (currentItem.equals(FILLER_ITEM)) return;

        int page = inGuiPlayers.getOrDefault(player, 1);
        if (slot == PAGE_LEFT_SLOT) {
            if (page > 1 && gui.containsKey(page - 1)) {
                open(player, page - 1);
            }
        } else if (slot == PAGE_RIGHT_SLOT) {
            if (gui.containsKey(page + 1)) {
                open(player, page + 1);
            }
        } else if (customKitButtonClick && profile.getSelectedCustomLadder() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.PARTY.NO-CUSTOM-KIT-SELECTED"));
        } else if (ladderSlots.containsKey(page) && ladderSlots.get(page).containsKey(slot)) {
            Ladder ladder = ladderSlots.get(page).get(slot);

            if (ladder instanceof NormalLadder) {
                if (!ladder.isEnabled() || !ladder.getMatchTypes().contains(matchType)) {
                    Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.LADDER-NOT-AVAILABLE"));
                    update();
                    return;
                } else if (((NormalLadder) ladder).isFrozen()) {
                    Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.LADDER-FROZEN"));
                    return;
                }
            } else if (ladder instanceof CustomLadder) {
                if (!ladder.isEnabled()) {
                    Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.CUSTOM-LADDER-NOT-READY"));
                    return;
                } else if (!ladder.getMatchTypes().contains(matchType)) {
                    Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.CUSTOM-LADDER-CANT-PLAY-MATCHTYPE"));
                    return;
                }
            }

            /*
             * Duel games ladder selector
             */
            if (party == null) {
                if (player.hasPermission("zpp.duel.selectarena")) {
                    new ArenaSelectorGui(ladder, matchType, this).open(player);
                } else if (player.hasPermission("zpp.duel.selectrounds")) {
                    new DuelRoundSelectorGui(matchType, ladder, null, this).open(player);
                } else {
                    Player target = DuelManager.getInstance().getPendingRequestTarget().get(player);
                    DuelRequest request = new DuelRequest(player, target, ladder, null, ladder.getRounds());

                    if (target.isOnline())
                        DuelManager.getInstance().sendRequest(request);
                    else {
                        Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.DUEL.TARGET-LEFT"));
                        player.closeInventory();
                    }
                }
            }
            /*
             * Party games ladder selector
             */
            else {
                /*
                 * Own party game ladder selector
                 */
                if (!this.matchType.equals(MatchType.PARTY_VS_PARTY)) {
                    if (party.getMembers().size() < 2) {
                        player.closeInventory();
                        Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.PARTY.NOT-ENOUGH-PLAYERS"));
                        return;
                    }

                    if (customKitButtonClick && ladder instanceof CustomLadder) {
                        startPartyMatch(player, party, ladder, ladder.getRounds());
                        return;
                    }

                    if (player.hasPermission("zpp.party.selectarena")) {
                        new ArenaSelectorGui(ladder, matchType, this).open(player);
                    } else if (player.hasPermission("zpp.party.selectrounds")) {
                        new DuelRoundSelectorGui(matchType, ladder, null, this).open(player);
                    } else {
                        startPartyMatch(player, party, ladder, ladder.getRounds());
                    }
                }
                /*
                 * Party vs party ladder selector
                 */
                else {
                    Party target = PartyManager.getInstance().getRequestManager().getPendingRequestTarget().get(party);

                    if (!PartyManager.getInstance().getParties().contains(target)) {
                        Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.PARTY.TARGET-PARTY-DISBANDED"));
                        GUIManager.getInstance().searchGUI(GUIType.Party_OtherParties).open(player);
                        return;
                    }

                    if (player.hasPermission("zpp.party.selectarena")) {
                        new ArenaSelectorGui(ladder, matchType, this).open(player);
                    } else if (player.hasPermission("zpp.party.selectrounds")) {
                        new DuelRoundSelectorGui(matchType, ladder, null, this).open(player);
                    } else {
                        player.closeInventory();

                        Arena arena = LadderUtil.getAvailableArena(ladder);
                        if (arena == null) {
                            Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.PARTY.NO-AVAILABLE-ARENA"));
                            return;
                        }

                        // Send the game request
                        PartyRequest partyRequest = new PartyRequest(party, target, ladder, arena, ladder.getRounds());
                        partyRequest.sendRequest();
                    }
                }
            }
        }
    }

    @Nullable
    private Match getMatch(Party party, Ladder ladder, Arena arena, int rounds) {
        Match match = null;

        if (party.getMembers().size() == 2)
            match = new Duel(ladder, arena, party.getMembers(), false, rounds);
        else {
            if (matchType.equals(MatchType.PARTY_FFA))
                match = new PartyFFA(ladder, arena, party, rounds);
            else if (matchType.equals(MatchType.PARTY_SPLIT))
                match = new PartySplit(ladder, arena, party, rounds);
        }
        return match;
    }

    private boolean isPartyCustomKitSelector() {
        return matchType.equals(MatchType.PARTY_FFA) || matchType.equals(MatchType.PARTY_SPLIT);
    }

    private boolean isPartyCustomKitButton(int slot) {
        return isPartyCustomKitSelector() && slot == CUSTOM_KIT_SLOT;
    }

    private void startPartyMatch(Player player, Party party, Ladder ladder, int rounds) {
        Arena arena = LadderUtil.getAvailableArena(ladder);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.PARTY.NO-AVAILABLE-ARENA"));
            return;
        }

        player.closeInventory();

        Match match = getMatch(party, ladder, arena, rounds);
        if (match == null) {
            Common.sendMMMessage(player, LanguageManager.getString("LADDER.SELECTOR.PARTY.ERROR"));
            return;
        }

        party.setMatch(match);
        match.startMatch();
    }

}