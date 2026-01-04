package dev.nandi0813.practice.command.ffa.arguments;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum ListArg {
    ;

    public static void run(Player player) {
        List<String> ffas = new ArrayList<>();
        for (FFAArena ffaArena : ArenaManager.getInstance().getFFAArenas()) {
            if (ffaArena.getFfa().isOpen()) {
                ffas.add(LanguageManager.getString("FFA.COMMAND.LIST.FORMAT")
                        .replace("%arena%", ffaArena.getDisplayName())
                        .replace("%players%", String.valueOf(ffaArena.getFfa().getPlayers().size()))
                );
            }
        }

        for (String line : LanguageManager.getList("FFA.COMMAND.LIST.LIST")) {
            if (line.contains("%arenas%")) {
                if (!ffas.isEmpty()) {
                    for (String ffa : ffas) {
                        Common.sendMMMessage(player, ffa);
                    }
                } else {
                    Common.sendMMMessage(player, LanguageManager.getString("FFA.COMMAND.LIST.EMPTY"));
                }
            } else {
                Common.sendMMMessage(player, line);
            }
        }
    }

}
