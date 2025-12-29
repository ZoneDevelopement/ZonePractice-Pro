package dev.nandi0813.practice.manager.playerkit;

import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.playerkit.items.EditorIcon;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Pair;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum StaticItems {
    ;

    // Main GUI
    public static final String MAIN_GUI_TITLE = PlayerKitManager.getInstance().getString("GUI.MAIN.TITLE");
    public static final ItemStack MAIN_GUI_BACK = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.BACK-TO").get();
    public static final ItemStack MAIN_GUI_SETTINGS_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.CUSTOM-SETTINGS").get();
    public static final ItemStack MAIN_GUI_GUIDE_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.GUIDE").get();
    public static final ItemStack MAIN_GUI_RESET_KIT_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.RESET-KIT").get();
    public static final ItemStack MAIN_GUI_CHANGE_NAME = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.CHANGE-NAME").get();
    public static final ItemStack MAIN_GUI_SHARE_KIT = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.SHARE-KIT").get();

    // Main GUI placeholders
    public static final ItemStack MAIN_GUI_HEAD_PLACEHOLDER_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.KIT-SLOT-PLACEHOLDERS.HEAD").get();
    public static final ItemStack MAIN_GUI_CHEST_PLACEHOLDER_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.KIT-SLOT-PLACEHOLDERS.CHEST").get();
    public static final ItemStack MAIN_GUI_LEGS_PLACEHOLDER_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.KIT-SLOT-PLACEHOLDERS.LEGS").get();
    public static final ItemStack MAIN_GUI_BOOTS_PLACEHOLDER_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.KIT-SLOT-PLACEHOLDERS.BOOTS").get();
    public static final ItemStack MAIN_GUI_OFF_HAND_PLACEHOLDER_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.KIT-SLOT-PLACEHOLDERS.OFFHAND").get();
    public static final ItemStack MAIN_GUI_HOTBAR_PLACEHOLDER_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.KIT-SLOT-PLACEHOLDERS.HOTBAR").get();
    public static final ItemStack MAIN_GUI_INVENTORY_PLACEHOLDER_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.MAIN.ICONS.KIT-SLOT-PLACEHOLDERS.OTHER-INVENTORY").get();

    // Category GUI
    public static final String CATEGORY_GUI_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.CATEGORY-GUI.TITLE");
    public static final int CATEGORY_GUI_SIZE = PlayerKitManager.getInstance().getInt("GUI.ITEMS.CATEGORY-GUI.SIZE");
    public static final EditorIcon CATEGORY_GUI_BACK_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.CATEGORY-GUI.ICONS.BACK-TO");
    public static final EditorIcon CATEGORY_GUI_NONE_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.CATEGORY-GUI.ICONS.NONE");
    public static final EditorIcon CATEGORY_GUI_ARMOR_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.CATEGORY-GUI.ICONS.ARMOR");
    public static final EditorIcon CATEGORY_GUI_WEAPON_TOOLS_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.CATEGORY-GUI.ICONS.WEAPONS-TOOLS");
    public static final EditorIcon CATEGORY_GUI_BOWS_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.CATEGORY-GUI.ICONS.BOWS-ARROWS");
    public static final EditorIcon CATEGORY_GUI_POTIONS_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.CATEGORY-GUI.ICONS.POTIONS");
    public static final EditorIcon CATEGORY_GUI_FOOD_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.CATEGORY-GUI.ICONS.FOOD");
    public static final EditorIcon CATEGORY_GUI_BLOCKS_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.CATEGORY-GUI.ICONS.BLOCKS");

    // Categories with page
    public static final EditorIcon CATEGORY_GUI_PAGE_BACK_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.ITEMS-GUI.OFFICIAL-ICONS.BACK-TO");
    public static final EditorIcon CATEGORY_GUI_PAGE_NEXT_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.ITEMS-GUI.OFFICIAL-ICONS.NEXT-PAGE");
    public static final EditorIcon CATEGORY_GUI_PAGE_PREVIOUS_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.ITEMS.ITEMS-GUI.OFFICIAL-ICONS.PREVIOUS-PAGE");
    public static final int CATEGORY_GUI_PAGE_SIZE = PlayerKitManager.getInstance().getInt("GUI.ITEMS.ITEMS-GUI.SIZE");

    public static final String CATEGORY_ARMOR_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.ITEMS-GUI.CATEGORIES.ARMOR.TITLE");
    public static final List<String> CATEGORY_ARMOR_ITEMS = PlayerKitManager.getInstance().getList("GUI.ITEMS.ITEMS-GUI.CATEGORIES.ARMOR.ITEMS");
    public static final String CATEGORY_WEAPONS_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.ITEMS-GUI.CATEGORIES.WEAPONS-TOOLS.TITLE");
    public static final List<String> CATEGORY_WEAPONS_ITEMS = PlayerKitManager.getInstance().getList("GUI.ITEMS.ITEMS-GUI.CATEGORIES.WEAPONS-TOOLS.ITEMS");
    public static final String CATEGORY_BOWS_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.ITEMS-GUI.CATEGORIES.BOWS-ARROWS.TITLE");
    public static final List<String> CATEGORY_BOWS_ITEMS = PlayerKitManager.getInstance().getList("GUI.ITEMS.ITEMS-GUI.CATEGORIES.BOWS-ARROWS.ITEMS");
    public static final String CATEGORY_FOOD_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.ITEMS-GUI.CATEGORIES.FOOD.TITLE");
    public static final List<String> CATEGORY_FOOD_ITEMS = PlayerKitManager.getInstance().getList("GUI.ITEMS.ITEMS-GUI.CATEGORIES.FOOD.ITEMS");
    public static final String CATEGORY_BLOCKS_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.ITEMS-GUI.CATEGORIES.BLOCKS.TITLE");
    public static final List<String> CATEGORY_BLOCKS_ITEMS = PlayerKitManager.getInstance().getList("GUI.ITEMS.ITEMS-GUI.CATEGORIES.BLOCKS.ITEMS");

    // Potions GUI
    public static final String POTIONS_GUI_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.ITEMS-GUI.CATEGORIES.POTION.TITLE");
    public static final ItemStack POTIONS_GUI_BACK_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ITEMS-GUI.CATEGORIES.POTION.OFFICIAL-ICONS.BACK-TO").get();
    public static final ItemStack POTIONS_GUI_SWITCH_SPLASH_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ITEMS-GUI.CATEGORIES.POTION.OFFICIAL-ICONS.SWITCH-TO-SPLASH").get();
    public static final ItemStack POTIONS_GUI_SWITCH_DRINKABLE_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ITEMS-GUI.CATEGORIES.POTION.OFFICIAL-ICONS.SWITCH-TO-DRINKABLE").get();
    public static final List<ItemStack> POTIONS_GUI_SPLASH_POTIONS = new ArrayList<>();
    public static final List<ItemStack> POTIONS_GUI_DRINKABLE_POTIONS = new ArrayList<>();

    static {
        for (String string : PlayerKitManager.getInstance().getList("GUI.ITEMS.ITEMS-GUI.CATEGORIES.POTION.SPLASH-POTIONS.ITEMS")) {
            ItemStack itemStack = ClassImport.getClasses().getLadderUtil().getPotionItem(string);
            if (itemStack != null) {
                POTIONS_GUI_SPLASH_POTIONS.add(itemStack);
            }
        }

        for (String string : PlayerKitManager.getInstance().getList("GUI.ITEMS.ITEMS-GUI.CATEGORIES.POTION.DRINKABLE-POTIONS.ITEMS")) {
            ItemStack itemStack = ClassImport.getClasses().getLadderUtil().getPotionItem(string);
            if (itemStack != null) {
                POTIONS_GUI_DRINKABLE_POTIONS.add(itemStack);
            }
        }
    }

    // Main Armor GUI
    public static final String MAIN_ARMOR_GUI_TITLE = PlayerKitManager.getInstance().getString("GUI.MAIN-ARMOR.TITLE");
    public static final int MAIN_ARMOR_GUI_SIZE = PlayerKitManager.getInstance().getInt("GUI.MAIN-ARMOR.SIZE");
    public static final EditorIcon MAIN_ARMOR_BACK_TO_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.MAIN-ARMOR.ICONS.OFFICIAL.BACK-TO");
    public static final EditorIcon MAIN_ARMOR_NONE_ICON = PlayerKitManager.getInstance().getEditorItem("GUI.MAIN-ARMOR.ICONS.OFFICIAL.NONE");
    public static final List<EditorIcon> MAIN_ARMOR_HELMET_ICONS = new ArrayList<>();
    public static final List<EditorIcon> MAIN_ARMOR_CHEST_ICONS = new ArrayList<>();
    public static final List<EditorIcon> MAIN_ARMOR_LEG_ICONS = new ArrayList<>();
    public static final List<EditorIcon> MAIN_ARMOR_BOOT_ICONS = new ArrayList<>();

    static {
        for (String path : PlayerKitManager.getInstance().getConfigSectionKeys("GUI.MAIN-ARMOR.ICONS.HELMETS")) {
            MAIN_ARMOR_HELMET_ICONS.add(PlayerKitManager.getInstance().getEditorItem("GUI.MAIN-ARMOR.ICONS.HELMETS." + path));
        }
        for (String path : PlayerKitManager.getInstance().getConfigSectionKeys("GUI.MAIN-ARMOR.ICONS.CHESTPLATES")) {
            MAIN_ARMOR_CHEST_ICONS.add(PlayerKitManager.getInstance().getEditorItem("GUI.MAIN-ARMOR.ICONS.CHESTPLATES." + path));
        }
        for (String path : PlayerKitManager.getInstance().getConfigSectionKeys("GUI.MAIN-ARMOR.ICONS.LEGGINGS")) {
            MAIN_ARMOR_LEG_ICONS.add(PlayerKitManager.getInstance().getEditorItem("GUI.MAIN-ARMOR.ICONS.LEGGINGS." + path));
        }
        for (String path : PlayerKitManager.getInstance().getConfigSectionKeys("GUI.MAIN-ARMOR.ICONS.BOOTS")) {
            MAIN_ARMOR_BOOT_ICONS.add(PlayerKitManager.getInstance().getEditorItem("GUI.MAIN-ARMOR.ICONS.BOOTS." + path));
        }
    }

    // Change amount
    public static final String CHANGE_AMOUNT_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.CHANGE-COUNT-GUI.TITLE");
    public static final ItemStack CHANGE_AMOUNT_BACK_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.CHANGE-COUNT-GUI.ICONS.BACK-TO").get();
    public static final ItemStack CHANGE_CUSTOM_AMOUNT_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.CHANGE-COUNT-GUI.ICONS.CUSTOM-COUNT").get();
    public static final String CHANGE_CUSTOM_AMOUNT_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.CUSTOM-COUNT-GUI.TITLE");

    // Enchant GUI
    public static final String ENCHANT_GUI_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.ENCHANT-GUI.TITLE");
    public static final ItemStack ENCHANT_GUI_BACK_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ENCHANT-GUI.ICONS.BACK-TO").get();
    public static final ItemStack ENCHANT_GUI_UNBREAKABLE_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ENCHANT-GUI.ICONS.MAKE-UNBREAKABLE").get();
    public static final ItemStack ENCHANT_GUI_BREAKABLE_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ENCHANT-GUI.ICONS.MAKE-BREAKABLE").get();
    public static final ItemStack ENCHANT_GUI_CLEAR_ENCHANTS_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ENCHANT-GUI.ICONS.CLEAR-ENCHANTS").get();
    public static final ItemStack ENCHANT_GUI_CHANGE_DURABILITY_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ENCHANT-GUI.ICONS.CHANGE-DURABILITY").get();
    public static final GUIItem ENCHANT_GUI_ENCHANT_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ENCHANT-GUI.ICONS.ENCHANTMENT-ICON");
    public static final ItemStack ENCHANT_GUI_SET_LEVEL_PLACEHOLDER_ICON = PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ENCHANT-GUI.ICONS.SET-ENCHANTMENT-LEVEL-PLACEHOLDER").get();
    public static final Map<Integer, GUIItem> ENCHANTMENT_LEVEL_MAP = new HashMap<>();
    public static final Map<String, List<Enchantment>> DISABLED_ENCHANTMENTS = new HashMap<>();

    static {
        for (int i = 1; i <= 5; i++) {
            ENCHANTMENT_LEVEL_MAP.put(i, PlayerKitManager.getInstance().getGuiItem("GUI.ITEMS.ENCHANT-GUI.ICONS.SET-ENCHANTMENT-LEVEL")
                    .replaceAll("%level%", String.valueOf(i)));
        }

        for (String path : PlayerKitManager.getInstance().getConfigSectionKeys("DISABLED-ENCHANTS")) {
            List<Enchantment> enchantments = new ArrayList<>();
            for (String enchantment : PlayerKitManager.getInstance().getList("DISABLED-ENCHANTS." + path)) {
                try {
                    enchantments.add(Enchantment.getByName(enchantment));
                } catch (Exception e) {
                    Common.sendConsoleMMMessage("<red>Invalid enchantment name: " + enchantment);
                }
            }
            DISABLED_ENCHANTMENTS.put(path, enchantments);
        }
    }

    public static final String CUSTOM_DURABILITY_GUI_TITLE = PlayerKitManager.getInstance().getString("GUI.ITEMS.CUSTOM-DURABILITY-GUI.TITLE");

    // Custom settings GUI
    public static final ItemStack BACK_TO_ITEM = PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NAV.GO-BACK").get();
    public static final Pair<ItemStack, ItemStack> REGEN_ITEM = new Pair<>(PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.REGENERATION.ENABLED").get(), PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.REGENERATION.DISABLED").get());
    public static final Pair<ItemStack, ItemStack> HUNGER_ITEM = new Pair<>(PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.HUNGER.ENABLED").get(), PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.HUNGER.DISABLED").get());
    public static final Pair<ItemStack, ItemStack> BUILD_ITEM = new Pair<>(PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.BUILD.ENABLED").get(), PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.BUILD.DISABLED").get());
    public static final GUIItem ROUNDS_ITEM = PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.ROUNDS");
    public static final GUIItem KNOCKBACK_ITEM = PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.KNOCKBACK");
    public static final GUIItem HITDELAY_ITEM = PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.HITDELAY");
    public static final GUIItem ENDERPEARL_ITEM = PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.ENDERPEARL-COOLDOWN");
    public static final GUIItem GAPPLE_ITEM = PlayerKitManager.getInstance().getGuiItem("GUI.CUSTOM-SETTINGS.ICONS.NORMAL-SETTINGS.GOLDENAPPLE-COOLDOWN");

}
