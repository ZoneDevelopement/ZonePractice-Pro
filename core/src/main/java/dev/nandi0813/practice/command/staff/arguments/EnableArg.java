package dev.nandi0813.practice.command.staff.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum EnableArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.staff")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.NO-PERMISSION"));
            return;
        }

        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.ENABLE.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.ENABLE.ONLY-IN-LOBBY"));
            return;
        }

        profile.setStaffMode(true);
        InventoryManager.getInstance().setLobbyInventory(player, false);
    }

}
