package dev.nandi0813.practice.manager.leaderboard.types;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import lombok.Getter;

import java.util.List;

@Getter
public enum LbSecondaryType {

    ELO(true),
    WIN(false),
    KILLS(false),
    DEATHS(false),
    WIN_STREAK(false),
    BEST_WIN_STREAK(false),
    LOSE_STREAK(false),
    BEST_LOSE_STREAK(false);

    private final boolean rankedRelated;

    private final double titleLineSpacing;
    private final double lineSpacing;

    private final String format;
    private final List<String> globalLines;
    private final List<String> ladderLines;

    LbSecondaryType(final boolean rankedRelated) {
        this.rankedRelated = rankedRelated;

        this.titleLineSpacing = ConfigManager.getConfig().getDouble("LEADERBOARD.HOLOGRAM.FORMAT." + this.name().toUpperCase() + ".TITLE-LINE-SPACING");
        this.lineSpacing = ConfigManager.getConfig().getDouble("LEADERBOARD.HOLOGRAM.FORMAT." + this.name().toUpperCase() + ".LINE-SPACING");
        this.format = ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT." + this.name().toUpperCase() + ".FORMAT");
        this.globalLines = ConfigManager.getConfig().getStringList("LEADERBOARD.HOLOGRAM.FORMAT." + this.name().toUpperCase() + ".LINES.GLOBAL");
        this.ladderLines = ConfigManager.getConfig().getStringList("LEADERBOARD.HOLOGRAM.FORMAT." + this.name().toUpperCase() + ".LINES.LADDER");
    }

}
