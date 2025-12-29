package dev.nandi0813.practice.command.staff.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import org.bukkit.entity.Player;

public enum VanishArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.staffmode")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.NO-PERMISSION"));
            return;
        }

        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.VANISH.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile.getStatus().equals(ProfileStatus.MATCH) || profile.getStatus().equals(ProfileStatus.EVENT)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.VANISH.CANT-USE"));
            return;
        }

        profile.setHideFromPlayers(!profile.isHideFromPlayers());
        PlayerHider.getInstance().toggleStaffVisibility(player);

        if (profile.isStaffMode()) {
            InventoryManager.getInstance().setStaffModeInventory(player);
        }

        if (profile.isHideFromPlayers())
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.VANISH.INVISIBLE"));
        else
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.VANISH.VISIBLE"));
    }

}
