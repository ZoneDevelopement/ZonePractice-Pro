package dev.nandi0813.practice.manager.backend;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public enum LanguageManager {
    ;

    @Getter
    private static File file;
    @Getter
    private static FileConfiguration config;

    public static void createFile(ZonePractice practice) {
        file = new File(practice.getDataFolder(), "language.yml");

        config = new YamlConfiguration();
        reload();
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    public static void reload() {
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    public static String getString(String loc) {
        return config.getString(loc.toUpperCase());
    }

    public static boolean getBoolean(String loc) {
        return config.getBoolean(loc.toUpperCase());
    }

    public static int getInt(String loc) {
        return config.getInt(loc.toUpperCase());
    }

    public static double getDouble(String loc) {
        return config.getDouble(loc.toUpperCase());
    }

    public static Set<String> getConfigSectionKeys(String loc) {
        return Objects.requireNonNull(config.getConfigurationSection(loc.toUpperCase())).getKeys(false);
    }

    public static List<String> getList(String loc) {
        return config.getStringList(loc.toUpperCase());
    }

}
