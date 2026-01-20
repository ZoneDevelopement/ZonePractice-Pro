package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.manager.gui.GUIItem;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Version-specific interface for loading GUIItems from configuration files.
 * This abstraction handles differences between legacy (1.8) and modern (1.13+) item building.
 */
public interface ConfigItemProvider {

    /**
     * Creates a GUIItem from a YAML configuration section.
     *
     * @param config The YAML configuration file
     * @param loc    The location path in the configuration (e.g., "ITEMS.EXAMPLE_ITEM")
     * @return A fully configured GUIItem ready to be converted to an ItemStack
     */
    GUIItem getGuiItem(YamlConfiguration config, String loc);

}
