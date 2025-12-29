package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.manager.fight.match.Match;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface LadderUtil {

    void loadInventory(Player player, ItemStack[] armor, ItemStack[] inventory, ItemStack[] extra);

    ItemStack changeItemColor(ItemStack item, Component teamColor);

    ItemStack getPotionItem(String potion);

    boolean isUnbreakable(ItemStack item);

    ItemMeta setUnbreakable(ItemMeta item, boolean unbreakable);

    ItemStack setDurability(ItemStack itemStack, int durability);

    void placeTnt(BlockPlaceEvent e, Match match);

}
