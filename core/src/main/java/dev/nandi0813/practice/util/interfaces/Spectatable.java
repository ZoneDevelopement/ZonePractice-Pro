package dev.nandi0813.practice.util.interfaces;

import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.module.interfaces.ChangedBlock;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public interface Spectatable {

    List<Player> getSpectators();

    void addSpectator(Player spectator, Player target, boolean teleport, boolean message);

    void removeSpectator(Player player);

    boolean canDisplay();

    GUIItem getSpectatorMenuItem();

    Cuboid getCuboid();

    void sendMessage(String message, boolean spectate);

    FightChangeOptimized getFightChange();

    /**
     * Returns true if this fight context has build mechanics enabled.
     * Block tracking and rollback only apply when this returns true.
     */
    boolean isBuild();

    /**
     * Returns {@code true} if the "break all blocks" setting is active for this
     * fight context, meaning explosions (and player breaks) may destroy any arena
     * block — not just player-placed ones.
     * <p>
     * For a {@link Match} this checks the single match ladder.
     * For an {@link FFA} this returns {@code true} if ANY of the assigned ladders
     * has the setting enabled (since multiple ladder types can coexist in FFA).
     */
    default boolean isBreakAllBlocks() {
        if (this instanceof Match match) {
            return match.getLadder().isBreakAllBlocks();
        }
        if (this instanceof FFA ffa) {
            return ffa.getPlayers().values().stream()
                    .anyMatch(ladder -> ladder != null && ladder.isBreakAllBlocks());
        }
        return false;
    }

    /**
     * Track a block change for rollback.
     * Delegates to {@link FightChangeOptimized#addBlockChange(ChangedBlock)}.
     */
    default void addBlockChange(ChangedBlock changedBlock) {
        FightChangeOptimized fc = getFightChange();
        if (fc != null) fc.addBlockChange(changedBlock);
    }

    /**
     * Track an entity for removal during rollback.
     * Delegates to {@link FightChangeOptimized#addEntityChange(Entity)}.
     */
    default void addEntityChange(Entity entity) {
        FightChangeOptimized fc = getFightChange();
        if (fc != null) fc.addEntityChange(entity);
    }

}
