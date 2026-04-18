package dev.nandi0813.practice.manager.gui.guis.customladder.premadecustom;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.fight.match.util.CustomKit;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.WeightClassType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CustomLadderEditorGui extends GUI {

    @Getter
    private static final ItemStack fillerItem = GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.FILLER-ITEM").get();

    @Getter
    private final Profile profile;
    @Getter
    private final NormalLadder ladder;
    @Getter
    private final int kit;
    @Getter
    private final boolean ranked;
    @Getter
    private final GUI backTo;

    private final CustomKit customKit;

    private static final List<Integer> NULL_SLOTS = Arrays.asList(14, 18, 27, 36, 45);

    public CustomLadderEditorGui(Profile profile, NormalLadder ladder, int kit, boolean ranked, GUI backTo) {
        super(GUIType.CustomLadder_Editor);
        this.profile = profile;
        this.ladder = ladder;
        this.kit = kit;
        this.ranked = ranked;
        this.backTo = backTo;

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.KIT-EDITOR.KIT-EDITOR.TITLE")
                        .replace("%kit%", String.valueOf(kit))
                        .replace("%weightClass%", (ranked ? WeightClass.RANKED.getName() : WeightClass.UNRANKED.getName()))
                        .replace("%ladder%", ladder.getDisplayName())
                        .replace("%ladderOriginal%", ladder.getName())
                , 6));

        if (ranked) {
            customKit = profile.getRankedCustomKits().get(ladder).computeIfAbsent(kit, k -> new CustomKit(
                    null,
                    ladder.getKitData().getStorage(),
                    ladder.getKitData().getArmor(),
                    ladder.getKitData().getExtra()));
        } else {
            customKit = profile.getUnrankedCustomKits().get(ladder).computeIfAbsent(kit, k -> new CustomKit(
                    null,
                    ladder.getKitData().getStorage(),
                    ladder.getKitData().getArmor(),
                    ladder.getKitData().getExtra()));
        }

        this.build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);
        inventory.clear();

        ItemStack infoItem = GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.INFO")
                .replace("%kit%", String.valueOf(this.kit))
                .replace("%weightClass%", (ranked ? WeightClass.RANKED.getName() : WeightClass.UNRANKED.getName()))
                .replace("%ladder%", ladder.getDisplayName())
                .replace("%ladderOriginal%", ladder.getName())
                .get();
        inventory.setItem(0, infoItem);

        inventory.setItem(6, GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.SAVE").get());
        inventory.setItem(7, GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.LOAD-DEFAULT").get());
        inventory.setItem(8, GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.CANCEL").get());

        // Frame
        for (int i : new int[]{1, 3, 5, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 28, 37, 46}) {
            inventory.setItem(i, GUIManager.getFILLER_ITEM());
        }

        inventory.setItem(2, getRankedItem());
        inventory.setItem(4, getEffectItem());

        // Fill armor slots with dummy items first to prevent extra items from going there
        int[] armorSlots = {18, 27, 36, 45};
        for (int slot : armorSlots) {
            inventory.setItem(slot, GUIManager.getDUMMY_ITEM());
        }

        if (ladder.getCustomKitExtraItems().get(ranked) != null) {
            for (ItemStack item : ladder.getCustomKitExtraItems().get(ranked)) {
                inventory.setItem(inventory.firstEmpty(), Objects.requireNonNullElseGet(item, GUIManager::getDUMMY_ITEM));
            }
        }

        inventory.remove(GUIManager.getDUMMY_ITEM());

        // Load armor BEFORE filler logic to prevent filler items from overwriting armor slots
        ItemStack[] customKitArmor = customKit.getArmor();
        if (customKitArmor == null) {
            // Legacy fallback for old kits where armor was appended to inventory[36..39]
            ItemStack[] customKitInventory = customKit.getInventory();
            if (customKitInventory != null && customKitInventory.length > 39) {
                customKitArmor = new ItemStack[]{
                        customKitInventory[36],
                        customKitInventory[37],
                        customKitInventory[38],
                        customKitInventory[39]
                };
            }
        }

        if (customKitArmor == null) {
            customKitArmor = ladder.getKitData().getArmor();
        }

        if (customKitArmor != null) {
            if (customKitArmor.length > 0 && customKitArmor[0] != null && !customKitArmor[0].getType().equals(Material.AIR)) inventory.setItem(45, customKitArmor[0]);
            else inventory.setItem(45, null);

            if (customKitArmor.length > 1 && customKitArmor[1] != null && !customKitArmor[1].getType().equals(Material.AIR)) inventory.setItem(36, customKitArmor[1]);
            else inventory.setItem(36, null);

            if (customKitArmor.length > 2 && customKitArmor[2] != null && !customKitArmor[2].getType().equals(Material.AIR)) inventory.setItem(27, customKitArmor[2]);
            else inventory.setItem(27, null);

            if (customKitArmor.length > 3 && customKitArmor[3] != null && !customKitArmor[3].getType().equals(Material.AIR)) inventory.setItem(18, customKitArmor[3]);
            else inventory.setItem(18, null);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null && !NULL_SLOTS.contains(i)) {
                inventory.setItem(i, fillerItem);
            }
        }

        if (customKit.getExtra() != null && customKit.getExtra().length > 0) {
            inventory.setItem(14, customKit.getExtra()[0]);
        } else {
            inventory.setItem(14, null);
        }

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        ItemStack item = e.getCurrentItem();
        Inventory inventory = e.getView().getTopInventory();
        InventoryAction action = e.getAction();

        if (!ladder.isEnabled() || !ladder.isEditable() || ladder.isFrozen()) {
            e.setCancelled(true);
            Common.sendMMMessage(player, LanguageManager.getString("LADDER.KIT-EDITOR.KIT-EDITOR.NOT-AVAILABLE"));
            GUIManager.getInstance().searchGUI(GUIType.CustomLadder_Selector).open(player);
            return;
        }

        if (inventory.getSize() > slot && !action.equals(InventoryAction.DROP_ONE_CURSOR) && !action.equals(InventoryAction.DROP_ALL_CURSOR)) {
            e.setCancelled(true);
        }

        if (slot == 14) {
            e.setCancelled(false);
            // Second hand item
        } else if (slot == 18 || slot == 27 || slot == 36 || slot == 45) {
            // Armor slots - allow editing based on armor type
            ItemStack cursorItem = e.getCursor();

            // If cursor is empty, allow picking up the armor
            if (cursorItem == null || cursorItem.getType().equals(Material.AIR)) {
                e.setCancelled(false);
                return;
            }

            // If cursor has an item, check if it's valid for this slot
            boolean isValidArmor = false;
            String materialName = cursorItem.getType().name();
            if (slot == 45 && materialName.endsWith("_BOOTS")) isValidArmor = true;
            else if (slot == 36 && materialName.endsWith("_LEGGINGS")) isValidArmor = true;
            else if (slot == 27 && materialName.endsWith("_CHESTPLATE")) isValidArmor = true;
            else if (slot == 18 && materialName.endsWith("_HELMET")) isValidArmor = true;

            e.setCancelled(!isValidArmor);
        } else if (slot == 6 || slot == 8) {
            player.setItemOnCursor(null);

            backTo.update();
            backTo.open(player);
        } else if (slot == 7) {
            player.setItemOnCursor(null);
            player.getInventory().setContents(ladder.getKitData().getStorage());

            ItemStack[] defaultArmor = ladder.getKitData().getArmor();
            inventory.setItem(45, getDefaultArmorItem(defaultArmor, 0));
            inventory.setItem(36, getDefaultArmorItem(defaultArmor, 1));
            inventory.setItem(27, getDefaultArmorItem(defaultArmor, 2));
            inventory.setItem(18, getDefaultArmorItem(defaultArmor, 3));

            ItemStack[] defaultExtra = ladder.getKitData().getExtra();
            inventory.setItem(14, (defaultExtra != null && defaultExtra.length > 0 && defaultExtra[0] != null) ? defaultExtra[0].clone() : null);
            player.updateInventory();
        } else if (slot == 2) {
            if (!ladder.getWeightClass().equals(WeightClassType.UNRANKED_AND_RANKED)) return;

            if (PlayerCooldown.isActive(player, CooldownObject.CUSTOM_KIT_WEIGHTCLASS_CHANGE)) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.KIT-EDITOR.KIT-EDITOR.WEIGHT-CLASS-CHANGE-COOLDOWN")
                        .replace("%timeLeft%", String.valueOf(PlayerCooldown.getLeftInDouble(player, CooldownObject.CUSTOM_KIT_WEIGHTCLASS_CHANGE))));
                return;
            }

            new CustomLadderEditorGui(profile, ladder, kit, !ranked, backTo).open(player);
            PlayerCooldown.addCooldown(player, CooldownObject.CUSTOM_KIT_WEIGHTCLASS_CHANGE, 5);
        } else if ((20 <= slot && slot <= 26) || (29 <= slot && slot <= 35) || (38 <= slot && slot <= 44) || (47 <= slot && slot <= 53)) {
            if (item != null && !item.getType().equals(Material.AIR) && !item.equals(fillerItem))
                player.setItemOnCursor(item);
        }
    }

    @Override
    public void handleCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        CustomLadderEditorGui customLadderEditorGui = (CustomLadderEditorGui) GUIManager.getInstance().getOpenGUI().get(player);
        Profile targetProfile = customLadderEditorGui.getProfile();
        NormalLadder ladder = customLadderEditorGui.getLadder();

        if (ladder.isEnabled() && ladder.isEditable() && !ladder.isFrozen()) {
            Inventory guiInventory = this.gui.get(1);

            // Save storage directly from player's inventory (must stay <= 36 for setStorageContents)
            customKit.setInventory(player.getInventory().getStorageContents().clone());

            // Save armor from GUI armor slots
            ItemStack boots = guiInventory.getItem(45);
            ItemStack leggings = guiInventory.getItem(36);
            ItemStack chestplate = guiInventory.getItem(27);
            ItemStack helmet = guiInventory.getItem(18);

            if (boots != null && (boots.getType().equals(Material.AIR) || boots.equals(GUIManager.getDUMMY_ITEM()) || boots.equals(fillerItem))) boots = null;
            if (leggings != null && (leggings.getType().equals(Material.AIR) || leggings.equals(GUIManager.getDUMMY_ITEM()) || leggings.equals(fillerItem))) leggings = null;
            if (chestplate != null && (chestplate.getType().equals(Material.AIR) || chestplate.equals(GUIManager.getDUMMY_ITEM()) || chestplate.equals(fillerItem))) chestplate = null;
            if (helmet != null && (helmet.getType().equals(Material.AIR) || helmet.equals(GUIManager.getDUMMY_ITEM()) || helmet.equals(fillerItem))) helmet = null;

            customKit.setArmor(new ItemStack[]{boots, leggings, chestplate, helmet});

            // Save offhand/extra item from GUI slot 14
            ItemStack extraItem = guiInventory.getItem(14);
            if (extraItem != null && (extraItem.getType().equals(Material.AIR) || extraItem.equals(GUIManager.getDUMMY_ITEM()) || extraItem.equals(fillerItem))) {
                extraItem = null;
            }
            customKit.setExtra(new ItemStack[]{extraItem});
        }

        PlayerUtil.clearInventory(player);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
        {
            if (targetProfile.getStatus().equals(ProfileStatus.EDITOR)) {
                if (GUIManager.getInstance().getOpenGUI().containsKey(player) && ((GUIManager.getInstance().getOpenGUI().get(player) instanceof CustomLadderEditorGui) || (GUIManager.getInstance().getOpenGUI().get(player) instanceof CustomLadderSumGui)))
                    return;

                InventoryManager.getInstance().setLobbyInventory(player, false);
            }
        }, 3L);
    }

    @Override
    public void open(Player player) {
        open(player, 1);

        Profile playerProfile = ProfileManager.getInstance().getProfile(player);
        playerProfile.setStatus(ProfileStatus.EDITOR);

        PlayerUtil.clearInventory(player);

        Map<Integer, CustomKit> kits;
        if (ranked) kits = profile.getRankedCustomKits().get(ladder);
        else kits = profile.getUnrankedCustomKits().get(ladder);

        if (kits.containsKey(kit))
            player.getInventory().setContents(kits.get(kit).getInventory());
        else
            player.getInventory().setContents(ladder.getKitData().getStorage());
    }

    private @Nullable ItemStack getRankedItem() {
        switch (ladder.getWeightClass()) {
            case UNRANKED, RANKED:
                return GUIManager.getFILLER_ITEM();
            case UNRANKED_AND_RANKED:
                if (this.ranked)
                    return GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.SWITCH-TO-UNRANKED").replace("%weightClass%", WeightClass.UNRANKED.getName()).get();
                else
                    return GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.SWITCH-TO-RANKED").replace("%weightClass%", WeightClass.RANKED.getName()).get();
        }
        return GUIManager.getFILLER_ITEM();
    }

    private ItemStack getEffectItem() {
        if (!ladder.getKitData().getEffects().isEmpty()) {
            List<String> effects = new ArrayList<>();
            for (PotionEffect potionEffect : ladder.getKitData().getEffects()) {
                effects.add(GUIFile.getString("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.HAS-EFFECT.FORMAT")
                        .replace("%name%", StringUtils.capitalize(potionEffect.getType().getKey().getKey().replace("_", " ").toLowerCase()))
                        .replace("%amplifier%", String.valueOf(potionEffect.getAmplifier() + 1))
                        .replace("%time%", StringUtil.formatMillisecondsToMinutes((potionEffect.getDuration() / 20) * 1000L))
                );
            }

            ItemStack effectItem = GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.HAS-EFFECT.ICON").get();
            ItemMeta effectItemMeta = effectItem.getItemMeta();
            ItemCreateUtil.hideItemFlags(effectItemMeta);

            List<String> lore = new ArrayList<>();
            for (Component lineComponent : Objects.requireNonNull(effectItem.getItemMeta().lore())) {
                String line = Common.serializeComponentToLegacyString(lineComponent);
                if (line.contains("%effects%")) lore.addAll(effects);
                else lore.add(line);
            }
            effectItemMeta.lore(lore.stream().map(Common::legacyToComponent).toList());
            effectItem.setItemMeta(effectItemMeta);
            return effectItem;
        } else {
            return GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.NO-EFFECT").get();
        }
    }

    private @Nullable ItemStack getDefaultArmorItem(ItemStack[] defaultArmor, int index) {
        if (defaultArmor == null || defaultArmor.length <= index) {
            return null;
        }

        ItemStack item = defaultArmor[index];
        if (item == null || item.getType().equals(Material.AIR)) {
            return null;
        }

        return item.clone();
    }

}
