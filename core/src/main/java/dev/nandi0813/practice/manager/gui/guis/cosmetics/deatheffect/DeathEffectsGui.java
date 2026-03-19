package dev.nandi0813.practice.manager.gui.guis.cosmetics.deatheffect;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for selecting a kill effect cosmetic.
 * Opens via the cosmetics hub → Kill Effects section.
 */
public class DeathEffectsGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    private static final ItemStack BACK_ITEM   = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BACK-TO").get();

    /* layout: 5 rows, last row = back + filler */
    private static final int ROWS      = 4;
    private static final int BACK_SLOT = 27;

    /* effect slots — 3 rows of 7 centered (cols 1-7 of rows 1-3) */
    private static final int[] EFFECT_SLOTS = {10, 11, 12, 13, 14, 15, 16,
                                               19, 20, 21, 22, 23, 24, 25};

    /* border around effect area: top row, bottom row, and left/right sides (rows 1-4) */
    private static final int[] FRAME_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17,
            18, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    };

    private final Profile profile;
    private final GUI    backToGui;

    public DeathEffectsGui(Profile profile, GUI backToGui) {
        super(GUIType.Cosmetics_DeathEffects);
        this.profile  = profile;
        this.backToGui = backToGui;

        String title = GUIFile.getConfig().getString(
                "GUIS.COSMETICS.DEATH-EFFECTS.TITLE", "&8Death Effects");
        this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
        build();
    }

    @Override public void build()  { update(); }

    @Override
    public void update() {
        Inventory inv = gui.get(1);
        inv.clear();

        for (int frameSlot : FRAME_SLOTS) {
            inv.setItem(frameSlot, FILLER_ITEM);
        }

        DeathEffect active = profile.getCosmeticsData().getDeathEffect();
        DeathEffect[] effects = DeathEffect.values();
        Player profilePlayer = profile.getPlayer().getPlayer();

        for (int i = 0; i < EFFECT_SLOTS.length && i < effects.length; i++) {
            DeathEffect deathEffect = effects[i];
            inv.setItem(EFFECT_SLOTS[i], buildEffectItem(deathEffect, active, profilePlayer));
        }

        inv.setItem(BACK_SLOT, BACK_ITEM != null ? BACK_ITEM : new ItemStack(Material.ARROW));
        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        if (slot == BACK_SLOT) {
            backToGui.update(true);
            backToGui.open(player);
            return;
        }

        // Identify which effect was clicked
        for (int i = 0; i < EFFECT_SLOTS.length && i < DeathEffect.values().length; i++) {
            if (slot == EFFECT_SLOTS[i]) {
                DeathEffect deathEffect = DeathEffect.values()[i];
                handleEffectClick(player, deathEffect);
                return;
            }
        }
    }

    private void handleEffectClick(Player player, DeathEffect deathEffect) {
        // Permission check
        if (deathEffect != DeathEffect.NONE && !player.isOp()
                && !player.hasPermission("zpp.cosmetics.killeffect.*")
                && !CosmeticsPermissionManager.hasDeathEffectPermission(player, deathEffect)) {
            String msg = GUIFile.getConfig().getString(
                    "GUIS.COSMETICS.DEATH-EFFECTS.NO-PERMISSION-MESSAGE",
                    "<red>You don't have permission for this kill effect!");
            Common.sendMMMessage(player, msg);
            return;
        }

        // Toggle off if already selected
        if (profile.getCosmeticsData().getDeathEffect() == deathEffect) {
            profile.getCosmeticsData().setDeathEffect(DeathEffect.NONE);
        } else {
            profile.getCosmeticsData().setDeathEffect(deathEffect);

            try {
                deathEffect.play(player.getLocation(), List.of(player));
            } catch (Exception ignored) {}
        }

        profile.saveData();
        update(true);
    }

    private ItemStack buildEffectItem(DeathEffect deathEffect, DeathEffect active, Player player) {
        Material mat = deathEffect.getConfiguredIcon();
        GUIItem item = new GUIItem(mat);

        boolean isActive = (deathEffect == active);
        boolean hasPerms = player == null || player.isOp()
                || player.hasPermission("zpp.cosmetics.killeffect.*")
                || CosmeticsPermissionManager.hasDeathEffectPermission(player, deathEffect)
                || deathEffect == DeathEffect.NONE;

        String nameTemplate = GUIFile.getConfig().getString(
                "GUIS.COSMETICS.DEATH-EFFECTS.ENTRIES." + deathEffect.name() + ".DISPLAY-NAME",
                deathEffect.getDefaultDisplayName());

        String statusPrefix;
        if (isActive) {
            statusPrefix = "&a&lSelected &8| &f";
        } else if (hasPerms) {
            statusPrefix = "&e&lUnlocked &8| &f";
        } else {
            statusPrefix = "&c&lLocked &8| &f";
        }

        item.setName(statusPrefix + nameTemplate);

        List<String> lore = new ArrayList<>();
        List<String> loreTemplate = GUIFile.getConfig().getStringList(
                "GUIS.COSMETICS.DEATH-EFFECTS.ENTRIES." + deathEffect.name() + ".LORE");
        if (loreTemplate.isEmpty()) {
            loreTemplate = GUIFile.getConfig().getStringList(
                    "GUIS.COSMETICS.DEATH-EFFECTS.DEFAULT-LORE");
        }
        for (String line : loreTemplate) {
            lore.add(line.replace("%status%", isActive ? "&aSelected" : (hasPerms ? "&eUnlocked" : "&cLocked")));
        }

        lore.add(0, "&8Status: " + (isActive ? "&aSelected" : (hasPerms ? "&eUnlocked" : "&cLocked")));
        lore.add(1, "");

        if (isActive) {
            lore.add("");
            lore.add(GUIFile.getConfig().getString(
                    "GUIS.COSMETICS.DEATH-EFFECTS.CLICK-TO-DESELECT", "&7Click to deselect this effect."));
        } else if (hasPerms) {
            lore.add("");
            lore.add(GUIFile.getConfig().getString(
                    "GUIS.COSMETICS.DEATH-EFFECTS.CLICK-TO-SELECT", "&eClick to select this effect."));
            lore.add("&7A preview will play for you.");
        } else {
            lore.add("");
            lore.add("&cYou do not have permission for this effect.");
        }

        item.setLore(lore);
        item.setGlowing(isActive);

        ItemStack stack = item.get();

        if (isActive && stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
                              org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            stack.setItemMeta(meta);
        }

        return ItemCreateUtil.hideItemFlags(stack);
    }
}
