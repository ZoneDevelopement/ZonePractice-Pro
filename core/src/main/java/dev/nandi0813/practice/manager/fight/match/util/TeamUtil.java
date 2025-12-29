package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.nandi0813.practice.manager.fight.match.type.playersvsplayers.partyvsparty.PartyVsParty;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.PermanentConfig;
import org.bukkit.entity.Player;

public enum TeamUtil {
    ;

    public static TeamEnum getOppositeTeam(TeamEnum team) {
        if (team.equals(TeamEnum.TEAM1)) {
            return TeamEnum.TEAM2;
        } else {
            return TeamEnum.TEAM1;
        }
    }

    public static boolean isSaveTeamMate(Match match, Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            return false;
        }

        if (player1 == player2) {
            return false;
        }

        if ((match instanceof PartySplit && !PermanentConfig.PARTY_SPLIT_TEAM_DAMAGE) ||
                (match instanceof PartyVsParty && !PermanentConfig.PARTY_VS_PARTY_TEAM_DAMAGE)) {
            return isInSameTeam(player1, player2);
        }
        return false;
    }

    private static boolean isInSameTeam(Player player1, Player player2) {
        Profile attackerProfile = ProfileManager.getInstance().getProfile(player1);
        if (!attackerProfile.getStatus().equals(ProfileStatus.MATCH)) {
            return false;
        }

        Profile targetProfile = ProfileManager.getInstance().getProfile(player2);
        if (!targetProfile.getStatus().equals(ProfileStatus.MATCH)) {
            return false;
        }

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player1);
        if (match != MatchManager.getInstance().getLiveMatchByPlayer(player2)) {
            return false;
        }

        if (match instanceof PlayersVsPlayers playersVsPlayers) {
            return playersVsPlayers.getTeam(player1).equals(playersVsPlayers.getTeam(player2));
        }

        return false;
    }

    public static String replaceTeamNames(String string, Player player, TeamEnum playerTeam) {
        return string
                .replaceAll("%team%", playerTeam.getNameMM())
                .replaceAll("%teamColor%", playerTeam.getColorMM())
                .replaceAll("%player%", playerTeam.getColorMM() + player.getName());
    }

}
