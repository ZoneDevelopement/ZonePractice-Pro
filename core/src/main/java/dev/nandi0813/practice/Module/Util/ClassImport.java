package dev.nandi0813.practice.Module.Util;

import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Module.Interfaces.ActionBar.ActionBar;
import dev.nandi0813.practice.Module.Interfaces.ChangedBlock;
import dev.nandi0813.practice.Module.Interfaces.KitData;
import dev.nandi0813.practice.Module.VersionNotSupportedException;
import dev.nandi0813.practice.Util.Common;
import org.bukkit.Bukkit;
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
