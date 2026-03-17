package dev.nandi0813.practice.manager.playerkit;

import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public enum PlayerKitUtil {
    ;

    /**
     * Parses an item string into an ItemStack.
     * <p>
     * Supported formats:
     * <ul>
     *   <li>{@code MATERIAL} – plain material, e.g. {@code ARROW}</li>
     *   <li>{@code MATERIAL::NUMBER} – material with a legacy damage/data value (1.8), e.g. {@code WOOL::14}</li>
     *   <li>{@code MATERIAL::NAMED_TYPE} – material with a named sub-type resolved via
     *       {@link LadderUtil#getPotionItem(String)},
     *       e.g. {@code TIPPED_ARROW::LONG_NIGHT_VISION}, {@code LINGERING_POTION::SPEED},
     *       {@code SPLASH_POTION::STRENGTH}</li>
     * </ul>
     */
    public static ItemStack getItem(String string) {
        try {
            if (string.contains("::")) {
                String[] split = string.split("::", 2);
                String suffix = split[1];

                // If the part after :: is numeric it's a legacy damage/data value (1.8 only)
                boolean isNumeric = suffix.chars().allMatch(Character::isDigit);
                if (isNumeric) {
                    ItemStack itemStack = new ItemStack(Material.valueOf(split[0]), 1);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta instanceof Damageable damageable) {
                        damageable.setDamage(Short.parseShort(suffix));
                        itemStack.setItemMeta(damageable);
                    }
                    return itemStack;
                } else {
                    // Named sub-type (PotionType, etc.) – delegate to the version-specific impl
                    return LadderUtil.getPotionItem(string);
                }
            } else if (string.equalsIgnoreCase("")) {
                return new ItemStack(Material.AIR);
            } else {
                return new ItemStack(Material.valueOf(string));
            }
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Invalid item: " + string);
        }
        return null;
    }

}
