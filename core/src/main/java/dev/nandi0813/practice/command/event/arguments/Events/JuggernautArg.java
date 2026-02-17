package dev.nandi0813.practice.command.event.arguments.Events;

import dev.nandi0813.practice.command.event.arguments.SpawnPointArg;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.juggernaut.JuggernautData;
import dev.nandi0813.practice.manager.fight.event.util.EventUtil;
import dev.nandi0813.practice.module.interfaces.KitData;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum JuggernautArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.NO-PERMISSION"));
            return;
        }

        String label2 = args[0];
        JuggernautData juggernautData = (JuggernautData) EventManager.getInstance().getEventData().get(EventType.JUGGERNAUT);

        // Checking if the event is live, if it is, it will send a message to the player and return.
        if (EventManager.getInstance().isEventLive(EventType.JUGGERNAUT)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.EVENT-LIVE"));
            return;
        }

        // Checking if the player is trying to enable or disable the arena.
        if (args.length == 2 && args[1].equalsIgnoreCase("enable")) {
            if (!juggernautData.isEnabled())
                EventUtil.changeStatus(juggernautData, player);
            else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.EVENT-ALREADY-ENABLED"));

            return;
        } else if (args.length == 2 && args[1].equalsIgnoreCase("disable")) {
            if (juggernautData.isEnabled())
                EventUtil.changeStatus(juggernautData, player);
            else
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.EVENT-ALREADY-DISABLED"));

            return;
        } else if (args.length == 2 && args[1].equalsIgnoreCase("help")) {
            sendHelpMSG(player, label, label2);

            return;
        }

        // Checking if the arena is enabled, if it is, it will send a message to the player and return.
        if (juggernautData.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.CANT-EDIT"));
            return;
        }

        // This is checking if the player is trying to scan the arena for spawn and spectator positions.
        if (args.length == 3 && args[1].equalsIgnoreCase("setkit")) {
            if (args[2].equalsIgnoreCase("juggernaut")) {
                KitData kitData = juggernautData.getJuggernautKitData();
                kitData.setKitData(player, true);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.JUGGERNAUT-KIT-SET"));
            } else if (args[2].equalsIgnoreCase("players")) {
                KitData kitData = juggernautData.getPlayerKitData();
                kitData.setKitData(player, true);

                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.PLAYERS-KIT-SET"));
            } else {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.KIT-COMMAND-HELP").replace("%label%", label).replace("%label2%", label2));
            }
        } else {
            sendHelpMSG(player, label, label2);
        }

    }

    private static void sendHelpMSG(Player player, String label, String label2) {
        for (String line : LanguageManager.getList("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.COMMAND-HELP"))
            Common.sendMMMessage(player, line.replace("%label%", label).replace("%label2%", label2));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 2) {
            arguments.add("setkit");
            arguments.add("enable");
            arguments.add("disable");

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("setkit")) {
            arguments.add("juggernaut");
            arguments.add("players");

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
