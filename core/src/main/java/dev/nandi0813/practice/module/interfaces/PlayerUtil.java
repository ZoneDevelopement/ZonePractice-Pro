package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface PlayerUtil {

    ItemStack getPlayerMainHand(Player player);

    boolean isItemInUse(Player player, Material material);

    ItemStack getItemInUse(Player player, Material material);

    void setItemInUseIf(Player player, Material material, ItemStack itemStack);

    List<Entity> dropPlayerInventory(Player player);

    void clearInventory(Player player);

    void setCollidesWithEntities(Player player, boolean bool);

    int getPing(Player player);

    ItemStack[] getInventoryStorageContent(Player player);

    double getPlayerHealth(Player player);

    void setActiveInventoryTitle(Player player, String title);

    void setPlayerListName(Player player, Component component);

    Fireball shootFireball(Player player, double speed);

    double TNT_VELOCITY_HORIZONTAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.TNT.HORIZONTAL");
    double TNT_VELOCITY_VERTICAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.TNT.VERTICAL");
    double FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.FIREBALL.HORIZONTAL");
    double FB_VELOCITY_VERTICAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.FIREBALL.VERTICAL");

    void applyFireballKnockback(final Player player, final Fireball fireball);

    void applyTntKnockback(Player player, TNTPrimed tnt);

    /**
     * Sets the player's attack speed based on hit delay.
     * For 1.8.8: Does nothing (uses setMaximumNoDamageTicks only)
     * For Modern (1.9+): Modifies GENERIC_ATTACK_SPEED attribute
     *
     * @param player   The player
     * @param hitDelay The hit delay in ticks (0 = instant, 20 = 1 second delay)
     */
    void setAttackSpeed(Player player, int hitDelay);

}
