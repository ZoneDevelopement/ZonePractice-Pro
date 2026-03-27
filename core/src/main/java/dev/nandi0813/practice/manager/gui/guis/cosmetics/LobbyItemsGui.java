package dev.nandi0813.practice.manager.gui.guis.cosmetics;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsData;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.ItemCreateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LobbyItemsGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GENERAL-FILLER-ITEM").get();
    private static final ItemStack BACK_ITEM = GUIFile.getGuiItem("GUIS.COSMETICS.ICONS.BACK-TO").get();

    private static final int ROWS = 3;
    private static final int BACK_SLOT = 18;
    private static final int[] OPTION_SLOTS = {10, 12, 14, 16};

    private final Profile profile;
    private final GUI backToGui;

    public LobbyItemsGui(Profile profile, GUI backToGui) {
        super(GUIType.Cosmetics_LobbyItems);
        this.profile = profile;
        this.backToGui = backToGui;

        String title = GUIFile.getConfig().getString("GUIS.COSMETICS.LOBBY-ITEMS.TITLE", "&8✦ Lobby Items");
        this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inv = gui.get(1);
        inv.clear();

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, FILLER_ITEM);
        }

        CosmeticsData.LobbyItemType selected = profile.getCosmeticsData().getLobbyItemType();
        CosmeticsData.LobbyItemType[] values = CosmeticsData.LobbyItemType.values();
        Player online = profile.getPlayer().getPlayer();

        for (int i = 0; i < OPTION_SLOTS.length && i < values.length; i++) {
            CosmeticsData.LobbyItemType type = values[i];
            inv.setItem(OPTION_SLOTS[i], buildOptionItem(type, selected, online));
        }

        inv.setItem(BACK_SLOT, BACK_ITEM == null ? new ItemStack(Material.ARROW) : BACK_ITEM);
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

        CosmeticsData.LobbyItemType clicked = getTypeBySlot(slot);
        if (clicked == null) {
            return;
        }

        if (!CosmeticsPermissionManager.hasLobbyItemPermission(player, clicked)) {
            String noPerm = GUIFile.getConfig().getString(
                    "GUIS.COSMETICS.LOBBY-ITEMS.NO-PERMISSION-MESSAGE",
                    "<red>You don't have permission for this lobby cosmetic item!"
            );
            Common.sendMMMessage(player, noPerm);
            return;
        }

        CosmeticsData.LobbyItemType current = profile.getCosmeticsData().getLobbyItemType();
        profile.getCosmeticsData().setLobbyItemType(current == clicked ? CosmeticsData.LobbyItemType.NONE : clicked);
        profile.saveData();
        InventoryManager.getInstance().applyLobbyCosmetics(player);

        update(true);
    }

    private CosmeticsData.LobbyItemType getTypeBySlot(int slot) {
        CosmeticsData.LobbyItemType[] values = CosmeticsData.LobbyItemType.values();
        for (int i = 0; i < OPTION_SLOTS.length && i < values.length; i++) {
            if (OPTION_SLOTS[i] == slot) {
                return values[i];
            }
        }
        return null;
    }

    private ItemStack buildOptionItem(CosmeticsData.LobbyItemType type, CosmeticsData.LobbyItemType selected, Player player) {
        boolean unlocked = player != null && CosmeticsPermissionManager.hasLobbyItemPermission(player, type);
        boolean active = type == selected;

        String basePath = "GUIS.COSMETICS.LOBBY-ITEMS.ENTRIES." + type.name();
        String name = GUIFile.getConfig().getString(basePath + ".NAME", "&e" + formatName(type));
        Material material = safeMaterial(GUIFile.getConfig().getString(basePath + ".MATERIAL"), defaultMaterial(type));

        List<String> lore = GUIFile.getConfig().getStringList(basePath + ".LORE");
        if (lore.isEmpty()) {
            lore = new ArrayList<>(List.of(
                    "",
                    "&7Status: %status%",
                    "&7Required: &f%permission%",
                    ""
            ));
        } else {
            lore = new ArrayList<>(lore);
        }

        String status = active ? "&aSelected" : (unlocked ? "&eUnlocked" : "&cLocked");
        String permission = type == CosmeticsData.LobbyItemType.NONE ? "none" : type.getPermissionNode();
        lore.replaceAll(line -> line
                .replace("%status%", status)
                .replace("%permission%", permission)
                .replace("%type%", formatName(type)));

        if (active) {
            lore.add("&7Click to disable.");
        } else if (unlocked) {
            lore.add("&eClick to select.");
        } else {
            lore.add("&cYou do not have permission.");
        }

        GUIItem item = new GUIItem(name, material, lore);
        item.setGlowing(active);

        return ItemCreateUtil.hideItemFlags(item.get());
    }

    private Material defaultMaterial(CosmeticsData.LobbyItemType type) {
        return switch (type) {
            case NONE -> Material.BARRIER;
            case WIND_CHARGE -> safeMaterial("WIND_CHARGE", Material.FIRE_CHARGE);
            case TRIDENT -> safeMaterial("TRIDENT", Material.IRON_SWORD);
            case SPEAR -> safeMaterial("SPEAR", safeMaterial("TRIDENT", Material.IRON_SWORD));
        };
    }

    private Material safeMaterial(String raw, Material fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }

        Material material = Material.matchMaterial(raw.toUpperCase(Locale.ROOT));
        return material == null ? fallback : material;
    }

    private String formatName(CosmeticsData.LobbyItemType type) {
        String text = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}


