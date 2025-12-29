package dev.nandi0813.practice.command.arena.arguments;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaSetupManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum FreezeArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.arena.freeze")) {
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.no-permission"));
            return;
        }

        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.arguments.freeze.command-help").replace("%label%", label));
            return;
        }

        Arena arena = ArenaManager.getInstance().getNormalArena(args[1]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.arguments.freeze.not-exists").replace("%arena%", args[1]));
            return;
        }

        if (!arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.arguments.freeze.arena-frozen").replace("%arena%", arena.getName()));
            return;
        }

        if (!arena.isFrozen())
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.arguments.freeze.freeze-success").replace("%arena%", arena.getName()));
        else
            Common.sendMMMessage(player, LanguageManager.getString("command.arena.arguments.freeze.unfreeze-success").replace("%arena%", arena.getName()));

        arena.setFrozen(!arena.isFrozen());
        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
        ArenaSetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.arena.freeze")) return arguments;

        if (args.length == 2) {
            for (Arena arena : ArenaManager.getInstance().getNormalArenas()) {
                if (arena.isEnabled())
                    arguments.add(arena.getName());
            }

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
