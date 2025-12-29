package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.SoftDependUtil;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import litebans.api.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChat implements Listener {


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        Party party = PartyManager.getInstance().getParty(player);
        String message = e.getMessage();

        if (ConfigManager.getBoolean("CHAT.PARTY-CHAT-ENABLED") && profile.isParty() && party != null && message.startsWith("@")) {
            e.setCancelled(true);

            if (party.isPartyChat() || party.getLeader() == player) {
                party.sendMessage(LanguageManager.getString("GENERAL-CHAT.PARTY-CHAT")
                        .replaceAll("%%player%%", player.getName())
                        .replaceAll("%%message%%", message.replaceFirst("@", "")));
            } else
                Common.sendMMMessage(player, LanguageManager.getString("PARTY.CANT-USE-PARTY-CHAT"));
        } else if (profile.isStaffChat()) {
            e.setCancelled(true);

            PlayerUtil.sendStaffMessage(player, message);
        } else if (player.hasPermission("zpp.staff") && ConfigManager.getBoolean("CHAT.STAFF-CHAT.SHORTCUT") && message.startsWith("#")) {
            e.setCancelled(true);

            PlayerUtil.sendStaffMessage(player, message.replaceFirst("#", ""));
        } else if (ConfigManager.getBoolean("CHAT.SERVER-CHAT-ENABLED")) {
            if (SoftDependUtil.isLITEBANS_ENABLED) {
                if (Database.get().isPlayerMuted(player.getUniqueId(), player.getAddress().getAddress().getHostAddress())) {
                    return;
                }
            }

            if (ConfigManager.getBoolean("PLAYER.GROUP-CHAT.ENABLED")) {
                Group group = profile.getGroup();
                if (group != null && group.getChatFormat() != null) {
                    e.setCancelled(true);

                    sendMessage(group.getChatFormat()
                            .replaceAll("%%division%%", (profile.getStats().getDivision() != null ? profile.getStats().getDivision().getFullName() : ""))
                            .replaceAll("%%division_short%%", profile.getStats().getDivision() != null ? profile.getStats().getDivision().getShortName() : "")
                            .replaceAll("%%player%%", player.getName())
                            .replaceAll("%%message%%", message));
                    return;
                }
            }

            e.setCancelled(true);

            sendMessage(LanguageManager.getString("GENERAL-CHAT.SERVER-CHAT")
                    .replaceAll("%%division%%", (profile.getStats().getDivision() != null ? profile.getStats().getDivision().getFullName() : ""))
                    .replaceAll("%%division_short%%", profile.getStats().getDivision() != null ? profile.getStats().getDivision().getShortName() : "")
                    .replaceAll("%%player%%", player.getName())
                    .replaceAll("%%message%%", message));
        }
    }

    private static void sendMessage(String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            Common.sendMMMessage(online, message);
        }
    }

}
