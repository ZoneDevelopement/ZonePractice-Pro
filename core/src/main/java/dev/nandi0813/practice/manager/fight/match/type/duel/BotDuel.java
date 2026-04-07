package dev.nandi0813.practice.manager.fight.match.type.duel;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.duel.bot.PvPBotManager;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
public class BotDuel extends Duel {

    private final NPC botNpc;
    private final Player botPlayer;

    public BotDuel(Ladder ladder, Arena arena, Player player, Player botPlayer, NPC botNpc, int winsNeeded) {
        super(ladder, arena, List.of(player, botPlayer), false, winsNeeded);
        this.botNpc = botNpc;
        this.botPlayer = botPlayer;
    }

    @Override
    public void startNextRound() {
        BotRound round = new BotRound(this, this.rounds.size() + 1);
        this.rounds.put(round.getRoundNumber(), round);
        round.startRound();
    }

    @Override
    public DuelRound getCurrentRound() {
        return (DuelRound) this.rounds.get(this.rounds.size());
    }

    @Override
    public void removePlayer(Player player, boolean quit) {
        if (player.equals(botPlayer)) {
            if (!players.contains(player)) {
                return;
            }

            players.remove(player);
            MatchManager.getInstance().getPlayerMatches().remove(player);
            removePlayerFromBelowName(player);
            PvPBotManager.getInstance().cleanupBot(this);
            return;
        }

        // When human player leaves, also cleanup the bot
        try {
            super.removePlayer(player, quit);
        } finally {
            // Ensure bot is cleaned up when match ends
            if (players.contains(botPlayer)) {
                players.remove(botPlayer);
                MatchManager.getInstance().getPlayerMatches().remove(botPlayer);
                removePlayerFromBelowName(botPlayer);
                PvPBotManager.getInstance().cleanupBot(this);
            }
        }
    }

    private static final class BotRound extends DuelRound {
        private BotRound(BotDuel duel, int round) {
            super(duel, round);
        }

        @Override
        public BotDuel getMatch() {
            return (BotDuel) super.getMatch();
        }
    }
}


