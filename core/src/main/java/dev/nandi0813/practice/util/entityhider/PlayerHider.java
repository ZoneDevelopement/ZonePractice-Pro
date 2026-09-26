package dev.nandi0813.practice.util.entityhider;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import dev.nandi0813.api.Event.Spectate.Start.MatchSpectateStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.nametag.NametagManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.manager.server.WorldEnum;
import dev.nandi0813.practice.util.Common;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class PlayerHider implements Listener {

    private static PlayerHider instance;

    public static PlayerHider getInstance() {
        if (instance == null)
            instance = new PlayerHider();
        return instance;
    }

    private PlayerHider() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    @EventHandler ( priority = EventPriority.MONITOR )
    public void playerJoin(PlayerJoinEvent e) {
        if (checkInvalidLobby()) return;

        final Player player = e.getPlayer();
        final Profile profile = ProfileManager.getInstance().getProfile(player);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
        {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (player == online) continue;

                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
                if (onlineProfile == null) continue;
                ProfileStatus onlineStatus = onlineProfile.getStatus();

                /*
                 * Hide the player from the online.
                 */
                if (onlineStatus.equals(ProfileStatus.MATCH) || onlineStatus.equals(ProfileStatus.EVENT) || onlineStatus.equals(ProfileStatus.FFA)) {
                    hidePlayer(online, player);
                    if (!ConfigManager.isShowPlayersInTab()) {
                        hidePlayer(player, online);
                    }
                } else if (!onlineStatus.equals(ProfileStatus.SPECTATE) && onlineProfile.isHidePlayers()) {
                    hidePlayer(online, player);
                } else if (profile.isHideFromPlayers() && !online.hasPermission("zpp.staffmode.see")) {
                    hidePlayer(online, player);
                }


                /*
                 * Hide the online from the player.
                 */
                if (onlineProfile.isHideFromPlayers() && !player.hasPermission("zpp.staffmode.see")) {
                    hidePlayer(player, online);
                } else if (profile.isHidePlayers() && ServerManager.getInstance().getInWorld().get(online) == WorldEnum.LOBBY) {
                    hidePlayer(player, online);
                }
            }
        }, 2L);
    }

    @EventHandler ( priority = EventPriority.MONITOR )
    public void playerTeleport(PlayerTeleportEvent e) {
        if (checkInvalidLobby()) return;
        if (e.getFrom().getWorld().equals(e.getTo().getWorld())) return;

        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
        {
            if (ServerManager.getInstance().getInWorld().get(player) != WorldEnum.LOBBY) return;

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (player == online) continue;

                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
                if (onlineProfile == null) continue;

                // Handle the teleported player
                if (profile.isHidePlayers() && ServerManager.getInstance().getInWorld().get(online) == WorldEnum.LOBBY) {
                    hidePlayer(player, online);
                } else if (!onlineProfile.isHideFromPlayers() || player.hasPermission("zpp.staffmode.see")) {
                    showPlayer(player, online);
                } else {
                    hidePlayer(player, online);
                }

                // Handle the online player
                if (!(onlineProfile.getStatus().equals(ProfileStatus.MATCH) || onlineProfile.getStatus().equals(ProfileStatus.EVENT) || onlineProfile.getStatus().equals(ProfileStatus.FFA))) {
                    if (onlineProfile.isHidePlayers() && ServerManager.getInstance().getInWorld().get(online) == WorldEnum.LOBBY) {
                        hidePlayer(online, player);
                    } else if (!profile.isHideFromPlayers() || online.hasPermission("zpp.staffmode.see")) {
                        showPlayer(online, player);
                        showTabEntry(online, player);
                    } else if (profile.isHideFromPlayers() || !online.hasPermission("zpp.staffmode.see")) {
                        hidePlayer(online, player);
                    }
                } else if (!ConfigManager.isShowPlayersInTab()) {
                    hidePlayer(player, online);
                }
            }
        }, 2L);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (!ConfigManager.isShowPlayersInTab()) return;

        final UUID uuid = e.getPlayer().getUniqueId();

        /*
         * The tab list entry of a hidden player (SHOW-PLAYERS-IN-TAB) is sent manually, so the server does not
         * remove it when the player disconnects. Removing it for everyone is safe, clients ignore the removal of
         * a player they do not know.
         */
        final WrapperPlayServerPlayerInfoRemove packet = new WrapperPlayServerPlayerInfoRemove(uuid);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(uuid)) continue;

            PacketEvents.getAPI().getPlayerManager().sendPacket(online, packet);
        }
    }

    /**
     * When a player starts spectating a match, hide the player from other spectators if they have the option enabled, and
     * show the player to the players in the match
     *
     * @param e The event that is being called.
     */
    @EventHandler
    public void onSpectatingStart(MatchSpectateStartEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        dev.nandi0813.api.Interface.Match match = e.getMatch();

        for (Player online : match.getSpectators()) {
            if (player == online) continue;

            if (profile.isHideSpectators())
                hidePlayer(player, online);

            if (ProfileManager.getInstance().getProfile(online).isHidePlayers())
                hidePlayer(online, player);
        }

        // Show match players.
        for (Player matchPlayer : match.getPlayers())
            showPlayer(player, matchPlayer);

        // Hide other players.
        if (match instanceof Match) {
            for (Player hide : MatchManager.getInstance().getHidePlayers((Match) match)) {
                this.hidePlayer(player, hide);
            }
        }
    }


    /**
     * If the player is in the lobby, hide or show all players in the lobby
     *
     * @param player The player who is toggling their lobby visibility.
     */
    public void toggleLobbyVisibility(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY))
            return;

        for (Player online : ServerManager.getInstance().getInWorld().keySet()) {
            if (player.equals(online)) continue;
            if (!ServerManager.getInstance().getInWorld().get(online).equals(WorldEnum.LOBBY)) continue;

            if (profile.isHidePlayers()) {
                hidePlayer(player, online);
            } else {
                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);

                if (!onlineProfile.isHideFromPlayers() || player.hasPermission("zpp.staffmode.see")) {
                    showPlayer(player, online);
                }
            }
        }
    }


    /**
     * If the player is spectating, hide or show all the other spectators in the match
     *
     * @param player The player who is toggling their visibility
     */
    public void toggleSpectatorVisibility(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            Match match = MatchManager.getInstance().getLiveMatchBySpectator(player);

            if (match != null && match.getSpectators().size() > 1) {
                for (Player online : match.getSpectators()) {
                    if (player.equals(online)) continue;

                    Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
                    if (profile.isHideSpectators()) {
                        hidePlayer(player, online);
                    } else {
                        if (!onlineProfile.isHideFromPlayers() || player.hasPermission("zpp.staffmode.see")) {
                            showPlayer(player, online);
                        }
                    }
                }
            }
            /*
             *
             * CURRENTLY SPECTATOR MODE DURING EVENTS IS NOT SUPPORTED.
             *
             */
        }
    }


    /**
     * If the player is in staff mode, hide them from all players who are not in staff mode
     *
     * @param player The player who is toggling staff mode.
     */
    public void toggleStaffVisibility(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (player.equals(online)) continue;

            if (profile.isHideFromPlayers()) {
                if (online.hasPermission("zpp.staffmode.see")) continue;

                hidePlayer(online, player);
            } else {
                if (ServerManager.getInstance().getInWorld().get(online) != WorldEnum.LOBBY) continue;

                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);

                if (!onlineProfile.isHidePlayers())
                    showPlayer(online, player);
                else
                    hidePlayer(online, player);
            }
        }
    }


    public void hidePlayer(Player observer, Player target) {
        boolean showPlayersInTab = ConfigManager.isShowPlayersInTab();

        observer.hidePlayer(ZonePractice.getInstance(), target);

        if (showPlayersInTab) {
            showTabEntry(observer, target);
        } else {
            WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo =
                    new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(target.getUniqueId());
            playerInfo.setListed(false);

            WrapperPlayServerPlayerInfoUpdate playerInfoUpdate =
                    new WrapperPlayServerPlayerInfoUpdate(
                            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
                            playerInfo
                    );

            PacketEvents.getAPI().getPlayerManager().sendPacket(observer, playerInfoUpdate);
        }
    }

    public void showPlayer(Player observer, Player target) {
        observer.showPlayer(ZonePractice.getInstance(), target);
    }

    public void showTabEntry(Player observer, Player target) {
        List<TextureProperty> properties = target.getPlayerProfile().getProperties().stream()
                .map(property -> new TextureProperty(
                        property.getName(),
                        property.getValue(),
                        property.getSignature()
                ))
                .toList();

        Component tabName = NametagManager.getInstance().getTabListName(target);

        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(
                        WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                        WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
                        WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY,
                        WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
                        WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
                        WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER
                ),
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        new UserProfile(target.getUniqueId(), target.getName(), properties),
                        true,
                        target.getPing(),
                        GameMode.valueOf(target.getGameMode().name()),
                        tabName,
                        null,
                        target.getPlayerListOrder()
                )
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(observer, packet);
    }

    private boolean checkInvalidLobby() {
        if (ServerManager.getLobby() == null) {
            Common.sendConsoleMMMessage(LanguageManager.getString("SET-SERVER-LOBBY"));
            return true;
        }
        return false;
    }

}
