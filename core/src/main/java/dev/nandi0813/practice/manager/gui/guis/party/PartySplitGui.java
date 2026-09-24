package dev.nandi0813.practice.manager.gui.guis.party;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PartySplitGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.PARTY.PARTY-SPLIT.ICONS.FILLER-ITEM").get();

    private static final int BACK_SLOT = 45;
    private static final int RANDOM_SPLIT_SLOT = 49;
    private static final int START_MATCH_SLOT = 50;

    private final Party party;
    private final Ladder ladder;
    private final Arena arena;
    private final int rounds;
    private final GUI backTo;
    private final Map<Player, TeamEnum> assignments = new LinkedHashMap<>();
    private final Map<Integer, Player> headSlots = new LinkedHashMap<>();

    public PartySplitGui(Party party, Ladder ladder, Arena arena, int rounds, GUI backTo) {
        super(GUIType.Party_Split);
        this.party = party;
        this.ladder = ladder;
        this.arena = arena;
        this.rounds = rounds;
        this.backTo = backTo;

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.PARTY.PARTY-SPLIT.TITLE"), 6));

        build();
    }

    @Override
    public void build() {
        for (int i = 0; i < gui.get(1).getSize(); i++) {
            gui.get(1).setItem(i, FILLER_ITEM);
        }

        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, FILLER_ITEM);
        }

        inventory.setItem(BACK_SLOT, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SPLIT.ICONS.BACK-TO-SELECTOR").get());
        inventory.setItem(RANDOM_SPLIT_SLOT, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SPLIT.ICONS.RANDOM-SPLIT").get());
        inventory.setItem(START_MATCH_SLOT, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SPLIT.ICONS.START-MATCH").get());

        headSlots.clear();
        int slot = 0;
        for (Player member : party.getMembers()) {
            if (slot >= 45) break;

            headSlots.put(slot, member);
            inventory.setItem(slot, getPlayerItem(member));
            slot++;
        }

        updatePlayers();
    }

    private ItemStack getPlayerItem(Player member) {
        TeamEnum team = assignments.get(member);
        GUIItem configItem;
        if (team == null) {
            configItem = GUIFile.getGuiItem("GUIS.PARTY.PARTY-SPLIT.ICONS.PLAYER-ITEM.UNASSIGNED");
        } else if (team == TeamEnum.TEAM1) {
            configItem = GUIFile.getGuiItem("GUIS.PARTY.PARTY-SPLIT.ICONS.PLAYER-ITEM.BLUE");
        } else {
            configItem = GUIFile.getGuiItem("GUIS.PARTY.PARTY-SPLIT.ICONS.PLAYER-ITEM.RED");
        }

        GUIItem headItem = new GUIItem(ItemCreateUtil.getPlayerHead(member));
        headItem.setName(configItem.getName());
        headItem.setLore(configItem.getLore());
        return headItem.replace("%player%", member.getName()).get();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Party clickParty = PartyManager.getInstance().getParty(player);
        int slot = e.getRawSlot();
        e.setCancelled(true);

        if (clickParty == null || !clickParty.getLeader().equals(player)) return;
        if (slot >= gui.get(1).getSize()) return;

        if (slot == BACK_SLOT) {
            backTo.open(player);
        } else if (slot == RANDOM_SPLIT_SLOT) {
            randomSplit();
            update();
        } else if (slot == START_MATCH_SLOT) {
            startMatch(player);
        } else if (headSlots.containsKey(slot)) {
            Player member = headSlots.get(slot);
            if (e.isLeftClick())
                assignments.put(member, TeamEnum.TEAM1);
            else if (e.isRightClick())
                assignments.put(member, TeamEnum.TEAM2);
            update();
        }
    }

    private void randomSplit() {
        assignments.clear();

        List<Player> members = new ArrayList<>(party.getMembers());
        Collections.shuffle(members);

        List<Player> team1 = members.subList(0, members.size() / 2);
        List<Player> team2 = members.subList(members.size() / 2, members.size());
        for (Player member : team1)
            assignments.put(member, TeamEnum.TEAM1);
        for (Player member : team2)
            assignments.put(member, TeamEnum.TEAM2);
    }

    private void startMatch(Player player) {
        Map<TeamEnum, List<Player>> teamAssignments = null;

        if (party.getMembers().size() > 2) {
            assignUnassignedMembers();

            teamAssignments = buildTeamAssignments();
            if (teamAssignments.get(TeamEnum.TEAM1).isEmpty() || teamAssignments.get(TeamEnum.TEAM2).isEmpty()) {
                Common.sendMMMessage(player, LanguageManager.getString("PARTY.PARTY-SPLIT.BOTH-TEAMS-NEEDED"));
                return;
            }
        }

        player.closeInventory();

        Arena selectedArena = arena;
        if (selectedArena == null)
            selectedArena = LadderUtil.getAvailableArena(ladder);
        if (selectedArena == null || selectedArena.getAvailableArena() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("PARTY.NO-AVAILABLE-ARENA"));
            return;
        }

        Match match;
        if (party.getMembers().size() == 2) {
            match = new Duel(ladder, selectedArena, new ArrayList<>(party.getMembers()), false, rounds);
        } else {
            match = new PartySplit(ladder, selectedArena, party, rounds, teamAssignments);
        }

        party.setMatch(match);
        match.startMatch();
    }

    private void assignUnassignedMembers() {
        for (Player member : party.getMembers()) {
            if (assignments.containsKey(member)) continue;

            int team1Count = Collections.frequency(assignments.values(), TeamEnum.TEAM1);
            int team2Count = Collections.frequency(assignments.values(), TeamEnum.TEAM2);

            assignments.put(member, team1Count <= team2Count ? TeamEnum.TEAM1 : TeamEnum.TEAM2);
        }
    }

    private Map<TeamEnum, List<Player>> buildTeamAssignments() {
        Map<TeamEnum, List<Player>> teamAssignments = new EnumMap<>(TeamEnum.class);
        teamAssignments.put(TeamEnum.TEAM1, new ArrayList<>());
        teamAssignments.put(TeamEnum.TEAM2, new ArrayList<>());

        for (Map.Entry<Player, TeamEnum> entry : assignments.entrySet())
            teamAssignments.get(entry.getValue()).add(entry.getKey());

        return teamAssignments;
    }

}