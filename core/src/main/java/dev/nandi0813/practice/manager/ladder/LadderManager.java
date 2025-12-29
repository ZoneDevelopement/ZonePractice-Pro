package dev.nandi0813.practice.manager.ladder;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.guis.customladder.EditorMenuGui;
import dev.nandi0813.practice.manager.gui.guis.customladder.premadecustom.CustomLadderSelectorGui;
import dev.nandi0813.practice.manager.gui.guis.queue.RankedGui;
import dev.nandi0813.practice.manager.gui.guis.queue.UnrankedGui;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StartUpCallback;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LadderManager {

    private static LadderManager instance;

    public static LadderManager getInstance() {
        if (instance == null)
            instance = new LadderManager();
        return instance;
    }

    private LadderManager() {
    }

    @Getter
    private final List<NormalLadder> ladders = new ArrayList<>();
    private final File folder = new File(ZonePractice.getInstance().getDataFolder() + "/ladders");

    @Getter
    private static final int DEFAULT_ELO = ConfigManager.getInt("QUEUE.RANKED.DEFAULT-ELO");

    public NormalLadder getLadder(String ladderName) {
        for (NormalLadder ladder : ladders)
            if (ladder.getName().equalsIgnoreCase(ladderName))
                return ladder;
        return null;
    }

    public List<NormalLadder> getEnabledLadders() {
        List<NormalLadder> list = new ArrayList<>();
        for (NormalLadder ladder : ladders)
            if (ladder.isEnabled())
                list.add(ladder);
        return list;
    }

    public void loadLadders(final StartUpCallback startUpCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () -> {
            if (folder.isDirectory() && folder.listFiles() != null) {
                for (File ladderFile : Objects.requireNonNull(folder.listFiles())) {
                    if (ladderFile.isFile() && ladderFile.getName().endsWith(".yml")) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(ladderFile);

                        if (config.isString("type")) {
                            String name = config.getString("name");

                            try {
                                LadderType type = LadderType.valueOf(config.getString("type"));

                                ladders.add((NormalLadder) type.getClassInstance().getConstructor(String.class, LadderType.class).newInstance(name, type));
                            } catch (IllegalArgumentException e) {
                                Common.sendConsoleMMMessage("<red>Invalid ladder format for ladder " + name + ".");
                            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                                     IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), startUpCallback::onLoadingDone);
        });
    }

    public void loadGUIs() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () -> {
            LadderSetupManager.getInstance().loadGUIs();
            GUIManager.getInstance().addGUI(new UnrankedGui());
            GUIManager.getInstance().addGUI(new RankedGui());
            GUIManager.getInstance().addGUI(new CustomLadderSelectorGui());
            GUIManager.getInstance().addGUI(new EditorMenuGui());
        });
    }

    public void saveLadders() {
        for (NormalLadder ladder : ladders)
            ladder.setData();
    }

}
