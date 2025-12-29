package dev.nandi0813.practice.manager.gui.guis;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerInvGui extends GUI {

    private final Player target;

    public PlayerInvGui(Player target) {
        super(GUIType.Player_Inventory);
        this.target = target;
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.PLAYER-INVENTORY.TITLE").replace("%player%", target.getName()), 5));

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);

            List<ItemStack> inventoryContent = Arrays.asList(target.getInventory().getContents());
            List<ItemStack> firstLine = new ArrayList<>();
            for (int i = 0; i < 36; i++) {
                if (i < 9)
                    firstLine.add(inventoryContent.get(i));

                if (i < 27) inventory.setItem(i, inventoryContent.get(i + 9));
                else inventory.setItem(i, firstLine.get(i - 27));
            }

            List<ItemStack> armorContent = Arrays.asList(target.getInventory().getArmorContents());
            for (int i = 36; i < 40; i++)
                inventory.setItem(i, armorContent.get(i - 36));

            if (!target.getActivePotionEffects().isEmpty())
                inventory.setItem(42, getEffectItem());
            inventory.setItem(43, getHealthItem());
            inventory.setItem(44, getHungerItem());

            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null)
                    inventory.setItem(i, GUIFile.getGuiItem("GUIS.PLAYER-INVENTORY.ICONS.FILLER-ITEM").get());
            }
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    private ItemStack getHealthItem() {
        return GUIFile.getGuiItem("GUIS.PLAYER-INVENTORY.ICONS.HEALTH")
                .replaceAll("%health%", String.valueOf(target.getHealth()))
                .get();
    }

    private ItemStack getHungerItem() {
        return GUIFile.getGuiItem("GUIS.PLAYER-INVENTORY.ICONS.HUNGER")
                .replaceAll("%hunger%", String.valueOf(target.getFoodLevel()))
                .get();
    }

    private ItemStack getEffectItem() {
        if (!target.getActivePotionEffects().isEmpty()) {
            List<String> effects = new ArrayList<>();
            for (PotionEffect potionEffect : target.getActivePotionEffects()) {
                effects.add(GUIFile.getString("GUIS.PLAYER-INVENTORY.ICONS.EFFECT.FORMAT")
                        .replaceAll("%name%", StringUtils.capitalize(potionEffect.getType().getName().replace("_", " ").toLowerCase()))
                        .replaceAll("%amplifier%", String.valueOf(potionEffect.getAmplifier() + 1))
                        .replaceAll("%time%", StringUtil.formatMillisecondsToMinutes((potionEffect.getDuration() / 20) * 1000L))
                );
            }

            List<String> lore = new ArrayList<>();
            for (String line : GUIFile.getStringList("GUIS.PLAYER-INVENTORY.ICONS.EFFECT.ICON.LORE")) {
                if (line.contains("%effects%")) lore.addAll(effects);
                else
                    lore.add(line);
            }

            GUIItem item = GUIFile.getGuiItem("GUIS.PLAYER-INVENTORY.ICONS.EFFECT.ICON");
            item.setLore(lore);

            return item.get();
        }

        return null;
    }

}
