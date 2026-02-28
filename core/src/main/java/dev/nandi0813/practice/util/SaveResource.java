package dev.nandi0813.practice.util;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.module.util.VersionChecker;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SaveResource {

    // Ladders common to all versions
    private static final String[] COMMON_LADDER_NAMES = {
            "archer.yml",
            "axe.yml",
            "battlerush.yml",
            "bedwars.yml",
            "boxing.yml",
            "bridges.yml",
            "builduhc.yml",
            "debuff.yml",
            "fireball.yml",
            "gapple.yml",
            "nodebuff.yml",
            "pearlfight.yml",
            "sg.yml",
            "skywars.yml",
            "soup.yml",
            "spleef.yml",
            "sumo.yml",
            "vanilla.yml"
    };

    // Ladders exclusive to 1.8.8
    private static final String[] LEGACY_ONLY_LADDER_NAMES = {
            "combo.yml"
    };

    // Ladders exclusive to modern versions (1.20+)
    private static final String[] MODERN_ONLY_LADDER_NAMES = {
            "mace.yml",
            "crystal.yml",
            "spear.yml"
    };

    /**
     * Gets the appropriate ladder list for the current server version
     *
     * @return Array of ladder file names to save
     */
    private String[] getLadderNames() {
        VersionChecker.BukkitVersion version = VersionChecker.getBukkitVersion();
        if (version == null) {
            Common.sendConsoleMMMessage("<yellow>Could not detect version, using common ladders only.");
            return COMMON_LADDER_NAMES;
        }

        // Combine common ladders with version-specific ones
        if (version == VersionChecker.BukkitVersion.v1_8_R3) {
            // 1.8.8: Common + Legacy-only
            return combineArrays(COMMON_LADDER_NAMES, LEGACY_ONLY_LADDER_NAMES);
        } else {
            // Modern versions: Common + Modern-only
            return combineArrays(COMMON_LADDER_NAMES, MODERN_ONLY_LADDER_NAMES);
        }
    }

    /**
     * Combines two string arrays into one
     */
    private String[] combineArrays(String[] array1, String[] array2) {
        String[] result = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    public void saveResources(ZonePractice practice) {
        saveResource(
                new File(practice.getDataFolder(), "language.yml"),
                practice.getResource("language.yml"));
        saveResource(
                new File(practice.getDataFolder(), "sidebar.yml"),
                practice.getResource("sidebar.yml"));
        saveResource(
                new File(practice.getDataFolder(), "groups.yml"),
                practice.getResource("groups.yml"));
        saveResource(
                new File(practice.getDataFolder(), "config.yml"),
                practice.getResource(this.getVersionPath() + "config.yml"));
        saveResource(
                new File(practice.getDataFolder(), "divisions.yml"),
                practice.getResource(this.getVersionPath() + "divisions.yml"));
        saveResource(
                new File(practice.getDataFolder(), "guis.yml"),
                practice.getResource(this.getVersionPath() + "guis.yml"));
        saveResource(
                new File(practice.getDataFolder(), "inventories.yml"),
                practice.getResource(this.getVersionPath() + "inventories.yml"));
        saveResource(
                new File(practice.getDataFolder(), "playerkit.yml"),
                practice.getResource(this.getVersionPath() + "playerkit.yml"));

        File ladderFolder = new File(practice.getDataFolder(), "/ladders");
        if (!ladderFolder.exists()) {
            if (!ladderFolder.mkdir()) {
                Common.sendConsoleMMMessage("<red>Couldn't create ladders folder.");
            }

            // Use version-specific ladder list
            for (String ladder : getLadderNames()) {
                saveLadder(practice, this.getVersionPath(), ladder);
            }

            try {
                FileUtils.deleteDirectory(new File(practice.getDataFolder(), this.getVersionPath().replace("/", "")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void saveResource(@NotNull File document, @NotNull InputStream defaults) {
        try {
            YamlDocument.create(document, defaults,
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("VERSION")).build());

        } catch (IOException e) {
            Common.sendConsoleMMMessage("<red>Couldn't load " + document.getName() + ".");
        }
    }

    private String getVersionPath() {
        return Objects.requireNonNull(VersionChecker.getBukkitVersion()).getFilePath();
    }

    private static void saveLadder(ZonePractice practice, String path, String fileName) {
        practice.saveResource(path + "ladders/" + fileName, false);
        File file = getFile(path + "ladders/" + fileName);
        if (file.exists()) {
            if (!file.renameTo(new File(practice.getDataFolder() + "/ladders/", fileName)))
                Common.sendConsoleMMMessage("<red>Couldn't move " + fileName + " to ladders folder.");
        }
    }

    private static File getFile(String path) {
        return new File(ZonePractice.getInstance().getDataFolder(), path);
    }

}
