package dev.nandi0813.practice.manager.fight.event.events.duel.sumo;

import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.BracketsData;
import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelEvent;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Sumo extends DuelEvent {

    public Sumo(Object starter, SumoData sumoData) {
        super(starter, sumoData, "COMMAND.EVENT.ARGUMENTS.SUMO");
    }

    @Override
    public SumoData getEventData() {
        return (SumoData) eventData;
    }

    @Override
    public void teleport(Player player, Location location) {
        ClassImport.getClasses().getPlayerUtil().clearInventory(player);
        PlayerUtil.setFightPlayer(player);

        player.teleport(location);

        this.getEventData().getKitData().loadKitData(player, true);
    }

}
