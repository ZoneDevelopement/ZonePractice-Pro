package dev.nandi0813.practice.command.event.arguments.Events;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.events.duel.sumo.SumoData;
import dev.nandi0813.practice.manager.fight.event.util.EventUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.KitData;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum SumoArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.NO-PERMISSION"));
            return;
        }

        SumoData sumoData = (SumoData) EventManager.getInstance().getEventData().get(EventType.SUMO);

        // Checking if the event is live, if it is, it will send a message to the player and return.
        if (EventManager.getInstance().isEventLive(EventType.SUMO)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SUMO.EVENT-LIVE"));
            return;
        }

        // Checking if the player is trying to enable or disable the arena.
        if (args.length == 2 && args[1].equalsIgnoreCase("enable")) {
            if (!sumoData.isEnabled())
                EventUtil.changeStatus(sumoData, player);
            else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SUMO.EVENT-ALREADY-ENABLED"));
            return;
        } else if (args.length == 2 && args[1].equalsIgnoreCase("disable")) {
            if (sumoData.isEnabled())
                EventUtil.changeStatus(sumoData, player);
            else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SUMO.EVENT-ALREADY-DISABLED"));
            return;
        } else if (args.length == 2 && args[1].equalsIgnoreCase("help")) {
            sendHelpMSG(player, label);

            return;
        }

        // Checking if the arena is enabled, if it is, it will send a message to the player and return.
        if (sumoData.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SUMO.CANT-EDIT"));
            return;
        }

        // Setting the kit for the event.
        if (args.length == 2 && args[1].equalsIgnoreCase("setkit")) {
            KitData kitData = sumoData.getKitData();
            kitData.setKitData(player, true);

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SUMO.KIT-SET"));
        } else {
            sendHelpMSG(player, label);
        }
    }

    private static void sendHelpMSG(Player player, String label) {
        for (String line : LanguageManager.getList("COMMAND.EVENT.ARGUMENTS.SUMO.COMMAND-HELP"))
            Common.sendMMMessage(player, line.replace("%label%", label));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 2) {
            arguments.add("enable");
            arguments.add("disable");
            arguments.add("setkit");

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
