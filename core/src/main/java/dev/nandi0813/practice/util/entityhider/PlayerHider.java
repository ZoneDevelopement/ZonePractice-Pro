package dev.nandi0813.practice.util.entityhider;

import dev.nandi0813.api.Event.Spectate.Start.MatchSpectateStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.manager.server.WorldEnum;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

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
                ProfileStatus onlineStatus = onlineProfile.getStatus();

                /*
                 * Hide the player from the online.
                 */
                if (onlineStatus.equals(ProfileStatus.MATCH) || onlineStatus.equals(ProfileStatus.EVENT) || onlineStatus.equals(ProfileStatus.FFA)) {
                    hidePlayer(online, player, false);
                } else if (!onlineStatus.equals(ProfileStatus.SPECTATE) && onlineProfile.isHidePlayers()) {
                    hidePlayer(online, player, false);
                } else if (profile.isHideFromPlayers() && !online.hasPermission("zpp.staffmode.see")) {
                    hidePlayer(online, player, true);
                }


                /*
                 * Hide the online from the player.
                 */
                if (onlineProfile.isHideFromPlayers() && !player.hasPermission("zpp.staffmode.see")) {
                    hidePlayer(player, online, true);
                } else if (profile.isHidePlayers() && ServerManager.getInstance().getInWorld().get(online).equals(WorldEnum.LOBBY)) {
                    hidePlayer(player, online, false);
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

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
        {
            if (!ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY)) return;

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (player == online) continue;

                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);

                // Handle the teleported player
                if (profile.isHidePlayers() && ServerManager.getInstance().getInWorld().get(online).equals(WorldEnum.LOBBY)) {
                    hidePlayer(player, online, false);
                } else if (!onlineProfile.isHideFromPlayers() || player.hasPermission("zpp.staffmode.see")) {
                    showPlayer(player, online);
                } else {
                    hidePlayer(player, online, true);
                }

                // Handle the online player
                if (!onlineProfile.getStatus().equals(ProfileStatus.MATCH) && !onlineProfile.getStatus().equals(ProfileStatus.EVENT) && !onlineProfile.getStatus().equals(ProfileStatus.FFA)) {
                    if (onlineProfile.isHidePlayers() && ServerManager.getInstance().getInWorld().get(online).equals(WorldEnum.LOBBY)) {
                        hidePlayer(online, player, false);
                    } else if (!profile.isHideFromPlayers() || online.hasPermission("zpp.staffmode.see")) {
                        showPlayer(online, player);
                    } else if (profile.isHideFromPlayers() || !online.hasPermission("zpp.staffmode.see")) {
                        hidePlayer(online, player, true);
                    }
                }
            }
        }, 2L);
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
                hidePlayer(player, online, false);

            if (ProfileManager.getInstance().getProfile(online).isHidePlayers())
                hidePlayer(online, player, false);
        }

        // Show match players.
        for (Player matchPlayer : match.getPlayers())
            showPlayer(player, matchPlayer);

        // Hide other players.
        if (match instanceof Match) {
            for (Player hide : MatchManager.getInstance().getHidePlayers((Match) match)) {
                this.hidePlayer(player, hide, false);
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

        if (ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY))
            return;

        for (Player online : ServerManager.getInstance().getInWorld().keySet()) {
            if (player.equals(online)) continue;
            if (!ServerManager.getInstance().getInWorld().get(online).equals(WorldEnum.LOBBY)) continue;

            if (profile.isHidePlayers()) {
                hidePlayer(player, online, false);
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
                        hidePlayer(player, online, false);
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

                hidePlayer(online, player, true);
            } else {
                if (!ServerManager.getInstance().getInWorld().get(online).equals(WorldEnum.LOBBY)) continue;

                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);

                if (!onlineProfile.isHidePlayers())
                    showPlayer(online, player);
                else
                    hidePlayer(online, player, false);
            }
        }
    }


    public void hidePlayer(Player observer, Player target, boolean fullHide) {
        ClassImport.getClasses().getPlayerHiderUtil().hidePlayer(observer, target, fullHide);
    }

    public void showPlayer(Player observer, Player target) {
        ClassImport.getClasses().getPlayerHiderUtil().showPlayer(observer, target);
    }

    /*
    public void show(Player observer, Player target, boolean onlyTab)
    {
        if (observer.canSee(target))
            observer.hidePlayer(target);

        if (onlyTab)
        {
            EntityPlayer entityTarget = ((CraftPlayer) target).getHandle();
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityTarget);
            ((CraftPlayer) observer).getHandle().playerConnection.sendPacket(packet);
        }
    }
     */

    private boolean checkInvalidLobby() {
        if (ServerManager.getLobby() == null) {
            Common.sendConsoleMMMessage(LanguageManager.getString("SET-SERVER-LOBBY"));
            return true;
        }
        return false;
    }

}
