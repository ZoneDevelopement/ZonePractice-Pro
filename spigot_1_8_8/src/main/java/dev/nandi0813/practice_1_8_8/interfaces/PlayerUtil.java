package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
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
        return player.getItemInHand();
    }

    @Override
    public boolean isItemInUse(Player player, Material material) {
        ItemStack itemInHand = player.getInventory().getItemInHand();
        return itemInHand != null && itemInHand.getType().equals(material);
    }

    @Override
    public ItemStack getItemInUse(Player player, Material material) {
        ItemStack itemInHand = player.getInventory().getItemInHand();
        if (itemInHand != null && itemInHand.getType().equals(material)) {
            return itemInHand;
        } else {
            return null;
        }
    }

    @Override
    public void setItemInUseIf(Player player, Material material, ItemStack itemStack) {
        ItemStack itemInHand = player.getInventory().getItemInHand();
        if (itemInHand != null && itemInHand.getType().equals(material)) {
            player.setItemInHand(itemStack);
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
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null) continue;
            if (item.getType().equals(Material.AIR)) continue;

            entities.add(player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
        this.clearInventory(player);

        return entities;
    }

    @Override
    public void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
    }

    @Override
    public void setCollidesWithEntities(Player player, boolean bool) {
        player.spigot().setCollidesWithEntities(bool);
    }

    @Override
    public int getPing(Player player) {
        return player.spigot().getPing();
    }

    @Override
    public ItemStack[] getInventoryStorageContent(Player player) {
        return player.getInventory().getContents();
    }

    @Override
    public double getPlayerHealth(Player player) {
        return player.getHealth();
    }

    @Override
    public void setActiveInventoryTitle(Player player, String title) {
    }

    @Override
    public void setPlayerListName(Player player, Component component) {
        player.setPlayerListName(StringUtil.CC(LegacyComponentSerializer.legacyAmpersand().serialize(component)));
    }

    @Override
    public Fireball shootFireball(Player player, double speed) {
        Vector direction = player.getEyeLocation().getDirection();

        final Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setVelocity(direction.multiply(speed));
        return fireball;
    }

    @Override
    public void applyFireballKnockback(final Player player, final Fireball fireball) {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            Vector direction = player.getLocation().toVector()
                    .subtract(fireball.getLocation().toVector())
                    .normalize();

            Vector velocity = new Vector(
                    direction.getX() * FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE,
                    FB_VELOCITY_VERTICAL_MULTIPLICATIVE,
                    direction.getZ() * FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE
            );

            player.setVelocity(velocity);
        }, 1L);
    }

    @Override
    public void applyTntKnockback(Player player, TNTPrimed tnt) {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            Vector direction = player.getLocation().toVector()
                    .subtract(tnt.getLocation().toVector())
                    .normalize();

            player.setVelocity(new Vector(
                    direction.getX() * TNT_VELOCITY_HORIZONTAL_MULTIPLICATIVE,
                    TNT_VELOCITY_VERTICAL_MULTIPLICATIVE,
                    direction.getZ() * TNT_VELOCITY_HORIZONTAL_MULTIPLICATIVE
            ));
        }, 1L);
    }

}
