package dev.nandi0813.practice.command.division;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.guis.DivisionGui;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DivisionsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!player.hasPermission("zpp.divisions.view")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.DIVISIONS.NO-PERMISSION"));
            return false;
        }

        if (!player.hasPermission("zpp.admin")) {
            switch (profile.getStatus()) {
                case MATCH:
                case FFA:
                case EVENT:
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.DIVISIONS.CANT-USE"));
                    return false;
            }
        }

        new DivisionGui(profile, null).open(player);

        return true;
    }

}
