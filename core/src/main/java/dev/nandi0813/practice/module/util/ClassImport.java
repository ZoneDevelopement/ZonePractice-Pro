package dev.nandi0813.practice.module.util;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.module.VersionNotSupportedException;
import dev.nandi0813.practice.module.interfaces.ChangedBlock;
import dev.nandi0813.practice.module.interfaces.KitData;
import dev.nandi0813.practice.module.interfaces.actionbar.ActionBar;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;

import java.lang.reflect.Constructor;
import java.util.Objects;

public enum ClassImport {
    ;

    private static Classes classes;

    public static Classes getClasses() {
        if (classes == null) {
            try {
                Class<?> c = getNamedClass();
                if (c != null && Classes.class.isAssignableFrom(c))
                    classes = (Classes) c.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
                throw new VersionNotSupportedException(e);
            }
        }
        return classes;
    }

    public static ChangedBlock createChangeBlock(Block block) {
        if (block == null) return null;

        Class<?> changedBlockClass = classes.getChangedBlockClass();
        if (ChangedBlock.class.isAssignableFrom(changedBlockClass)) {
            try {
                Constructor<ChangedBlock> constructor = (Constructor<ChangedBlock>) changedBlockClass.getConstructor(Block.class);
                return constructor.newInstance(block);
            } catch (Exception e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Creates a ChangedBlock for a position whose block has already changed (e.g. is now AIR)
     * but whose original material is known. Used for TNT blocks that were chain-primed before
     * the EntityExplodeEvent fires.
     */
    public static ChangedBlock createChangeBlock(Block block, Material originalMaterial) {
        if (block == null || originalMaterial == null) return null;

        Class<?> changedBlockClass = classes.getChangedBlockClass();
        if (ChangedBlock.class.isAssignableFrom(changedBlockClass)) {
            try {
                Constructor<ChangedBlock> constructor = (Constructor<ChangedBlock>) changedBlockClass.getConstructor(Block.class, Material.class);
                return constructor.newInstance(block, originalMaterial);
            } catch (Exception e) {
                Common.sendConsoleMMMessage("<red>Error creating ChangedBlock with material override: " + e.getMessage());
            }
        }
        return null;
    }

    public static ChangedBlock createChangeBlock(final BlockPlaceEvent blockPlaceEvent) {
        if (blockPlaceEvent.getBlock().getLocation() == null) return null;

        Class<?> changedBlockClass = classes.getChangedBlockClass();
        if (ChangedBlock.class.isAssignableFrom(changedBlockClass)) {
            try {
                Constructor<ChangedBlock> constructor = (Constructor<ChangedBlock>) changedBlockClass.getConstructor(BlockPlaceEvent.class);
                return constructor.newInstance(blockPlaceEvent);
            } catch (Exception e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        }
        return null;
    }

    public static KitData createKitData() {
        Class<?> kitDataClass = classes.getKitDataClass();
        if (KitData.class.isAssignableFrom(kitDataClass)) {
            try {
                Constructor<KitData> constructor = (Constructor<KitData>) kitDataClass.getConstructor();
                return constructor.newInstance();
            } catch (Exception e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        }
        return null;
    }

    public static KitData createKitData(KitData kitData) {
        Class<?> kitDataClass = classes.getKitDataClass();
        if (KitData.class.isAssignableFrom(kitDataClass)) {
            try {
                Constructor<KitData> constructor = (Constructor<KitData>) kitDataClass.getConstructor(KitData.class);
                return constructor.newInstance(kitData);
            } catch (Exception e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        }
        return null;
    }

    public static ActionBar createActionBarClass(Profile profile) {
        Class<?> actionBarClass = classes.getActionBarClass();
        if (ActionBar.class.isAssignableFrom(actionBarClass)) {
            try {
                Constructor<ActionBar> constructor = (Constructor<ActionBar>) actionBarClass.getConstructor(Profile.class);
                return constructor.newInstance(profile);
            } catch (Exception e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        }
        return null;
    }

    private static Class<?> getNamedClass() {

        String version = Objects.requireNonNull(VersionChecker.getBukkitVersion()).getModuleVersionExtension();
        if (version == null) {
            return null;
        }

        try {
            return Class.forName("dev.nandi0813.practice_" + version + "." + "Classes");
        } catch (final ClassNotFoundException e) {
            Common.sendConsoleMMMessage("<gray>[<gold>ZonePractice<gray>] <red>Class " + "Classes" + " cannot be found. Bukkit version: " + Bukkit.getServer().getBukkitVersion() + ".");
            return null;
        }
    }

}
