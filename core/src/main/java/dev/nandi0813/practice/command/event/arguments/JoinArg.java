package dev.nandi0813.practice.command.event.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum JoinArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JOIN.COMMAND-HELP").replace("%label%", label));
            return;
        }

        if (!player.hasPermission("zpp.event.join")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JOIN.NO-PERMISSION"));
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.LOBBY) || profile.isParty()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JOIN.CANT-JOIN"));
            return;
        }

        if (EventManager.getInstance().getEventByPlayer(player) != null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JOIN.ALREADY-IN-EVENT"));
            return;
        }

        for (Event event : EventManager.getInstance().getEvents()) {
            if (event.getStatus().equals(EventStatus.COLLECTING)) {
                event.addPlayer(player);
                return;
            }
        }
        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JOIN.NO-EVENT"));
    }

}
