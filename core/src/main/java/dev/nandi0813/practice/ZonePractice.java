package dev.nandi0813.practice;

import com.github.retrooper.packetevents.PacketEvents;
import dev.nandi0813.practice.command.accept.AcceptCommand;
import dev.nandi0813.practice.command.arena.ArenaCommand;
import dev.nandi0813.practice.command.division.DivisionsCommand;
import dev.nandi0813.practice.command.duel.DuelCommand;
import dev.nandi0813.practice.command.event.EventCommand;
import dev.nandi0813.practice.command.ffa.FFACommand;
import dev.nandi0813.practice.command.hologram.HologramCommand;
import dev.nandi0813.practice.command.ladder.LadderCommand;
import dev.nandi0813.practice.command.matchstats.MatchStatsCommand;
import dev.nandi0813.practice.command.party.PartyCommand;
import dev.nandi0813.practice.command.practice.PracticeCommand;
import dev.nandi0813.practice.command.preview.PreviewCommand;
import dev.nandi0813.practice.command.privatemessage.MessageCommand;
import dev.nandi0813.practice.command.privatemessage.ReplyCommand;
import dev.nandi0813.practice.command.settings.SettingsCommand;
import dev.nandi0813.practice.command.setup.SetupCommand;
import dev.nandi0813.practice.command.singlecommands.*;
import dev.nandi0813.practice.command.spectate.SpectateCommand;
import dev.nandi0813.practice.command.staff.StaffCommand;
import dev.nandi0813.practice.command.statistics.StatisticsCommand;
import dev.nandi0813.practice.listener.*;
import dev.nandi0813.practice.manager.arena.ArenaListener;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.setup.SpawnMarkerManager;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.backend.*;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.listener.BuildBlockListener;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.util.EntityHiderListener;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.leaderboard.LeaderboardManager;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramManager;
import dev.nandi0813.practice.manager.nametag.NametagManager;
import dev.nandi0813.practice.manager.playerkit.PlayerKitManager;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.manager.sidebar.SidebarManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.module.util.VersionChecker;
import dev.nandi0813.practice.util.*;
import dev.nandi0813.practice.util.placeholderapi.PlayerExpansion;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

public final class ZonePractice extends JavaPlugin {

    @Getter
    private Map<StartUpTypes, Boolean> startUpProgress = new EnumMap<>(StartUpTypes.class);

    @Getter
    private static ZonePractice instance;
    @Getter
    private static BukkitAudiences adventure;
    @Getter
    private static MiniMessage miniMessage;

    @Getter
    private static volatile boolean fullyLoaded = false;

    private Metrics metrics;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        miniMessage = MiniMessage.miniMessage();
        PacketEvents.getAPI().init();
        metrics = new Metrics(this, 16055);

