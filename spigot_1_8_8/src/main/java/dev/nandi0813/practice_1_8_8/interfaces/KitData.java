package dev.nandi0813.practice_1_8_8.interfaces;

import org.bukkit.entity.Player;

public class KitData extends dev.nandi0813.practice.module.interfaces.KitData {

    public KitData() {
        super();
    }

    public KitData(dev.nandi0813.practice.module.interfaces.KitData kitData) {
        super(kitData);
    }

    @Override
    public void setStorage(Player player) {
        this.storage = player.getInventory().getContents().clone();
    }

    @Override
    public void setArmor(Player player) {
        this.armor = player.getInventory().getArmorContents().clone();
    }

    @Override
    public void setExtra(Player player) {
        this.extra = null;
    }

    @Override
    public void loadStorage(Player player) {
        player.getInventory().setContents(this.storage.clone());
    }

    @Override
    public void loadArmor(Player player) {
        player.getInventory().setArmorContents(this.armor.clone());
    }

    @Override
    public void loadExtra(Player player) { /* Unsued */ }

}
