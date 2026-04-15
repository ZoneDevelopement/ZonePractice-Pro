package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.ZonePractice;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime tag bridge that stores identifiers in PersistentDataContainer and keeps
 * associated live objects in-memory for the current server session.
 */
public enum PersistentTagUtil {
    ;

    private static final Map<String, Object> OBJECT_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<String, String> BLOCK_TAGS = new ConcurrentHashMap<>();

    private static NamespacedKey key(String tag) {
        return new NamespacedKey(ZonePractice.getInstance(), "zpp_" + tag.toLowerCase());
    }

    private static String register(Object value) {
        String token = UUID.randomUUID().toString();
        OBJECT_REGISTRY.put(token, value);
        return token;
    }

    private static void unregister(String token) {
        if (token != null) {
            OBJECT_REGISTRY.remove(token);
        }
    }

    public static void setTag(PersistentDataHolder holder, String tag, Object value) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        NamespacedKey namespacedKey = key(tag);

        unregister(container.get(namespacedKey, PersistentDataType.STRING));
        container.set(namespacedKey, PersistentDataType.STRING, register(value));
    }

    public static <T> T getTag(PersistentDataHolder holder, String tag, Class<T> type) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        String token = container.get(key(tag), PersistentDataType.STRING);
        if (token == null) {
            return null;
        }

        Object value = OBJECT_REGISTRY.get(token);
        if (!type.isInstance(value)) {
            return null;
        }
        return type.cast(value);
    }

    public static boolean hasTag(PersistentDataHolder holder, String tag) {
        return holder.getPersistentDataContainer().has(key(tag), PersistentDataType.STRING);
    }

    public static void clearTag(PersistentDataHolder holder, String tag) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        NamespacedKey namespacedKey = key(tag);

        unregister(container.get(namespacedKey, PersistentDataType.STRING));
        container.remove(namespacedKey);
    }

    public static void clearAllPluginTags(PersistentDataHolder holder) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        String namespace = key("cleanup").getNamespace();

        for (NamespacedKey namespacedKey : new HashSet<>(container.getKeys())) {
            if (!namespace.equals(namespacedKey.getNamespace())) {
                continue;
            }
            if (!namespacedKey.getKey().startsWith("zpp_")) {
                continue;
            }

            unregister(container.get(namespacedKey, PersistentDataType.STRING));
            container.remove(namespacedKey);
        }
    }

    public static void setBlockTag(Block block, String tag, Object value) {
        String blockKey = blockKey(block, tag);
        unregister(BLOCK_TAGS.put(blockKey, register(value)));
    }

    public static <T> T getBlockTag(Block block, String tag, Class<T> type) {
        String token = BLOCK_TAGS.get(blockKey(block, tag));
        if (token == null) {
            return null;
        }

        Object value = OBJECT_REGISTRY.get(token);
        if (!type.isInstance(value)) {
            return null;
        }
        return type.cast(value);
    }

    public static boolean hasBlockTag(Block block, String tag) {
        return BLOCK_TAGS.containsKey(blockKey(block, tag));
    }

    public static void clearBlockTag(Block block, String tag) {
        unregister(BLOCK_TAGS.remove(blockKey(block, tag)));
    }

    public static void setEntityTag(Entity entity, String tag, Object value) {
        setTag(entity, tag, value);
    }

    public static <T> T getEntityTag(Entity entity, String tag, Class<T> type) {
        return getTag(entity, tag, type);
    }

    public static boolean hasEntityTag(Entity entity, String tag) {
        return hasTag(entity, tag);
    }

    public static void clearEntityTag(Entity entity, String tag) {
        clearTag(entity, tag);
    }

    public static void clearAllEntityTags(Entity entity) {
        clearAllPluginTags(entity);
    }

    private static String blockKey(Block block, String tag) {
        return block.getWorld().getUID() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ() + ":" + tag;
    }
}

