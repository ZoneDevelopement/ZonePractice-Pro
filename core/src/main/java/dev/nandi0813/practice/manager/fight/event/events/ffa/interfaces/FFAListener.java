package dev.nandi0813.practice.manager.fight.event.events.ffa.interfaces;

import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventListenerInterface;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class FFAListener extends EventListenerInterface {

    @Override
    public void onPlayerQuit(Event event, PlayerQuitEvent e) {
        if (event instanceof FFAEvent ffaEvent) {
            ffaEvent.removePlayer(e.getPlayer(), true);
        }
    }

}
