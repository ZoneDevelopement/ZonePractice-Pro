package dev.nandi0813.practice.manager.sidebar;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.sidebar.adapter.PracticeAdapter;
import dev.nandi0813.practice.manager.sidebar.adapter.SidebarAdapter;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.PermanentConfig;
import lombok.Getter;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SidebarManager extends ConfigFile implements Listener {

    private static SidebarManager instance;

    public static SidebarManager getInstance() {
        if (instance == null)
            instance = new SidebarManager();
        return instance;
    }

    @Getter
    private ScoreboardLibrary scoreboardLibrary;

    private final Map<Player, PracticeSidebar> boards = new HashMap<>();
    @Getter
    private final SidebarAdapter sidebarAdapter;
    private BukkitTask updateTask;

    private SidebarManager() {
        super("", "sidebar");
        this.sidebarAdapter = new PracticeAdapter();

        Bukkit.getServer().getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    public void load() {
        if (!isSidebarGloballyEnabled()) {
            return;
        }

        ensureScoreboardLibrary();
        startUpdateTask();
    }

    public boolean isSidebarGloballyEnabled() {
        return ConfigManager.getBoolean("SIDEBAR.ENABLED");
    }

    private void ensureScoreboardLibrary() {
        if (scoreboardLibrary != null) {
            return;
        }

        try {
            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(ZonePractice.getInstance());
        } catch (NoPacketAdapterAvailableException e) {
            // If no packet adapter was found, you can fallback to the no-op implementation:
            scoreboardLibrary = new NoopScoreboardLibrary();
            Common.sendConsoleMMMessage("<red>No scoreboard packet adapter available!");
        }
    }

    private void startUpdateTask() {
        if (!isSidebarGloballyEnabled()) {
            stopUpdateTask();
            return;
        }

        if (updateTask != null) {
            updateTask.cancel();
        }

        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(ZonePractice.getInstance(), () -> {
            for (PracticeSidebar practiceSidebar : new ArrayList<>(boards.values())) {
                if (practiceSidebar == null || practiceSidebar.getSidebar() == null || practiceSidebar.getSidebar().closed()) {
                    continue;
                }
                practiceSidebar.update();
            }
        }, 20L, ConfigManager.getInt("SIDEBAR.UPDATE-TIME"));
    }

    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
        {
            if (!isSidebarGloballyEnabled()) return;

            Profile profile = ProfileManager.getInstance().getProfile(player);
            if (!profile.isSidebar()) return;

            if (PermanentConfig.JOIN_TELEPORT_LOBBY)
                loadSidebar(player);
        }, 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (Bukkit.getPlayer(player.getUniqueId()) != null) {
                return;
            }

            unLoadSidebar(player);
        }, 1L);
    }

    public void loadSidebar(Player player) {
        if (!isSidebarGloballyEnabled())
            return;

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null || !profile.isSidebar())
            return;

        // If sidebar already exists, ensure it's still open before returning
        if (boards.containsKey(player)) {
            PracticeSidebar sidebar = boards.get(player);
            if (sidebar != null && sidebar.getSidebar() != null && !sidebar.getSidebar().closed()) {
                return;
            }
            // If the sidebar was closed, remove it so we can recreate it
            boards.remove(player);
        }

        ensureScoreboardLibrary();

        boards.put(player, new PracticeSidebar(this, scoreboardLibrary.createSidebar(), player));
    }

    public void unLoadSidebar(Player player) {
        if (!boards.containsKey(player))
            return;

        PracticeSidebar sidebar = boards.get(player);
        if (sidebar.getSidebar() != null) {
            sidebar.getSidebar().close();
            boards.remove(player);
        }
    }

    /**
     * Immediately updates a specific player's scoreboard
     * This is useful for real-time updates (e.g., hit counter)
     *
     * @param player the player whose scoreboard should be updated
     */
    public void updatePlayerSidebar(Player player) {
        if (!isSidebarGloballyEnabled())
            return;

        if (!boards.containsKey(player))
            return;

        PracticeSidebar sidebar = boards.get(player);
        if (sidebar != null && sidebar.getSidebar() != null && !sidebar.getSidebar().closed()) {
            sidebar.update();
        }
    }

    /**
     * Immediately updates multiple players' scoreboards
     * This is useful for real-time updates involving multiple players
     *
     * @param players the players whose scoreboards should be updated
     */
    public void updatePlayersSidebar(Player... players) {
        for (Player player : players) {
            updatePlayerSidebar(player);
        }
    }

    public void reloadSidebarConfig() {
        reloadFile();

        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            if (!isSidebarGloballyEnabled()) {
                for (Player player : new ArrayList<>(boards.keySet())) {
                    unLoadSidebar(player);
                }
                stopUpdateTask();
                return;
            }

            ensureScoreboardLibrary();
            startUpdateTask();

            for (Player player : Bukkit.getOnlinePlayers()) {
                Profile profile = ProfileManager.getInstance().getProfile(player);
                if (profile == null) {
                    continue;
                }

                if (profile.isSidebar()) {
                    loadSidebar(player);
                } else {
                    unLoadSidebar(player);
                }
            }

            for (Player player : new ArrayList<>(boards.keySet())) {
                updatePlayerSidebar(player);
            }
        });
    }

    @Override
    public void setData() {
    }

    @Override
    public void getData() {
    }

    public void close() {
        stopUpdateTask();

        for (Player player : new ArrayList<>(boards.keySet())) {
            unLoadSidebar(player);
        }

        try {
            if (scoreboardLibrary != null) {
                scoreboardLibrary.close();
            }
        } catch (IllegalStateException ignored) {
        }
    }
}
