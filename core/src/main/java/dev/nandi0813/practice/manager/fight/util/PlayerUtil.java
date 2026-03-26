package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerUtil {

    private static boolean hasPersonalCraftingGridOpen(Player player) {
        InventoryType type = player.getOpenInventory().getType();
        return type == InventoryType.CRAFTING || type == InventoryType.CREATIVE;
    }

    public static ItemStack getPlayerMainHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    public static boolean isItemInUse(Player player, Material material) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        return itemInMainHand.getType().equals(material) || itemInOffHand.getType().equals(material);
    }

    public static ItemStack getItemInUse(Player player, Material material) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        if (itemInMainHand.getType().equals(material)) return itemInMainHand;
        if (itemInOffHand.getType().equals(material)) return itemInOffHand;
        return null;
    }

    public static void setItemInUseIf(Player player, Material material, ItemStack itemStack) {
        if (player.getInventory().getItemInMainHand().getType().equals(material)) {
            player.getInventory().setItemInMainHand(itemStack);
        }
        if (player.getInventory().getItemInOffHand().getType().equals(material)) {
            player.getInventory().setItemInOffHand(itemStack);
        }
    }

    public static List<Entity> dropPlayerInventory(Player player) {
        List<Entity> entities = new ArrayList<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType().equals(Material.AIR)) continue;

            entities.add(player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
        // Only player inventory views expose the personal 2x2 crafting grid at these slots.
        if (hasPersonalCraftingGridOpen(player)) {
            for (int i = 1; i <= 4; i++) {
                ItemStack item = player.getOpenInventory().getItem(i);
                if (item == null || item.getType().equals(Material.AIR)) continue;
                entities.add(player.getWorld().dropItemNaturally(player.getLocation(), item));
            }
        }
        // Drop cursor item if any
        ItemStack cursor = player.getItemOnCursor();
        if (!cursor.getType().equals(Material.AIR))
            entities.add(player.getWorld().dropItemNaturally(player.getLocation(), cursor));

        clearInventory(player);

        return entities;
    }

    public static void clearInventory(Player player) {
        player.getInventory().clear();
        // Clear crafting-grid slots only for personal inventory views (avoid wiping chest GUI slots).
        if (hasPersonalCraftingGridOpen(player)) {
            for (int i = 1; i <= 4; i++) {
                player.getOpenInventory().setItem(i, null);
            }
        }
        // Clear any item held on the cursor
        player.setItemOnCursor(null);
    }

    public static void setCollidesWithEntities(Player player, boolean bool) {
        player.setCollidable(bool);
    }

    public static int getPing(Player player) {
        return player.getPing();
    }

    public static ItemStack[] getInventoryStorageContent(Player player) {
        return player.getInventory().getStorageContents();
    }

    public static double getPlayerHealth(Player player) {
        return player.getHealth() + player.getAbsorptionAmount();
    }

    public static void setActiveInventoryTitle(Player player, String title) {
        player.getOpenInventory().setTitle(StringUtil.CC(title));
    }

    public static void setPlayerListName(Player player, Component component) {
        player.playerListName(component);
    }

    public static Fireball shootFireball(Player player, double speed) {
        final Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setAcceleration(fireball.getAcceleration().normalize().multiply(speed));
        return fireball;
    }

    public static void returnItemToCurrentSlotOrInventory(Player player, ItemStack itemStack) {
        if (player == null || itemStack == null || itemStack.getType().isAir()) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack remaining = itemStack.clone();

        ItemStack[] storage = inventory.getStorageContents();
        for (int i = 0; i < storage.length && remaining.getAmount() > 0; i++) {
            ItemStack current = storage[i];
            if (current == null || current.getType().isAir() || !current.isSimilar(remaining)) {
                continue;
            }

            int space = current.getMaxStackSize() - current.getAmount();
            if (space <= 0) {
                continue;
            }

            int moved = Math.min(space, remaining.getAmount());
            current.setAmount(current.getAmount() + moved);
            inventory.setItem(i, current);
            remaining.setAmount(remaining.getAmount() - moved);
        }

        if (remaining.getAmount() > 0) {
            ItemStack offHand = inventory.getItemInOffHand();
            if (offHand.isSimilar(remaining) && offHand.getAmount() < offHand.getMaxStackSize()) {
                int moved = Math.min(offHand.getMaxStackSize() - offHand.getAmount(), remaining.getAmount());
                offHand.setAmount(offHand.getAmount() + moved);
                inventory.setItemInOffHand(offHand);
                remaining.setAmount(remaining.getAmount() - moved);
            }
        }

        if (remaining.getAmount() > 0) {
            Map<Integer, ItemStack> overflow = inventory.addItem(remaining);
            if (!overflow.isEmpty()) {
                overflow.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
            }
        }
    }

    public static void setAttackSpeed(Player player, int hitDelay) {
        // ...existing code...
        org.bukkit.attribute.AttributeInstance attackSpeed =
                player.getAttribute(org.bukkit.attribute.Attribute.ATTACK_SPEED);

        if (attackSpeed != null) {
            attackSpeed.getModifiers().forEach(attackSpeed::removeModifier);

            double attackSpeedValue;
            if (hitDelay <= 0) {
                attackSpeedValue = 100.0;
            } else if (hitDelay >= 20) {
                attackSpeedValue = 4.0;
            } else {
                attackSpeedValue = 4.0 + ((20.0 - hitDelay) / 20.0) * 96.0;
            }

            attackSpeed.setBaseValue(attackSpeedValue);
        }
    }

    public static boolean isPlayerStuck(Player player) {
        Block feetBlock = player.getLocation().getBlock();
        Block headBlock = player.getEyeLocation().getBlock();

        boolean isFeetSolid = feetBlock.getType().isSolid();
        boolean isHeadSolid = headBlock.getType().isSolid();

        return isFeetSolid || isHeadSolid;
    }

}
