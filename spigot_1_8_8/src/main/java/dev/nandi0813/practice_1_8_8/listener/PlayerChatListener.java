package dev.nandi0813.practice_1_8_8.listener;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.ChatFormatUtil;
import dev.nandi0813.practice.util.Common;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        Party party = PartyManager.getInstance().getParty(player);
        String message = e.getMessage();

        // --- Party chat ---
        if (ConfigManager.getBoolean("CHAT.PARTY-CHAT-ENABLED") && profile.isParty() && party != null && message.startsWith("@")) {
            if (party.isPartyChat() || party.getLeader() == player) {
                e.getRecipients().clear();
                e.getRecipients().addAll(party.getMembers());
                applyLegacyFormat(e, ChatFormatUtil.buildPartyChatMessage(player, message));
            } else {
                e.setCancelled(true);
                final String cantUse = LanguageManager.getString("PARTY.CANT-USE-PARTY-CHAT");
                Bukkit.getScheduler().runTask(dev.nandi0813.practice.ZonePractice.getInstance(),
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
            applyLegacyFormat(e, ChatFormatUtil.buildServerChatMessage(profile, player, message));
        }
    }

    /**
     * Restricts recipients to staff and applies the staff chat format.
     */
    private void applyStaffChat(AsyncPlayerChatEvent e, Player player, String rawMessage) {
        e.getRecipients().clear();
        e.getRecipients().addAll(ChatFormatUtil.getStaffRecipients());
        applyLegacyFormat(e, ChatFormatUtil.buildStaffChatMessage(player, rawMessage));
    }

    /**
     * Serialises a MiniMessage string to a legacy §-coloured string and injects
     * it into the event via setFormat / setMessage so Bukkit delivers it natively.
     */
    private void applyLegacyFormat(AsyncPlayerChatEvent e, String miniMessageString) {
        String legacy = LegacyComponentSerializer.legacySection()
                .serialize(Common.deserializeMiniMessage(miniMessageString));
        e.setFormat("%2$s");
        e.setMessage(legacy);
    }
}
