package dev.nandi0813.practice.manager.playerkit;

import dev.nandi0813.practice.manager.playerkit.items.KitItem;
import dev.nandi0813.practice.util.KitData;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class KitItems {

    private final KitData kitData;

    private final KitItem helmet;
    private final KitItem chest;
    private final KitItem legs;
    private final KitItem boots;
    private final KitItem offhand;
    private final Map<Integer, KitItem> hotbar = new HashMap<>();
    private final Map<Integer, KitItem> inventory = new HashMap<>();

    private final Map<Integer, KitItem> slots = new HashMap<>();

    public KitItems(final KitData kitData) {
        this.kitData = kitData;

        if (kitData.getArmor() != null) {
            helmet = new KitItem(StaticItems.MAIN_GUI_HEAD_PLACEHOLDER_ICON, kitData.getArmor()[3]);
            chest = new KitItem(StaticItems.MAIN_GUI_CHEST_PLACEHOLDER_ICON, kitData.getArmor()[2]);
            legs = new KitItem(StaticItems.MAIN_GUI_LEGS_PLACEHOLDER_ICON, kitData.getArmor()[1]);
            boots = new KitItem(StaticItems.MAIN_GUI_BOOTS_PLACEHOLDER_ICON, kitData.getArmor()[0]);
        } else {
            helmet = new KitItem(StaticItems.MAIN_GUI_HEAD_PLACEHOLDER_ICON);
            chest = new KitItem(StaticItems.MAIN_GUI_CHEST_PLACEHOLDER_ICON);
            legs = new KitItem(StaticItems.MAIN_GUI_LEGS_PLACEHOLDER_ICON);
            boots = new KitItem(StaticItems.MAIN_GUI_BOOTS_PLACEHOLDER_ICON);
        }

        if (kitData.getExtra() != null) {
            offhand = new KitItem(StaticItems.MAIN_GUI_OFF_HAND_PLACEHOLDER_ICON, kitData.getExtra()[0]);
        } else {
            offhand = new KitItem(StaticItems.MAIN_GUI_OFF_HAND_PLACEHOLDER_ICON);
        }

        if (kitData.getStorage() != null) {
            for (int i = 0; i < 9; i++) {
                hotbar.put(i, new KitItem(StaticItems.MAIN_GUI_HOTBAR_PLACEHOLDER_ICON, kitData.getStorage()[i]));
            }
            for (int i = 9; i < 36; i++) {
                inventory.put(i - 9, new KitItem(StaticItems.MAIN_GUI_INVENTORY_PLACEHOLDER_ICON, kitData.getStorage()[i]));
            }
        } else {
            for (int i = 0; i < 9; i++) {
                hotbar.put(i, new KitItem(StaticItems.MAIN_GUI_HOTBAR_PLACEHOLDER_ICON));
            }
            for (int i = 9; i < 36; i++) {
                inventory.put(i - 9, new KitItem(StaticItems.MAIN_GUI_INVENTORY_PLACEHOLDER_ICON));
            }
        }

        this.assignSlots();
        this.resetNull();
    }

    private void assignSlots() {
        slots.put(10, helmet);
        slots.put(11, chest);
        slots.put(12, legs);
        slots.put(13, boots);
        slots.put(15, offhand);

        for (int i = 18; i <= 44; i++) {
            slots.put(i, inventory.get(i - 18));
        }

        for (int i = 45; i <= 53; i++) {
            slots.put(i, hotbar.get(i - 45));
        }
    }

    public List<ItemStack> getInventoryContent() {
        List<ItemStack> inventoryContent = new ArrayList<>();

        for (KitItem kitItem : hotbar.values())
            inventoryContent.add(kitItem.get());

        for (KitItem kitItem : inventory.values())
            inventoryContent.add(kitItem.get());

        return inventoryContent;
    }

    public List<ItemStack> getArmorContent() {
        List<ItemStack> armorContent = new ArrayList<>();

        armorContent.add(boots.get());
        armorContent.add(legs.get());
        armorContent.add(chest.get());
        armorContent.add(helmet.get());

        return armorContent;
    }

    public void reset() {
        for (KitItem kitItem : slots.values()) {
            kitItem.reset();
        }
    }

    public void resetNull() {
        for (KitItem kitItem : slots.values()) {
            if (kitItem.isNull())
                kitItem.reset();
        }
    }

    public void save() {
        List<ItemStack> extraContent = new ArrayList<>();
        extraContent.add(offhand.get());
        this.kitData.setExtra(extraContent.toArray(new ItemStack[0]));
        this.kitData.setStorage(this.getInventoryContent().toArray(new ItemStack[0]));
        this.kitData.setArmor(this.getArmorContent().toArray(new ItemStack[0]));
    }

}
