package dev.nandi0813.practice.command.event.arguments.Events;

import dev.nandi0813.practice.command.event.arguments.SpawnPointArg;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.events.ffa.splegg.SpleggData;
import dev.nandi0813.practice.manager.fight.event.util.EventUtil;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum SpleggArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.NO-PERMISSION"));
            return;
        }

        SpleggData spleggData = (SpleggData) EventManager.getInstance().getEventData().get(EventType.SPLEGG);

        // Checking if the event is live, if it is, it will send a message to the player and return.
        if (EventManager.getInstance().isEventLive(EventType.SPLEGG)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SPLEGG.EVENT-LIVE"));
            return;
        }

        // Checking if the player is trying to enable or disable the arena.
        if (args.length == 2 && args[1].equalsIgnoreCase("enable")) {
            if (!spleggData.isEnabled())
                EventUtil.changeStatus(spleggData, player);
            else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SPLEGG.EVENT-ALREADY-ENABLED"));
        } else if (args.length == 2 && args[1].equalsIgnoreCase("disable")) {
            if (spleggData.isEnabled())
                EventUtil.changeStatus(spleggData, player);
            else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SPLEGG.EVENT-ALREADY-DISABLED"));
        } else
            sendHelpMSG(player, label);
    }

    private static void sendHelpMSG(Player player, String label) {
        for (String line : LanguageManager.getList("COMMAND.EVENT.ARGUMENTS.SPLEGG.COMMAND-HELP"))
            Common.sendMMMessage(player, line.replace("%label%", label));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 2) {
            arguments.add("enable");
            arguments.add("disable");

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
