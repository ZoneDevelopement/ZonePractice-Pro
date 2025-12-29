package dev.nandi0813.practice.command.ladder.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum InfoArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.NO-PERMISSION"));
            return;
        }

        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.INFO.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Ladder ladder = LadderManager.getInstance().getLadder(args[1]);
        if (ladder == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.INFO.NOT-EXISTS").replace("%ladder%", args[1]));
            return;
        }

        for (String line : LanguageManager.getList("COMMAND.LADDER.ARGUMENTS.INFO.LADDER-INFO")) {
            Common.sendMMMessage(player, line
                    .replace("%ladder%", ladder.getName())
                    .replace("%type%", ladder.getType().getName())
                    .replace("%icon%", ladder.getIcon() != null ? LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.INFO.STATUS-NAMES.SET") : LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.INFO.STATUS-NAMES.NOT-SET"))
                    .replace("%displayName%", ladder.getDisplayName())
                    .replace("%inventory%", ladder.getKitData().isSet() ? LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.INFO.STATUS-NAMES.SET") : LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.INFO.STATUS-NAMES.NOT-SET"))
                    .replace("%effects%", String.valueOf(ladder.getKitData().getEffects().size()))
                    .replace("%status%", ladder.isEnabled() ? LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.INFO.STATUS-NAMES.ENABLED") : LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.INFO.STATUS-NAMES.DISABLED"))
            );
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 2) {
            for (Ladder ladder : LadderManager.getInstance().getLadders())
                arguments.add(ladder.getName());

            return org.bukkit.util.StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
