package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.type.FireballFight;
import dev.nandi0813.practice.util.Common;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;

public class LadderUtil implements dev.nandi0813.practice.module.interfaces.LadderUtil {

    @Override
    public void loadInventory(Player player, ItemStack[] armor, ItemStack[] inventory, ItemStack[] extra) {
        player.getInventory().setArmorContents(armor);
        player.getInventory().setStorageContents(inventory);
        player.getInventory().setExtraContents(extra);
    }

    private static final String[] MATERIAL_TYPES = {
            "_WOOL", "_STAINED_CLAY", "_STAINED_GLASS", "_STAINED_GLASS_PANE", "_CARPET",
            "_CONCRETE", "_CONCRETE_POWDER", "_TERRACOTTA", "_GLAZED_TERRACOTTA", "_CANDLE", "_BANNER"
    };

    public ItemStack changeItemColor(@NotNull ItemStack item, Component teamColor) {
        String itemType = item.getType().toString();
        TextColor textColor = teamColor.color();
        Color color = Color.YELLOW;
        if (textColor != null) {
            color = Color.fromRGB(
                    Objects.requireNonNull(teamColor.color()).red(),
                    Objects.requireNonNull(teamColor.color()).green(),
                    Objects.requireNonNull(teamColor.color()).blue()
            );
        }

        if (item.getType().name().startsWith("LEATHER_")) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            if (meta != null) {
                meta.setColor(color);
                item.setItemMeta(meta);
            }
            return item;
        }

        for (String type : MATERIAL_TYPES) {
            if (itemType.contains(type) && textColor != null) {
                try {
                    Material material = Material.getMaterial(textColor.toString().toUpperCase() + type);

                    if (material != null) {
                        return item.withType(material);
                    }
                } catch (Exception ignored) {
                    break;
                }
            }
        }

        return item;
    }

    @Override
    public ItemStack getPotionItem(String string) {
        try {
            if (string.contains("::")) {
                String[] split = string.split("::");
                ItemStack itemStack = new ItemStack(Material.valueOf(split[0]));

                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                if (potionMeta != null)
                    potionMeta.setBasePotionType(PotionType.valueOf(split[1]));

                itemStack.setItemMeta(potionMeta);
                return itemStack;
            }
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Invalid item: " + string);
        }
        return null;
    }

    @Override
    public boolean isUnbreakable(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() != null) {
            return item.getItemMeta().isUnbreakable();
        }
        return false;
    }

    @Override
    public ItemMeta setUnbreakable(ItemMeta itemMeta, boolean unbreakable) {
        if (itemMeta != null) {
            itemMeta.setUnbreakable(unbreakable);
        }
        return itemMeta;
    }

    @Override
    public ItemStack setDurability(ItemStack itemStack, int durability) {
        if (itemStack.getItemMeta() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof Damageable damageable) {
                int newDamage = itemStack.getType().getMaxDurability() - durability;
                if (newDamage < 0 || newDamage > itemStack.getType().getMaxDurability()) {
                    newDamage = itemStack.getType().getMaxDurability();
                }

                damageable.setDamage(newDamage);
                itemStack.setItemMeta(damageable);
                return itemStack;
            }
        }
        return itemStack;
    }

    @Override
    public void placeTnt(BlockPlaceEvent e, Match match) {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (e.isCancelled()) {
                return;
            }

            Block block = e.getBlock();
            block.setType(Material.AIR);
            block.getState().update();

            TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation().subtract(-0.5, 0, -0.5), EntityType.TNT);
            tnt.setMetadata(FIGHT_ENTITY, new FixedMetadataValue(ZonePractice.getInstance(), match));
            tnt.setIsIncendiary(false);

            if (match.getLadder() instanceof FireballFight) {
                tnt.setMetadata(FireballFight.FIREBALL_FIGHT_TNT, new FixedMetadataValue(ZonePractice.getInstance(), match));
                tnt.setMetadata(FireballFight.FIREBALL_FIGHT_TNT_SHOOTER, new FixedMetadataValue(ZonePractice.getInstance(), e.getPlayer()));
            }
        }, 2L);
    }

}
