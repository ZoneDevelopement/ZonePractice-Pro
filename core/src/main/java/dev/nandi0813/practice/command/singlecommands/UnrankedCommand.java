package dev.nandi0813.practice.command.singlecommands;

import dev.nandi0813.practice.manager.backend.LanguageManager;
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

public class UnrankedCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!profile.getStatus().equals(ProfileStatus.LOBBY) || profile.isStaffMode() || profile.isParty()) {
            Common.sendMMMessage(player, LanguageManager.getString("CANT-USE-COMMAND"));
            return false;
        }

        if (!player.hasPermission("zpp.bypass.unranked.limit") && profile.getUnrankedLeft() <= 0) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.UNRANKED.NO-UNRANKED-LEFT"));
            return false;
        }

        if (args.length == 0) {
            GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).open(player);
        } else if (args.length == 1) {
            NormalLadder ladder = LadderManager.getInstance().getLadder(args[0]);
            if (ladder == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.UNRANKED.LADDER-NOT-FOUND").replace("%ladder%", args[0]));
                return false;
            }

            QueueManager.getInstance().createUnrankedQueue(player, ladder);
        }

        return true;
    }

}
