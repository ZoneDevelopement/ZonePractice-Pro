package dev.nandi0813.practice;

import dev.nandi0813.api.Enum.DivisionName;
import dev.nandi0813.api.Enum.WeightClass;
import dev.nandi0813.api.ZonePracticeApi;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.entity.Player;

public class ZonePracticeApiImpl extends ZonePracticeApi {

    public static void setup() {
        ZonePracticeApi.instance = new ZonePracticeApiImpl();
    }

    @Override
    public String getPlayerDivision(Player player, DivisionName divisionName) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return null;
        if (profile.getStats().getDivision() == null) return null;

        return switch (divisionName) {
            case FULL -> StringUtil.CC(profile.getStats().getDivision().getFullName());
            case SHORT -> StringUtil.CC(profile.getStats().getDivision().getShortName());
        };
    }

    @Override
    public int getPlayerUnRankedLeft(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return -1;

        return profile.getUnrankedLeft();
    }

    @Override
    public int getPlayerRankedLeft(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return -1;

        return profile.getRankedLeft();
    }

    @Override
    public void resetPlayerUnRanked(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        Group group = profile.getGroup();
        if (group == null) return;

        profile.setUnrankedLeft(group.getUnrankedLimit());
    }

    @Override
    public void resetPlayerRanked(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        Group group = profile.getGroup();
        if (group == null) return;

        profile.setRankedLeft(group.getRankedLimit());
    }

    @Override
    public void addPlayerUnRanked(Player player, int i) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        profile.setUnrankedLeft(profile.getUnrankedLeft() + i);
    }

    @Override
    public void addPlayerRanked(Player player, int i) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        profile.setRankedLeft(profile.getRankedLeft() + i);
    }

    @Override
    public void endMatch(Player player, String s) {
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        if (s != null)
            match.sendMessage(s, true);

        match.endMatch();
    }

    @Override
    public int getElo(Player player, String s) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return -1;

        NormalLadder ladder = LadderManager.getInstance().getLadder(s);
        if (ladder == null) return -1;
        if (ladder.isRanked()) return -1;

        return profile.getStats().getLadderStat(ladder).getElo();
    }

    @Override
    public int getExperience(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return -1;

        return profile.getStats().getExperience();
    }

    @Override
    public int getLadderWins(Player player, String s, WeightClass weightClass) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return -1;

        NormalLadder ladder = LadderManager.getInstance().getLadder(s);
        if (ladder == null) return -1;

        switch (weightClass) {
            case RANKED:
                if (ladder.isRanked())
                    return profile.getStats().getLadderStat(ladder).getRankedWins();
            case UNRANKED:
                return profile.getStats().getLadderStat(ladder).getUnRankedWins();
            default:
                return -1;
        }
    }

    @Override
    public int getLadderLosses(Player player, String s, WeightClass weightClass) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return -1;

        NormalLadder ladder = LadderManager.getInstance().getLadder(s);
        if (ladder == null) return -1;

        switch (weightClass) {
            case RANKED:
                if (ladder.isRanked())
                    return profile.getStats().getLadderStat(ladder).getRankedLosses();
            case UNRANKED:
                return profile.getStats().getLadderStat(ladder).getUnRankedLosses();
            default:
                return -1;
        }
    }

    @Override
    public int getGlobalWins(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return -1;

        return profile.getStats().getGlobalWins();
    }

}
