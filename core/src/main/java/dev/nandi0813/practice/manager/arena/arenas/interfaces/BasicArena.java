package dev.nandi0813.practice.manager.arena.arenas.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class BasicArena {

    protected final String name;

    @Setter
    protected Location corner1;
    @Setter
    protected Location corner2;
    @Setter
    protected Cuboid cuboid;

    @Setter
    protected Location position1;
    @Setter
    protected Location position2;
    @Setter
    protected List<Location> ffaPositions = new ArrayList<>();

    @Setter
    protected boolean buildMax = false;
    @Setter
    protected int buildMaxValue = ConfigManager.getInt("MATCH-SETTINGS.BUILD-LIMIT-DEFAULT");

    @Setter
    protected boolean deadZone = false;
    @Setter
    protected int deadZoneValue = 0;

    @Setter
    protected boolean available = true;

    protected BasicArena(String name) {
        this.name = name;
    }

    public void createCuboid() {
        if (corner1 != null && corner2 != null) {
            cuboid = new Cuboid(corner1, corner2);
            ArenaManager.getInstance().getArenaCuboids().put(cuboid, this);
        }
    }

    public Location getAvailableLocation() {
        if (position1 != null) return position1;
        if (position2 != null) return position2;
        if (!ffaPositions.isEmpty()) return ffaPositions.stream().findAny().get();
        if (corner1 != null) return corner1;
        if (corner2 != null) return corner2;
        return null;
    }

    public List<Location> getStandingLocations() {
        List<Location> locations = new ArrayList<>();
        if (position1 != null) locations.add(position1);
        if (position2 != null) locations.add(position2);
        locations.addAll(ffaPositions);
        return locations;
    }

    public boolean teleport(Player player) {
        Location location = getAvailableLocation();
        if (location == null) {
            Common.sendMMMessage(player, LanguageManager.getString("ARENA.NO-AVAILABLE-LOCATION"));
            return false;
        }

        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.teleport(location);
        player.closeInventory();

        return true;
    }

    public void loadChunks() {
        if (this.cuboid != null) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () ->
                    ClassImport.getClasses().getArenaUtil().loadArenaChunks(this));

            if (ArenaManager.LOAD_CHUNKS) {
                ArenaManager.LOADED_CHUNKS.addAll(this.cuboid.getChunks());
            }
        }
    }

}
