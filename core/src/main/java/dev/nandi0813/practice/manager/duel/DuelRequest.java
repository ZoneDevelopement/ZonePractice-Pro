package dev.nandi0813.practice.manager.duel;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Getter
@Setter
public class DuelRequest {

    private Player sender;
    private Player target;
    private Ladder ladder;
    private Arena arena;
    private int rounds;

    public DuelRequest(Player sender, Player target, Ladder ladder, Arena arena, int rounds) {
        this.sender = sender;
        this.target = target;
        this.ladder = ladder;
        this.arena = arena;
        this.rounds = rounds;
    }

    public void sendRequest() {
        String arenaName;
        if (arena != null)
            arenaName = arena.getDisplayName();
        else
            arenaName = LanguageManager.getString("COMMAND.DUEL.REQUEST-MESSAGE.RANDOM-ARENA-NAME");

        for (String line : LanguageManager.getList("COMMAND.DUEL.REQUEST-MESSAGE.SENDER")) {
            Common.sendMMMessage(sender, line
                    .replace("%ladder%", ladder.getDisplayName())
                    .replace("%arena%", arenaName)
                    .replace("%rounds%", String.valueOf(rounds))
                    .replace("%target%", target.getName())
                    .replace("%targetPing%", String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(target)))
            );
        }

        for (String line : LanguageManager.getList("COMMAND.DUEL.REQUEST-MESSAGE.TARGET")) {
            Common.sendMMMessage(target, line
                    .replace("%ladder%", ladder.getDisplayName())
                    .replace("%arena%", arenaName)
                    .replace("%rounds%", String.valueOf(rounds))
                    .replace("%sender%", sender.getName())
                    .replace("%senderPing%", String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(sender)))
            );
        }
    }

    public void acceptRequest() {
        DuelManager.getInstance().getRequests().get(target).remove(this);

        Arena arena;
        if (this.getArena() != null) {
            arena = this.getArena();

            if (arena.getAvailableArena() == null) {
                Common.sendMMMessage(sender, LanguageManager.getString("COMMAND.DUEL.ARENA-BUSY").replace("%arena%", this.getArena().getDisplayName()));
                arena = LadderUtil.getAvailableArena(ladder);
            }
        } else
            arena = LadderUtil.getAvailableArena(ladder);

        if (arena != null) {
            Duel duel = new Duel(ladder, arena, Arrays.asList(sender, target), false, rounds);
            duel.startMatch();
        } else {
            Common.sendMMMessage(sender, LanguageManager.getString("COMMAND.DUEL.NO-AVAILABLE-ARENA"));
            Common.sendMMMessage(target, LanguageManager.getString("COMMAND.DUEL.NO-AVAILABLE-ARENA"));
        }
    }

}
