package dev.nandi0813.practice.manager.profile;

import dev.nandi0813.api.Event.NewPlayerJoin;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.MysqlManager;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.gui.guis.profile.ProfileSettingsGui;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StartUpCallback;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileManager {

    private static ProfileManager instance;

    public static ProfileManager getInstance() {
        if (instance == null)
            instance = new ProfileManager();
        return instance;
    }

    private ProfileManager() {
    }

    @Getter
    private final Map<Player, UUID> uuids = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Profile> profiles = new ConcurrentHashMap<>();

    private final File folder = new File(ZonePractice.getInstance().getDataFolder() + "/profiles");


    public Profile getProfile(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        Profile cached = profiles.get(uuid);
        if (cached != null) {
            return cached;
        }

        return loadProfileIfExists(uuid, Bukkit.getOfflinePlayer(uuid), false);
    }

    public Profile getProfile(Player player) {
        if (player == null) {
            return null;
        }
        UUID uuid = player.getUniqueId();
        uuids.put(player, uuid);
        Profile profile = profiles.get(uuid);
        if (profile == null) {
            return loadProfileIfExists(uuid, player, true);
        }
        if (!profile.isFullDataLoaded()) {
            profile.ensureFullDataLoaded();
            loadProfileInfo(profile);
        }
        return profile;
    }

    public Profile getProfile(OfflinePlayer player) {
        if (player == null) return null;

        UUID uuid = player.getUniqueId();
        Profile profile = profiles.get(uuid);
        if (profile != null) {
            return profile;
        }

        boolean loadFull = player.isOnline();
        return loadProfileIfExists(uuid, player, loadFull);
    }

    public Profile getProfile(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof Player)
            return getProfile((Player) entity);
        return null;
    }

    public Profile newProfile(Player player, UUID uuid) {
        Profile profile = new Profile(uuid);

        profile.getFile().setDefaultData();
        profile.getData();
        profile.getStats().setDivision(DivisionManager.getInstance().getDivision(profile));

        profiles.put(uuid, profile);
        loadProfileInfo(profile);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                Bukkit.getPluginManager().callEvent(new NewPlayerJoin(player)), 20L * 2);

        return profile;
    }

    public void loadProfiles(final StartUpCallback startUpCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            loadProfilesFromDisk();

            Collection<Profile> loadedProfiles = profiles.values();
            MysqlManager.loadProfilesAsync(loadedProfiles).whenComplete((ignored, throwable) -> {
                if (throwable != null) {
                    Common.sendConsoleMMMessage("<red>Error: " + throwable.getMessage());
                }
                Bukkit.getScheduler().runTask(ZonePractice.getInstance(), startUpCallback::onLoadingDone);
            });
        });
    }

    private void loadProfilesFromDisk() {
        if (!folder.exists() && !folder.mkdirs()) {
            Common.sendConsoleMMMessage("<red>Error: Could not create profiles folder.");
        }

        if (!folder.isDirectory()) return;

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File profileFile : files) {
            if (!profileFile.isFile() || !profileFile.getName().endsWith(".yml")) continue;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(profileFile);
            String uuidString = config.getString("uuid");
            UUID uuid = parseUuid(uuidString);

            if (uuid == null) {
                Common.sendConsoleMMMessage("<yellow>Warning: Skipping corrupted profile file <white>" + profileFile.getName() + "<yellow> (invalid or missing uuid: <white>" + uuidString + "<yellow>)");
                continue;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            Profile profile = new Profile(uuid, offlinePlayer);
            profile.loadStatsOnlyData();
            profile.getStats().setDivision(DivisionManager.getInstance().getDivision(profile));
            profiles.put(uuid, profile);
        }
    }

    private UUID parseUuid(String uuidString) {
        if (uuidString == null || uuidString.isBlank()) return null;

        try {
            return UUID.fromString(uuidString.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public void loadAllProfileInformations() {
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                Profile profile = getProfile(online);
                if (profile != null) {
                    loadProfileInfo(profile);
                }
            }
        });
    }

    public void loadProfileInfo(Profile profile) {
        if (profile == null) {
            return;
        }

        profile.getStats().setDivision(DivisionManager.getInstance().getDivision(profile));

        Player online = profile.getOnlinePlayer();
        if (online != null && online.isOnline() && profile.getSettingsGui() == null) {
            profile.setSettingsGui(new ProfileSettingsGui(profile));
        }
    }

    public void saveProfiles() {
        // Iterate over a stable snapshot to avoid ConcurrentModificationException
        // when autosave overlaps joins/quits/profile updates.
        for (Profile profile : new ArrayList<>(profiles.values())) {
            if (profile != null) {
                profile.saveData();
            }
        }
    }

    public void demoteOfflineProfile(UUID uuid) {
        if (uuid == null) {
            return;
        }

        Player online = Bukkit.getPlayer(uuid);
        if (online != null && online.isOnline()) {
            return;
        }

        Profile profile = profiles.get(uuid);
        if (profile == null) {
            return;
        }

        profile.saveData();
        profile.loadStatsOnlyData();
        profile.demoteToStatsOnly();
        MysqlManager.saveProfileAsync(profile);
    }

    public void clearPlayerReference(Player player) {
        if (player == null) {
            return;
        }

        uuids.remove(player);
    }

    private Profile loadProfileIfExists(UUID uuid, OfflinePlayer offlinePlayer, boolean loadFull) {
        File profileFile = new File(folder, uuid.toString().toLowerCase() + ".yml");
        if (!profileFile.exists()) {
            return null;
        }

        Profile loaded = profiles.computeIfAbsent(uuid, id -> new Profile(id, offlinePlayer));
        if (loadFull) {
            loaded.ensureFullDataLoaded();
            loadProfileInfo(loaded);
        } else if (!loaded.isFullDataLoaded() && loaded.getStats().getLadderStats().isEmpty()) {
            loaded.loadStatsOnlyData();
            loaded.getStats().setDivision(DivisionManager.getInstance().getDivision(loaded));
        }

        return loaded;
    }

}
