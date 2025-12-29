package dev.nandi0813.practice.command.staff.arguments;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.entity.Player;

public enum ChatArg {
    ;

    public static void run(Player player, String[] args) {
        if (!player.hasPermission("zpp.staffmode.chat")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.NO-PERMISSION"));
            return;
        }

        if (!ConfigManager.getBoolean("CHAT.STAFF-CHAT.ENABLED")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.CHAT.DISABLED"));
            return;
        }

        if (args.length == 1) {
            Profile profile = ProfileManager.getInstance().getProfile(player);
            profile.setStaffChat(!profile.isStaffChat());

            if (profile.isStaffChat())
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.CHAT.CHAT-ENABLED"));
            else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.CHAT.CHAT-DISABLED"));
        } else {
            StringBuilder message = new StringBuilder();
            for (int i = 1; i < args.length; i++)
                message.append(args[i]).append(" ");

            PlayerUtil.sendStaffMessage(player, message.toString());
        }
    }

    public static void run(String[] args) {
        if (!ConfigManager.getBoolean("CHAT.STAFF-CHAT.ENABLED")) {
            Common.sendConsoleMMMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.CHAT.DISABLED"));
            return;
        }

        if (args.length == 1)
            Common.sendConsoleMMMessage("<red>Use: /staff chat <message>");
        else {
            StringBuilder message = new StringBuilder();
            for (int i = 1; i < args.length; i++)
                message.append(args[i]).append(" ");

            PlayerUtil.sendStaffMessage(null, message.toString());
        }
    }

}
