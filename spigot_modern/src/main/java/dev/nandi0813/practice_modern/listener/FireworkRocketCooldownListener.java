package dev.nandi0813.practice_modern.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles firework rocket cooldown for elytra boost in modern Minecraft versions.
 * This prevents spam-boosting with firework rockets when flying with elytra.
 * Delegates to {@link dev.nandi0813.practice.module.interfaces.ItemCooldownHandler}
 * so the modern implementation can apply a native hotbar visual cooldown.
 */
public class FireworkRocketCooldownListener implements Listener {

    // Tracks players whose interact event was already handled this tick to prevent
    // duplicate processing from the MAIN_HAND + OFF_HAND double-fire.
    private final Set<UUID> handledThisTick = new HashSet<>();

    @EventHandler
    public void onFireworkRocketUse(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        // Check if player is using a firework rocket
        if (item == null || item.getType() != Material.FIREWORK_ROCKET) {
            return;
        }

        // Check if player is wearing elytra
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() != Material.ELYTRA) {
            return;
        }

        // Deduplicate: PlayerInteractEvent fires once per hand (MAIN + OFF), skip the second call this tick
        UUID uuid = player.getUniqueId();
        if (!handledThisTick.add(uuid)) {
            return;
        }
        // Clean up after this tick so the next click is processed normally
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> handledThisTick.remove(uuid));

        // Check if player is in FFA
        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa != null) {
            int duration = ffa.getPlayers().get(player).getFireworkRocketCooldown();
            if (duration <= 0) {
                return;
            }

            ClassImport.getClasses().getItemCooldownHandler().handleFireworkRocketFFA(
                    player,
                    ffa.getFightPlayers().get(player),
                    duration,
                    e,
                    "MATCH.COOLDOWN.FIREWORK-ROCKET-COOLDOWN"
            );
            return;
        }

        // Check if player is in a match
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match != null) {
            int duration = match.getLadder().getFireworkRocketCooldown();
            if (duration <= 0) {
                return;
            }

            if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            ClassImport.getClasses().getItemCooldownHandler().handleFireworkRocketMatch(
                    player,
                    match.getMatchPlayers().get(player),
                    duration,
                    e,
                    "MATCH.COOLDOWN.FIREWORK-ROCKET-COOLDOWN"
            );
        }
    }

}
