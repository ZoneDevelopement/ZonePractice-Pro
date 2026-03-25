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
            // Reload backend to ensure data is current now that worlds are loaded.
            BackendManager.reload();

            Object lobbyObj = BackendManager.getConfig().get("lobby");
            if (lobbyObj instanceof Location) {
                // Old format: Location was stored as a ConfigurationSerializable ("==" key).
                // Read it, then immediately migrate to the new primitive-field format so
                // future async saves never attempt to serialize a Location object again.
                lobby = (Location) lobbyObj;
                if (lobby.getWorld() != null) {
                    writeLobbyPrimitives(lobby);
                    BackendManager.save();
                    Common.sendConsoleMMMessage("<yellow>Migrated lobby location to primitive-field format in backend.yml.");
                }
            } else if (BackendManager.getConfig().isConfigurationSection("lobby")) {
                // New format: plain fields — world, x, y, z, yaw, pitch.
                String worldName = BackendManager.getConfig().getString("lobby.world");
                if (worldName != null) {
                    World world = Bukkit.getWorld(worldName);
                    lobby = new Location(
                            world,
                            BackendManager.getConfig().getDouble("lobby.x"),
                            BackendManager.getConfig().getDouble("lobby.y"),
                            BackendManager.getConfig().getDouble("lobby.z"),
                            (float) BackendManager.getConfig().getDouble("lobby.yaw"),
                            (float) BackendManager.getConfig().getDouble("lobby.pitch")
                    );
                }
            }
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Lobby cannot be found.");
        }
    }

    /**
     * Writes the lobby location to the BackendManager config as individual primitive fields
     * (world name + x/y/z/yaw/pitch) so that no {@link org.bukkit.configuration.serialization.ConfigurationSerializable}
     * object is ever stored in the config. This prevents SnakeYAML from calling
     * {@link Location#serialize()} during async saves, which can throw a NullPointerException
     * if the Location's world {@code WeakReference} has been cleared.
     */
    private void writeLobbyPrimitives(Location location) {
        // Remove any existing entry (could be a Location object or an old section)
        BackendManager.getConfig().set("lobby", null);
        BackendManager.getConfig().set("lobby.world", location.getWorld().getName());
        BackendManager.getConfig().set("lobby.x", location.getX());
        BackendManager.getConfig().set("lobby.y", location.getY());
        BackendManager.getConfig().set("lobby.z", location.getZ());
        BackendManager.getConfig().set("lobby.yaw", (double) location.getYaw());
        BackendManager.getConfig().set("lobby.pitch", (double) location.getPitch());
    }

    public void setLobby(Player player, Location newLobby) {
        lobby = newLobby;
        newLobby.getWorld().setSpawnLocation(newLobby.getBlockX(), newLobby.getBlockY(), newLobby.getBlockZ());
        InventoryManager.getInstance().setLobbyInventory(player, true);

        writeLobbyPrimitives(newLobby);
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
                    dev.nandi0813.practice.manager.fight.util.PlayerUtil.clearInventory(player);
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
        goldenHead.reload();
    }

    public void alertPlayers(String permission, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.hasPermission(permission))
                    Common.sendMMMessage(player, message);
        });
    }

    /**
     * Resolves a player by name. First checks currently online players (exact match),
     * then falls back to the offlinePlayers map. This ensures that online players are
     * always found even if the offlinePlayers map is in an inconsistent state due to
     * the async loading in {@link #loadOfflinePlayers()}.
     *
     * @param name the player name to look up
     * @return the matching OfflinePlayer, or {@code null} if not found
     */
    public OfflinePlayer resolvePlayer(String name) {
        // First try online players — this is always reliable
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;

        // Fall back to the offline map
        return offlinePlayers.get(name);
    }

    public static void runConsoleCommand(String command) {
        if (!ZonePractice.getInstance().isEnabled()) {
            return;
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console, command);
    }

}