        if (VersionChecker.getBukkitVersion() == null) {
            Common.sendConsoleMMMessage("<red>Unsupported server version! Please use 1.8.8 or 1.8.9 or 1.20.6 or 1.21.4");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new SaveResource().saveResources(this);

        ConfigManager.createFile();
        LanguageManager.createFile(this);
        GUIFile.createFile(this);
        MysqlManager.openConnection();
        DivisionManager.getInstance().getData();
        ArenaWorldUtil.createArenaWorld();
        BackendManager.createFile(this);

        ZonePracticeApiImpl.setup();
        StartUpUtil.loadStartUpProgressMap();

        this.registerCommands(Bukkit.getServer());
        this.registerListeners(Bukkit.getPluginManager());

        ServerManager.getInstance().loadLobby();
        InventoryManager.getInstance().loadInventories();
        PlayerKitManager.getInstance().load();
        NametagManager.getInstance().initialize(); // Initialize after all plugins loaded to detect TAB conflicts

        LadderManager.getInstance().loadLadders(() ->
        {
            LadderManager.getInstance().getLadders().sort(Comparator.comparing(Ladder::getName));
            startUpProgress.replace(StartUpTypes.LADDER_LOADING, true);

            ArenaManager.getInstance().loadArenas(() ->
            {
                ArenaGUISetupManager.getInstance().loadGUIs();
                startUpProgress.replace(StartUpTypes.ARENA_LOADING, true);

                // Clean up any orphaned marker armor stands from previous server sessions
                org.bukkit.World arenasWorld = ArenaWorldUtil.getArenasWorld();
                if (arenasWorld != null) {
                    SpawnMarkerManager.getInstance().cleanupOrphanedMarkers(arenasWorld);
                }
            });

            ProfileManager.getInstance().loadProfiles(() ->
            {
                ProfileManager.getInstance().loadAllProfileInformations();
                startUpProgress.replace(StartUpTypes.PROFILE_LOADING, true);

                LeaderboardManager.getInstance().createAllLB(() ->
                {
                    startUpProgress.replace(StartUpTypes.LEADERBOARD_LOADING, true);
                    LadderManager.getInstance().loadGUIs();

                    HologramManager.getInstance().loadHolograms();
                    dev.nandi0813.practice.manager.leaderboard.hologram.HologramProtectionListener.register();
                    startUpProgress.replace(StartUpTypes.HOLOGRAM_LOADING, true);

                    SidebarManager.getInstance().load();
                    startUpProgress.replace(StartUpTypes.SIDEBAR_LOADING, true);

                    this.loadPlaceholderAPI();

                    // Mark plugin as fully loaded
                    fullyLoaded = true;
                });
            });
        });

        EventManager.getInstance().loadEventData(() ->
        {
            EventManager.getInstance().loadGUIs();
            startUpProgress.replace(StartUpTypes.EVENT_LOADING, true);
        });

        FFAManager.getInstance();
        EntityHiderListener.getInstance();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();

        // Shutdown nametag manager and unregister packet blocker
        NametagManager.getInstance().shutdown();

        // Clear all spawn markers to prevent them persisting after server restart
        SpawnMarkerManager.getInstance().clearAllMarkers();

        // Clear all event spawn markers
        dev.nandi0813.practice.manager.fight.event.setup.EventSpawnMarkerManager.getInstance().clearAllMarkers();

        MatchManager.getInstance().endMatches();
        FFAManager.getInstance().endFFAs();
        HologramManager.getInstance().saveAndDespawnHolograms(); // Use saveAndDespawn for shutdown to clean up armor stands
        EventManager.getInstance().endEvents();
        EventManager.getInstance().saveEventData();
        ArenaManager.getInstance().saveArenas();
        ProfileManager.getInstance().saveProfiles();
        LadderManager.getInstance().saveLadders();
        SidebarManager.getInstance().close();
        InventoryManager.getInstance().setData();
        if (adventure != null) adventure.close();
        if (metrics != null) metrics.shutdown();
        MysqlManager.closeConnection();
        BackendManager.save();
    }

