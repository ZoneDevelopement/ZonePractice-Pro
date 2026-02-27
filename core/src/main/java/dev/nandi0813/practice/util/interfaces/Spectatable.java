package dev.nandi0813.practice.util.interfaces;

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
