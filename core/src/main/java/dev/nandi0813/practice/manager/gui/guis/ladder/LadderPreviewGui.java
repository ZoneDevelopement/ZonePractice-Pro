package dev.nandi0813.practice.manager.gui.guis.ladder;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.module.interfaces.KitData;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LadderPreviewGui extends GUI {

    private final NormalLadder ladder;
    private final KitData kitData;

    public LadderPreviewGui(NormalLadder ladder) {
        super(GUIType.Ladder_Preview);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.LADDER-PREVIEW.TITLE").replace("%ladder%", ladder.getDisplayName()), 6));

        this.ladder = ladder;
        this.kitData = ladder.getKitData();

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
            inventory.clear();

            // Set filler items
            for (int i : new int[]{36, 37, 38, 39, 40, 41, 42, 43, 44, 46, 47, 48, 49})
                gui.get(1).setItem(i, GUIManager.getFILLER_ITEM());

            gui.get(1).setItem(45, getEffectItem());

            // Inventory Content
            if (kitData.getStorage() != null) {
                List<ItemStack> inventoryContent = Arrays.asList(kitData.getStorage());
                List<ItemStack> firstLine = new ArrayList<>();
                for (int i = 0; i < 36; i++) {
                    if (i < 9)
                        firstLine.add(inventoryContent.get(i));

                    if (i < 27) inventory.setItem(i, inventoryContent.get(i + 9));
                    else inventory.setItem(i, firstLine.get(i - 27));
                }
            }

            if (kitData.getArmor() != null) {
                // Armor content
                List<ItemStack> armorContent = new ArrayList<>(Arrays.asList(kitData.getArmor()));
                for (int i = 50; i < 54; i++)
                    gui.get(1).setItem(i, armorContent.get(i - 50));
            }

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    private ItemStack getEffectItem() {
        if (!kitData.getEffects().isEmpty()) {
            List<String> effects = new ArrayList<>();
            for (PotionEffect potionEffect : kitData.getEffects()) {
                effects.add(GUIFile.getString("GUIS.LADDER-PREVIEW.ICONS.HAS-EFFECT.FORMAT")
                        .replace("%name%", StringUtils.capitalize(potionEffect.getType().getName().replace("_", " ").toLowerCase()))
                        .replace("%amplifier%", String.valueOf(potionEffect.getAmplifier() + 1))
                        .replace("%time%", StringUtil.formatMillisecondsToMinutes((potionEffect.getDuration() / 20) * 1000L))
                );
            }

            List<String> lore = new ArrayList<>();
            for (String line : GUIFile.getStringList("GUIS.LADDER-PREVIEW.ICONS.HAS-EFFECT.ICON.LORE")) {
                if (line.contains("%effects%")) lore.addAll(effects);
                else
                    lore.add(line.replace("%ladder%", ladder.getDisplayName()));
            }

            return GUIFile.getGuiItem("GUIS.LADDER-PREVIEW.ICONS.HAS-EFFECT.ICON")
                    .replace("%ladder%", ladder.getDisplayName())
                    .setLore(lore)
                    .get();
        } else
            return GUIFile.getGuiItem("GUIS.LADDER-PREVIEW.ICONS.NO-EFFECT").get();
    }

}
