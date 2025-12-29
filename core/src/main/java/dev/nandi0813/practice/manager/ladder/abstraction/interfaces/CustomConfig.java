package dev.nandi0813.practice.manager.ladder.abstraction.interfaces;

import org.bukkit.configuration.file.YamlConfiguration;

public interface CustomConfig {

    void setCustomConfig(YamlConfiguration config);

    void getCustomConfig(YamlConfiguration config);

}
