package dev.nandi0813.practice.manager.fight.ffa;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice.manager.fight.util.KitSelectionHandler;
import dev.nandi0813.practice.manager.fight.match.util.KitUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * FFA-specific fight player that handles custom kit selection.
 * Players can choose from their saved custom kits before entering combat.
 * Until a kit is selected, the player is in a spectator-like state (no interaction).
 */
@Getter
public class FFAFightPlayer extends FightPlayer {

    private final FFA ffa;
    private NormalLadder ladder;

    private KitSelectionHandler kitSelectionHandler;
    private int chosenKit = -1;

    private ItemStack[] savedInventory;
    private ItemStack[] savedArmor;
    private ItemStack[] savedExtra;

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
     * Used only for initial kit selection on join.
     */
    public void showKitChooserOrApplyKit() {
        if (this.kitSelectionHandler != null) {
            this.kitSelectionHandler.showKitChooserOrApplyKit(TeamEnum.FFA);

            // If the kit chooser books are shown, auto-select the default kit after
            // a timeout so the player doesn't stay invincible (unlimited HP) forever.
            if (this.kitSelectionHandler.isWaitingForKitSelection()) {
                scheduleDefaultKitFallback();
            }
        } else {
            applyDefaultKit();
        }
    }

    /**
     * Schedules the default kit fallback. If the player still hasn't selected a
     * custom kit after {@code FFA.CUSTOM-KIT-SELECTION-TIME} seconds, the default ladder
     * kit (slot 8) is applied so they become a full combatant.
     */
    private void scheduleDefaultKitFallback() {
        int seconds = ConfigManager.getInt("FFA.CUSTOM-KIT-SELECTION-TIME", 15);
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            // Only apply if the player is still in this FFA and hasn't chosen a kit.
            if (ffa.getPlayers().containsKey(player) && isWaitingForKitSelection()) {
                selectKit(8); // slot 8 = default ladder kit
            }
        }, seconds * 20L);
    }

    /**
     * Restores the player's kit on death.
     * Uses saved kit data if a custom kit was selected, otherwise applies the current ladder's default kit.
     */
    public void restoreKitOnDeath() {
        if (savedInventory != null) {
            KitUtil.loadKit(player, TeamEnum.FFA, savedArmor, savedInventory, savedExtra);
        } else {
            KitUtil.loadDefaultLadderKit(player, TeamEnum.FFA, ladder);
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

            KitSelectionHandler handler = this.kitSelectionHandler;
            savedInventory = cloneItems(handler.getKits().get(slot).getInventory());
            savedArmor = cloneItems(handler.getKits().get(slot).getArmor());
            savedExtra = cloneItems(handler.getKits().get(slot).getExtra());
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

    /**
     * Resets the player's state for a new ladder (called on /ffa kit switch).
     */
    public void resetForNewLadder(NormalLadder newLadder) {
        this.ladder = newLadder;
        this.chosenKit = -1;
        this.savedInventory = null;
        this.savedArmor = null;
        this.savedExtra = null;
        this.kitSelectionHandler = new KitSelectionHandler(player, getProfile(), newLadder);
    }

    private static ItemStack[] cloneItems(ItemStack[] source) {
        if (source == null) return null;
        ItemStack[] copy = source.clone();
        for (int i = 0; i < copy.length; i++) {
            if (copy[i] != null) copy[i] = copy[i].clone();
        }
        return copy;
    }
}