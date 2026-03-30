package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice.manager.fight.util.KitSelectionHandler;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import lombok.Getter;
import org.bukkit.entity.Player;

public class MatchFightPlayer extends FightPlayer {

    private final Match match;
    private final Ladder ladder;

    @Getter
    private boolean hasChosenKit;

    private KitSelectionHandler kitSelectionHandler;

    public MatchFightPlayer(Player player, Match match) {
        super(player, match);

        this.match = match;
        this.ladder = match.getLadder();

        this.hasChosenKit = true;
        if (this.getProfile().getAllowedCustomKits() >= 1 && ladder instanceof NormalLadder) {
            this.kitSelectionHandler = new KitSelectionHandler(player, getProfile(), ladder);
            this.hasChosenKit = this.kitSelectionHandler.isHasChosenKit();
            this.loadKits();
        }
    }

    public void loadKits() {
        if (this.kitSelectionHandler != null) {
            this.kitSelectionHandler.loadKitsForRanked(match instanceof Duel duel && duel.isRanked());
            this.hasChosenKit = this.kitSelectionHandler.isHasChosenKit();
        }
    }

    public void setKitChooserOrKit(TeamEnum team) {
        if (this.kitSelectionHandler != null) {
            this.kitSelectionHandler.showKitChooserOrApplyKit(team);
        } else {
            KitUtil.loadDefaultLadderKit(player, team, ladder);
        }
    }

    public void setChosenKit(int slot, TeamEnum team) {
        if (this.kitSelectionHandler != null) {
            this.kitSelectionHandler.selectKit(slot, team);
            this.hasChosenKit = this.kitSelectionHandler.isHasChosenKit();
        }
    }

}
