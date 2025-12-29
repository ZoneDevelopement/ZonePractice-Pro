package dev.nandi0813.practice.manager.fight.event.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.util.Cuboid;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class EventData extends ConfigFile {

    private static final ZonePractice practice = ZonePractice.getInstance();

    protected EventType type;
    protected boolean enabled;
    protected GUIItem icon;

    protected Cuboid cuboid;
    protected Location cuboidLoc1;
    protected Location cuboidLoc2;

    protected final List<Location> spawns;

    protected int broadcastInterval;
    protected int waitBeforeStart;
    protected int maxQueueTime;
    protected int duration;
    protected int startTime;
    protected int minPlayer;
    protected int maxPlayer;

    public EventData(final EventType type) {
        super("/events/", type.getName().toLowerCase());
        this.type = type;
        this.spawns = new ArrayList<>();

        this.enabled = false;
        this.icon = new GUIItem(type.getIcon());
        this.broadcastInterval = type.getBroadcastInterval();
        this.waitBeforeStart = type.getWaitBeforeStart();
        this.maxQueueTime = type.getMaxQueueTime();
        this.duration = type.getDuration();
        this.startTime = type.getStartTime();
        this.minPlayer = type.getMinPlayer();
        this.maxPlayer = type.getMaxPlayer();
    }

    @Override
    public void setData() {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                config.set("enabled", enabled);
                config.set("icon", icon.get());
                config.set("settings.broadcastInterval", broadcastInterval);
                config.set("settings.waitBeforeStart", waitBeforeStart);
                config.set("settings.maxQueueTime", maxQueueTime);
                config.set("settings.duration", duration);
                config.set("settings.startTime", startTime);
                config.set("settings.minPlayer", minPlayer);

                if (cuboidLoc1 != null)
                    config.set("cuboid.1", cuboidLoc1);
                if (cuboidLoc2 != null)
                    config.set("cuboid.2", cuboidLoc2);

                if (!spawns.isEmpty()) {
                    config.set("spawns", spawns);
                }

                setCustomData();

                saveFile();
            }
        };

        if (practice.isEnabled())
            task.runTaskAsynchronously(practice);
        else
            task.run();
    }

    protected abstract void setCustomData();

    @Override
    public void getData() {
        if (config.isItemStack("icon"))
            this.icon = new GUIItem(config.getItemStack("icon"));

        if (config.isInt("settings.broadcastInterval"))
            this.broadcastInterval = config.getInt("settings.broadcastInterval");

        if (config.isInt("settings.waitBeforeStart"))
            this.waitBeforeStart = config.getInt("settings.waitBeforeStart");

        if (config.isInt("settings.maxQueueTime"))
            this.maxQueueTime = config.getInt("settings.maxQueueTime");

        if (config.isInt("settings.duration"))
            this.duration = config.getInt("settings.duration");

        if (config.isInt("settings.startTime"))
            this.startTime = config.getInt("settings.startTime");

        if (config.isInt("settings.minPlayer"))
            this.minPlayer = config.getInt("settings.minPlayer");

        if (config.isInt("settings.maxPlayer"))
            this.maxPlayer = config.getInt("settings.maxPlayer");

        if (config.isSet("cuboid.1"))
            this.setCuboidLoc1((Location) config.get("cuboid.1"));
        if (config.isSet("cuboid.2"))
            this.setCuboidLoc2((Location) config.get("cuboid.2"));

        if (config.isList("spawns")) {
            for (Object obj : config.getList("spawns")) {
                if (obj instanceof Location) {
                    addSpawn((Location) obj);
                }
            }
        }

        if (config.isSet("enabled")) {
            this.enabled = config.getBoolean("enabled");
        }

        this.getCustomData();
    }

    protected abstract void getCustomData();

    public void addSpawn(final Location spawn) {
        if (enabled) {
            throw new IllegalStateException("Cannot add spawn while event is enabled.");
        }

        if (cuboid == null) {
            throw new IllegalStateException(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SPAWN-POSITION.NO-REGION"));
        }

        if (!cuboid.contains(spawn)) {
            throw new IllegalStateException("Spawn point not in region.");
        }

        for (Location loc : this.spawns) {
            if (loc.getBlock().equals(spawn.getBlock())) {
                throw new IllegalStateException("Spawn point already exists.");
            }
        }

        spawns.add(spawn);
    }

    public void removeSpawn(final Location spawn) throws IllegalStateException, IOException {
        if (enabled) {
            throw new IllegalStateException("Cannot remove spawn while event is enabled.");
        }

        for (Location loc : this.spawns) {
            if (loc.getBlock().equals(spawn.getBlock())) {
                spawns.remove(loc);
                return;
            }
        }

        throw new IOException("Spawn not found.");
    }

    public void clearSpawn() throws IllegalStateException {
        if (enabled) {
            throw new IllegalStateException("Cannot clear spawns while event is enabled.");
        }

        spawns.clear();
    }

    public void setIcon(final GUIItem icon) throws IllegalStateException {
        if (enabled) {
            throw new IllegalStateException("Cannot change icon while event is enabled");
        }

        if (icon.getMaterial().equals(Material.AIR)) {
            throw new IllegalStateException("Icon cannot be air.");
        }

        if (icon.getName() == null || icon.getName().isEmpty()) {
            throw new IllegalStateException("Icon name cannot be empty.");
        }

        this.icon = icon;
    }

    public GUIItem getIcon() {
        return icon.cloneItem();
    }

    public void setCuboidLoc1(final Location cuboidLoc1) throws IllegalStateException {
        if (enabled) {
            throw new IllegalStateException("Cannot change cuboid while event is enabled");
        }

        this.cuboidLoc1 = cuboidLoc1;
        createCuboid();
    }

    public void setCuboidLoc2(final Location cuboidLoc2) {
        if (enabled) {
            throw new IllegalStateException("Cannot change cuboid while event is enabled");
        }

        this.cuboidLoc2 = cuboidLoc2;
        createCuboid();
    }

    private void createCuboid() {
        if (cuboidLoc1 != null && cuboidLoc2 != null) {
            this.cuboid = new Cuboid(cuboidLoc1, cuboidLoc2);

            spawns.removeIf(spawn -> !cuboid.contains(spawn));
        }
    }

    public void setEnabled(final boolean enabled) throws IOException {
        if (this.enabled && !enabled) {
            if (EventManager.getInstance().isEventLive(type)) {
                throw new IOException("Cannot disable event while it is live.");
            }
        } else if (!this.enabled && enabled) {
            if (cuboid == null) {
                throw new IOException("Cuboid not set.");
            } else if (spawns.isEmpty()) {
                throw new IOException("No spawns set.");
            } else if (icon == null) {
                throw new IOException("Icon not set.");
            } else {
                enable();
            }
        } else {
            throw new IOException("Event already " + (enabled ? "enabled" : "disabled" + "."));
        }

        this.enabled = enabled;
    }

    protected abstract void enable() throws IOException;

    public Location getAvailableLocation() {
        if (!spawns.isEmpty()) return spawns.get(0);
        if (cuboidLoc1 != null) return cuboidLoc1;
        if (cuboidLoc2 != null) return cuboidLoc2;

        return null;
    }

}
