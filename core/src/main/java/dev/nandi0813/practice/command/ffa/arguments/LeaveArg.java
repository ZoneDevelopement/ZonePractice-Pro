package dev.nandi0813.practice.command.ffa.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum LeaveArg {
    ;

    public static void run(Player player) {
        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);

        if (ffa == null) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.COMMAND.LEAVE.NOT-IN-FFA"));
            return;
        }

        ffa.removePlayer(player);
    }

}
