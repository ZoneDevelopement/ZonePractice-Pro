package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.manager.arena.util.BedLocation;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BedUtil {

    public static BedLocation getBedLocation(Block block) {
        if (block == null) return null;

        Location bedLoc = block.getLocation();
        Bed bed = (Bed) block.getState().getBlockData();

        if (bed.getPart().equals(Bed.Part.HEAD))
            bedLoc = block.getRelative(bed.getFacing().getOppositeFace()).getLocation();

        return new BedLocation(bedLoc.getWorld(), bedLoc.getX(), bedLoc.getY(), bedLoc.getZ(), bed.getFacing());
    }

    public static void placeBed(Location loc, BlockFace face) {
        Block bedFoot = loc.getBlock();
        bedFoot.setBlockData(Material.RED_BED.createBlockData());
        Bed bedFootData = (Bed) Bukkit.createBlockData(Material.RED_BED);
        bedFootData.setPart(Bed.Part.FOOT);
        bedFootData.setFacing(face);

        Block bedHead = bedFoot.getRelative(face);
        bedHead.setBlockData(Material.RED_BED.createBlockData());
        Bed bedHeadData = (Bed) Bukkit.createBlockData(Material.RED_BED);
        bedHeadData.setPart(Bed.Part.HEAD);
        bedHeadData.setFacing(face);

        bedFoot.setBlockData(bedFootData, false);
        bedHead.setBlockData(bedHeadData, false);
    }

    public static boolean onBedBreak(final @NotNull BlockBreakEvent e, final @NotNull Match match) {
        Player player = e.getPlayer();

        if (match.getCurrentStat(player).isSet()) return false;

        final Map<TeamEnum, Boolean> bedStatus = match.getCurrentRound().getBedStatus();
        if (!bedStatus.get(TeamEnum.TEAM1) && !bedStatus.get(TeamEnum.TEAM2)) return false;

        Block bedBlock = e.getBlock();
        if (!bedBlock.getType().toString().contains("_BED")) return false;

        TeamEnum team = ((Team) match).getTeam(player);
        Location bedLoc = bedBlock.getLocation();

        boolean destroy = false;
        if (match.getArena().getBedLoc1().getLocation().equals(bedLoc)
                || match.getArena().getBedLoc1().getLocation().getBlock().getRelative(match.getArena().getBedLoc1().getFacing()).getLocation().equals(bedLoc)) {
            e.setCancelled(true);

            if (team.equals(TeamEnum.TEAM2)) {
                destroy = true;

                if (bedStatus.get(TeamEnum.TEAM1)) {
                    bedStatus.replace(TeamEnum.TEAM1, false);
                    sendBedDestroyMessage(match, TeamEnum.TEAM1);
                }
            } else
                Common.sendMMMessage(player, LanguageManager.getString("MATCH.CANT-BREAK-OWN-BED"));
        } else if (match.getArena().getBedLoc2().getLocation().equals(bedLoc)
                || match.getArena().getBedLoc2().getLocation().getBlock().getRelative(match.getArena().getBedLoc2().getFacing()).getLocation().equals(bedLoc)) {
            e.setCancelled(true);

            if (team.equals(TeamEnum.TEAM1)) {
                destroy = true;

                if (bedStatus.get(TeamEnum.TEAM2)) {
                    bedStatus.replace(TeamEnum.TEAM2, false);
                    sendBedDestroyMessage(match, TeamEnum.TEAM2);
                }
            } else
                Common.sendMMMessage(player, LanguageManager.getString("MATCH.CANT-BREAK-OWN-BED"));
        }

        if (destroy) {
            BedLocation bedLocation = getBedLocation(e.getBlock());
            Block head = bedLocation.getBlock().getRelative(bedLocation.getFacing());

            match.addBlockChange(new ChangedBlock(bedLocation.getBlock()));

            head.setBlockData(Material.AIR.createBlockData());
            bedLocation.getBlock().setBlockData(Material.AIR.createBlockData());
        }

        return destroy;
    }

    private static void sendBedDestroyMessage(Match match, TeamEnum team) {
        String languagePath = switch (match.getLadder().getType()) {
            case BEDWARS -> "MATCH." + match.getType().getPathName() + ".LADDER-SPECIFIC.BED-WARS";
            case FIREBALL_FIGHT -> "MATCH." + match.getType().getPathName() + ".LADDER-SPECIFIC.FIREBALL-FIGHT";
            case MLG_RUSH -> "MATCH." + match.getType().getPathName() + ".LADDER-SPECIFIC.MLG-RUSH";
            default -> null;
        };

        if (languagePath == null) return;

        match.sendMessage(LanguageManager.getString(languagePath + ".BED-DESTROYED")
                        .replace("%team%", team.getNameMM())
                        .replace("%teamColor%", team.getColorMM())
                , true);
    }

}
