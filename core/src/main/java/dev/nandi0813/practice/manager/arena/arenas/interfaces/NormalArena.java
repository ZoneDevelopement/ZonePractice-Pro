package dev.nandi0813.practice.manager.arena.arenas.interfaces;

import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.arena.util.BedLocation;
import dev.nandi0813.practice.manager.arena.util.PortalLocation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class NormalArena extends BasicArena implements DisplayName {

    // Not used yet
    protected boolean portalProtection = false;
    protected int portalProtectionValue;

    // In bed related arenas
    protected BedLocation bedLoc1;
    protected BedLocation bedLoc2;

    // Portal related arenas
    protected PortalLocation portalLoc1;
    protected PortalLocation portalLoc2;

    protected NormalArena(String name) {
        super(name);
    }

    @Override
    public Location getAvailableLocation() {
        if (position1 != null) return position1;
        if (position2 != null) return position2;
        if (bedLoc1 != null) return bedLoc1.getLocation();
        if (bedLoc2 != null) return bedLoc2.getLocation();
        if (portalLoc1 != null) return portalLoc1.getCenter();
        if (portalLoc2 != null) return portalLoc2.getCenter();
        if (corner1 != null) return corner1;
        if (corner2 != null) return corner2;
        if (!ffaPositions.isEmpty()) return ffaPositions.stream().findAny().get();
        return null;
    }

    public void setBasicData(YamlConfiguration config, String path) {
        if (corner1 != null)
            config.set(path + "corner.1", corner1);
        else
            config.set(path + "corner.1", null);

        if (corner2 != null)
            config.set(path + "corner.2", corner2);
        else
            config.set(path + "corner.2", null);

        if (position1 != null)
            config.set(path + "position.1", position1);
        else
            config.set(path + "position.1", null);

        if (position2 != null)
            config.set(path + "position.2", position2);
        else
            config.set(path + "position.2", null);

        if (!ffaPositions.isEmpty())
            config.set(path + "ffa-positions", ffaPositions);
        else
            config.set(path + "ffa-positions", null);

        if (isBuildMax())
            config.set(path + "buildmax", buildMaxValue);
        else
            config.set(path + "buildmax", null);

        if (isDeadZone())
            config.set(path + "deadzone", deadZoneValue);
        else
            config.set(path + "deadzone", null);

        if (isPortalProtection())
            config.set(path + "portalprot", portalProtectionValue);
        else
            config.set(path + "portalprot", null);

        if (bedLoc1 != null)
            ArenaUtil.saveBedData(config, path + "bedlocation.1", bedLoc1);
        else
            config.set(path + "bedlocation.1", null);

        if (bedLoc2 != null)
            ArenaUtil.saveBedData(config, path + "bedlocation.2", bedLoc2);
        else
            config.set(path + "bedlocation.2", null);

        if (portalLoc1 != null)
            config.set(path + "portallocation.1", portalLoc1.getCenter());
        else
            config.set(path + "portallocation.1", null);

        if (portalLoc2 != null)
            config.set(path + "portallocation.2", portalLoc2.getCenter());
        else
            config.set(path + "portallocation.2", null);
    }

    public void getBasicData(YamlConfiguration config, String path) {
        if (config.get(path + "corner.1") != null) corner1 = (Location) config.get(path + "corner.1");
        if (config.get(path + "corner.2") != null) corner2 = (Location) config.get(path + "corner.2");
        createCuboid();

        if (config.get(path + "position.1") != null) position1 = (Location) config.get(path + "position.1");
        if (config.get(path + "position.2") != null) position2 = (Location) config.get(path + "position.2");

        if (config.get(path + "ffa-positions") != null)
            ffaPositions = (List<Location>) config.getList(path + "ffa-positions");

        if (config.isInt(path + "buildmax")) {
            setBuildMax(true);
            setBuildMaxValue(config.getInt(path + "buildmax"));
        }

        if (config.isInt(path + "deadzone")) {
            setDeadZone(true);
            setDeadZoneValue(config.getInt(path + "deadzone"));
        }

        if (config.isInt(path + "portalprot")) {
            setPortalProtection(true);
            setPortalProtectionValue(config.getInt(path + "portalprot"));
        }

        if (config.get(path + "bedlocation.1") != null)
            setBedLoc1(ArenaUtil.getBedData(config, path + "bedlocation.1"));
        if (config.get(path + "bedlocation.2") != null)
            setBedLoc2(ArenaUtil.getBedData(config, path + "bedlocation.2"));

        if (config.get(path + "portallocation.1") != null)
            portalLoc1 = new PortalLocation((Location) config.get(path + "portallocation.1"));
        if (config.get(path + "portallocation.2") != null)
            portalLoc2 = new PortalLocation((Location) config.get(path + "portallocation.2"));
    }

    public List<PortalLocation> getPortalLocations() {
        final List<PortalLocation> portalLocations = new ArrayList<>();
        portalLocations.add(portalLoc1);
        portalLocations.add(portalLoc2);
        return portalLocations;
    }

}
