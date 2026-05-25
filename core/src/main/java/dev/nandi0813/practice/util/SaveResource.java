package dev.nandi0813.practice.util;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.nandi0813.practice.ZonePractice;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
            "vanilla.yml",
            "sword.yml"
    };

    public void saveResources(ZonePractice practice) {
        Map<String, Object> backup = backupCustomKeys(practice);

        for (String fileName : ROOT_FILES) {
            InputStream resource = practice.getResource(fileName);
            if (resource == null) {
                Common.sendConsoleMMMessage("<red>Missing bundled resource: " + fileName);
                continue;
            }
            saveResource(new File(practice.getDataFolder(), fileName), resource);
        }

        restoreCustomKeys(practice, backup);

        File ladderFolder = new File(practice.getDataFolder(), "ladders");
        if (!ladderFolder.exists()) {
            for (String fileName : LADDER_FILES) {
                practice.saveResource("ladders/" + fileName, false);
            }
        }
    }

    private Map<String, Object> backupCustomKeys(ZonePractice practice) {
        File file = new File(practice.getDataFolder(), "config.yml");
        if (!file.exists()) return Collections.emptyMap();

        YamlConfiguration disk;
        try {
            disk = new YamlConfiguration();
            disk.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            return Collections.emptyMap();
        }

        YamlConfiguration bundled;
        try (InputStream in = practice.getResource("config.yml")) {
            if (in == null) return Collections.emptyMap();
            bundled = new YamlConfiguration();
            bundled.load(new InputStreamReader(in));
        } catch (IOException | InvalidConfigurationException e) {
            return Collections.emptyMap();
        }

        Map<String, Object> backup = new HashMap<>();
        for (String key : disk.getKeys(true)) {
            if (!bundled.isSet(key)) {
                backup.put(key, disk.get(key));
            }
        }
        return backup;
    }

    private void restoreCustomKeys(ZonePractice practice, Map<String, Object> backup) {
        if (backup.isEmpty()) return;

        File file = new File(practice.getDataFolder(), "config.yml");
        if (!file.exists()) return;

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            return;
        }

        boolean changed = false;
        for (Map.Entry<String, Object> entry : backup.entrySet()) {
            if (config.isSet(entry.getKey())) continue;
            config.set(entry.getKey(), fixTimeStrings(entry.getValue()));
            changed = true;
        }

        if (!changed) return;

        try {
            config.save(file);
        } catch (IOException e) {
            Common.sendConsoleMMMessage("<red>Couldn't restore custom config keys.");
        }
    }

    private Object fixTimeStrings(Object value) {
        if (!(value instanceof List)) return value;

        List<?> list = (List<?>) value;
        List<Object> fixed = new ArrayList<>(list.size());
        for (Object el : list) {
            if (el instanceof String && ((String) el).matches("\\d{1,2}:\\d{2}")) {
                fixed.add(((String) el) + " ");
            } else {
                fixed.add(el);
            }
        }
        return fixed;
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
