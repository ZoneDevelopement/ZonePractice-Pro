package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

public enum BlockUtil {
    ;

    public static void breakBlock(Match match, Block block) {
        if (match == null) return;

        match.addBlockChange(new ChangedBlock(block));
        block.breakNaturally();
    }

    public static void breakBlock(FFA ffa, Block block) {
        if (ffa == null) return;

        ffa.getFightChange().addBlockChange(new ChangedBlock(block));
        block.breakNaturally();
    }

    /**
     * Dispatches to the correct {@code breakBlock} overload based on the runtime type of
     * the {@link Spectatable} (Match or FFA). Tracks the block for rollback and breaks it.
     */
    public static void breakBlock(Spectatable spectatable, Block block) {
        if (spectatable instanceof Match match) {
            breakBlock(match, block);
        } else if (spectatable instanceof FFA ffa) {
            breakBlock(ffa, block);
        }
    }

    public static void setMetadata(Block block, String tag, Object value) {
        PersistentTagUtil.setBlockTag(block, tag, value);
    }

    public static void setMetadata(Entity entity, String tag, Object value) {
        PersistentTagUtil.setEntityTag(entity, tag, value);
    }

    public static <T> T getMetadata(Block block, String tag, Class<T> type) {
        return PersistentTagUtil.getBlockTag(block, tag, type);
    }

    public static <T> T getMetadata(Entity entity, String tag, Class<T> type) {
        return PersistentTagUtil.getEntityTag(entity, tag, type);
    }

    public static <T> T getMetadata(Item item, String tag, Class<T> type) {
        return PersistentTagUtil.getTag(item, tag, type);
    }

    public static boolean hasMetadata(Block block, String tag) {
        return PersistentTagUtil.hasBlockTag(block, tag);
    }

    public static boolean hasMetadata(Entity entity, String tag) {
        return PersistentTagUtil.hasEntityTag(entity, tag);
    }

    public static boolean hasMetadata(Item item, String tag) {
        return PersistentTagUtil.hasTag(item, tag);
    }

    public static void clearMetadata(Block block, String tag) {
        PersistentTagUtil.clearBlockTag(block, tag);
    }

    public static void clearMetadata(Entity entity, String tag) {
        PersistentTagUtil.clearEntityTag(entity, tag);
    }

    public static void clearAllMetadata(Entity entity) {
        PersistentTagUtil.clearAllEntityTags(entity);
    }

}
