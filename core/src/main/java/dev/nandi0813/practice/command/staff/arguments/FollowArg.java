package dev.nandi0813.practice.command.staff.arguments;

import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.entity.Player;

public enum FollowArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.staffmode.follow")) {
            player.sendMessage(StringUtil.CC("&cYou don't have permission."));
            return;
        }

        player.sendMessage(StringUtil.CC("&cCurrently not a feature of the plugin."));

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
                    player.sendMessage(StringUtil.CC("&cPlayer is not online."));
            }
            else
                player.sendMessage(StringUtil.CC("&cYou can only use this command in staff mode."));
        }
        else
            player.sendMessage(StringUtil.CC("&c/" + label + " follow <player>"));
         */
    }

}
