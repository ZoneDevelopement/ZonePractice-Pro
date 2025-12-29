package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.manager.arena.util.BedLocation;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public abstract class BedUtil implements Listener {

    public abstract boolean onBedBreak(final @NotNull BlockBreakEvent e, final @NotNull Match match);

    public abstract BedLocation getBedLocation(Block block);

    public abstract void placeBed(Location loc, BlockFace face);

    protected void sendBedDestroyMessage(Match match, TeamEnum team) {
        String languagePath = switch (match.getLadder().getType()) {
            case BEDWARS -> "MATCH." + match.getType().getPathName() + ".LADDER-SPECIFIC.BED-WARS";
            case FIREBALL_FIGHT -> "MATCH." + match.getType().getPathName() + ".LADDER-SPECIFIC.FIREBALL-FIGHT";
            default -> null;
        };

        if (languagePath == null) return;

        match.sendMessage(LanguageManager.getString(languagePath + ".BED-DESTROYED")
                        .replaceAll("%team%", team.getNameMM())
                        .replaceAll("%teamColor%", team.getColorMM())
                , true);
    }

}
