package dev.nandi0813.practice.command.event.arguments;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum HostArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.HOST.COMMAND-HELP").replace("%label%", label));
            return;
        }

        if (!player.hasPermission("zpp.event.host")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.HOST.NO-PERMISSION"));
            return;
        }

        if (!EventManager.getInstance().getEvents().isEmpty() && ConfigManager.getBoolean("EVENT.MULTIPLE")) {
            for (Event event : EventManager.getInstance().getEvents()) {
                if (!event.getStatus().equals(EventStatus.COLLECTING)) continue;

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.HOST.CANT-HOST-NOW"));
                return;
            }
        } else if (!EventManager.getInstance().getEvents().isEmpty() && !ConfigManager.getBoolean("EVENT.MULTIPLE")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.HOST.CANT-HOST-MULTIPLE"));
            return;
        }

        GUIManager.getInstance().searchGUI(GUIType.Event_Host).open(player);
    }

}
