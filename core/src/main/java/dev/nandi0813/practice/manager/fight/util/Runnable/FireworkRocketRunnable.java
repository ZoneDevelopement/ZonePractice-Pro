package dev.nandi0813.practice.manager.fight.util.Runnable;

import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import org.bukkit.entity.Player;

public class FireworkRocketRunnable extends GameRunnable {

    public FireworkRocketRunnable(Player player, FightPlayer fightPlayer, int seconds, boolean expBar) {
        super(player, fightPlayer, seconds, CooldownObject.FIREWORK_ROCKET, expBar);
    }

    @Override
    public void abstractCancel() {
    }

}
