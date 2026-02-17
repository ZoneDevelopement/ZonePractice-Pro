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

/**
 * COMPLETELY REWRITTEN LadderDynamicHologram with strict ladder state tracking.
 *
 * Key changes:
 * - Tracks current ladder index to prevent double-rotations
 * - getNextLadder() enforces strict rotation logic
 * - Relies on parent Hologram class for despawn/spawn management
 * - No manual ArmorStand manipulation - parent handles all entities
 */
@Getter
public class LadderDynamicHologram extends Hologram {

    private List<NormalLadder> ladders;

    // Track current position in ladder rotation to prevent skips/doubles
    private int currentLadderIndex = -1;

    public LadderDynamicHologram(String name, Location baseLocation) {
        super(name, baseLocation, HologramType.LADDER_DYNAMIC);
        this.ladders = new ArrayList<>();
        this.currentLadderIndex = -1;
    }

    public LadderDynamicHologram(String name) {
        super(name, HologramType.LADDER_DYNAMIC);
        // currentLadderIndex initialized in getData via getAbstractData
    }

    @Override
    public void getAbstractData(YamlConfiguration config) {
        this.ladders = new ArrayList<>();
        this.currentLadderIndex = -1;

        if (config.isSet("holograms." + name + ".ladders")) {
            for (String ladderName : config.getStringList("holograms." + name + ".ladders")) {
                NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);
                if (ladder != null && ladder.isEnabled()) {
                    ladders.add(ladder);
                }
            }
        }

        // Initialize index to 0 if we have ladders
        if (!ladders.isEmpty()) {
            currentLadderIndex = 0;
        }
    }

    @Override
    public void setAbstractData(YamlConfiguration config) {
        if (!ladders.isEmpty()) {
            config.set("holograms." + name + ".ladders", getLadderNames(ladders));
        } else {
            config.set("holograms." + name + ".ladders", null);
        }
    }

    @Override
    public boolean isReadyToEnable() {
        return !ladders.isEmpty() && leaderboardType != null;
    }

    @Override
    public Leaderboard getNextLeaderboard() {
        Ladder nextLadder = getNextLadder();
        if (nextLadder == null) {
            return null;
        }

        return LeaderboardManager.getInstance().searchLB(
            hologramType.getLbMainType(),
            leaderboardType,
            nextLadder
        );
    }

    /**
     * STRICT LADDER ROTATION: Gets the next ladder in the rotation sequence.
     * Uses index-based rotation to ensure no skips or duplicates.
     *
     * This method is called during each hologram update cycle.
     * The parent Hologram class detects when the ladder changes and does a full respawn.
     *
     * @return The next ladder to display, or null if none available
     */
    public Ladder getNextLadder() {
        if (ladders == null || ladders.isEmpty()) {
            currentLadderIndex = -1;
            return null;
        }

        // If index is invalid, reset to 0
        if (currentLadderIndex < 0 || currentLadderIndex >= ladders.size()) {
            currentLadderIndex = 0;
        }

        // Get current ladder
        Ladder ladder = ladders.get(currentLadderIndex);

        // Advance to next ladder for next rotation
        currentLadderIndex = (currentLadderIndex + 1) % ladders.size();

        return ladder;
    }

    /**
     * Helper method to extract ladder names for config storage.
     *
     * @param ladders List of ladders
     * @return List of ladder names
     */
    private static List<String> getLadderNames(List<NormalLadder> ladders) {
        List<String> names = new ArrayList<>();
        for (Ladder ladder : ladders) {
            names.add(ladder.getName());
        }
        return names;
    }
}


