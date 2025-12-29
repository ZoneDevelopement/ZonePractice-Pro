package dev.nandi0813.practice.manager.profile;

import dev.nandi0813.api.Event.NewPlayerJoin;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.gui.guis.profile.ProfileSettingsGui;
import dev.nandi0813.practice.util.StartUpCallback;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
    private final Map<OfflinePlayer, UUID> uuids = new HashMap<>();
    @Getter
    private final Map<UUID, Profile> profiles = new HashMap<>();

    private final File folder = new File(ZonePractice.getInstance().getDataFolder() + "/profiles");


    public Profile getProfile(UUID uuid) {
        return profiles.getOrDefault(uuid, null);
    }

    public Profile getProfile(Player player) {
        if (player == null) return null;
        if (uuids.containsKey(player))
            return getProfile(uuids.get(player));

        uuids.put(player, player.getUniqueId());
        return getProfile(player);
    }

    public Profile getProfile(OfflinePlayer player) {
        if (player == null) return null;
        if (uuids.containsKey(player))
            return getProfile(uuids.get(player));

        uuids.put(player, player.getUniqueId());
        return getProfile(player);
    }

    public Profile getProfile(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof Player)
            return getProfile((Player) entity);
        return null;
    }

    public Profile newProfile(Player player, UUID uuid) {
        final Profile profile = new Profile(uuid);

        profile.getFile().setDefaultData();
        profile.getData();
        profile.getStats().setDivision(DivisionManager.getInstance().getDivision(profile));

        ProfileManager.getInstance().getProfiles().put(uuid, profile);
        ProfileManager.getInstance().loadProfileInfo(profile);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                Bukkit.getPluginManager().callEvent(new NewPlayerJoin(player)), 20L * 2);

        return profile;
    }

    public void loadProfiles(final StartUpCallback startUpCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            if (folder.isDirectory() && folder.listFiles() != null) {
                for (File profileFile : Objects.requireNonNull(folder.listFiles())) {
                    if (profileFile.isFile() && profileFile.getName().endsWith(".yml")) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(profileFile);
                        String uuidString = config.getString("uuid");

                        UUID uuid = UUID.fromString(uuidString);
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

                        if (offlinePlayer != null && offlinePlayer.getName() != null) {
                            Profile profile = new Profile(uuid, offlinePlayer);
                            profile.getData();
                            profiles.put(uuid, profile);
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), startUpCallback::onLoadingDone);
        });
    }

    public void loadAllProfileInformations() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            for (Profile profile : ProfileManager.getInstance().getProfiles().values())
                loadProfileInfo(profile);
        });
    }

    public void loadProfileInfo(Profile profile) {
        profile.getStats().setDivision(DivisionManager.getInstance().getDivision(profile));
        profile.setSettingsGui(new ProfileSettingsGui(profile));
    }

    public void saveProfiles() {
        for (Profile profile : profiles.values()) profile.saveData();
    }

}
