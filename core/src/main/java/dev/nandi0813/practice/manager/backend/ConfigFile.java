package dev.nandi0813.practice.manager.backend;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
public abstract class ConfigFile {

    protected final String fileName;

    protected final File file;
    protected final YamlConfiguration config;

    protected ConfigFile(String path, String fileName) {
        this.fileName = fileName;

        this.file = new File(ZonePractice.getInstance().getDataFolder() + path, fileName + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public abstract void setData();

    public abstract void getData();

    public void reloadFile() {
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    public void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    public Object get(String loc) {
        return config.get(loc);
    }

    public String getString(String loc) {
        return config.getString(loc);
    }

    public boolean getBoolean(String loc) {
        return config.getBoolean(loc);
    }

    public int getInt(String loc) {
        return config.getInt(loc);
    }

    public double getDouble(String loc) {
        return config.getDouble(loc);
    }

    public Set<String> getConfigSectionKeys(String loc) {
        return Objects.requireNonNull(config.getConfigurationSection(loc)).getKeys(false);
    }

    public boolean isList(String loc) {
        return config.isList(loc);
    }

    public List<String> getList(String loc) {
        return config.getStringList(loc);
    }

    public ItemStack getItemStack(String loc) {
        return config.getItemStack(loc);
    }

    public GUIItem getGuiItem(String loc) {
        return BackendUtil.getGuiItem(config, loc);
    }

}
