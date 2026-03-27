package dev.nandi0813.practice.manager.gui.guis.cosmetics;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.guis.cosmetics.armortrim.ArmorTrimMainGui;
import dev.nandi0813.practice.manager.gui.guis.cosmetics.deatheffect.DeathEffectsGui;
import dev.nandi0813.practice.manager.gui.guis.cosmetics.shield.ShieldLayoutListGui;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsData;
import dev.nandi0813.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
import dev.nandi0813.practice.manager.profile.cosmetics.shield.ShieldLayout;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Main cosmetics hub. Opens from the lobby hotbar item or /cosmetics command.
 * Four navigation buttons lead to sub-GUIs:
 *  • Armor Trims  (existing CosmeticsGui)
 *  • Shield       (new ShieldCosmeticsGui)
 *  • Lobby Items  (new LobbyItemsGui)
 *  • Kill Effects (new KillEffectGui)
 */
public class CosmeticsHubGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();

    private static final int ROWS           = 3;
    private static final int TRIMS_SLOT       = 10;
    private static final int SHIELD_SLOT      = 12;
    private static final int LOBBY_ITEMS_SLOT = 14;
    private static final int KILL_EFF_SLOT    = 16;

    private final Profile profile;

    public CosmeticsHubGui(Profile profile) {
        super(GUIType.Cosmetics_Hub);
        this.profile = profile;

        String title = GUIFile.getConfig().getString(
                "GUIS.COSMETICS.HUB.TITLE", "&8✦ Cosmetics");
        this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
        build();
    }

    @Override public void build()  { update(); }

    @Override
    public void update() {
        Inventory inv = gui.get(1);
        inv.clear();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, FILLER_ITEM);

        inv.setItem(TRIMS_SLOT,    buildTrimsButton());
        inv.setItem(SHIELD_SLOT,   buildShieldButton());
        inv.setItem(LOBBY_ITEMS_SLOT, buildLobbyItemsButton());
        inv.setItem(KILL_EFF_SLOT, buildKillEffectButton());

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        switch (slot) {
            case TRIMS_SLOT -> new ArmorTrimMainGui(profile, this).open(player);
            case SHIELD_SLOT -> new ShieldLayoutListGui(profile, this).open(player);
            case LOBBY_ITEMS_SLOT -> new LobbyItemsGui(profile, this).open(player);
            case KILL_EFF_SLOT -> new DeathEffectsGui(profile, this).open(player);
        }
    }

    // ── Button builders ──────────────────────────────────────────────

    private ItemStack buildTrimsButton() {
        String name = GUIFile.getConfig().getString(
                "GUIS.COSMETICS.HUB.BUTTONS.ARMOR-TRIMS.NAME", "&6✦ Armor Trims");
        Material mat = safeMaterial(
                GUIFile.getConfig().getString("GUIS.COSMETICS.HUB.BUTTONS.ARMOR-TRIMS.MATERIAL"),
                Material.DIAMOND_CHESTPLATE);
        List<String> lore = getOrDefaultLore("GUIS.COSMETICS.HUB.BUTTONS.ARMOR-TRIMS.LORE",
                List.of("&7Customize your armor tier,", "&7trim patterns and materials."));

        GUIItem item = new GUIItem(name, mat, lore);
        item.setGlowing(GUIFile.getConfig().getBoolean(
                "GUIS.COSMETICS.HUB.BUTTONS.ARMOR-TRIMS.GLOW", true));
        return ItemCreateUtil.hideItemFlags(item.get());
    }

    private ItemStack buildShieldButton() {
        String name = GUIFile.getConfig().getString(
                "GUIS.COSMETICS.HUB.BUTTONS.SHIELD.NAME", "&9✦ Shield");
        Material mat = safeMaterial(
                GUIFile.getConfig().getString("GUIS.COSMETICS.HUB.BUTTONS.SHIELD.MATERIAL"),
                Material.SHIELD);
        List<String> lore = getOrDefaultLore("GUIS.COSMETICS.HUB.BUTTONS.SHIELD.LORE",
                List.of("&7Design your shield with any", "&7color and pattern combination.", "&7Save multiple layouts."));

        ShieldLayout active = profile.getCosmeticsData().getActiveShieldLayout();
        List<String> finalLore = new ArrayList<>(lore);
        finalLore.add("");
        finalLore.add("&7Active layout: &e" + (active != null ? active.getName() : "&cNone"));
        finalLore.add("&7Saved layouts: &e"
                + profile.getCosmeticsData().getShieldLayouts().size());

        GUIItem item = new GUIItem(name, mat, finalLore);
        item.setGlowing(GUIFile.getConfig().getBoolean(
                "GUIS.COSMETICS.HUB.BUTTONS.SHIELD.GLOW", false));
        return item.get();
    }

    private ItemStack buildKillEffectButton() {
        String name = GUIFile.getConfig().getString(
                "GUIS.COSMETICS.HUB.BUTTONS.KILL-EFFECTS.NAME", "&c✦ Death Effects");
        Material mat = safeMaterial(
                GUIFile.getConfig().getString("GUIS.COSMETICS.HUB.BUTTONS.KILL-EFFECTS.MATERIAL"),
                Material.BLAZE_POWDER);
        List<String> lore = getOrDefaultLore("GUIS.COSMETICS.HUB.BUTTONS.KILL-EFFECTS.LORE",
                List.of("&7Choose a particle effect", "&7that plays when you kill someone."));

        DeathEffect active = profile.getCosmeticsData().getDeathEffect();
        List<String> finalLore = new ArrayList<>(lore);
        finalLore.add("");
        finalLore.add("&7Active effect: &e" + (active != null ? active.getDisplayName() : "&cNone"));

        GUIItem item = new GUIItem(name, mat, finalLore);
        item.setGlowing(GUIFile.getConfig().getBoolean(
                "GUIS.COSMETICS.HUB.BUTTONS.KILL-EFFECTS.GLOW", false));
        return item.get();
    }

    private ItemStack buildLobbyItemsButton() {
        String name = GUIFile.getConfig().getString(
                "GUIS.COSMETICS.HUB.BUTTONS.LOBBY-ITEMS.NAME", "&b✦ Lobby Items");
        Material mat = safeMaterial(
                GUIFile.getConfig().getString("GUIS.COSMETICS.HUB.BUTTONS.LOBBY-ITEMS.MATERIAL"),
                Material.ELYTRA);
        List<String> lore = getOrDefaultLore("GUIS.COSMETICS.HUB.BUTTONS.LOBBY-ITEMS.LORE",
                List.of("&7Select movement cosmetics", "&7for your lobby loadout."));

        CosmeticsData.LobbyItemType active = profile.getCosmeticsData().getLobbyItemType();
        List<String> finalLore = new ArrayList<>(lore);
        finalLore.add("");
        finalLore.add("&7Active item: &e" + formatName(active == null ? "NONE" : active.name()));

        GUIItem item = new GUIItem(name, mat, finalLore);
        item.setGlowing(GUIFile.getConfig().getBoolean(
                "GUIS.COSMETICS.HUB.BUTTONS.LOBBY-ITEMS.GLOW", false));
        return item.get();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private List<String> getOrDefaultLore(String key, List<String> defaults) {
        List<String> lore = GUIFile.getConfig().getStringList(key);
        return lore.isEmpty() ? defaults : lore;
    }

    private static Material safeMaterial(String name, Material fallback) {
        if (name == null || name.isBlank()) return fallback;
        try { return Material.valueOf(name.toUpperCase()); } catch (Exception ignored) { return fallback; }
    }

    private static String formatName(String raw) {
        if (raw == null) return "None";
        String lower = raw.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}