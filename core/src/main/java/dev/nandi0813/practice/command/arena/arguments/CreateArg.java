package dev.nandi0813.practice.command.arena.arguments;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.guis.arena.ArenaCreateGui;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

import java.text.Normalizer;

public enum CreateArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.NO-PERMISSION"));
            return;
        }

        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CREATE.COMMAND-HELP").replace("%label%", label));
            return;
        }

        if (ArenaManager.getInstance().getArenaList().size() == 243) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.CREATE.REACHED-MAX"));
            return;
        }

        String name = args[1];
        String response = checkName(name);
        if (response != null) {
            Common.sendMMMessage(player, response);
            return;
        }

        new ArenaCreateGui(name).open(player);
    }

    public static String checkName(String input) {
        if (input.isEmpty())
            return LanguageManager.getString("ARENA.CREATE.NO-NAME");
        else if (input.contains(" "))
            return LanguageManager.getString("ARENA.CREATE.ONLY-ALPHANUMERIC");
        else if (!Normalizer.isNormalized(input, Normalizer.Form.NFD))
            return LanguageManager.getString("ARENA.CREATE.NO-ACCENTS");
        else if (input.length() < 4)
            return LanguageManager.getString("ARENA.CREATE.FEW-CHAR");
        else if (input.length() > 16)
            return LanguageManager.getString("ARENA.CREATE.MANY-CHAR");
        else if (ArenaManager.getInstance().getArena(input) != null)
            return LanguageManager.getString("ARENA.CREATE.NAME-TAKEN");
        else
            return null;
    }

}
