package dev.nandi0813.practice.command.event.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum StopArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.event.stop.collecting") && !player.hasPermission("zpp.event.stop.live")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.NO-PERMISSION"));
            return;
        }

        int eventSize = EventManager.getInstance().getEvents().size();
        if (eventSize == 0) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.NO-EVENT"));
            return;
        }

        if (args.length == 1) {
            if (eventSize > 1) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.MORE-EVENTS"));
                return;
            }

            Event event = EventManager.getInstance().getEvents().get(0);
            if (event == null) return;

            // Check permissions based on event status
            if (event.getStatus().equals(EventStatus.COLLECTING)) {
                if (!player.hasPermission("zpp.event.stop.collecting")) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.NO-PERMISSION"));
                    return;
                }
            } else {
                if (!player.hasPermission("zpp.event.stop.live")) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.NO-PERMISSION"));
                    return;
                }
            }

            event.forceEnd(player);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.END-SUCCESS").replace("%event%", event.getType().getName()));
        } else if (args.length == 2) {
            Event event = null;
            for (Event e : EventManager.getInstance().getEvents()) {
                if (e.getType().getName().equalsIgnoreCase(args[1])) {
                    event = e;
                    break;
                }
            }

            if (event == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.NO-LIVE-EVENT").replace("%event%", args[1]));
                return;
            }

            // Check permissions based on event status
            if (event.getStatus().equals(EventStatus.COLLECTING)) {
                if (!player.hasPermission("zpp.event.stop.collecting")) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.NO-PERMISSION"));
                    return;
                }
            } else {
                if (!player.hasPermission("zpp.event.stop.live")) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.NO-PERMISSION"));
                    return;
                }
            }

            event.forceEnd(player);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.STOP.END-SUCCESS").replace("%event%", event.getType().getName()));
        } else
            HelpArg.run(player, label);
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.event.stop.collecting") && !player.hasPermission("zpp.event.stop.live"))
            return arguments;

        if (args.length == 2) {
            for (Event event : EventManager.getInstance().getEvents())
                arguments.add(event.getType().name());

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
