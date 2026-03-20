package dev.nandi0813.practice.util;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.nandi0813.practice.ZonePractice;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SaveResource {

    private static final String[] ROOT_FILES = {
            "language.yml",
            "sidebar.yml",
            "groups.yml",
            "config.yml",
            "divisions.yml",
            "guis.yml",
            "inventories.yml",
            "playerkit.yml"
    };

    private static final String[] LADDER_FILES = {
            "archer.yml",
            "axe.yml",
            "battlerush.yml",
            "bedwars.yml",
            "boxing.yml",
            "bridges.yml",
            "builduhc.yml",
            "crystal.yml",
            "debuff.yml",
            "fireball.yml",
            "gapple.yml",
            "mace.yml",
            "nodebuff.yml",
            "mlgrush.yml",
            "pearlfight.yml",
            "sg.yml",
            "skywars.yml",
            "soup.yml",
            "spear.yml",
            "spleef.yml",
            "sumo.yml",
            "tntsumo.yml",
            "vanilla.yml"
    };

    public void saveResources(ZonePractice practice) {
        for (String fileName : ROOT_FILES) {
            InputStream resource = practice.getResource(fileName);
            if (resource == null) {
                Common.sendConsoleMMMessage("<red>Missing bundled resource: " + fileName);
                continue;
            }
            saveResource(new File(practice.getDataFolder(), fileName), resource);
        }

        File ladderFolder = new File(practice.getDataFolder(), "ladders");
        if (!ladderFolder.exists()) {
            for (String fileName : LADDER_FILES) {
                practice.saveResource("ladders/" + fileName, false);
            }
        }
    }

    private void saveResource(@NotNull File document, @NotNull InputStream defaults) {
        try {
            YamlDocument.create(document, defaults,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("VERSION")).build());
        } catch (IOException e) {
            Common.sendConsoleMMMessage("<red>Couldn't load " + document.getName() + ".");
        }
    }

}
