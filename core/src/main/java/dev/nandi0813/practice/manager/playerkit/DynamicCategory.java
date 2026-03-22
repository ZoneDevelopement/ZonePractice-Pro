package dev.nandi0813.practice.manager.playerkit;

import dev.nandi0813.practice.manager.playerkit.guis.CategoryGUI;
import dev.nandi0813.practice.manager.playerkit.items.EditorIcon;
import lombok.Getter;

/**
 * Represents a fully config-driven item category.
 * All categories (built-in and custom) are loaded from playerkit.yml at startup.
 * Adding a new category only requires editing the config — no code changes needed.
 */
@Getter
public class DynamicCategory {

    /** Config key id, e.g. "MOB-EGGS", "BLOCKS", "MY-CUSTOM-CATEGORY" */
    private final String id;
    /** Icon shown in the category picker GUI */
    private final EditorIcon icon;
    /** The paginated item list GUI for this category */
    private final CategoryGUI gui;

    public DynamicCategory(String id, EditorIcon icon, CategoryGUI gui) {
        this.id   = id;
        this.icon = icon;
        this.gui  = gui;
    }
}
