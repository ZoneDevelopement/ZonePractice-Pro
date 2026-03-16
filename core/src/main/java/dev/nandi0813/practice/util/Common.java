package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum Common {
    ;

    public static void send(CommandSender sender, Component component) {
        if (sender == null) return;
        sender.sendMessage(component);
    }

    public static void sendMMMessage(Player player, String line) {
        if (line == null) {
            return;
        }

        if (line.contains("&") || line.contains("§")) {
            line = StringUtil.legacyColorToMiniMessage(line);
        }

        if (line.isEmpty()) {
            return;
        }

        if (SoftDependUtil.isPAPI_ENABLED) {
            line = PlaceholderAPI.setPlaceholders(player, line);
        }

        send(player, ZonePractice.getMiniMessage().deserialize(line));
    }

    public static void sendConsoleMMMessage(String string) {
        send(ZonePractice.getInstance().getServer().getConsoleSender(), ZonePractice.getMiniMessage().deserialize(string));
    }

    public static Component deserializeMiniMessage(String line) {
        return ZonePractice.getMiniMessage().deserialize(line);
    }

    public static String serializeComponentToLegacyString(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    public static String mmToNormal(String line) {
        if (line.contains("&") || line.contains("§")) {
            line = StringUtil.legacyColorToMiniMessage(line);
        }

        return StringUtil.CC(serializeComponentToLegacyString(deserializeMiniMessage(line)));
    }

    public static String serializeNormalToMMString(String normalString) {
        String normalized = normalString.replace('&', LegacyComponentSerializer.SECTION_CHAR);
        Component component = LegacyComponentSerializer.legacySection().deserialize(normalized);
        return ZonePractice.getMiniMessage().serialize(component);
    }

    public static String colorize(String message) {
        return StringUtil.CC(message);
    }

    public static Component legacyToComponent(String message) {
        if (message == null) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static String stripLegacyColor(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static List<String> mmToNormal(List<String> list) {
        List<String> newList = new ArrayList<>();
        for (String line : list) {
            newList.add(mmToNormal(line));
        }
        return newList;
    }

    public static String getItemDisplayName(ItemMeta itemMeta) {
        if (itemMeta == null || !itemMeta.hasDisplayName() || itemMeta.displayName() == null) {
            return "";
        }
        return serializeComponentToLegacyString(itemMeta.displayName());
    }

    public static String getItemDisplayName(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return "";
        }
        return getItemDisplayName(itemStack.getItemMeta());
    }

    public static short getItemDamage(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return 0;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Damageable damageable) {
            return (short) damageable.getDamage();
        }
        return 0;
    }

    private static final Map<String, String> LEGACY_ENCHANTMENT_ALIASES = Map.ofEntries(
            Map.entry("DAMAGE_ALL", "sharpness"),
            Map.entry("DAMAGE_UNDEAD", "smite"),
            Map.entry("DAMAGE_ARTHROPODS", "bane_of_arthropods"),
            Map.entry("ARROW_DAMAGE", "power"),
            Map.entry("ARROW_KNOCKBACK", "punch"),
            Map.entry("ARROW_FIRE", "flame"),
            Map.entry("ARROW_INFINITE", "infinity"),
            Map.entry("DIG_SPEED", "efficiency"),
            Map.entry("DURABILITY", "unbreaking"),
            Map.entry("LOOT_BONUS_BLOCKS", "fortune"),
            Map.entry("LOOT_BONUS_MOBS", "looting"),
            Map.entry("OXYGEN", "respiration"),
            Map.entry("PROTECTION_ENVIRONMENTAL", "protection"),
            Map.entry("PROTECTION_FIRE", "fire_protection"),
            Map.entry("PROTECTION_FALL", "feather_falling"),
            Map.entry("PROTECTION_EXPLOSIONS", "blast_protection"),
            Map.entry("PROTECTION_PROJECTILE", "projectile_protection"),
            Map.entry("WATER_WORKER", "aqua_affinity"),
            Map.entry("THORNS", "thorns"),
            Map.entry("KNOCKBACK", "knockback"),
            Map.entry("FIRE_ASPECT", "fire_aspect"),
            Map.entry("SILK_TOUCH", "silk_touch")
    );

    public static Iterable<Enchantment> getAllEnchantments() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    }

    public static Enchantment resolveEnchantment(String enchantmentName) {
        if (enchantmentName == null || enchantmentName.isBlank()) {
            return null;
        }

        String normalized = enchantmentName.trim().toLowerCase(Locale.ROOT).replace("minecraft:", "");
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(normalized));
        if (enchantment != null) {
            return enchantment;
        }

        String mapped = LEGACY_ENCHANTMENT_ALIASES.get(enchantmentName.trim().toUpperCase(Locale.ROOT));
        if (mapped != null) {
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(mapped));
            if (enchantment != null) {
                return enchantment;
            }
        }

        for (Enchantment value : getAllEnchantments()) {
            String key = value.getKey().getKey();
            if (key.equalsIgnoreCase(enchantmentName)
                    || key.equalsIgnoreCase(normalized)) {
                return value;
            }
        }
        return null;
    }
}