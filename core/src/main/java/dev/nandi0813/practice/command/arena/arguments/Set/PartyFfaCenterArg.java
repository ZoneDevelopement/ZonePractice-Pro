package dev.nandi0813.practice.command.arena.arguments.Set;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public final class PartyFfaCenterArg {

    private PartyFfaCenterArg() {}

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.NO-PERMISSION"));
            return;
        }

        if (args.length != 3) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PARTYFFACENTER.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Arena arena = ArenaManager.getInstance().getNormalArena(args[2]);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PARTYFFACENTER.ARENA-NOT-EXISTS").replace("%arena%", args[2]));
            return;
        }

        if (arena.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PARTYFFACENTER.ARENA-ENABLED").replace("%arena%", arena.getName()));
            return;
        }

        if (arena.isBuild() && !arena.getCopies().isEmpty()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PARTYFFACENTER.CANT-EDIT").replace("%arena%", arena.getName()));
            return;
        }

        if (arena.getCuboid() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PARTYFFACENTER.NO-REGION"));
            return;
        }

        Location center = player.getLocation().clone();
        if (!arena.getCuboid().contains(center)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PARTYFFACENTER.POS-OUTSIDE-REGION"));
            return;
        }

        arena.setPartyFfaCenter(center);
        ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Main).update();
        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.ARENA.ARGUMENTS.PARTYFFACENTER.SET")
                .replace("%arena%", arena.getName()));
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
