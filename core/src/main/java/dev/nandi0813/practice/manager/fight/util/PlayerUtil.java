package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerUtil {

    private static final NamespacedKey ATTACK_COOLDOWN_MODIFIER_KEY = NamespacedKey.minecraft("zpp_attack_cooldown_modifier");

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

        PlayerInventory inventory = player.getInventory();

        for (ItemStack item : inventory.getStorageContents()) {
            dropIfPresent(player, item, entities);
        }
        for (ItemStack item : inventory.getArmorContents()) {
            dropIfPresent(player, item, entities);
        }

        dropIfPresent(player, inventory.getItemInOffHand(), entities);

        // Includes personal crafting slots/result in player inventory implementations.
        for (ItemStack item : inventory.getExtraContents()) {
            dropIfPresent(player, item, entities);
        }

        // Cursor item is not part of inventory arrays.
        dropIfPresent(player, player.getItemOnCursor(), entities);

        clearInventory(player);

        return entities;
    }

    private static void dropIfPresent(Player player, ItemStack item, List<Entity> entities) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        entities.add(player.getWorld().dropItemNaturally(player.getLocation(), item.clone()));
    }

    public static void clearInventory(Player player) {
        player.getInventory().clear();
        // Clear off-hand slot
        player.getInventory().setItemInOffHand(null);
        // Clear crafting-grid slots only for personal inventory views (avoid wiping chest GUI slots).
        if (hasPersonalCraftingGridOpen(player)) {
            for (int i = 1; i <= 4; i++) {
                player.getOpenInventory().setItem(i, null);
            }
        }
        // Clear any item held on the cursor
        player.setItemOnCursor(null);
    }

    public static void clearMainInventory(Player player) {
        if (player == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack[] storage = inventory.getStorageContents();
        inventory.setStorageContents(new ItemStack[storage.length]);
        inventory.setExtraContents(new ItemStack[inventory.getExtraContents().length]);

        if (hasPersonalCraftingGridOpen(player)) {
            for (int i = 1; i <= 4; i++) {
                player.getOpenInventory().setItem(i, null);
            }
        }

        player.setItemOnCursor(null);
    }

    public static void clearArmor(Player player) {
        if (player == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        inventory.setHelmet(null);
        inventory.setChestplate(null);
        inventory.setLeggings(null);
        inventory.setBoots(null);
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

    @SuppressWarnings("deprecation")
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

    public static void setAttackSpeed(Player player, double cooldownMultiplier) {
        AttributeInstance attackSpeed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeed == null) {
            return;
        }

        // Remove only our own modifier, keep sword/fist/item modifiers intact.
        for (AttributeModifier modifier : attackSpeed.getModifiers()) {
            if (ATTACK_COOLDOWN_MODIFIER_KEY.equals(modifier.getKey())) {
                attackSpeed.removeModifier(modifier);
            }
        }

        // Cooldown multiplier scales cooldown ticks, so ATTACK_SPEED must use inverse scale.
        double speedMultiplier;
        if (cooldownMultiplier <= 0D) {
            speedMultiplier = 256D; // Effectively no cooldown while still preserving item deltas.
        } else {
            speedMultiplier = 1D / cooldownMultiplier;
        }

        AttributeModifier cooldownModifier = new AttributeModifier(
                ATTACK_COOLDOWN_MODIFIER_KEY,
                speedMultiplier - 1D,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                EquipmentSlotGroup.ANY
        );
        attackSpeed.addModifier(cooldownModifier);
    }

    public static void resetAttackSpeed(Player player) {
        AttributeInstance attackSpeed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeed == null) {
            return;
        }

        for (AttributeModifier modifier : attackSpeed.getModifiers()) {
            if (ATTACK_COOLDOWN_MODIFIER_KEY.equals(modifier.getKey())) {
                attackSpeed.removeModifier(modifier);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPlayerStuck(Player player) {
        Block feetBlock = player.getLocation().getBlock();
        Block headBlock = player.getEyeLocation().getBlock();

        boolean isFeetSolid = feetBlock.getType().isSolid();
        boolean isHeadSolid = headBlock.getType().isSolid();

        return isFeetSolid || isHeadSolid;
    }

}
