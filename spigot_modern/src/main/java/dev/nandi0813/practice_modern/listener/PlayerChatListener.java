package dev.nandi0813.practice_modern.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.SoftDependUtil;
import dev.nandi0813.practice.util.PAPIUtil;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        Party party = PartyManager.getInstance().getParty(player);
        String message = PlainTextComponentSerializer.plainText().serialize(e.message());

        // --- Party chat ---
        if (ConfigManager.getBoolean("CHAT.PARTY-CHAT-ENABLED") && profile.isParty() && party != null && message.startsWith("@")) {
            e.setCancelled(true);

            if (party.isPartyChat() || party.getLeader() == player) {
                final String partyMsg = LanguageManager.getString("GENERAL-CHAT.PARTY-CHAT")
                        .replace("%%player%%", player.getName())
                        .replace("%%message%%", message.replaceFirst("@", ""));
                Bukkit.getScheduler().runTask(ZonePractice.getInstance(),
                        () -> party.sendMessage(partyMsg));
            } else {
                final String cantUse = LanguageManager.getString("PARTY.CANT-USE-PARTY-CHAT");
                Bukkit.getScheduler().runTask(ZonePractice.getInstance(),
                        () -> Common.sendMMMessage(player, cantUse));
            }
            return;
        }

        // --- Staff chat (toggle) ---
        if (profile.isStaffChat()) {
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(),
                    () -> PlayerUtil.sendStaffMessage(player, message));
            return;
        }

        // --- Staff chat (shortcut: #message) ---
        if (player.hasPermission("zpp.staff") && ConfigManager.getBoolean("CHAT.STAFF-CHAT.SHORTCUT") && message.startsWith("#")) {
            e.setCancelled(true);
            final String staffMsg = message.replaceFirst("#", "");
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(),
                    () -> PlayerUtil.sendStaffMessage(player, staffMsg));
            return;
        }

        // --- Custom server chat ---
        if (ConfigManager.getBoolean("CHAT.SERVER-CHAT-ENABLED")) {
            // Build the format string
            final String format;
            if (ConfigManager.getBoolean("PLAYER.GROUP-CHAT.ENABLED")) {
                Group group = profile.getGroup();
                if (group != null && group.getChatFormat() != null) {
                    format = group.getChatFormat();
                } else {
                    format = LanguageManager.getString("GENERAL-CHAT.SERVER-CHAT");
                }
            } else {
                format = LanguageManager.getString("GENERAL-CHAT.SERVER-CHAT");
            }

            String division     = profile.getStats().getDivision() != null ? profile.getStats().getDivision().getFullName()  : "";
            String divisionShort = profile.getStats().getDivision() != null ? profile.getStats().getDivision().getShortName() : "";

            // Replace all static placeholders now; leave PAPI to the renderer (per-viewer)
            String preFormatted = format
                    .replace("%%division%%",       division)
                    .replace("%%division_short%%", divisionShort)
                    .replace("%%player%%",          player.getName())
                    .replace("%%message%%",         message);

            // Use the Adventure ChatRenderer so we don't cancel — the event itself
            // delivers the component to each viewer through the normal pipeline.
            // NOTE: AsyncChatEvent fires on an async thread; the renderer is also invoked
            // async by Paper. PlaceholderAPI is NOT thread-safe, so PAPI placeholders are
            // resolved here on the async thread only when isPAPI_ENABLED is true.
            // Most PAPI expansions are effectively read-only and safe in practice, but if
            // stricter safety is needed, pre-resolve per-viewer on the main thread before
            // the event fires (e.g. via a sync task cache).
            e.renderer((source, sourceDisplayName, msg, viewer) -> {
                if (SoftDependUtil.isPAPI_ENABLED && viewer instanceof Player viewerPlayer) {
                    return PAPIUtil.runThroughFormat(viewerPlayer, preFormatted);
                }

                return ZonePractice.getMiniMessage().deserialize(preFormatted);
            });
        }
    }
}
