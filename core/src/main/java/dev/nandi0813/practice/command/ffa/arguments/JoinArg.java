package dev.nandi0813.practice.command.ffa.arguments;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;


public enum JoinArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (args.length != 2) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.COMMAND.JOIN.HELP").replace("%label%", label));
            return;
        }

        FFAArena ffaArena = ArenaManager.getInstance().getFFAArena(args[1]);
        if (ffaArena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.COMMAND.JOIN.ARENA-NOT-FOUND"));
            return;
        }

        FFA ffa = ffaArena.getFfa();
        if (ffa == null || !ffa.isOpen()) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.COMMAND.JOIN.ARENA-CLOSED").replace("%arena%", ffaArena.getDisplayName()));
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.COMMAND.JOIN.CANT-JOIN-FFA"));
            return;
        }

        ffa.getLadderSelectorGui().update();
        ffa.getLadderSelectorGui().open(player);
    }

    public static List<String> tabComplete(Player player, String[] args) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        List<String> arguments = new ArrayList<>();

        if (!profile.getStatus().equals(ProfileStatus.LOBBY))
            return arguments;

        if (args.length == 2) {
            for (FFAArena ffaArena : ArenaManager.getInstance().getFFAArenas()) {
                FFA ffa = ffaArena.getFfa();
                if (ffa != null && ffa.isOpen()) {
                    arguments.add(ffaArena.getName());
                }
            }

            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
