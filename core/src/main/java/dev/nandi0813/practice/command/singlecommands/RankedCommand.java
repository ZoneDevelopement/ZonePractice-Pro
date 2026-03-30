package dev.nandi0813.practice.command.singlecommands;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.queue.QueueManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankedCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        boolean lobbyOrQueue = profile.getStatus().equals(ProfileStatus.LOBBY) || profile.getStatus().equals(ProfileStatus.QUEUE);
        if (!lobbyOrQueue || profile.isStaffMode() || profile.isParty()) {
            Common.sendMMMessage(player, LanguageManager.getString("CANT-USE-COMMAND"));
            return false;
        }

        if (args.length == 0) {
            GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).open(player);
            return true;
        }

        if (!player.hasPermission("zpp.bypass.ranked.requirements")) {
            Division requirement = DivisionManager.getInstance().getMinimumForRanked();
            if (requirement != null && !DivisionManager.getInstance().meetsMinimumForRanked(profile)) {
                // Show progress towards requirement
                sendRankedProgressMessage(player, profile, requirement);

                return false;
            }
        }

        if (profile.getRankedLeft() <= 0 && !player.hasPermission("zpp.bypass.ranked.limit")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.RANKED.NO-RANKED-LEFT"));
            return false;
        }

        if (args.length == 1) {
            NormalLadder ladder = LadderManager.getInstance().getLadder(args[0]);
            if (ladder == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.RANKED.LADDER-NOT-FOUND").replace("%ladder%", args[0]));
                return false;
            }

            QueueManager.getInstance().createRankedQueue(player, ladder);
        }

        return true;
    }

    private void sendRankedProgressMessage(Player player, Profile profile, Division requirement) {
        int currentExp = profile.getStats().getExperience();
        int requiredExp = requirement.getExperience();
        int currentWins = profile.getStats().getGlobalWins();
        int requiredWins = requirement.getWin();

        Common.sendMMMessage(player, "");
        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.RANKED.PROGRESS-HEADER"));

        // Experience progress
        int expProgress = Math.min(100, (int) ((double) currentExp / requiredExp * 100));
        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.RANKED.PROGRESS-EXP")
                .replace("%current%", String.valueOf(currentExp))
                .replace("%required%", String.valueOf(requiredExp))
                .replace("%percent%", String.valueOf(expProgress))
        );

        // Wins progress (if wins are counted)
        if (DivisionManager.getInstance().isCOUNT_BY_WINS()) {
            int winsProgress = Math.min(100, (int) ((double) currentWins / requiredWins * 100));
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.RANKED.PROGRESS-WINS")
                    .replace("%current%", String.valueOf(currentWins))
                    .replace("%required%", String.valueOf(requiredWins))
                    .replace("%percent%", String.valueOf(winsProgress))
            );
        }

        Common.sendMMMessage(player, "");
    }

}
