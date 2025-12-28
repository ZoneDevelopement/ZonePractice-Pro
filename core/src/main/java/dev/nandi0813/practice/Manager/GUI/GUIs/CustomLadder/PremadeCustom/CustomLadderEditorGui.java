package dev.nandi0813.practice.Manager.GUI.GUIs.CustomLadder.PremadeCustom;

import dev.nandi0813.practice.Manager.Backend.GUIFile;
import dev.nandi0813.practice.Manager.Backend.LanguageManager;
import dev.nandi0813.practice.Manager.Fight.Match.Enum.WeightClass;
import dev.nandi0813.practice.Manager.Fight.Match.Util.CustomKit;
import dev.nandi0813.practice.Manager.GUI.GUI;
import dev.nandi0813.practice.Manager.GUI.GUIManager;
import dev.nandi0813.practice.Manager.GUI.GUIType;
import dev.nandi0813.practice.Manager.Inventory.InventoryManager;
import dev.nandi0813.practice.Manager.Ladder.Abstraction.Normal.NormalLadder;
import dev.nandi0813.practice.Manager.Ladder.Enum.WeightClassType;
import dev.nandi0813.practice.Manager.Profile.Enum.ProfileStatus;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileManager;
import dev.nandi0813.practice.Module.Interfaces.ItemCreateUtil;
import dev.nandi0813.practice.Module.Util.ClassImport;
import dev.nandi0813.practice.Module.Util.VersionChecker;
import dev.nandi0813.practice.Util.Common;
import dev.nandi0813.practice.Util.Cooldown.CooldownObject;
import dev.nandi0813.practice.Util.Cooldown.PlayerCooldown;
import dev.nandi0813.practice.Util.InventoryUtil;
import dev.nandi0813.practice.Util.StringUtil;
import dev.nandi0813.practice.ZonePractice;
import lombok.Getter;
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
            customKit = profile.getRankedCustomKits().get(ladder).computeIfAbsent(kit, k -> new CustomKit(null, ladder.getKitData().getStorage(), ladder.getKitData().getExtra()));
        } else {
            customKit = profile.getUnrankedCustomKits().get(ladder).computeIfAbsent(kit, k -> new CustomKit(null, ladder.getKitData().getStorage(), ladder.getKitData().getExtra()));
        }

        this.build();
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

            List<ItemStack> armorContent = new ArrayList<>(Arrays.asList(ladder.getKitData().getArmor()));
            for (int i : new int[]{18, 27, 36, 45}) {
                if (armorContent.get(Math.abs(i / 9 - 5)) != null)
                    inventory.setItem(i, armorContent.get(Math.abs(i / 9 - 5)));
                else
                    inventory.setItem(i, GUIManager.getDUMMY_ITEM());
            }

            ItemStack infoItem = GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.INFO")
                    .replaceAll("%kit%", String.valueOf(this.kit))
                    .replaceAll("%weightClass%", (ranked ? WeightClass.RANKED.getName() : WeightClass.UNRANKED.getName()))
                    .replaceAll("%ladder%", ladder.getDisplayName())
                    .replaceAll("%ladderOriginal%", ladder.getName())
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

            if (ladder.getCustomKitExtraItems().get(ranked) != null) {
                for (ItemStack item : ladder.getCustomKitExtraItems().get(ranked)) {
                    if (item != null)
                        inventory.setItem(inventory.firstEmpty(), item);
                    else
                        inventory.setItem(inventory.firstEmpty(), GUIManager.getDUMMY_ITEM());
                }
            }

            inventory.remove(GUIManager.getDUMMY_ITEM());

            for (int i = 0; i < inventory.getSize(); i++)
                if (inventory.getItem(i) == null)
                    inventory.setItem(i, fillerItem);

            if (Objects.requireNonNull(VersionChecker.getBukkitVersion()).isSecondHand()) {
                if (customKit.getExtra() != null) {
                    if (customKit.getExtra().length > 0) {
                        inventory.setItem(14, customKit.getExtra()[0]);
                    } else {
                        inventory.setItem(14, null);
                    }
                } else {
                    inventory.setItem(14, GUIManager.getDUMMY_ITEM());
                }
            }

            updatePlayers();
        });
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
        } else if (slot == 6 || slot == 8) {
            player.setItemOnCursor(null);

            backTo.update();
            backTo.open(player);
        } else if (slot == 7) {
            player.setItemOnCursor(null);
            player.getInventory().setContents(ladder.getKitData().getStorage());
            player.updateInventory();
        } else if (slot == 2) {
            if (!ladder.getWeightClass().equals(WeightClassType.UNRANKED_AND_RANKED)) return;

            if (PlayerCooldown.isActive(player, CooldownObject.CUSTOM_KIT_WEIGHTCLASS_CHANGE)) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.KIT-EDITOR.KIT-EDITOR.WEIGHT-CLASS-CHANGE-COOLDOWN")
                        .replaceAll("%timeLeft%", String.valueOf(PlayerCooldown.getLeftInDouble(player, CooldownObject.CUSTOM_KIT_WEIGHTCLASS_CHANGE))));
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
            ItemStack[] inventoryStorageContent = ClassImport.getClasses().getPlayerUtil().getInventoryStorageContent(player);
            customKit.setInventory(inventoryStorageContent);
            if (Objects.requireNonNull(VersionChecker.getBukkitVersion()).isSecondHand()) {
                customKit.setExtra(new ItemStack[]{this.gui.get(1).getItem(14)});
            }
        }

        ClassImport.getClasses().getPlayerUtil().clearInventory(player);

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

        ClassImport.getClasses().getPlayerUtil().clearInventory(player);

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
            case UNRANKED:
                return GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.ONLY-UNRANKED").replaceAll("%weightClass%", WeightClass.UNRANKED.getName()).get();
            case RANKED:
                return GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.ONLY-RANKED").replaceAll("%weightClass%", WeightClass.RANKED.getName()).get();
            case UNRANKED_AND_RANKED:
                if (this.ranked)
                    return GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.SWITCH-TO-UNRANKED").replaceAll("%weightClass%", WeightClass.UNRANKED.getName()).get();
                else
                    return GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.SWITCH-TO-RANKED").replaceAll("%weightClass%", WeightClass.RANKED.getName()).get();
        }
        return GUIManager.getFILLER_ITEM();
    }

    private ItemStack getEffectItem() {
        if (!ladder.getKitData().getEffects().isEmpty()) {
            List<String> effects = new ArrayList<>();
            for (PotionEffect potionEffect : ladder.getKitData().getEffects()) {
                effects.add(GUIFile.getString("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.HAS-EFFECT.FORMAT")
                        .replace("%name%", StringUtils.capitalize(potionEffect.getType().getName().replace("_", " ").toLowerCase()))
                        .replace("%amplifier%", String.valueOf(potionEffect.getAmplifier() + 1))
                        .replace("%time%", StringUtil.formatMillisecondsToMinutes((potionEffect.getDuration() / 20) * 1000L))
                );
            }

            ItemStack effectItem = GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.HAS-EFFECT.ICON").get();
            ItemMeta effectItemMeta = effectItem.getItemMeta();
            ItemCreateUtil.hideItemFlags(effectItemMeta);

            List<String> lore = new ArrayList<>();
            for (String line : effectItem.getItemMeta().getLore()) {
                if (line.contains("%effects%")) lore.addAll(effects);
                else lore.add(line);
            }
            effectItemMeta.setLore(lore);
            effectItem.setItemMeta(effectItemMeta);
            return effectItem;
        } else {
            return GUIFile.getGuiItem("GUIS.KIT-EDITOR.KIT-EDITOR.ICONS.NO-EFFECT").get();
        }
    }

}
