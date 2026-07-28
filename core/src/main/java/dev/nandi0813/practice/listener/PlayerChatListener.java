package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.ChatFormatUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.PAPIUtil;
import dev.nandi0813.practice.util.SoftDependUtil;
import dev.nandi0813.practice.util.StringUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Set;

public class PlayerChatListener implements Listener {

    private static final String MESSAGE_PLACEHOLDER = "<zpp-message>";

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncChatEvent e) {
        Player player = e.getPlayer();

        Profile profile = ProfileManager.getInstance().getProfile(player);
        Party party = PartyManager.getInstance().getParty(player);

        String rawMessage = PlainTextComponentSerializer.plainText()
                .serialize(e.message());


        // Party chat
        if (ConfigManager.getBoolean("CHAT.PARTY-CHAT-ENABLED")
                && profile.isParty()
                && party != null
                && rawMessage.startsWith("@")) {

            if (party.isPartyChat() || party.getLeader() == player) {

                setViewers(e, party.getMembers());

                applyRenderer(
                        e,
                        Component.text(rawMessage.substring(1)),
                        ChatFormatUtil.buildPartyChatMessage(player)
                );

            } else {
                e.setCancelled(true);

                String cantUse = LanguageManager.getString(
                        "PARTY.CANT-USE-PARTY-CHAT"
                );

                Bukkit.getScheduler().runTask(
                        ZonePractice.getInstance(),
                        () -> Common.sendMMMessage(player, cantUse)
                );
            }

            return;
        }


        // Staff chat toggle
        if (profile.isStaffChat()) {

            applyStaffChat(e, player, rawMessage);

            return;
        }


        // Staff shortcut
        if (player.hasPermission("zpp.staff")
                && ConfigManager.getBoolean("CHAT.STAFF-CHAT.SHORTCUT")
                && rawMessage.startsWith("#")) {

            applyStaffChat(
                    e,
                    player,
                    rawMessage.substring(1)
            );

            return;
        }


        // Server chat
        if (ConfigManager.getBoolean("CHAT.SERVER-CHAT-ENABLED")) {

            applyMatchChatIsolation(e, player);

            applyRenderer(
                    e,
                    Component.text(rawMessage),
                    ChatFormatUtil.buildServerChatMessage(
                            profile,
                            player
                    )
            );
        }
    }


    private void applyStaffChat(
            AsyncChatEvent e,
            Player player,
            String message
    ) {

        setViewers(
                e,
                ChatFormatUtil.getStaffRecipients()
        );

        applyRenderer(
                e,
                Component.text(message),
                ChatFormatUtil.buildStaffChatMessage(
                        player
                )
        );
    }


    private void applyRenderer(
            AsyncChatEvent e,
            Component message,
            String formatString
    ) {

        e.renderer((_, _, _, viewer) -> {

            String format = formatString.replace(
                    "%%message%%",
                    MESSAGE_PLACEHOLDER
            );


            Component component;

            if (SoftDependUtil.isPAPI_ENABLED
                    && viewer instanceof Player viewerPlayer) {

                component = PAPIUtil.runThroughFormat(
                        viewerPlayer,
                        format
                );

            } else {

                component = ZonePractice.getMiniMessage()
                        .deserialize(
                                StringUtil.legacyToMiniMessage(format)
                        );
            }


            return component.replaceText(
                    TextReplacementConfig.builder()
                            .matchLiteral(MESSAGE_PLACEHOLDER)
                            .replacement(message)
                            .build()
            );
        });
    }


    private void applyMatchChatIsolation(
            AsyncChatEvent e,
            Player sender
    ) {

        if (!ConfigManager.isMatchChatIsolated()) {
            return;
        }

        MatchManager matchManager = MatchManager.getInstance();
        FFAManager ffaManager = FFAManager.getInstance();

        Set<Audience> viewers = e.viewers();

        viewers.add(Bukkit.getConsoleSender());


        Match match = matchManager.getLiveMatchByPlayer(sender);

        if (match != null) {
            viewers.clear();
            viewers.addAll(match.getPlayers());
            return;
        }


        FFA ffa = ffaManager.getFFAByPlayer(sender);

        if (ffa != null) {
            viewers.clear();
            viewers.addAll(ffa.getPlayers().keySet());
            return;
        }


        viewers.removeIf(viewer ->
                viewer instanceof Player recipient
                        && (
                        matchManager.getLiveMatchByPlayer(recipient) != null
                                ||
                                ffaManager.getFFAByPlayer(recipient) != null
                )
        );
    }


    private void setViewers(
            AsyncChatEvent e,
            Collection<? extends Audience> targets
    ) {

        Set<Audience> viewers = e.viewers();

        viewers.clear();

        viewers.add(Bukkit.getConsoleSender());
        viewers.addAll(targets);
    }
}