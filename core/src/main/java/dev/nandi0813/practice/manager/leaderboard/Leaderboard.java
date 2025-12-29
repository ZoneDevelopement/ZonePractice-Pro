package dev.nandi0813.practice.manager.leaderboard;

import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.leaderboard.types.LbMainType;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import java.util.Map;

@Getter
public class Leaderboard {

    private final LbMainType mainType;
    private final LbSecondaryType secondaryType;

    private final NormalLadder ladder;
    @Setter
    private Map<OfflinePlayer, Integer> list;
    @Setter
    private boolean isUpdating;

    public Leaderboard(LbMainType mainType, LbSecondaryType secondaryType, NormalLadder ladder, Map<OfflinePlayer, Integer> list) {
        this.mainType = mainType;
        this.secondaryType = secondaryType;

        this.ladder = ladder;
        this.list = list;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

}
