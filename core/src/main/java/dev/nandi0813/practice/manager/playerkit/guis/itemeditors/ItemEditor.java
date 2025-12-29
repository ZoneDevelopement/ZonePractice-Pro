package dev.nandi0813.practice.manager.playerkit.guis.itemeditors;

import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.playerkit.items.EditorIcon;
import org.bukkit.inventory.Inventory;

import java.util.List;

public abstract class ItemEditor extends GUI {

    protected final List<EditorIcon> icons;

    public ItemEditor(GUIType type, List<EditorIcon> icons) {
        super(type);
        this.icons = icons;
    }

    protected EditorIcon getIcon(int slot) {
        for (EditorIcon icon : icons) {
            if (icon.getSlot() == slot) {
                return icon;
            }
        }
        return null;
    }

    @Override
    public void build() {
        this.update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        for (EditorIcon editorIcon : this.icons) {
            if (inventory.getSize() <= editorIcon.getSlot() || editorIcon.getSlot() < 0) {
                continue;
            }

            inventory.setItem(editorIcon.getSlot(), editorIcon.get());
        }

        this.updatePlayers();
    }

}
