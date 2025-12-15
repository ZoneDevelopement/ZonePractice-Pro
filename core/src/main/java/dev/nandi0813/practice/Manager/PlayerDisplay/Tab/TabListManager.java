package dev.nandi0813.practice.Manager.PlayerDisplay.Tab;

import dev.nandi0813.practice.Manager.Backend.ConfigManager;
import dev.nandi0813.practice.ZonePractice;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class TabListManager implements Listener {

    private static final boolean ENABLED = ConfigManager.getBoolean("TAB-LIST.ENABLED");
    private static final int UPDATE_INTERVAL;
    public static final String HEADER_TEXT = String.join("\n", ConfigManager.getList("TAB-LIST.HEADER"));
    public static final String FOOTER_TEXT = String.join("\n", ConfigManager.getList("TAB-LIST.FOOTER"));

    static {
        int update_interval = ConfigManager.getInt("TAB-LIST.UPDATE");
        if (update_interval < 1) {
            update_interval = 1;
        }
        UPDATE_INTERVAL = update_interval;
    }

    private final Map<Player, TabList> tabLists;

    private static TabListManager instance;

    public static TabListManager getInstance() {
        if (instance == null)
            instance = new TabListManager();
        return instance;
    }

    private TabListManager() {
        this.tabLists = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    public void start() {
        if (ENABLED) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(ZonePractice.getInstance(),
                    this::updateTabs, 100L, 20L * UPDATE_INTERVAL);
        }
    }

    @EventHandler
    public synchronized void onPlayerJoin(PlayerJoinEvent e) {
        if (ENABLED) {
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                            tabLists.put(e.getPlayer(), new TabList(e.getPlayer())),
                    2L);
        }
    }

    @EventHandler
    public synchronized void onPlayerQuit(PlayerQuitEvent e) {
        tabLists.remove(e.getPlayer());
    }

    private synchronized void updateTabs() {
        for (TabList tabList : tabLists.values()) {
            tabList.setTab();
        }
    }

}
