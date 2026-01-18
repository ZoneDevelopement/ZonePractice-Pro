package dev.nandi0813.practice.util.interfaces;

import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
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

}
