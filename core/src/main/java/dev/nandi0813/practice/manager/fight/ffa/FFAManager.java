package dev.nandi0813.practice.manager.fight.ffa;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


@Getter
public class FFAManager {

    private static FFAManager instance;

    public static FFAManager getInstance() {
        if (instance == null)
            instance = new FFAManager();
        return instance;
    }

    private FFAManager() {
    }

    public List<FFA> getOpenFFAs() {
        List<FFA> ffas = new ArrayList<>();
        for (FFAArena ffaArena : ArenaManager.getInstance().getFFAArenas())
            if (ffaArena.getFfa().isOpen())
                ffas.add(ffaArena.getFfa());
        return ffas;
    }

    public void endFFAs() {
        for (FFAArena ffaArena : ArenaManager.getInstance().getFFAArenas())
            ffaArena.getFfa().close("");
    }

    public FFA getFFAByPlayer(Player player) {
        for (FFAArena ffaArena : ArenaManager.getInstance().getFFAArenas())
            if (ffaArena.getFfa().getPlayers().containsKey(player))
                return ffaArena.getFfa();
        return null;
    }

    public FFA getFFABySpectator(Player player) {
        Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(player);
        if (spectatable instanceof FFA)
            return (FFA) spectatable;
        else
            return null;
    }

}
