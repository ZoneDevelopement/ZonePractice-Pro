package dev.nandi0813.practice.manager.server.sound;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.util.Common;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null)
            instance = new SoundManager();
        return instance;
    }

    private final Map<SoundType, SoundEffect> sounds = new HashMap<>();

    private SoundManager() {
        this.loadSounds();
    }

    private void loadSounds() {
        if (ConfigManager.getConfig().isConfigurationSection("SOUNDS")) {
            for (String soundName : ConfigManager.getConfigSectionKeys("SOUNDS")) {
                try {
                    SoundType type = SoundType.valueOf(soundName);
                    sounds.put(type, new SoundEffect(soundName));
                } catch (IllegalArgumentException e) {
                    Common.sendConsoleMMMessage("<red>Invalid sound type: " + soundName);
                }
            }
        }
    }

    public SoundEffect getSound(SoundType type) {
        return sounds.get(type);
    }

}
