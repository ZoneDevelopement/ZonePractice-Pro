package dev.nandi0813.practice.manager.fight.event.events.duel.brackets;

import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelEvent;
import dev.nandi0813.practice.util.KitData;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Brackets extends DuelEvent {

    private final KitData selectedKitData;

    public Brackets(Object starter, BracketsData eventData) {
        this(starter, eventData, null);
    }

    public Brackets(Object starter, BracketsData eventData, KitData selectedKitData) {
        super(starter, eventData, "COMMAND.EVENT.ARGUMENTS.BRACKETS");

        // Snapshot the chosen kit so runtime edits do not mutate live event kits.
        this.selectedKitData = selectedKitData != null
                ? new KitData(selectedKitData)
                : new KitData(eventData.getKitData());
    }

    @Override
    public BracketsData getEventData() {
        return (BracketsData) eventData;
    }

    @Override
    public void teleport(Player player, Location location) {
        player.teleport(location);
        PlayerUtil.setFightPlayer(player);

        this.selectedKitData.loadKitData(player, true);
    }

}
