package dev.nandi0813.practice.manager.server;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.backend.BackendManager;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.sidebar.SidebarManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.GoldenHead;
import dev.nandi0813.practice.util.StartUpTypes;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerManager implements Listener {

    private final ZonePractice zonePractice;

    private static ServerManager instance;

    public static ServerManager getInstance() {
        if (instance == null)
            instance = new ServerManager();
        return instance;
    }

    @Getter
    private static Location lobby = null;
    @Getter
    private final Map<Player, WorldEnum> inWorld = new HashMap<>();

    @Getter
    private final Map<String, OfflinePlayer> offlinePlayers = new HashMap<>(); // All the player that has ever been on the server is here.
    @Getter
    private final List<Player> onlineStaffs = new ArrayList<>();

    @Getter
    private final GoldenHead goldenHead;

    @Getter
    private final AutoSaveRunnable autoSaveRunnable = new AutoSaveRunnable();
    @Getter
    private final MysqlSaveRunnable mysqlSaveRunnable = new MysqlSaveRunnable();
    @Getter
    private final InactiveProfileRunnable inactiveProfileRunnable = new InactiveProfileRunnable();
    @Getter
    private final ProfileLimitRunnable profileLimitRunnable = new ProfileLimitRunnable();

    private ServerManager() {
        this.zonePractice = ZonePractice.getInstance();
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());

        this.goldenHead = new GoldenHead();

        start();
    }

    public void start() {
        if (ConfigManager.getBoolean("AUTO-SAVE.ENABLED"))
            autoSaveRunnable.begin();
        if (ConfigManager.getBoolean("PLAYER.DELETE-INACTIVE-USER.ENABLED")) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (ZonePractice.getInstance().getStartUpProgress().get(StartUpTypes.PROFILE_LOADING)) {
                        getInactiveProfileRunnable().begin();
                        this.cancel();
                    }
                }
            };
            runnable.runTaskTimer(ZonePractice.getInstance(), 0, 20L * 5);
        }
        if (ConfigManager.getBoolean("RANKED.LIMIT.ENABLED"))
            profileLimitRunnable.begin();
        if (ConfigManager.getBoolean("MYSQL-DATABASE.ENABLED"))
            mysqlSaveRunnable.begin();

        loadOfflinePlayers();
    }

    public void loadOfflinePlayers() {
        Bukkit.getScheduler().runTaskAsynchronously(zonePractice, () ->
        {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                offlinePlayers.put(offlinePlayer.getName(), offlinePlayer);
            }
        });
    }

    public void loadLobby() {
        try {
            // Reload backend to ensure Location objects are properly deserialized now that worlds are loaded
            BackendManager.reload();
            lobby = (Location) BackendManager.getConfig().get("lobby");
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Lobby cannot be found.");
        }
    }

    public void setLobby(Player player, Location newLobby) {
        lobby = newLobby;
        newLobby.getWorld().setSpawnLocation(newLobby.getBlockX(), newLobby.getBlockY(), newLobby.getBlockZ());
        InventoryManager.getInstance().setLobbyInventory(player, true);

        BackendManager.getConfig().set("lobby", lobby);
        BackendManager.save();
    }

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        WorldEnum from = null;
        if (inWorld.containsKey(player)) {
            from = inWorld.get(player);
        }
        World to = e.getTo().getWorld();

        inWorld.remove(player);

        if (lobby != null && to.equals(lobby.getWorld())) {
            if (from != null && from.equals(WorldEnum.OTHER)) {
                InventoryManager.getInstance().setLobbyInventory(player, true);
                if (!profile.isSidebar()) {
                    SidebarManager.getInstance().loadSidebar(player);
                }

                final Profile profile1 = profile;
                Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                {
                    PlayerUtil.setPlayerWorldTime(player);

                    if (ConfigManager.getBoolean("STAFF-MODE.JOIN-HIDE-FROM-PLAYERS") && player.hasPermission("zpp.staffmode"))
                        profile1.setHideFromPlayers(true);
                }, 10L);
            }

            inWorld.put(player, WorldEnum.LOBBY);
        } else if (to.equals(ArenaWorldUtil.getArenasWorld()) || to.equals(ArenaWorldUtil.getArenasCopyWorld())) {
            inWorld.put(player, WorldEnum.ARENA);
        } else {
            if (from != null && !from.equals(WorldEnum.OTHER)) {
                if (profile.getStatus().equals(ProfileStatus.LOBBY)) {
                    ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.OFFLINE);
                    ClassImport.getClasses().getPlayerUtil().clearInventory(player);
                    SidebarManager.getInstance().unLoadSidebar(player);
                }
            }

            inWorld.put(player, WorldEnum.OTHER);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (ServerManager.getLobby() == null) {
            Common.sendConsoleMMMessage(LanguageManager.getString("SET-SERVER-LOBBY"));
            if (player.isOp())
                Common.sendMMMessage(player, LanguageManager.getString("SET-SERVER-LOBBY"));
        }

        // Add player to the offline players list if they aren't in it.
        if (!offlinePlayers.containsKey(player.getName())) {
            offlinePlayers.put(player.getName(), player);
        }

        if (lobby == null) {
            inWorld.put(player, WorldEnum.OTHER);
        }
    }

    public void reloadFiles() {
        ConfigManager.reload();
        LanguageManager.reload();
        InventoryManager.getInstance().reloadFile();
        DivisionManager.getInstance().reloadRanks();
        BackendManager.reload();
    }

    public void alertPlayers(String permission, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.hasPermission(permission))
                    Common.sendMMMessage(player, message);
        });
    }

    public static void runConsoleCommand(String command) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console, command);
    }

}
