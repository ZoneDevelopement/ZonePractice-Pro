package dev.nandi0813.practice.manager.gui.guis.selectors;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.type.partyffa.PartyFFA;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.party.Party;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class MatchStarterGui extends GUI {

    protected final MatchType matchType;
    protected final Ladder ladder;
    protected final GUI backTo;

    public MatchStarterGui(GUIType type, MatchType matchType, Ladder ladder, GUI backTo) {
        super(type);

        this.matchType = matchType;
        this.ladder = ladder;
        this.backTo = backTo;
    }

    @Nullable
    protected Match getMatch(Party party, Arena arena, int rounds) {
        Match match = null;
        List<Player> matchPlayers = new ArrayList<>(party.getMembers());

        if (party.getMembers().size() == 2) {
            match = new Duel(ladder, arena, matchPlayers, false, rounds);
        } else {
            if (matchType.equals(MatchType.PARTY_FFA)) {
                match = new PartyFFA(ladder, arena, party, rounds);
            } else if (matchType.equals(MatchType.PARTY_SPLIT)) {
                match = new PartySplit(ladder, arena, party, rounds);
            }
        }
        return match;
    }

}
