package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil implements dev.nandi0813.practice.module.interfaces.PlayerUtil {

    @Override
    public ItemStack getPlayerMainHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public boolean isItemInUse(Player player, Material material) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        return itemInMainHand.getType().equals(material) || itemInOffHand.getType().equals(material);
    }

    @Override
    public ItemStack getItemInUse(Player player, Material material) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        if (itemInMainHand.getType().equals(material)) return itemInMainHand;
        if (itemInOffHand.getType().equals(material)) return itemInOffHand;
        return null;
    }

    @Override
    public void setItemInUseIf(Player player, Material material, ItemStack itemStack) {
        if (player.getInventory().getItemInMainHand().getType().equals(material)) {
            player.getInventory().setItemInMainHand(itemStack);
        }
        if (player.getInventory().getItemInOffHand().getType().equals(material)) {
            player.getInventory().setItemInOffHand(itemStack);
        }
    }

    @Override
    public List<Entity> dropPlayerInventory(Player player) {
        List<Entity> entities = new ArrayList<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType().equals(Material.AIR)) continue;

            entities.add(player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
        this.clearInventory(player);

        return entities;
    }

    public void clearInventory(Player player) {
        player.getInventory().clear();
    }

    public void setCollidesWithEntities(Player player, boolean bool) {
        player.setCollidable(bool);
    }

    @Override
    public int getPing(Player player) {
        return player.getPing();
    }

    @Override
    public ItemStack[] getInventoryStorageContent(Player player) {
        return player.getInventory().getStorageContents();
    }

    @Override
    public double getPlayerHealth(Player player) {
        return player.getHealth() + player.getAbsorptionAmount();
    }

    @Override
    public void setActiveInventoryTitle(Player player, String title) {
        player.getOpenInventory().setTitle(StringUtil.CC(title));
    }

    @Override
    public void setPlayerListName(Player player, Component component) {
        player.playerListName(component);
    }

    @Override
    public Fireball shootFireball(Player player, double speed) {
        final Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setAcceleration(fireball.getAcceleration().normalize().multiply(speed));
        return fireball;
    }

    @Override
    public void applyFireballKnockback(final Player player, final Fireball fireball) {
        final Location playerLoc = player.getLocation();
        final Location fireballLoc = fireball.getLocation();

        final float yield = fireball.getYield() > 0 ? fireball.getYield() : 1.0f;

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            double distance = playerLoc.distance(fireballLoc);

            double safeDistance = 0.6;
            double factor = 1.0;

            if (distance > safeDistance) {
                double impactRadius = yield * 2.5;
                double decayRange = impactRadius - safeDistance;

                if (decayRange <= 0.1) decayRange = 1.0;

                factor = 1.0 - ((distance - safeDistance) / decayRange);
            }

            if (factor <= 0.1) return;
            if (factor > 1) factor = 1;

            Vector direction = playerLoc.toVector().subtract(fireballLoc.toVector());

            if (direction.lengthSquared() == 0) {
                direction = new Vector(0, 0.1, 0);
            } else {
                direction.normalize();
            }

            Vector velocity = new Vector(
                    direction.getX() * FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor,
                    FB_VELOCITY_VERTICAL_MULTIPLICATIVE * factor,
                    direction.getZ() * FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor
            );

            player.setVelocity(velocity);
        }, 1L);
    }

    @Override
    public void applyTntKnockback(Player player, TNTPrimed tnt) {
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

}
