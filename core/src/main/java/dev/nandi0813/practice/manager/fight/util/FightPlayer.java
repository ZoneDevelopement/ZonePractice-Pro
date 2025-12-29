package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.manager.fight.util.Runnable.GameRunnable;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class FightPlayer {

    protected final Player player;
    protected final UUID uuid;
    protected final Profile profile;

    protected final Spectatable spectatable;
    protected final List<GameRunnable> gameRunnables = new ArrayList<>();

    public FightPlayer(Player player, Spectatable spectatable) {
        this.player = player;
        this.uuid = ProfileManager.getInstance().getUuids().get(player);
        this.profile = ProfileManager.getInstance().getProfile(player);
        this.spectatable = spectatable;
    }

    public boolean isExpBarUsed() {
        for (GameRunnable gameRunnable : this.gameRunnables)
            if (gameRunnable.isExpBarUse())
                return true;
        return false;
    }

    public void die(String deathMessage, Statistic statistic) {
        for (GameRunnable gameRunnable : new ArrayList<>(this.gameRunnables))
            gameRunnable.cancel(true);

        if (deathMessage != null && !deathMessage.equalsIgnoreCase("")) {
            spectatable.sendMessage(deathMessage.replaceAll("%player%", this.player.getName()), true);
        }

        statistic.setDeaths(statistic.getDeaths() + 1);
    }

}
