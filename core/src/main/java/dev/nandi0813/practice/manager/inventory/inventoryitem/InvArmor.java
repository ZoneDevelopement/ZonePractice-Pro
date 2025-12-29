package dev.nandi0813.practice.manager.inventory.inventoryitem;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Setter
@Getter
public class InvArmor {

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public ItemStack[] getArmorContent() {
        return new ItemStack[]{this.boots, this.leggings, this.chestplate, this.helmet};
    }

    public void setArmorContent(ItemStack[] armorContent) {
        this.boots = armorContent[0];
        this.leggings = armorContent[1];
        this.chestplate = armorContent[2];
        this.helmet = armorContent[3];
    }

    public boolean isNull() {
        return this.helmet == null && this.chestplate == null && this.leggings == null && this.boots == null;
    }

}
