package dev.nandi0813.practice.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class KitData {

    protected ItemStack[] storage;
    protected ItemStack[] armor;
    protected ItemStack[] extra;
    @Setter
    protected List<PotionEffect> effects = new ArrayList<>();

    public KitData() {
    }

    public KitData(KitData kitData) {
        if (kitData.getStorage() != null) {
            this.storage = kitData.getStorage().clone();
        }

        if (kitData.getArmor() != null) {
            this.armor = kitData.getArmor().clone();
        }

        if (kitData.getExtra() != null) {
            this.extra = kitData.getExtra().clone();
        }

        this.effects = new ArrayList<>(kitData.getEffects());
    }

    public boolean isSet() {
        return storage != null && armor != null;
    }

    public void saveData(final FileConfiguration config, final String map) {
        if (storage != null)
            config.set((map != null ? map + "." : "") + "inventory", ItemSerializationUtil.itemStackArrayToBase64(storage));
        if (armor != null)
            config.set((map != null ? map + "." : "") + "armor", ItemSerializationUtil.itemStackArrayToBase64(armor));
        if (extra != null)
            config.set((map != null ? map + "." : "") + "extra", ItemSerializationUtil.itemStackArrayToBase64(extra));
        if (!effects.isEmpty())
            config.set((map != null ? map + "." : "") + "effects", effects.toArray());
    }

    public void getData(final FileConfiguration config, final String map) {
        String storagePath = (map != null ? map + "." : "") + "inventory";
        String armorPath = (map != null ? map + "." : "") + "armor";
        String extraPath = (map != null ? map + "." : "") + "extra";
        String effectsPath = (map != null ? map + "." : "") + "effects";

        if (config.isString(storagePath))
            this.storage = ItemSerializationUtil.itemStackArrayFromBase64(config.getString(storagePath));
        if (config.isString(armorPath))
            this.armor = ItemSerializationUtil.itemStackArrayFromBase64(config.getString(armorPath));
        if (config.isString(extraPath))
            this.extra = ItemSerializationUtil.itemStackArrayFromBase64(config.getString(extraPath));
        if (config.isSet(effectsPath)) {
            this.effects = new ArrayList<>();
            Object rawEffects = config.get(effectsPath);
            if (rawEffects instanceof List<?> list) {
                for (Object entry : list) {
                    if (entry instanceof PotionEffect potionEffect) {
                        this.effects.add(potionEffect);
                    }
                }
            } else if (rawEffects instanceof PotionEffect[] array) {
                this.effects.addAll(Arrays.asList(array));
            }
        }
    }

    public void setKitData(final Player player, final boolean setEffect) {
        this.setStorage(player);
        this.setArmor(player);
        this.setExtra(player);

        if (setEffect && !player.getActivePotionEffects().isEmpty()) {
            this.effects = new ArrayList<>();

            for (PotionEffect effect : player.getActivePotionEffects()) {
                int durationInSeconds = effect.getDuration() / 20;
                int roundedDurationInSeconds = (int) Math.ceil(durationInSeconds / 10.0) * 10;
                int roundedDurationInMillis = roundedDurationInSeconds * 20;
                this.effects.add(new PotionEffect(effect.getType(), roundedDurationInMillis, effect.getAmplifier()));
            }

        }
    }

    public void loadKitData(final Player player, final boolean clearActiveEffects) {
        this.loadStorage(player);
        this.loadArmor(player);
        this.loadExtra(player);

        if (!effects.isEmpty()) {
            if (clearActiveEffects)
                player.getActivePotionEffects().clear();

            for (PotionEffect effect : effects)
                player.addPotionEffect(effect);
        }

        player.updateInventory();
    }

    public void setStorage(Player player) {
        this.storage = player.getInventory().getStorageContents().clone();
    }

    public void setArmor(Player player) {
        this.armor = player.getInventory().getArmorContents().clone();
    }

    public void setExtra(Player player) {
        this.extra = player.getInventory().getExtraContents().clone();
    }

    public void setStorage(ItemStack[] storage) {
        this.storage = storage.clone();
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor.clone();
    }

    public void setExtra(ItemStack[] extra) {
        this.extra = extra.clone();
    }

    public void loadStorage(Player player) {
        player.getInventory().setContents(this.storage.clone());
    }

    public void loadArmor(Player player) {
        player.getInventory().setArmorContents(this.armor.clone());
    }

    public void loadExtra(Player player) {
        player.getInventory().setExtraContents(this.extra.clone());
    }

}
