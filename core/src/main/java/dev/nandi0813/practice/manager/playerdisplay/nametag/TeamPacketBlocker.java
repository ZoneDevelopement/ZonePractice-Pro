package dev.nandi0813.practice.manager.playerdisplay.nametag;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intercepts and blocks team packets from other plugins (like TAB) to prevent
 * conflicts with our NametagManager.
 * <p>
 * This allows ZonePracticePro to have full control over scoreboard teams while
 * other plugins like TAB can still function for other features (tablist, etc.)
 */
public class TeamPacketBlocker extends PacketListenerAbstract {

    private static TeamPacketBlocker instance;
    private static boolean registered = false;

    // Team names managed by our NametagManager - we allow these through
    private final Set<String> ourTeamNames = ConcurrentHashMap.newKeySet();

    // Whether blocking is enabled
    private boolean blockingEnabled = false;

    @Getter
    private boolean tabPluginPresent = false;

    private TeamPacketBlocker() {
        super(PacketListenerPriority.HIGHEST);
    }

    public static TeamPacketBlocker getInstance() {
        if (instance == null) {
            instance = new TeamPacketBlocker();
        }
        return instance;
    }

    /**
     * Registers the packet listener and enables blocking if TAB is detected.
     */
    public void register() {
        if (registered) return;

        // Check if TAB plugin is present
        Plugin tabPlugin = Bukkit.getPluginManager().getPlugin("TAB");
        tabPluginPresent = tabPlugin != null && tabPlugin.isEnabled();

        if (tabPluginPresent) {
            blockingEnabled = true;
            PacketEvents.getAPI().getEventManager().registerListener(this);
            registered = true;
        }
    }

    /**
     * Unregisters the packet listener.
     */
    public void unregister() {
        if (!registered) return;

        PacketEvents.getAPI().getEventManager().unregisterListener(this);
        registered = false;
        blockingEnabled = false;
    }

    /**
     * Register a team name as "ours" - packets for this team will be allowed through.
     */
    public void registerOurTeam(String teamName) {
        ourTeamNames.add(teamName);
    }

    /**
     * Unregister a team name when we delete it.
     */
    public void unregisterOurTeam(String teamName) {
        ourTeamNames.remove(teamName);
    }

    /**
     * Check if a team name belongs to us.
     */
    public boolean isOurTeam(String teamName) {
        return ourTeamNames.contains(teamName);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!blockingEnabled) return;

        if (event.getPacketType() != PacketType.Play.Server.TEAMS) return;

        WrapperPlayServerTeams packet = new WrapperPlayServerTeams(event);
        String teamName = packet.getTeamName();

        // Allow our own team packets through
        if (isOurTeam(teamName)) {
            return;
        }

        // Block team packets from other sources (like TAB)
        // This prevents the race condition that causes "Cannot remove from team" errors
        event.setCancelled(true);
    }

}
