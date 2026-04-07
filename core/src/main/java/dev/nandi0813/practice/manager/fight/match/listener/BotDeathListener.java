package dev.nandi0813.practice.manager.fight.match.listener;

import dev.nandi0813.practice.manager.duel.bot.PvPBotTrait;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.type.duel.BotDuel;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Handles Citizens bot deaths and routes them through the match system so the human opponent wins.
 */
public class BotDeathListener implements Listener {

    @EventHandler
    public void onNpcDeath(NPCDeathEvent event) {
        NPC npc = event.getNPC();
        if (!npc.hasTrait(PvPBotTrait.class)) {
            return;
        }

        if (!(npc.getEntity() instanceof Player botPlayer)) {
            return;
        }

        PvPBotTrait trait = npc.getOrAddTrait(PvPBotTrait.class);
        UUID targetPlayerId = trait.getTargetPlayerId();
        if (targetPlayerId == null) {
            return;
        }

        Player opponent = Bukkit.getPlayer(targetPlayerId);
        if (opponent == null || !opponent.isOnline() || opponent.isDead()) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(opponent);
        if (profile == null) {
            return;
        }

        ProfileStatus status = profile.getStatus();
        if (status != ProfileStatus.MATCH && status != ProfileStatus.STARTING) {
            return;
        }

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(opponent);
        if (!(match instanceof BotDuel botDuel)) {
            return;
        }

        if (botDuel.getBotPlayer() == null || !botDuel.getBotPlayer().getUniqueId().equals(botPlayer.getUniqueId())) {
            return;
        }

        match.killPlayer(botPlayer, opponent, DeathCause.PLAYER_ATTACK.getMessage());
    }
}