package dev.nandi0813.practice.manager.leaderboard.hologram.holograms;

import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.leaderboard.Leaderboard;
import dev.nandi0813.practice.manager.leaderboard.LeaderboardManager;
import dev.nandi0813.practice.manager.leaderboard.hologram.Hologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dynamic hologram that rotates through multiple ladders on a timer.
 */
@Getter
public class LadderDynamicHologram extends Hologram {

    private List<NormalLadder> ladders;
    private int currentLadderIndex = -1;

    public LadderDynamicHologram(String name, Location baseLocation) {
        super(name, baseLocation, HologramType.LADDER_DYNAMIC);
        this.ladders = new ArrayList<>();
    }

    public LadderDynamicHologram(String name) {
        super(name, HologramType.LADDER_DYNAMIC);
    }

    @Override
    public void getAbstractData(YamlConfiguration config) {
        ladders = new ArrayList<>();
        currentLadderIndex = -1;

        String path = "holograms." + name + ".ladders";
        if (config.isSet(path)) {
            List<String> ladderNames = config.getStringList(path);
            if (ladderNames != null && !ladderNames.isEmpty()) {
                for (String ladderName : ladderNames) {
                    NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);
                    if (ladder != null && ladder.isEnabled()) {
                        ladders.add(ladder);
                    }
                }
            }
        }


        if (!ladders.isEmpty()) {
            currentLadderIndex = 0;
        }
    }

    @Override
    public void setAbstractData(YamlConfiguration config) {
        String path = "holograms." + name + ".ladders";
        if (ladders.isEmpty()) {
            config.set(path, null);
        } else {
            config.set(path, ladders.stream().map(Ladder::getName).collect(Collectors.toList()));
        }
    }

    @Override
    public boolean isReadyToEnable() {
        return !ladders.isEmpty() && leaderboardType != null;
    }

    @Override
    public Leaderboard getNextLeaderboard() {
        Ladder ladder = getCurrentLadder();
        if (ladder == null) {
            return null;
        }
        return LeaderboardManager.getInstance().searchLB(hologramType.getLbMainType(), leaderboardType, ladder);
    }

    /**
     * Gets the current ladder without advancing rotation.
     */
    public Ladder getCurrentLadder() {
        if (ladders.isEmpty()) {
            currentLadderIndex = -1;
            return null;
        }

        if (currentLadderIndex < 0 || currentLadderIndex >= ladders.size()) {
            currentLadderIndex = 0;
        }

        return ladders.get(currentLadderIndex);
    }

    /**
     * @deprecated Use {@link #getCurrentLadder()} instead
     */
    @Deprecated
    public Ladder getNextLadder() {
        return getCurrentLadder();
    }

    /**
     * Advances to the next ladder in rotation.
     * Called by HologramRunnable on timer tick.
     */
    public void rotateLadder() {
        if (ladders.isEmpty()) {
            currentLadderIndex = -1;
            return;
        }
        currentLadderIndex = (currentLadderIndex + 1) % ladders.size();
    }
}
