package dev.nandi0813.practice_modern.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.ChatFormatUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.SoftDependUtil;
import dev.nandi0813.practice.util.PAPIUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Set;

public class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        Party party = PartyManager.getInstance().getParty(player);
        String message = PlainTextComponentSerializer.plainText().serialize(e.message());

        // --- Party chat ---
        if (ConfigManager.getBoolean("CHAT.PARTY-CHAT-ENABLED") && profile.isParty() && party != null && message.startsWith("@")) {
            if (party.isPartyChat() || party.getLeader() == player) {
                setViewers(e, party.getMembers());
                applyRenderer(e, ChatFormatUtil.buildPartyChatMessage(player, message));
            } else {
                e.setCancelled(true);
                final String cantUse = LanguageManager.getString("PARTY.CANT-USE-PARTY-CHAT");
                Bukkit.getScheduler().runTask(ZonePractice.getInstance(),
                        () -> Common.sendMMMessage(player, cantUse));
            }
            return;
        }

        // --- Staff chat (toggle) ---
        if (profile.isStaffChat()) {
            applyStaffChat(e, player, message);
            return;
        }

        // --- Staff chat (shortcut: #message) ---
        if (player.hasPermission("zpp.staff") && ConfigManager.getBoolean("CHAT.STAFF-CHAT.SHORTCUT") && message.startsWith("#")) {
            applyStaffChat(e, player, message.replaceFirst("#", ""));
            return;
        }

        // --- Custom server chat ---
        if (ConfigManager.getBoolean("CHAT.SERVER-CHAT-ENABLED")) {
            applyRenderer(e, ChatFormatUtil.buildServerChatMessage(profile, player, message));
        }
    }

    /**
     * Restricts viewers to staff and applies the staff chat renderer.
     */
    private void applyStaffChat(AsyncChatEvent e, Player player, String rawMessage) {
        setViewers(e, ChatFormatUtil.getStaffRecipients());
        applyRenderer(e, ChatFormatUtil.buildStaffChatMessage(player, rawMessage));
    }

    /**
     * Replaces the viewer set with the given collection of players.
     */
    private void setViewers(AsyncChatEvent e, Collection<? extends Audience> targets) {
        Set<Audience> viewers = e.viewers();
        viewers.clear();
        viewers.add(ZonePractice.getAdventure().console());
        viewers.addAll(targets);
    }

    /**
     * Sets a ChatRenderer that deserialises the given MiniMessage string,
     * resolving PAPI placeholders per-viewer when available.
     */
    private void applyRenderer(AsyncChatEvent e, String miniMessageString) {
        e.renderer((source, sourceDisplayName, msg, viewer) -> {
            if (SoftDependUtil.isPAPI_ENABLED && viewer instanceof Player viewerPlayer) {
                return PAPIUtil.runThroughFormat(viewerPlayer, miniMessageString);
            }
            return ZonePractice.getMiniMessage().deserialize(miniMessageString);
        });
    }
}
