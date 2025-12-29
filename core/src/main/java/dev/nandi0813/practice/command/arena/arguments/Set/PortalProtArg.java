package dev.nandi0813.practice.command.arena.arguments.Set;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum PortalProtArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.NO-PERMISSION"));
            return;
        }

        if (args.length != 4) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL-PROTECTION.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Arena arena = ArenaManager.getInstance().getNormalArena(args[2]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL-PROTECTION.ARENA-NOT-EXISTS").replace("%arena%", args[2]));
            return;
        }

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL-PROTECTION.ARENA-ENABLED").replace("%arena%", arena.getName()));
            return;
        }

        if (!arena.isBuild()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL-PROTECTION.NOT-BUILD").replace("%arena%", arena.getName()));
            return;
        }

        try {
            final int portalProtection = Integer.parseInt(args[3]);

            if (portalProtection < 0 || portalProtection > 20) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL-PROTECTION.NOT-INTEGER"));
                return;
            }

            arena.setPortalProtectionValue(portalProtection);
            arena.setPortalProtection(true);

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL-PROTECTION.SET-PORTALPROT").replace("%arena%", arena.getName()).replace("%portalProt%", String.valueOf(portalProtection)));
        } catch (NumberFormatException e) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PORTAL-PROTECTION.NOT-INTEGER"));
        }
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 3) {
            for (Arena arena : ArenaManager.getInstance().getNormalArenas())
                arguments.add(arena.getName());

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
