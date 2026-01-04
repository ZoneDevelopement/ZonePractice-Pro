package dev.nandi0813.practice.manager.gui.guis.selectors;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.duel.DuelManager;
import dev.nandi0813.practice.manager.duel.DuelRequest;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.party.matchrequest.PartyRequest;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DuelRoundSelectorGui extends MatchStarterGui {

    private static int MAX_ROUNDS = ConfigManager.getInt("MATCH-SETTINGS.DUEL.ROUND-SELECTOR.MAX");
    private static int MIN_ROUNDS = ConfigManager.getInt("MATCH-SETTINGS.DUEL.ROUND-SELECTOR.MIN");

    static {
        if (MAX_ROUNDS > 100) MAX_ROUNDS = 100;
        if (MIN_ROUNDS < 1) MIN_ROUNDS = 1;
    }

    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.KIT-EDITOR.DUEL-ROUND-SELECTOR.ICONS.BACK-TO").get();
    private static final GUIItem ROUND_SELECTOR = GUIFile.getGuiItem("GUIS.KIT-EDITOR.DUEL-ROUND-SELECTOR.ICONS.ROUND-SELECTOR");
    private static final GUIItem SHOW_LADDER = GUIFile.getGuiItem("GUIS.KIT-EDITOR.DUEL-ROUND-SELECTOR.ICONS.SHOW-LADDER");
    private static final GUIItem SHOW_ARENA = GUIFile.getGuiItem("GUIS.KIT-EDITOR.DUEL-ROUND-SELECTOR.ICONS.SHOW-ARENA");
    private static final ItemStack START_MATCH_ITEM = GUIFile.getGuiItem("GUIS.KIT-EDITOR.DUEL-ROUND-SELECTOR.ICONS.START-MATCH").get();

    private Arena arena;
    private int rounds;

    public DuelRoundSelectorGui(MatchType matchType, Ladder ladder, Arena arena, GUI backTo) {
        super(GUIType.DuelRound_Selector, matchType, ladder, backTo);

        this.arena = arena;
        this.rounds = ladder.getRounds();

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.KIT-EDITOR.DUEL-ROUND-SELECTOR.TITLE"), 1));

        this.build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        inventory.setItem(0, BACK_TO_ITEM);

        ItemStack ladderIcon = ladder.getIcon().clone();
        GUIItem ladderIconItem = SHOW_LADDER.cloneItem();
        if (ladderIcon != null && ladderIcon.getType() != null) {
            ladderIconItem
                    .setMaterial(ladderIcon.getType())
                    .setDamage(ladderIcon.getDurability());
        }

        inventory.setItem(5, ladderIconItem
                .replace("%ladder%", ladder.getDisplayName())
                .get());

        if (arena != null) {
            ItemStack arenaIcon = arena.getIcon().clone();
            GUIItem arenaIconItem = SHOW_ARENA.cloneItem();
            if (arenaIcon != null && arenaIcon.getType() != null) {
                arenaIconItem
                        .setMaterial(arenaIcon.getType())
                        .setDamage(arenaIcon.getDurability());
            }

            inventory.setItem(6, arenaIconItem
                    .replace("%arena%", arena.getDisplayName())
                    .get());
        }

        inventory.setItem(8, START_MATCH_ITEM);

        update();
    }

    @Override
    public void update() {
        gui.get(1).setItem(3, ROUND_SELECTOR.cloneItem()
                .replace("%rounds%", String.valueOf(rounds))
                .replace("%recommended_rounds%", String.valueOf(ladder.getRounds()))
                .get());

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        Party party = PartyManager.getInstance().getParty(player);
        int slot = e.getRawSlot();

        if (slot == 0) {
            backTo.open(player);
        } else if (slot == 3) {
            if (e.isRightClick() && rounds < MAX_ROUNDS) {
                this.rounds++;
                this.update();
            } else if (e.isLeftClick() && rounds > MIN_ROUNDS) {
                this.rounds--;
                this.update();
            }
        } else if (slot == 8) {
            if (!ladder.isEnabled() || !ladder.getMatchTypes().contains(matchType)) {
                Common.sendMMMessage(player, LanguageManager.getString("DUEL-ROUND-SELECTOR.LADDER-NOT-AVAILABLE"));

                LadderSelectorGui ladderSelectorGui = getLadderBackToGui();
                if (ladderSelectorGui != null) {
                    ladderSelectorGui.update();
                    ladderSelectorGui.open(player);
                    return;
                } else {
                    player.closeInventory();
                }
            }

            if (ladder instanceof NormalLadder && ((NormalLadder) ladder).isFrozen()) {
                Common.sendMMMessage(player, LanguageManager.getString("DUEL-ROUND-SELECTOR.LADDER-FROZEN"));

                LadderSelectorGui ladderSelectorGui = getLadderBackToGui();
                if (ladderSelectorGui != null) {
                    ladderSelectorGui.update();
                    ladderSelectorGui.open(player);
                    return;
                } else {
                    player.closeInventory();
                }
            }

            if (arena != null && (!arena.isEnabled() || arena.isFrozen())) {
                Common.sendMMMessage(player, LanguageManager.getString("DUEL-ROUND-SELECTOR.ARENA-NOT-AVAILABLE"));
                arena = null;
            }

            /*
             * Arena availability is checked in the Duel Request
             */

            /*
             * Duel games arena selector
             */
            if (party == null) {
                Player target = DuelManager.getInstance().getPendingRequestTarget().get(player);

                if (!target.isOnline()) {
                    Common.sendMMMessage(player, LanguageManager.getString("DUEL-ROUND-SELECTOR.ENEMY-OFFLINE"));
                    player.closeInventory();
                    return;
                }

                DuelManager.getInstance().sendRequest(new DuelRequest(player, target, ladder, arena, rounds));
                player.closeInventory();
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
                        Common.sendMMMessage(player, LanguageManager.getString("DUEL-ROUND-SELECTOR.PARTY-NOT-ENOUGH-PLAYERS"));
                        return;
                    }

                    Match match = getMatch(party, arena, rounds);
                    if (match == null) {
                        Common.sendMMMessage(player, LanguageManager.getString("DUEL-ROUND-SELECTOR.PARTY-RANDOM-ERROR"));
                        return;
                    }

                    party.setMatch(match);
                    match.startMatch();
                }
                /*
                 * Party vs party game arena selector
                 */
                else {
                    Party target = PartyManager.getInstance().getRequestManager().getPendingRequestTarget().get(party);

                    if (!PartyManager.getInstance().getParties().contains(target)) {
                        Common.sendMMMessage(player, LanguageManager.getString("DUEL-ROUND-SELECTOR.ENEMY-PARTY-OFFLINE"));
                        GUIManager.getInstance().searchGUI(GUIType.Party_OtherParties).open(player);
                        return;
                    }

                    PartyRequest partyRequest = new PartyRequest(party, target, ladder, arena, rounds);
                    partyRequest.sendRequest();
                }
            }
        }
    }

    private LadderSelectorGui getLadderBackToGui() {
        if (backTo instanceof LadderSelectorGui) {
            return (LadderSelectorGui) backTo;
        } else if (backTo instanceof ArenaSelectorGui arenaSelectorGui) {
            if (arenaSelectorGui.backTo instanceof LadderSelectorGui) {
                return (LadderSelectorGui) arenaSelectorGui.backTo;
            }
        }
        return null;
    }

}
