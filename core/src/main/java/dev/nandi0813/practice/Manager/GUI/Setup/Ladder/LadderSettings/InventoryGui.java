package dev.nandi0813.practice.Manager.GUI.Setup.Ladder.LadderSettings;

import dev.nandi0813.practice.Manager.Backend.GUIFile;
import dev.nandi0813.practice.Manager.Backend.LanguageManager;
import dev.nandi0813.practice.Manager.GUI.GUI;
import dev.nandi0813.practice.Manager.GUI.GUIItem;
import dev.nandi0813.practice.Manager.GUI.GUIManager;
import dev.nandi0813.practice.Manager.GUI.GUIType;
import dev.nandi0813.practice.Manager.GUI.Setup.Ladder.LadderSetupManager;
import dev.nandi0813.practice.Manager.Ladder.Abstraction.Normal.NormalLadder;
import dev.nandi0813.practice.Manager.Ladder.LadderManager;
import dev.nandi0813.practice.Module.Interfaces.KitData;
import dev.nandi0813.practice.Module.Util.VersionChecker;
import dev.nandi0813.practice.Util.ArmorUtil;
import dev.nandi0813.practice.Util.Common;
import dev.nandi0813.practice.Util.InventoryUtil;
import dev.nandi0813.practice.Util.StringUtil;
import dev.nandi0813.practice.ZonePractice;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class InventoryGui extends GUI {

    private static final boolean secondHand = Optional.ofNullable(VersionChecker.getBukkitVersion())
            .map(VersionChecker.BukkitVersion::isSecondHand)
            .orElse(false);

    @Getter
    private final NormalLadder ladder;
    private final KitData kitData;

    public InventoryGui(NormalLadder ladder) {
        super(GUIType.Ladder_Inventory);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.LADDER.INVENTORY.TITLE").replace("%ladder%", ladder.getName()), 6));

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

            for (int i : new int[]{36, 37, 38, 39, 40, 41, 42, 43, 44, 46})
                inventory.setItem(i, GUIManager.getFILLER_ITEM());

            inventory.setItem(45, GUIFile.getGuiItem("GUIS.SETUP.LADDER.INVENTORY.ICONS.BACK-TO").get());
            inventory.setItem(47, getCustomKitEditorItem(ladder));
            inventory.setItem(48, getEffectItem(kitData.getEffects()));

            if (kitData.isSet()) {
                // Inventory content
                List<ItemStack> inventoryContent = Arrays.asList(kitData.getStorage());
                List<ItemStack> firstLine = new ArrayList<>();
                for (int i = 0; i < 36; i++) {
                    if (i < 9)
                        firstLine.add(inventoryContent.get(i));

                    if (i < 27)
                        inventory.setItem(i, inventoryContent.get(i + 9));
                    else
                        inventory.setItem(i, firstLine.get(i - 27));
                }

                // Armor content
                List<ItemStack> armorContent = new ArrayList<>(Arrays.asList(kitData.getArmor()));
                for (int i = 50; i < 54; i++) {
                    inventory.setItem(i, armorContent.get(i - 50));
                }

                if (secondHand) {
                    if (kitData.getExtra() != null)
                        inventory.setItem(49, kitData.getExtra()[0]);
                } else
                    inventory.setItem(49, GUIManager.getFILLER_ITEM());
            }

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();
        ClickType click = e.getClick();
        int slot = e.getRawSlot();

        if (inventory.getSize() > slot) {
            if (slot == 45 || slot == 46 || slot == 47 || slot == 48 || Objects.equals(e.getCurrentItem(), GUIManager.getFILLER_ITEM())) {
                e.setCancelled(true);

                if (slot == 45)
                    LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).open(player);
                if (slot == 47 && ladder.isEditable())
                    LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_CustomKitExtra_unRanked).open(player);
            } else {
                if (!ladder.isEnabled()) {
                    if (click.equals(ClickType.LEFT) || click.equals(ClickType.RIGHT)) {
                        if (!player.getItemOnCursor().getType().equals(Material.AIR)) {
                            if (slot == 50 && !ArmorUtil.isBoots(player.getItemOnCursor())) e.setCancelled(true);
                            if (slot == 51 && !ArmorUtil.isLeggings(player.getItemOnCursor())) e.setCancelled(true);
                            if (slot == 52 && !ArmorUtil.isChestplate(player.getItemOnCursor())) e.setCancelled(true);
                            if (slot == 53 && !ArmorUtil.isHelmet(player.getItemOnCursor())) e.setCancelled(true);
                        }
                    } else e.setCancelled(true);
                } else {
                    e.setCancelled(true);
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.LADDER.CANT-EDIT-ENABLED"));
                }
            }
        }
    }

    public void handleCloseEvent(InventoryCloseEvent e) {
        if (LadderManager.getInstance().getLadders().contains(ladder) && !ladder.isEnabled())
            this.save();
    }

    @Override
    public void handleDragEvent(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    public void save() {
        List<ItemStack> inventoryContent = new ArrayList<>();
        List<ItemStack> armorContent = new ArrayList<>();

        Inventory inventory = gui.get(1);
        for (int i = 27; i < 36; i++) inventoryContent.add(inventory.getItem(i));
        for (int i = 0; i < 27; i++) inventoryContent.add(inventory.getItem(i));
        for (int i = 50; i < 54; i++) armorContent.add(inventory.getItem(i));

        if (secondHand) {
            List<ItemStack> extraContent = new ArrayList<>();
            extraContent.add(inventory.getItem(49));
            kitData.setExtra(extraContent.toArray(new ItemStack[0]));
        }

        kitData.setStorage(inventoryContent.toArray(new ItemStack[0]));
        kitData.setArmor(armorContent.toArray(new ItemStack[0]));
    }


    private static ItemStack getCustomKitEditorItem(NormalLadder ladder) {
        if (ladder.isEditable())
            return GUIFile.getGuiItem("GUIS.SETUP.LADDER.INVENTORY.ICONS.CUSTOM-KIT-EDITOR.EDITABLE").get();
        else
            return GUIFile.getGuiItem("GUIS.SETUP.LADDER.INVENTORY.ICONS.CUSTOM-KIT-EDITOR.NOT-EDITABLE").get();
    }

    private ItemStack getEffectItem(List<PotionEffect> effects) {
        if (!effects.isEmpty()) {
            GUIItem guiItem = GUIFile.getGuiItem("GUIS.SETUP.LADDER.INVENTORY.ICONS.EFFECTS.HAS-EFFECT").replaceAll("%ladder%", ladder.getName());

            List<String> effectStrings = new ArrayList<>();
            for (PotionEffect potionEffect : effects)
                effectStrings.add(GUIFile.getString("GUIS.SETUP.LADDER.INVENTORY.ICONS.EFFECTS.HAS-EFFECT.EFFECT-FORMAT")
                        .replace("%name%", StringUtils.capitalize(potionEffect.getType().getName().replace("_", " ").toLowerCase()))
                        .replace("%amplifier%", String.valueOf(potionEffect.getAmplifier() + 1))
                        .replace("%time%", StringUtil.formatMillisecondsToMinutes((potionEffect.getDuration() / 20) * 1000L))
                );

            List<String> lore = new ArrayList<>();
            for (String line : guiItem.getLore()) {
                if (line.contains("%effects%"))
                    lore.addAll(effectStrings);
                else
                    lore.add(line);
            }

            guiItem.setLore(lore);
            return guiItem.get();
        } else
            return GUIFile.getGuiItem("GUIS.SETUP.LADDER.INVENTORY.ICONS.EFFECTS.NO-EFFECT").get();
    }

}
