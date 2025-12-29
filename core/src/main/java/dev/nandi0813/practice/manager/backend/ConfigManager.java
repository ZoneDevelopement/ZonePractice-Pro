package dev.nandi0813.practice.manager.backend;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public enum ConfigManager {
    ;

    private static final ZonePractice practice = ZonePractice.getInstance();
    private static File file;
    @Getter
    private static YamlConfiguration config;

    public static void createFile() {
        file = new File(practice.getDataFolder(), "config.yml");
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

    public static Object get(String loc) {
        return getConfig().get(loc);
    }

    public static String getString(String loc) {
        String s = config.getString(loc);
        if (s != null)
            return StringUtil.CC(s);
        return "";
    }

    public static boolean getBoolean(String loc) {
        return getConfig().getBoolean(loc);
    }

    public static int getInt(String loc) {
        return getConfig().getInt(loc);
    }

    public static double getDouble(String loc) {
        return getConfig().getDouble(loc);
    }

    public static Set<String> getConfigSectionKeys(String loc) {
        return Objects.requireNonNull(getConfig().getConfigurationSection(loc)).getKeys(false);
    }

    public static List<String> getList(String loc) {
        return StringUtil.CC(getConfig().getStringList(loc));
    }

    public static Set<String> getConfigList(String loc) {
        return config.getConfigurationSection(loc.toUpperCase()).getKeys(false);
    }

    public static GUIItem getGuiItem(String loc) {
        return BackendUtil.getGuiItem(config, loc);
    }

}
