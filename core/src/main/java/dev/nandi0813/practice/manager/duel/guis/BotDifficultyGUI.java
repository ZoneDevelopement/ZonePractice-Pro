package dev.nandi0813.practice.manager.duel.guis;

import dev.nandi0813.practice.manager.duel.bot.PvPBotTrait;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI for selecting bot difficulty
 * Designed to be extended for future party games and other bot-related gameplay modes
 */
@Getter
public class BotDifficultyGUI extends GUI {

    private static final int GUI_SIZE = 3; // 3 rows x 9 columns = 27 slots
    private static final int NOOB_SLOT = 11;
    private static final int EASY_SLOT = 12;
    private static final int NORMAL_SLOT = 13;
    private static final int HARD_SLOT = 14;
    private static final int EXPERT_SLOT = 15;

    private final BotDifficultyCallback callback;

    /**
     * Callback interface for when a difficulty is selected
     * This allows for extensibility to other game modes (party games, etc.)
     */
    @FunctionalInterface
    public interface BotDifficultyCallback {
        void onDifficultySelected(Player player, PvPBotTrait.BotDifficulty difficulty);
    }

    public BotDifficultyGUI(BotDifficultyCallback callback) {
        super(GUIType.Bot_DifficultySelector);
        this.callback = callback;
        this.build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inventory = InventoryUtil.createInventory("Select Bot Difficulty", GUI_SIZE);

        // Create difficulty item stacks
        inventory.setItem(NOOB_SLOT, createDifficultyItem(
            Material.DIRT,
            "§cNOOB",
            "§7The Beginner",
            "§8▸ Spam clicks every 2-3 ticks",
            "§8▸ No strafing or jump crits",
            "§8▸ 2.0 block reach",
            "§8▸ Heals at 15% health"
        ));

        inventory.setItem(EASY_SLOT, createDifficultyItem(
            Material.STONE,
            "§aEASY",
            "§7The Casual",
            "§8▸ Sometimes respects cooldown",
            "§8▸ 10% strafe chance",
            "§8▸ 2.5 block reach",
            "§8▸ Heals at 30% health"
        ));

        inventory.setItem(NORMAL_SLOT, createDifficultyItem(
            Material.IRON_INGOT,
            "§eNORMAL",
            "§7The Average Player",
            "§8▸ Respects dynamic cooldown",
            "§8▸ 30% strafe chance, 10% jump crits",
            "§8▸ 2.8 block reach",
            "§8▸ 15% shield chance, heals at 40%"
        ));

        inventory.setItem(HARD_SLOT, createDifficultyItem(
            Material.DIAMOND,
            "§6HARD",
            "§7The Tryhard",
            "§8▸ Perfect 1.21.11 attack timing",
            "§8▸ 50% strafe chance, 25% jump crits",
            "§8▸ 3.0 block reach (maximum)",
            "§8▸ 40% shield chance, 80% axe breaks"
        ));

        inventory.setItem(EXPERT_SLOT, createDifficultyItem(
            Material.NETHERITE_INGOT,
            "§5EXPERT",
            "§7The Hacker/Pro",
            "§8▸ Perfect mechanics & timing",
            "§8▸ 70% strafe chance, always attempts crits",
            "§8▸ Flawless W-tapping on every hit",
            "§8▸ 85% shield chance, uses Totems instantly"
        ));

        this.gui.put(1, inventory);
        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        Inventory inventory = e.getView().getTopInventory();

        if (inventory.getSize() <= slot) {
            return;
        }

        PvPBotTrait.BotDifficulty selectedDifficulty = switch (slot) {
            case NOOB_SLOT -> PvPBotTrait.BotDifficulty.NOOB;
            case EASY_SLOT -> PvPBotTrait.BotDifficulty.EASY;
            case NORMAL_SLOT -> PvPBotTrait.BotDifficulty.NORMAL;
            case HARD_SLOT -> PvPBotTrait.BotDifficulty.HARD;
            case EXPERT_SLOT -> PvPBotTrait.BotDifficulty.EXPERT;
            default -> null;
        };

        if (selectedDifficulty != null) {
            // Invoke the callback with the selected difficulty
            callback.onDifficultySelected(player, selectedDifficulty);
            player.closeInventory();
        }
    }

    /**
     * Create a difficulty selection item with lore
     */
    @SuppressWarnings("deprecation")
    private ItemStack createDifficultyItem(Material material, String name, String subtitle, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);

            List<String> fullLore = new ArrayList<>();
            fullLore.add(subtitle);
            fullLore.add(""); // Empty line for spacing
            fullLore.addAll(Arrays.asList(lore));

            meta.setLore(fullLore);
            item.setItemMeta(meta);
        }

        return item;
    }
}
