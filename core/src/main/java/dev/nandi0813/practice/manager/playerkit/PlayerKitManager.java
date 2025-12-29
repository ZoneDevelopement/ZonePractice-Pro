package dev.nandi0813.practice.manager.playerkit;

import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.playerkit.guis.CategoryGUI;
import dev.nandi0813.practice.manager.playerkit.guis.PotionsGUI;
import dev.nandi0813.practice.manager.playerkit.guis.itemeditors.ArmorGUI;
import dev.nandi0813.practice.manager.playerkit.guis.itemeditors.ItemCategory;
import dev.nandi0813.practice.manager.playerkit.items.EditorIcon;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerKitManager extends ConfigFile implements Listener {

    private static PlayerKitManager instance;

    public static PlayerKitManager getInstance() {
        if (instance == null)
            instance = new PlayerKitManager();
        return instance;
    }

    private PlayerKitManager() {
        super("", "playerkit");
    }

    private final Map<Player, PlayerKitEditing> editing = new HashMap<>();

    private final Map<String, CustomLadder> copy = new HashMap<>();
    private final Map<Player, CustomLadder> copying = new HashMap<>();

    public void load() {
        GUIManager guiManager = GUIManager.getInstance();
        guiManager.addGUI(new ItemCategory());
        guiManager.addGUI(new ArmorGUI(GUIType.PlayerCustom_Helmet, StaticItems.MAIN_ARMOR_HELMET_ICONS));
        guiManager.addGUI(new ArmorGUI(GUIType.PlayerCustom_Chestplate, StaticItems.MAIN_ARMOR_CHEST_ICONS));
        guiManager.addGUI(new ArmorGUI(GUIType.PlayerCustom_Leggings, StaticItems.MAIN_ARMOR_LEG_ICONS));
        guiManager.addGUI(new ArmorGUI(GUIType.PlayerCustom_Boots, StaticItems.MAIN_ARMOR_BOOT_ICONS));
        guiManager.addGUI(new CategoryGUI(GUIType.PlayerCustom_Armor, StaticItems.CATEGORY_ARMOR_TITLE, StaticItems.CATEGORY_ARMOR_ITEMS));
        guiManager.addGUI(new CategoryGUI(GUIType.PlayerCustom_Weapons_Tools, StaticItems.CATEGORY_WEAPONS_TITLE, StaticItems.CATEGORY_WEAPONS_ITEMS));
        guiManager.addGUI(new CategoryGUI(GUIType.PlayerCustom_Bows, StaticItems.CATEGORY_BOWS_TITLE, StaticItems.CATEGORY_BOWS_ITEMS));
        guiManager.addGUI(new CategoryGUI(GUIType.PlayerCustom_Food, StaticItems.CATEGORY_FOOD_TITLE, StaticItems.CATEGORY_FOOD_ITEMS));
        guiManager.addGUI(new CategoryGUI(GUIType.PlayerCustom_Blocks, StaticItems.CATEGORY_BLOCKS_TITLE, StaticItems.CATEGORY_BLOCKS_ITEMS));
        guiManager.addGUI(new PotionsGUI());
    }

    @Override
    public void setData() {
    }

    @Override
    public void getData() {
    }

    public EditorIcon getEditorItem(String loc) {
        EditorIcon guiItem = new EditorIcon();

        if (config.isString(loc + ".NAME"))
            guiItem.setName(config.getString(loc + ".NAME"));

        if (config.isString(loc + ".MATERIAL"))
            guiItem.setMaterial(Material.valueOf(config.getString(loc + ".MATERIAL")));

        short damage = 0;
        if (config.isInt(loc + ".DAMAGE"))
            damage = Short.parseShort(String.valueOf(config.getInt(loc + ".DAMAGE")));
        if (damage != 0) guiItem.setDamage(damage);

        if (config.isList(loc + ".LORE"))
            guiItem.setLore(config.getStringList(loc + ".LORE"));

        if (config.isInt(loc + ".SLOT"))
            guiItem.setSlot(config.getInt(loc + ".SLOT"));

        return guiItem;
    }

    public String getCopyCode() {
        String uuid = UUID.randomUUID().toString();
        String code;
        do {
            code = uuid.replaceAll("[^a-zA-Z0-9]", "").substring(0, 7);
        } while (this.copy.containsKey(code));
        return code;
    }

}
