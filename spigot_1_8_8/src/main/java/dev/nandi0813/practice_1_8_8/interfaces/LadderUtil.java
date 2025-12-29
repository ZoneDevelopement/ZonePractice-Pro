package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.type.FireballFight;
import dev.nandi0813.practice.module.interfaces.ItemCreateUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;

public class LadderUtil implements dev.nandi0813.practice.module.interfaces.LadderUtil {

    @Override
    public void loadInventory(Player player, ItemStack[] armor, ItemStack[] inventory, ItemStack[] extra) {
        player.getInventory().setArmorContents(armor);
        player.getInventory().setContents(inventory);
    }

    public ItemStack changeItemColor(ItemStack item, Component teamColor) {
        Material itemType = item.getType();
        String color = LegacyComponentSerializer.legacyAmpersand().serialize(teamColor.append(Component.text("-")));
        color = color.replace("-", "");

        if (itemType.equals(Material.WOOL) ||
                itemType.equals(Material.STAINED_CLAY) ||
                itemType.equals(Material.STAINED_GLASS) ||
                itemType.equals(Material.STAINED_GLASS_PANE) ||
                itemType.equals(Material.CARPET)) {
            item.setDurability(ItemCreateUtil.getDurabilityByColor(color.charAt(1)));
        } else if (item.getType().name().startsWith("LEATHER_")) {
            Color c = StringUtil.translateChatColorToColor(Objects.requireNonNull(ChatColor.getByChar(color.charAt(1))));

            if (c != null) {
                LeatherArmorMeta lch = (LeatherArmorMeta) item.getItemMeta();
                lch.setColor(c);
                item.setItemMeta(lch);
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

                if (itemStack.getType() == Material.POTION) {
                    itemStack.setDurability((((short) Integer.parseInt(split[1]))));
                }
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
            item.getItemMeta().spigot().isUnbreakable();
        }
        return false;
    }

    @Override
    public ItemMeta setUnbreakable(ItemMeta itemMeta, boolean unbreakable) {
        if (itemMeta != null) {
            itemMeta.spigot().setUnbreakable(unbreakable);
        }
        return itemMeta;
    }

    @Override
    public ItemStack setDurability(ItemStack itemStack, int durability) {
        itemStack.setDurability((short) (itemStack.getType().getMaxDurability() - durability));
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
            block.getState().setData(new MaterialData(Material.AIR));
            block.getState().update();

            TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation().subtract(-0.5, 0, -0.5), EntityType.PRIMED_TNT);
            tnt.setMetadata(FIGHT_ENTITY, new FixedMetadataValue(ZonePractice.getInstance(), match));
            tnt.setIsIncendiary(false);

            if (match.getLadder() instanceof FireballFight) {
                tnt.setMetadata(FireballFight.FIREBALL_FIGHT_TNT, new FixedMetadataValue(ZonePractice.getInstance(), match));
                tnt.setMetadata(FireballFight.FIREBALL_FIGHT_TNT_SHOOTER, new FixedMetadataValue(ZonePractice.getInstance(), e.getPlayer()));
            }
        }, 2L);
    }

}