    /**
     * It registers all the commands that the plugin uses
     */
    private void registerCommands(Server server) {
        AcceptCommand acceptCommand = new AcceptCommand();
        if (server.getPluginCommand("accept") != null) {
            server.getPluginCommand("accept").setExecutor(acceptCommand);
            server.getPluginCommand("accept").setTabCompleter(acceptCommand);
        }

        ArenaCommand arenaCommand = new ArenaCommand();
        if (server.getPluginCommand("arena") != null) {
            server.getPluginCommand("arena").setExecutor(arenaCommand);
            server.getPluginCommand("arena").setTabCompleter(arenaCommand);
        }

        DuelCommand duelCommand = new DuelCommand();
        if (server.getPluginCommand("duel") != null) {
            server.getPluginCommand("duel").setExecutor(duelCommand);
            server.getPluginCommand("duel").setTabCompleter(duelCommand);
        }

        EventCommand eventCommand = new EventCommand();
        if (server.getPluginCommand("event") != null) {
            server.getPluginCommand("event").setExecutor(eventCommand);
            server.getPluginCommand("event").setTabCompleter(eventCommand);
        }

        HologramCommand hologramCommand = new HologramCommand();
        if (server.getPluginCommand("hologram") != null) {
            server.getPluginCommand("hologram").setExecutor(hologramCommand);
            server.getPluginCommand("hologram").setTabCompleter(hologramCommand);
        }

        LadderCommand ladderCommand = new LadderCommand();
        if (server.getPluginCommand("ladder") != null) {
            server.getPluginCommand("ladder").setExecutor(ladderCommand);
            server.getPluginCommand("ladder").setTabCompleter(ladderCommand);
        }

        MatchStatsCommand matchStatsCommand = new MatchStatsCommand();
        if (server.getPluginCommand("matchinv") != null) {
            server.getPluginCommand("matchinv").setExecutor(matchStatsCommand);
        }

        PartyCommand partyCommand = new PartyCommand();
        if (server.getPluginCommand("party") != null) {
            server.getPluginCommand("party").setExecutor(partyCommand);
            server.getPluginCommand("party").setTabCompleter(partyCommand);
        }

        PracticeCommand practiceCommand = new PracticeCommand();
        if (server.getPluginCommand("practice") != null) {
            server.getPluginCommand("practice").setExecutor(practiceCommand);
            server.getPluginCommand("practice").setTabCompleter(practiceCommand);
        }

        PreviewCommand previewCommand = new PreviewCommand();
        if (server.getPluginCommand("preview") != null) {
            server.getPluginCommand("preview").setExecutor(previewCommand);
            server.getPluginCommand("preview").setTabCompleter(previewCommand);
        }

        DivisionsCommand divisionsCommand = new DivisionsCommand();
        if (server.getPluginCommand("divisions") != null) {
            server.getPluginCommand("divisions").setExecutor(divisionsCommand);
        }

        SettingsCommand settingsCommand = new SettingsCommand();
        if (server.getPluginCommand("settings") != null) {
            server.getPluginCommand("settings").setExecutor(settingsCommand);
        }

        SetupCommand setupCommand = new SetupCommand();
        if (server.getPluginCommand("setup") != null) {
            server.getPluginCommand("setup").setExecutor(setupCommand);
        }

        SpectateCommand spectateCommand = new SpectateCommand();
        if (server.getPluginCommand("spectate") != null) {
            server.getPluginCommand("spectate").setExecutor(spectateCommand);
            server.getPluginCommand("spectate").setTabCompleter(spectateCommand);
        }

        StaffCommand staffCommand = new StaffCommand();
        if (server.getPluginCommand("staff") != null) {
            server.getPluginCommand("staff").setExecutor(staffCommand);
            server.getPluginCommand("staff").setTabCompleter(staffCommand);
        }

        StatisticsCommand statisticsCommand = new StatisticsCommand();
        if (server.getPluginCommand("statistics") != null) {
            server.getPluginCommand("statistics").setExecutor(statisticsCommand);
            server.getPluginCommand("statistics").setTabCompleter(statisticsCommand);
        }

        UnrankedCommand unrankedCommand = new UnrankedCommand();
        if (server.getPluginCommand("unranked") != null) {
            server.getPluginCommand("unranked").setExecutor(unrankedCommand);
        }

        RankedCommand rankedCommand = new RankedCommand();
        if (server.getPluginCommand("ranked") != null) {
            server.getPluginCommand("ranked").setExecutor(rankedCommand);
        }

        EditorCommand editorCommand = new EditorCommand();
        if (server.getPluginCommand("editor") != null) {
            server.getPluginCommand("editor").setExecutor(editorCommand);
        }

        CopyKitCommand copyKitCommand = new CopyKitCommand();
        if (server.getPluginCommand("copykit") != null) {
            server.getPluginCommand("copykit").setExecutor(copyKitCommand);
        }

        FFACommand ffaCommand = new FFACommand();
        if (server.getPluginCommand("ffa") != null) {
            server.getPluginCommand("ffa").setExecutor(ffaCommand);
            server.getPluginCommand("ffa").setTabCompleter(ffaCommand);
        }

        IgnoreQueueCommand ignoreQueueCommand = new IgnoreQueueCommand();
        if (server.getPluginCommand("ignorequeue") != null) {
            server.getPluginCommand("ignorequeue").setExecutor(ignoreQueueCommand);
            server.getPluginCommand("ignorequeue").setTabCompleter(ignoreQueueCommand);
        }

        if (ConfigManager.getBoolean("CHAT.PRIVATE-CHAT-ENABLED")) {
            new MessageCommand();
            new ReplyCommand();
        }

        if (ConfigManager.getBoolean("MATCH-SETTINGS.LEAVE-COMMAND.ENABLED")) {
            new LeaveCommand();
        }
    }

    private void loadPlaceholderAPI() {
        if (SoftDependUtil.isPAPI_ENABLED) {
            PlayerExpansion playerExpansion = new PlayerExpansion();
            playerExpansion.register();
        }
    }

    /**
     * It registers all the events that are used in the plugin
     */
    private void registerListeners(PluginManager pm) {
        pm.registerEvents(new BuildBlockListener(), this);
        pm.registerEvents(ClassImport.getClasses().getBuildListener(), this);

        pm.registerEvents(new PlayerPreLogin(), this);
        pm.registerEvents(new PlayerJoin(), this);
        pm.registerEvents(new PlayerQuit(), this);
        pm.registerEvents(new PlayerInteract(), this);
        pm.registerEvents(new WeatherChange(), this);
        pm.registerEvents(new ItemConsume(), this);
        pm.registerEvents(new ProjectileLaunch(), this);
        pm.registerEvents(new PlayerCommandPreprocess(), this);
        pm.registerEvents(new PlayerChat(), this);
        pm.registerEvents(new EntityDamage(), this);
        pm.registerEvents(new ArenaListener(), this);
    }

}
