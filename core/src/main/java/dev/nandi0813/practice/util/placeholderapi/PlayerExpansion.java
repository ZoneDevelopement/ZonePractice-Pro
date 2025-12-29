package dev.nandi0813.practice.util.placeholderapi;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.leaderboard.Leaderboard;
import dev.nandi0813.practice.manager.leaderboard.LeaderboardManager;
import dev.nandi0813.practice.manager.leaderboard.types.LbMainType;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.manager.queue.QueueManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public class PlayerExpansion extends PlaceholderExpansion {


    @Override
    public @NotNull String getIdentifier() {
        return "zppro";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nandi0813";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        final Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return null;

        String[] input = params.split("_");
        if (input.length == 0) return null;

        switch (input[0]) {
            case "group":
                if (input.length == 1) return null;

                Group group = profile.getGroup();
                if (group == null) return null;

                switch (input[1]) {
                    case "name":
                        if (input.length == 2) return group.getDisplayName();
                        break;
                    case "prefix":
                        if (input.length == 2) return ZonePractice.getMiniMessage().serialize(group.getPrefix());
                        break;
                    case "suffix":
                        if (input.length == 2) return ZonePractice.getMiniMessage().serialize(group.getSuffix());
                        break;
                    case "limit":
                        if (input.length == 2) return null;

                        switch (input[2]) {
                            case "u":
                                return String.valueOf(group.getUnrankedLimit());
                            case "r":
                                return String.valueOf(group.getRankedLimit());
                        }
                        break;
                }
                break;
            case "in":
                if (input.length == 1) return null;

                switch (input[1]) {
                    case "queue":
                        if (input.length == 2) {
                            return String.valueOf(QueueManager.getInstance().getQueues().size());
                        } else {
                            if (input.length != 3) return null;

                            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
                            if (ladder == null) return null;

                            return String.valueOf(QueueManager.getInstance().getQueueSize(ladder));
                        }
                    case "fight":
                        if (input.length == 2) {
                            return String.valueOf(MatchManager.getInstance().getPlayerInMatchSize());
                        } else {
                            if (input.length != 3) return null;

                            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
                            if (ladder == null) return null;

                            return String.valueOf(MatchManager.getInstance().getPlayerInMatchSize(ladder));
                        }
                }
                break;
            case "division":
                if (input.length == 1) return null;
                if (profile.getStats().getDivision() == null) return null;

                switch (input[1]) {
                    // division_short
                    case "short":
                        return profile.getStats().getDivision().getShortName();
                    // division_full
                    case "full":
                        return profile.getStats().getDivision().getFullName();
                }
                break;
            case "wins":
                if (input.length == 1) return null;

                switch (input[1]) {
                    case "global":
                        if (input.length == 2) {
                            // wins_global
                            return String.valueOf(profile.getStats().getGlobalWins());
                        } else if (input.length == 3) {
                            switch (input[2]) {
                                // wins_global_u
                                case "u":
                                    return String.valueOf(profile.getStats().getWins(false));
                                // wins_global_r
                                case "r":
                                    return String.valueOf(profile.getStats().getWins(true));
                            }
                        }
                        break;
                    case "ladder":
                        if (input.length != 4) return null;

                        NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
                        if (ladder == null) return null;

                        switch (input[3]) {
                            // wins_ladder_<ladder>_u
                            case "u":
                                return String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins());
                            // wins_ladder_<ladder>_r
                            case "r":
                                if (ladder.isRanked())
                                    return String.valueOf(profile.getStats().getLadderStat(ladder).getRankedWins());
                                break;
                        }
                        break;
                }
                break;
            case "losses":
                if (input.length == 1) return null;

                switch (input[1]) {
                    case "global":
                        if (input.length == 2) {
                            // losses_global
                            return String.valueOf(profile.getStats().getGlobalLosses());
                        } else if (input.length == 3) {
                            switch (input[2]) {
                                // losses_global_u
                                case "u":
                                    return String.valueOf(profile.getStats().getLosses(false));
                                // losses_global_r
                                case "r":
                                    return String.valueOf(profile.getStats().getLosses(true));
                            }
                        }
                        break;
                    case "ladder":
                        if (input.length != 4) return null;

                        NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
                        if (ladder == null) return null;

                        switch (input[3]) {
                            // losses_ladder_<ladder>_u
                            case "u":
                                return String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses());
                            // losses_ladder_<ladder>_r
                            case "r":
                                if (ladder.isRanked())
                                    return String.valueOf(profile.getStats().getLadderStat(ladder).getRankedLosses());
                                break;
                        }
                        break;
                }
                break;
            case "elo":
                if (input.length == 1) return null;

                switch (input[1]) {
                    // elo_global
                    case "global":
                        if (input.length == 2)
                            return String.valueOf(profile.getStats().getGlobalElo());
                        break;
                    // elo_ladder_<ladder>
                    case "ladder":
                        if (input.length != 3) return null;

                        NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
                        if (ladder == null || !ladder.isRanked()) return null;

                        return String.valueOf(profile.getStats().getLadderStat(ladder).getElo());
                }
                break;
            case "lb":
                if (input.length == 1) return null;

                Leaderboard leaderboard;
                Map<OfflinePlayer, Integer> list;
                OfflinePlayer lbPlayer;
                int rank;

                switch (input[1]) {
                    case "global":
                        if (input.length != 5) return null;

                        try {
                            rank = Integer.parseInt(input[3]);
                            if (rank < 1 || rank > 10) return null;
                        } catch (NumberFormatException e) {
                            return null;
                        }

                        leaderboard = switch (input[2]) {
                            case "wins" ->
                                    LeaderboardManager.getInstance().searchLB(LbMainType.GLOBAL, LbSecondaryType.WIN, null);
                            case "elo" ->
                                    LeaderboardManager.getInstance().searchLB(LbMainType.GLOBAL, LbSecondaryType.ELO, null);
                            default -> null;
                        };

                        if (leaderboard == null) return null;
                        list = leaderboard.getList();
                        if (list.size() < rank) return null;

                        lbPlayer = new ArrayList<>(list.keySet()).get(rank - 1);

                        switch (input[4]) {
                            // lb_global_wins_<1-10>_k || lb_global_elo_<1-10>_k
                            case "k":
                                return lbPlayer.getName();
                            // lb_global_wins_<1-10>_v || lb_global_elo_<1-10>_v
                            case "v":
                                return String.valueOf(list.get(lbPlayer));
                        }
                        break;
                    case "ladder":
                        if (input.length != 6) return null;

                        try {
                            rank = Integer.parseInt(input[4]);
                            if (rank < 1 || rank > 10) return null;
                        } catch (NumberFormatException e) {
                            return null;
                        }

                        NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
                        if (ladder == null) return null;

                        leaderboard = switch (input[3]) {
                            case "wins" ->
                                    LeaderboardManager.getInstance().searchLB(LbMainType.LADDER, LbSecondaryType.WIN, ladder);
                            case "elo" ->
                                    LeaderboardManager.getInstance().searchLB(LbMainType.LADDER, LbSecondaryType.ELO, ladder);
                            default -> null;
                        };

                        if (leaderboard == null) return null;
                        list = leaderboard.getList();
                        if (list.size() < rank) return null;

                        lbPlayer = new ArrayList<>(list.keySet()).get(rank - 1);

                        switch (input[5]) {
                            // lb_ladder_<ladder>_wins_<1-10>_k || lb_ladder_<ladder>_elo_<1-10>_k
                            case "k":
                                return lbPlayer.getName();
                            // lb_ladder_<ladder>_wins_<1-10>_v || lb_ladder_<ladder>_elo_<1-10>_v
                            case "v":
                                return String.valueOf(list.get(lbPlayer));
                        }
                        break;
                }
            case "ffa":
                if (input.length == 2) return null;

                FFAArena ffaArena = ArenaManager.getInstance().getFFAArena(input[1]);
                if (ffaArena == null) return null;

                FFA ffa = ffaArena.getFfa();
                if (ffa != null && ffa.isOpen()) {
                    if (input.length != 3) return null;

                    switch (input[2]) {
                        case "players":
                            return String.valueOf(ffa.getPlayers().size());
                        case "spectators":
                            return String.valueOf(ffa.getSpectators().size());
                    }
                }
                break;
        }

        return null;
    }

}
