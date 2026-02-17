package dev.nandi0813.practice.manager.nametag;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NametagManager {

    private static NametagManager instance;

    public static NametagManager getInstance() {
        if (instance == null)
            instance = new NametagManager();
        return instance;
    }

    private final Map<String, FakeTeam> TEAMS = new ConcurrentHashMap<>();
    private final Map<String, FakeTeam> CACHED_FAKE_TEAMS = new ConcurrentHashMap<>();

    /**
     * Initialize the NametagManager and detect TAB plugin conflicts.
     * Should be called on plugin enable.
     */
    public void initialize() {
        // Detect TAB plugin and manage conflict resolution
        TeamPacketBlocker.getInstance().register();
    }

    /**
     * Shutdown the NametagManager.
     * Should be called on plugin disable.
     */
    public void shutdown() {
        TeamPacketBlocker.getInstance().unregister();
    }

    // ==============================================================
    // TAB Integration Helper Methods
    // ==============================================================

    /**
     * Checks if TAB plugin is managing teams instead of our internal system.
     *
     * @return true if TAB is active and managing teams
     */
    private boolean isUsingTabSystem() {
        return TeamPacketBlocker.getInstance().isNametagSystemDisabled();
    }

    /**
     * Gets the TAB integration instance if available.
     *
     * @return TabIntegration instance or null
     */
    private TabIntegration getTabIntegration() {
        if (!isUsingTabSystem()) return null;

        TabIntegration integration = TeamPacketBlocker.getInstance().getTabIntegration();
        return (integration != null && integration.isAvailable()) ? integration : null;
    }

    /**
     * Attempts to set a nametag via TAB API.
     *
     * @return true if handled by TAB, false if should use internal system
     */
    private boolean trySetViaTab(Player player, Component prefix, NamedTextColor nameColor, Component suffix, int sortPriority) {
        TabIntegration tab = getTabIntegration();
        if (tab != null) {
            tab.setNametag(player, prefix, nameColor, suffix, sortPriority);
            return true;
        }
        return isUsingTabSystem(); // Return true if TAB is active but API unavailable (skip internal system)
    }

    /**
     * Attempts to reset a nametag via TAB API.
     *
     * @return true if handled by TAB, false if should use internal system
     */
    private boolean tryResetViaTab(String playerName) {
        TabIntegration tab = getTabIntegration();
        if (tab != null) {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                tab.resetNametag(player);
                return true;
            }
        }
        return isUsingTabSystem(); // Return true if TAB is active but API unavailable (skip internal system)
    }

    // ==============================================================
    // Team Management (Internal System)
    // ==============================================================

    /**
     * Gets the current team given a prefix and suffix
     * If there is no team similar to this, then a new
     * team is created.
     */
    private FakeTeam getFakeTeam(Component prefix, NamedTextColor nameColor, Component suffix) {
        return TEAMS.values().stream().filter(fakeTeam -> fakeTeam.isSimilar(prefix, nameColor, suffix)).findFirst().orElse(null);
    }

    /**
     * Adds a player to a FakeTeam. If they are already on this team,
     * we do NOT change that.
     */
    private void addPlayerToTeam(String player, Component prefix, NamedTextColor namedTextColor, Component suffix, int sortPriority) {
        FakeTeam previous = getFakeTeam(player);

        if (previous != null && previous.isSimilar(prefix, namedTextColor, suffix))
            return;

        reset(player);

        FakeTeam joining = getFakeTeam(prefix, namedTextColor, suffix);
        if (joining != null) {
            joining.addMember(player);
        } else {
            joining = new FakeTeam(prefix, namedTextColor, suffix, sortPriority);
            joining.addMember(player);
            TEAMS.put(joining.getName(), joining);

            // Register this team with the packet blocker so our packets aren't blocked
            TeamPacketBlocker.getInstance().registerOurTeam(joining.getName());

            addTeamPackets(joining);
        }

        Player adding = Bukkit.getPlayerExact(player);
        if (adding != null) {
            addPlayerToTeamPackets(joining, adding.getName());
            cache(adding.getName(), joining);
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
            addPlayerToTeamPackets(joining, offlinePlayer.getName());
            cache(offlinePlayer.getName(), joining);
        }
    }

    /**
     * Resets a player's nametag to default.
     * Automatically uses TAB API if available, otherwise uses internal system.
     */
    public FakeTeam reset(String player) {
        if (tryResetViaTab(player)) {
            return null; // Handled by TAB or TAB is active (skip internal)
        }

        return resetInternal(player, decache(player));
    }

    /**
     * Internal method to reset nametag using our packet system.
     * Only called when TAB is not managing teams.
     */
    private FakeTeam resetInternal(String player, FakeTeam fakeTeam) {
        if (fakeTeam != null && fakeTeam.getMembers().remove(player)) {
            boolean delete;
            Player removing = Bukkit.getPlayerExact(player);
            if (removing != null) {
                delete = removePlayerFromTeamPackets(fakeTeam, removing.getName());
            } else {
                OfflinePlayer toRemoveOffline = Bukkit.getOfflinePlayer(player);
                delete = removePlayerFromTeamPackets(fakeTeam, toRemoveOffline.getName());
            }

            if (delete) {
                removeTeamPackets(fakeTeam);
                TEAMS.remove(fakeTeam.getName());

                // Unregister this team from the packet blocker
                TeamPacketBlocker.getInstance().unregisterOurTeam(fakeTeam.getName());
            }
        }

        return fakeTeam;
    }

    // ==============================================================
    // Below are public methods to modify the cache
    // ==============================================================
    private FakeTeam decache(String player) {
        return CACHED_FAKE_TEAMS.remove(player);
    }

    public FakeTeam getFakeTeam(String player) {
        return CACHED_FAKE_TEAMS.get(player);
    }

    private void cache(String player, FakeTeam fakeTeam) {
        CACHED_FAKE_TEAMS.put(player, fakeTeam);
    }

    // ==============================================================
    // Public API Methods
    // ==============================================================

    /**
     * Sets a player's nametag with prefix, color, and suffix.
     * Automatically uses TAB API if available, otherwise uses internal system.
     */
    public void setNametag(Player player, Component prefix, NamedTextColor namedTextColor, Component suffix, int sortPriority) {
        if (trySetViaTab(player, prefix, namedTextColor, suffix, sortPriority)) {
            return; // Handled by TAB or TAB is active (skip internal)
        }

        setNametagInternal(player.getName(), prefix, namedTextColor, suffix, sortPriority);

        // In Minecraft 1.21+, scoreboard team colors can affect the tab list
        // We need to explicitly preserve the lobby tab list name to prevent match nametag colors from bleeding into the tab list
        preserveTabListName(player);
    }

    /**
     * Preserves the player's tab list name based on their lobby settings.
     * This prevents match nametag colors from affecting the tab list in modern Minecraft versions.
     * Only called when TAB is not managing the tab list.
     */
    private void preserveTabListName(Player player) {
        try {
            dev.nandi0813.practice.manager.profile.Profile profile =
                dev.nandi0813.practice.manager.profile.ProfileManager.getInstance().getProfile(player);
            if (profile == null) return;

            // Re-calculate the lobby tab list name from profile data
            Component lobbyPrefix = Component.empty();
            Component lobbySuffix = Component.empty();
            NamedTextColor lobbyNameColor = NamedTextColor.GRAY;

            dev.nandi0813.practice.manager.profile.group.Group group = profile.getGroup();
            if (group != null) {
                lobbyPrefix = group.getPrefix();
                lobbySuffix = group.getSuffix();
                lobbyNameColor = group.getNameColor();
            }

            // Apply custom prefix/suffix if set
            if (profile.getPrefix() != null) lobbyPrefix = profile.getPrefix();
            if (profile.getSuffix() != null) lobbySuffix = profile.getSuffix();

            // Build the tab list name (same as lobby)
            Component tabListName = lobbyPrefix.append(Component.text(player.getName(), lobbyNameColor)).append(lobbySuffix);

            // Apply division placeholders if needed
            if (profile.getStats().getDivision() != null) {
                tabListName = tabListName
                    .replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                        .match("%division%")
                        .replacement(profile.getStats().getDivision().getComponentFullName())
                        .build())
                    .replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                        .match("%division_short%")
                        .replacement(profile.getStats().getDivision().getComponentShortName())
                        .build());
            } else {
                tabListName = tabListName
                    .replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                        .match("%division%")
                        .replacement(Component.empty())
                        .build())
                    .replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                        .match("%division_short%")
                        .replacement(Component.empty())
                        .build());
            }

            // Set the tab list name to maintain lobby formatting
            dev.nandi0813.practice.module.util.ClassImport.getClasses().getPlayerUtil().setPlayerListName(player, tabListName);
        } catch (Exception e) {
            // Silently fail - this is a best-effort preservation
        }
    }

    /**
     * Internal method to set nametag using our packet system.
     * Only called when TAB is not managing teams.
     */
    private void setNametagInternal(String player, Component prefix, NamedTextColor namedTextColor, Component suffix, int sortPriority) {
        Component finalPrefix = prefix != null ? prefix : Component.empty();
        Component finalSuffix = suffix != null ? suffix : Component.empty();
        addPlayerToTeam(player, finalPrefix, namedTextColor, finalSuffix, sortPriority);
    }

    /**
     * Sends all current teams to a player (for when they join).
     * Skips if TAB is managing teams.
     */
    public void sendTeams(Player player) {
        if (isUsingTabSystem()) {
            return; // TAB handles this
        }

        for (FakeTeam fakeTeam : TEAMS.values()) {
            new Packet(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getNameColor(), fakeTeam.getSuffix(), fakeTeam.getMembers()).send(player);
        }
    }


    // ==============================================================
    // Below are private methods to construct a new Scoreboard packet
    // ==============================================================
    private void removeTeamPackets(FakeTeam fakeTeam) {
        new Packet(fakeTeam.getName()).send();
    }

    private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, String... players) {
        return removePlayerFromTeamPackets(fakeTeam, Arrays.asList(players));
    }

    private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, List<String> players) {
        new Packet(fakeTeam.getName(), players, WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES).send();
        fakeTeam.getMembers().removeAll(players);
        return fakeTeam.getMembers().isEmpty();
    }

    private void addTeamPackets(FakeTeam fakeTeam) {
        new Packet(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getNameColor(), fakeTeam.getSuffix(), fakeTeam.getMembers()).send();
    }

    private void addPlayerToTeamPackets(FakeTeam fakeTeam, String player) {
        new Packet(fakeTeam.getName(), Collections.singletonList(player), WrapperPlayServerTeams.TeamMode.ADD_ENTITIES).send();
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}