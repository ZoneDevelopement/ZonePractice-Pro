package dev.nandi0813.practice.manager.fight.event.events.ffa.splegg;

import dev.nandi0813.practice.manager.fight.event.events.ffa.interfaces.FFAEvent;
import dev.nandi0813.practice.module.util.ClassImport;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Splegg extends FFAEvent {

    private final Map<Player, Integer> shotEggs = new HashMap<>();
    private final Map<Player, Integer> shotBlocks = new HashMap<>();

    public Splegg(Object starter, SpleggData eventData) {
        super(starter, eventData, "COMMAND.EVENT.ARGUMENTS.SPLEGG");
    }

    @Override
    protected void customCustomStart() {
        for (Player player : this.players) {
            this.shotEggs.put(player, 0);
            this.shotBlocks.put(player, 0);
        }
    }

    @Override
    protected void loadInventory(Player player) {
        ClassImport.getClasses().getPlayerUtil().clearInventory(player);
        player.getInventory().addItem(this.getEventData().getEggLauncher());
        player.updateInventory();
    }

    @Override
    public SpleggData getEventData() {
        return (SpleggData) this.eventData;
    }
}
