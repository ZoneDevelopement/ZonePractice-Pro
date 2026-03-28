package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.nametag.NametagManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.CosmeticsPermissionSanitizer;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.sidebar.SidebarManager;
import dev.nandi0813.practice.telemetry.transport.stats.PracticeStatsTelemetryLogger;
import dev.nandi0813.practice.util.PermanentConfig;
import dev.nandi0813.practice.util.UpdateChecker;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoin implements Listener {

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final UUID uuid = player.getUniqueId();

        Profile profile = ProfileManager.getInstance().getProfile(uuid);
        if (profile == null)
            profile = ProfileManager.getInstance().newProfile(player, uuid);

        profile.checkGroup();

        // Send nametag teams
        NametagManager.getInstance().sendTeams(player);

        profile.setLastJoin(System.currentTimeMillis());
        PracticeStatsTelemetryLogger.markDirty(profile);

        // Check how many custom kits the player is allowed to save.
        int customKitPerm = profile.getCustomKitPerm();
        if (customKitPerm > 0) {
            profile.setAllowedCustomKits(customKitPerm);
        }

        // Set the lobby inventory
        if (PermanentConfig.JOIN_TELEPORT_LOBBY) {
            final Profile profile1 = profile;
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
            {
                PlayerUtil.setPlayerWorldTime(player);

                if (ConfigManager.getBoolean("STAFF-MODE.JOIN-HIDE-FROM-PLAYERS") && player.hasPermission("zpp.staffmode"))
                    profile1.setHideFromPlayers(true);
            }, 10L);

            InventoryManager.getInstance().setLobbyInventory(player, true);
        } else {
            ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.OFFLINE);
            SidebarManager.getInstance().unLoadSidebar(player);
        }

        // Notify operators about available updates (delayed so the player is fully in the world)
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                UpdateChecker.notifyPlayer(player), 40L);

        // Revalidate saved cosmetics after permission plugins have finished loading player nodes.
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (!player.isOnline()) {
                return;
            }

            Profile liveProfile = ProfileManager.getInstance().getProfile(player);
            if (liveProfile == null) {
                return;
            }

            CosmeticsPermissionSanitizer.sanitize(player, liveProfile);
        }, 40L);
    }

}
