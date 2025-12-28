package dev.nandi0813.practice.Util;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.nandi0813.practice.Module.Util.VersionChecker;
import dev.nandi0813.practice.ZonePractice;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SaveResource {

    private static final String[] LADDER_NAMES = {
            "archer.yml",
            "axe.yml",
            "bedwars.yml",
            "boxing.yml",
            "builduhc.yml",
            "combo.yml",
            "debuff.yml",
            "gapple.yml",
            "nodebuff.yml",
            "pearlfight.yml",
            "sg.yml",
            "skywars.yml",
            "soup.yml",
            "spleef.yml",
            "sumo.yml",
            "vanilla.yml",
            "fireball.yml",
            "bridges.yml",
            "battlerush.yml"
    };

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

            for (String ladder : LADDER_NAMES) {
                saveLadder(practice, this.getVersionPath(), ladder);
            }

            try {
                FileUtils.deleteDirectory(new File(practice.getDataFolder(), this.getVersionPath().replaceAll("/", "")));
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
