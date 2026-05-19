package dev.nandi0813.practice.command.staff.arguments;

import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum FollowArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.staffmode.follow")) {
            Common.sendMMMessage(player, "<red>You don't have permission.");
            return;
        }

        Common.sendMMMessage(player, "<red>Currently not a feature of the plugin.");

        /*
        if (args.length == 2)
        {
            Profile profile = ProfileManager.getInstance().getProfile(player);

            if (profile.isStaffMode())
            {
                Player target = Bukkit.getPlayer(args[1]);

                if (target != null)
                {

                }
                else
                    Common.sendMMMessage(player, "<red>Player is not online.");
            }
            else
                Common.sendMMMessage(player, "<red>You can only use this command in staff mode.");
        }
        else
            Common.sendMMMessage(player, "<red>/" + label + " follow <yellow><player>"));
         */
    }

}
