package dev.nandi0813.practice.manager.backend;

import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Backend utility for loading items from configuration files.
 * Delegates to version-specific ConfigItemProvider implementations.
 */
public enum BackendUtil {
    ;

    /**
     * Creates a GUIItem from a YAML configuration using version-specific logic.
     *
     * <p>In 1.8.8: Uses DAMAGE value for item data (colors, variants)</p>
     * <p>In Modern (1.13+): Ignores DAMAGE and makes items unbreakable to prevent durability bars</p>
     *
     * @param config The YAML configuration file
     * @param loc    The location path in the configuration (e.g., "ITEMS.EXAMPLE_ITEM")
     * @return A fully configured GUIItem
     */
    public static GUIItem getGuiItem(YamlConfiguration config, String loc) {
        return ClassImport.getClasses().getConfigItemProvider().getGuiItem(config, loc);
    }

}
