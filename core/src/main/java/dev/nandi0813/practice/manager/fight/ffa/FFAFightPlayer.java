package dev.nandi0813.practice.manager.fight.ffa;

import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice.manager.fight.util.KitSelectionHandler;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * FFA-specific fight player that handles custom kit selection.
 * Players can choose from their saved custom kits before entering combat.
 * Until a kit is selected, the player is in a spectator-like state (no interaction).
 */
@Getter
public class FFAFightPlayer extends FightPlayer {

    private final FFA ffa;
    private final NormalLadder ladder;
    
    private KitSelectionHandler kitSelectionHandler;
    private int chosenKit;

    public FFAFightPlayer(Player player, FFA ffa, NormalLadder ladder) {
        super(player, ffa);

        this.ffa = ffa;
        this.ladder = ladder;

        // Initialize kit selection handler if player has custom kits
        if (this.getProfile().getAllowedCustomKits() >= 1) {
            this.kitSelectionHandler = new KitSelectionHandler(player, getProfile(), ladder);
        }
    }

    /**
     * Displays the kit chooser GUI or applies the chosen kit.
     */
    public void showKitChooserOrApplyKit() {
        if (this.kitSelectionHandler != null) {
            this.kitSelectionHandler.showKitChooserOrApplyKit(TeamEnum.FFA);
        } else {
            // No custom kits, apply default kit directly
            applyDefaultKit();
        }
    }

    /**
     * Called when player clicks a kit slot to select it.
     * After selection, the player becomes a full combatant.
     */
    public void selectKit(int slot) {
        if (this.kitSelectionHandler != null && this.kitSelectionHandler.getKits() != null 
                && this.kitSelectionHandler.getKits().containsKey(slot)) {
            this.kitSelectionHandler.selectKit(slot, TeamEnum.FFA);
            this.chosenKit = slot;
        }
    }

    /**
     * Returns whether the player is waiting to select a kit.
     * While waiting, the player cannot be hurt or interact with others.
     */
    public boolean isWaitingForKitSelection() {
        return this.kitSelectionHandler != null && this.kitSelectionHandler.isWaitingForKitSelection();
    }

    /**
     * Applies the default ladder kit to the player.
     */
    public void applyDefaultKit() {
        this.kitSelectionHandler = new KitSelectionHandler(player, getProfile(), ladder);
        this.kitSelectionHandler.showKitChooserOrApplyKit(TeamEnum.FFA);
    }
}

