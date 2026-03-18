package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil {

    private static final double TNT_VELOCITY_HORIZONTAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.TNT.HORIZONTAL");
    private static final double TNT_VELOCITY_VERTICAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.TNT.VERTICAL");
    private static final double FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.FIREBALL.HORIZONTAL");
    private static final double FB_VELOCITY_VERTICAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.FIREBALL.VERTICAL");
    
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
        // Drop items from the 2x2 crafting grid (indices 1-4 of the open inventory)
        for (int i = 1; i <= 4; i++) {
            ItemStack item = player.getOpenInventory().getItem(i);
            if (item == null || item.getType().equals(Material.AIR)) continue;
            entities.add(player.getWorld().dropItemNaturally(player.getLocation(), item));
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
        // Clear the 2x2 crafting grid slots (indices 1-4 of the player's open inventory)
        for (int i = 1; i <= 4; i++)
            player.getOpenInventory().setItem(i, null);
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

    public static void applyFireballKnockback(final Player player, final Fireball fireball) {
        final Location playerLoc = player.getLocation();
        final Location fireballLoc = fireball.getLocation();

        final float yield = fireball.getYield() > 0 ? fireball.getYield() : 1.0f;

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            double dx = playerLoc.getX() - fireballLoc.getX();
            double dz = playerLoc.getZ() - fireballLoc.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            // Use only horizontal distance for factor calculation so that jumping
            // (which only increases vertical separation) does not reduce the knockback strength.
            double effectiveDistance = horizontalDistance;

            double safeDistance = 0.8;
            double factor = 1.0;

            if (effectiveDistance > safeDistance) {
                double impactRadius = yield * 2.5;
                double decayRange = impactRadius - safeDistance;

                if (decayRange <= 0.1) decayRange = 1.0;

                factor = 1.0 - ((effectiveDistance - safeDistance) / decayRange);
            }

            if (factor <= 0.1) return;
            if (factor > 1) factor = 1;

            // Compute horizontal direction separately so the vertical component
            // of the direction vector doesn't steal from horizontal velocity.
            double horizontalLen = Math.sqrt(dx * dx + dz * dz);
            double horizDirX;
            double horizDirZ;

            if (horizontalLen < 0.001) {
                // Fireball is almost directly below – pick the player's facing direction
                Vector facing = playerLoc.getDirection();
                double facingLen = Math.sqrt(facing.getX() * facing.getX() + facing.getZ() * facing.getZ());
                if (facingLen < 0.001) {
                    horizDirX = 0;
                    horizDirZ = 0;
                } else {
                    horizDirX = facing.getX() / facingLen;
                    horizDirZ = facing.getZ() / facingLen;
                }
            } else {
                horizDirX = dx / horizontalLen;
                horizDirZ = dz / horizontalLen;
            }

            // Apply a slight reduction when airborne so it's weaker than grounded, but not drastically
            double airMultiplier = player.isOnGround() ? 1.0 : 0.8;

            Vector velocity = new Vector(
                    horizDirX * FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor * airMultiplier,
                    FB_VELOCITY_VERTICAL_MULTIPLICATIVE * factor,
                    horizDirZ * FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor * airMultiplier
            );

            player.setVelocity(velocity);
        }, 1L);
    }

    public static void applyTntKnockback(Player player, TNTPrimed tnt) {
        final Location playerLoc = player.getLocation();
        final Location tntLoc = tnt.getLocation();
        final float yield = tnt.getYield();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            double distance = playerLoc.distance(tntLoc);

            double impactRadius = (yield > 0 ? yield : 4.0) * 2.0;
            double factor = 1.0 - (distance / impactRadius);

            if (factor <= 0.1) return;
            if (factor > 1) factor = 1;

            Vector direction = playerLoc.toVector().subtract(tntLoc.toVector());

            if (direction.lengthSquared() == 0) {
                direction = new Vector(0, 0.1, 0);
            } else {
                direction.normalize();
            }

            Vector velocity = new Vector(
                    direction.getX() * TNT_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor,
                    TNT_VELOCITY_VERTICAL_MULTIPLICATIVE * factor,
                    direction.getZ() * TNT_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor
            );

            player.setVelocity(velocity);
        }, 1L);
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
