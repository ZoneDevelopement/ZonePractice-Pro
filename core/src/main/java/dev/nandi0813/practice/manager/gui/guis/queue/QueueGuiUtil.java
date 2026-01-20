package dev.nandi0813.practice.manager.gui.guis.queue;

import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.leaderboard.Leaderboard;
import dev.nandi0813.practice.manager.leaderboard.LeaderboardManager;
import dev.nandi0813.practice.manager.leaderboard.types.LbMainType;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum QueueGuiUtil {
    ;

    static List<String> replaceLore(String format, List<String> lore, Ladder ladder) {
        List<String> newLore = new ArrayList<>();
        for (String s : lore) {
            if (s.contains("%lb_")) {
                newLore.add(getLbString(format, s, ladder));
            } else {
                newLore.add(s);
            }
        }
        return newLore;
    }

    static String getLbString(String format, String s, Ladder ladder) {
        Pattern pattern = Pattern.compile("%lb_(.*?)_(\\d+)%");
        Matcher matcher = pattern.matcher(s);

        if (!matcher.matches()) {
            return "&cInvalid format!";
        }

        String lbType = matcher.group(1);
        String number = matcher.group(2);

        LbSecondaryType lbSecondaryType;
        int placement;
        try {
            lbSecondaryType = LbSecondaryType.valueOf(lbType.toUpperCase());
            placement = Integer.parseInt(number);
        } catch (Exception e) {
            return "&cInvalid format!";
        }

        Leaderboard leaderboard = LeaderboardManager.getInstance().searchLB(LbMainType.LADDER, lbSecondaryType, ladder);
        if (leaderboard == null) {
            return "&cNo leaderboard found!";
        }

        List<OfflinePlayer> players = new ArrayList<>(leaderboard.getList().keySet());
        if (players.size() < placement) {
            return "&cNo player found!";
        }

        OfflinePlayer player = players.get(placement - 1);
        Profile profile = ProfileManager.getInstance().getProfile(player);

        Division division = null;
        if (profile != null) {
            division = profile.getStats().getDivision();
        }
        
        int score = leaderboard.getList().get(player);

        return format
                .replace("%placement%", String.valueOf(placement))
                .replace("%player%", player.getName())
                .replace("%score%", String.valueOf(score))
                .replace("%division%", division != null ? division.getFullName() : "&cN/A")
                .replace("%division_short%", division != null ? division.getShortName() : "&cN/A");
    }

}
