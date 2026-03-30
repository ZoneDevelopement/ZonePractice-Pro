package dev.nandi0813.practice.command.ffa.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;

public enum LeaveArg {
    ;

    public static void run(Player player) {
        // First check if player is in an FFA as a participant
        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);

        if (ffa != null) {
            ffa.removePlayer(player);
            return;
        }

        // Then check if player is spectating an FFA
        FFA spectatingFfa = FFAManager.getInstance().getFFABySpectator(player);
        if (spectatingFfa != null) {
            spectatingFfa.removeSpectator(player);
            return;
        }

        // Player is not in any FFA
        Common.sendMMMessage(player, LanguageManager.getString("FFA.COMMAND.LEAVE.NOT-IN-FFA"));
    }

}
