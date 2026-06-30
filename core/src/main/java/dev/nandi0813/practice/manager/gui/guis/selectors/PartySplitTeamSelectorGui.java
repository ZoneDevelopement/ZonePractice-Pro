package dev.nandi0813.practice.manager.gui.guis.selectors;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lets the party owner assign each party member to a team (left click cycles a player's
 * head between Team 1 and Team 2) before starting a Party Split match. The GUI grows in
 * size as more players are in the party, and a confirm button in the bottom-right corner
 * starts the match once both teams have at least one player.
 */
public class PartySplitTeamSelectorGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.PARTY-SPLIT-TEAM-SELECTOR.ICONS.FILLER-ITEM").get();
    private static final ItemStack BACK_TO_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.PARTY-SPLIT-TEAM-SELECTOR.ICONS.BACK-TO-SELECTOR").get();
    private static final GUIItem UNASSIGNED_PLAYER_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.PARTY-SPLIT-TEAM-SELECTOR.ICONS.UNASSIGNED-PLAYER");
    private static final GUIItem TEAM1_PLAYER_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.PARTY-SPLIT-TEAM-SELECTOR.ICONS.TEAM1-PLAYER");
    private static final GUIItem TEAM2_PLAYER_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.PARTY-SPLIT-TEAM-SELECTOR.ICONS.TEAM2-PLAYER");
    private static final ItemStack CONFIRM_INCOMPLETE_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.PARTY-SPLIT-TEAM-SELECTOR.ICONS.CONFIRM-INCOMPLETE").get();
    private static final ItemStack CONFIRM_READY_ITEM = GUIFile.getGuiItem("GUIS.SELECTORS.PARTY-SPLIT-TEAM-SELECTOR.ICONS.CONFIRM-READY").get();

    private static final int MIN_ROWS = 2;
    private static final int MAX_ROWS = 6;

    private final Party party;
    private final Ladder ladder;
    private final Arena arena;
    private final int rounds;
    private final GUI backTo;

    // Player -> assigned team (TEAM1/TEAM2), unassigned players are simply absent from this map
    private final Map<Player, TeamEnum> assignments = new HashMap<>();
    private final Map<Integer, Player> playerSlots = new LinkedHashMap<>();

    private int backSlot;
    private int confirmSlot;

    public PartySplitTeamSelectorGui(Ladder ladder, Arena arena, int rounds, Party party, GUI backTo) {
        super(GUIType.PartySplit_TeamSelector);

        this.party = party;
        this.ladder = ladder;
        this.arena = arena;
        this.rounds = rounds;
        this.backTo = backTo;

        int rows = computeRows(party.getMembers().size());
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SELECTORS.PARTY-SPLIT-TEAM-SELECTOR.TITLE"), rows));

        build();
    }

    private int computeRows(int playerCount) {
        // Reserve the last row for controls (back + confirm), the rest hold player heads
        int contentRowsNeeded = (int) Math.ceil(playerCount / 9.0);
        int rows = contentRowsNeeded + 1;
        if (rows < MIN_ROWS) rows = MIN_ROWS;
        if (rows > MAX_ROWS) rows = MAX_ROWS;
        return rows;
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);
        int size = inventory.getSize();
        int controlRowStart = size - 9;

        for (int i = 0; i < size; i++) {
            inventory.setItem(i, FILLER_ITEM);
        }

        playerSlots.clear();

        // Remove assignments for players who are no longer in the party
        assignments.keySet().removeIf(player -> !party.getMembers().contains(player));

        int slot = 0;
        for (Player member : party.getMembers()) {
            if (slot >= controlRowStart) {
                // Ran out of space (extremely large party) - stop placing more heads
                break;
            }

            ItemStack head = buildHeadItem(member);
            inventory.setItem(slot, head);
            playerSlots.put(slot, member);
            slot++;
        }

        backSlot = controlRowStart;
        confirmSlot = size - 1;

        inventory.setItem(backSlot, BACK_TO_ITEM);
        inventory.setItem(confirmSlot, hasBothTeams() ? CONFIRM_READY_ITEM : CONFIRM_INCOMPLETE_ITEM);

        updatePlayers();
    }

    private ItemStack buildHeadItem(Player member) {
        ItemStack baseHead = ItemCreateUtil.getPlayerHead(member);
        TeamEnum team = assignments.get(member);

        GUIItem template;
        if (team == TeamEnum.TEAM1) {
            template = TEAM1_PLAYER_ITEM.cloneItem();
        } else if (team == TeamEnum.TEAM2) {
            template = TEAM2_PLAYER_ITEM.cloneItem();
        } else {
            template = UNASSIGNED_PLAYER_ITEM.cloneItem();
        }

        template.setBaseItem(baseHead);
        template.replace("%player%", member.getName());

        if (team != null) {
            template.replace("%team%", team.getNameMM());
            template.replace("%teamColor%", team.getColorMM());
        }

        return template.get();
    }

    private boolean hasBothTeams() {
        boolean hasTeam1 = assignments.containsValue(TeamEnum.TEAM1);
        boolean hasTeam2 = assignments.containsValue(TeamEnum.TEAM2);
        return hasTeam1 && hasTeam2;
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player owner = (Player) e.getWhoClicked();
        e.setCancelled(true);

        // Only the party owner can assign teams
        if (party.getLeader() != owner) return;

        int slot = e.getRawSlot();
        Inventory inventory = e.getView().getTopInventory();
        if (inventory.getSize() <= slot) return;

        if (slot == backSlot) {
            if (backTo != null) {
                backTo.open(owner);
            } else {
                owner.closeInventory();
            }
            return;
        }

        if (slot == confirmSlot) {
            handleConfirm(owner);
            return;
        }

        if (!playerSlots.containsKey(slot)) return;

        Player target = playerSlots.get(slot);
        cycleTeam(target);
        update();
    }

    private void cycleTeam(Player target) {
        TeamEnum current = assignments.get(target);

        if (current == null) {
            assignments.put(target, TeamEnum.TEAM1);
        } else if (current == TeamEnum.TEAM1) {
            assignments.put(target, TeamEnum.TEAM2);
        } else {
            assignments.remove(target);
        }
    }

    private void handleConfirm(Player owner) {
        if (party.getMembers().size() < 2) {
            owner.closeInventory();
            Common.sendMMMessage(owner, LanguageManager.getString("LADDER.SELECTOR.PARTY.NOT-ENOUGH-PLAYERS"));
            return;
        }

        if (!hasBothTeams()) {
            Common.sendMMMessage(owner, LanguageManager.getString("LADDER.SELECTOR.PARTY.ERROR"));
            return;
        }

        Map<TeamEnum, List<Player>> presetTeams = new EnumMap<>(TeamEnum.class);
        presetTeams.put(TeamEnum.TEAM1, new java.util.ArrayList<>());
        presetTeams.put(TeamEnum.TEAM2, new java.util.ArrayList<>());

        for (Player member : party.getMembers()) {
            TeamEnum team = assignments.get(member);
            // Any player who wasn't manually assigned a team is auto-balanced into
            // whichever team currently has fewer players, so nobody is left out of the match.
            if (team == null) {
                team = presetTeams.get(TeamEnum.TEAM1).size() <= presetTeams.get(TeamEnum.TEAM2).size()
                        ? TeamEnum.TEAM1
                        : TeamEnum.TEAM2;
            }
            presetTeams.get(team).add(member);
        }

        owner.closeInventory();

        Match match = new PartySplit(ladder, arena, party, rounds, presetTeams);
        party.setMatch(match);
        match.startMatch();
    }

          }
