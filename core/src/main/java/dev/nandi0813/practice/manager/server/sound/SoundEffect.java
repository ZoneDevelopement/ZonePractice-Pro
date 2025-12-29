package dev.nandi0813.practice.manager.server.sound;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class SoundEffect {

    @Getter
    private final boolean enabled;
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundEffect(String name) {
        this.enabled = ConfigManager.getBoolean("SOUNDS." + name + ".ENABLED");
        this.sound = Sound.valueOf(ConfigManager.getString("SOUNDS." + name + ".SOUND"));
        this.volume = (float) ConfigManager.getDouble("SOUNDS." + name + ".VOLUME");
        this.pitch = (float) ConfigManager.getDouble("SOUNDS." + name + ".PITCH");
    }

    public void play(Player player) {
        if (enabled)
            player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public void play(List<Player> players) {
        if (enabled)
            players.forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
    }

    public void play(Player player, Location location) {
        if (enabled)
            player.playSound(location, sound, volume, pitch);
    }

    public void play(List<Player> players, Location location) {
        if (enabled)
            players.forEach(player -> player.playSound(location, sound, volume, pitch));
    }

}
