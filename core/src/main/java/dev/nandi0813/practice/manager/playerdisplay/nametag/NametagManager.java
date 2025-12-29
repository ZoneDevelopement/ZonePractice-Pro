package dev.nandi0813.practice.manager.playerdisplay.nametag;

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

    public FakeTeam reset(String player) {
        return reset(player, decache(player));
    }

    private FakeTeam reset(String player, FakeTeam fakeTeam) {
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
    // Below are public methods to modify certain data
    // ==============================================================
    public void setNametag(Player player, Component prefix, NamedTextColor namedTextColor, Component suffix, int sortPriority) {
        setNametag(player.getName(), prefix, namedTextColor, suffix, sortPriority);
    }

    void setNametag(String player, Component prefix, NamedTextColor namedTextColor, Component suffix, int sortPriority) {
        addPlayerToTeam(player, prefix != null ? prefix : Component.empty(), namedTextColor, suffix != null ? suffix : Component.empty(), sortPriority);
    }

    public void sendTeams(Player player) {
        for (FakeTeam fakeTeam : TEAMS.values()) {
            new Packet(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getNameColor(), fakeTeam.getSuffix(), fakeTeam.getMembers()).send(player);
        }
    }

    /*
    void reset() {
        for (FakeTeam fakeTeam : TEAMS.values()) {
            removePlayerFromTeamPackets(fakeTeam, fakeTeam.getMembers());
            removeTeamPackets(fakeTeam);
        }
        CACHED_FAKE_TEAMS.clear();
        TEAMS.clear();
    }
     */

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