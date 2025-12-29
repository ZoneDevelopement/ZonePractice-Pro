package dev.nandi0813.practice.manager.leaderboard.hologram.holograms;

import dev.nandi0813.practice.manager.leaderboard.Leaderboard;
import dev.nandi0813.practice.manager.leaderboard.LeaderboardManager;
import dev.nandi0813.practice.manager.leaderboard.hologram.Hologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramType;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public class GlobalHologram extends Hologram {

    public GlobalHologram(String name, Location baseLocation) {
        super(name, baseLocation, HologramType.GLOBAL);
    }

    public GlobalHologram(String name) {
        super(name, HologramType.GLOBAL);
    }

    @Override
    public void getAbstractData(YamlConfiguration config) {
    }

    @Override
    public void setAbstractData(YamlConfiguration config) {
    }

    @Override
    public boolean isReadyToEnable() {
        return leaderboardType != null;
    }

    @Override
    public Leaderboard getNextLeaderboard() {
        return LeaderboardManager.getInstance().searchLB(hologramType.getLbMainType(), leaderboardType, null);
    }

}
