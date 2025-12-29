package dev.nandi0813.practice.manager.fight.util.Runnable;

import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import org.bukkit.entity.Player;

public class EnderPearlRunnable extends GameRunnable {

    public EnderPearlRunnable(Player player, FightPlayer fightPlayer, int seconds, boolean expBar) {
        super(player, fightPlayer, seconds, CooldownObject.ENDER_PEARL, expBar);
    }

    @Override
    public void abstractCancel() {
    }

}
