package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.manager.backend.BackendManager;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.nandi0813.practice.manager.leaderboard.Leaderboard;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public abstract class Hologram {

    protected static final YamlConfiguration config = BackendManager.getConfig();
    private static final String NULL_STRING = ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.NULL-LINE");

    protected final String name;
    protected Location baseLocation;
    protected boolean enabled;

    @Setter
    protected HologramType hologramType;
    @Setter
    protected LbSecondaryType leaderboardType;

    @Setter
    protected HologramRunnable hologramRunnable = new HologramRunnable(this);
    @Setter
    protected int showStat;

    protected Leaderboard currentLB;

    // Track existing armor stands to prevent flickering during updates
    protected final List<ArmorStand> armorStands = new ArrayList<>();

    // Flag to prevent concurrent hologram updates
    private volatile boolean isUpdating = false;

    public Hologram(String name, Location baseLocation, HologramType hologramType) {
        this.name = name;
        this.baseLocation = baseLocation.subtract(0, 2, 0);
        this.hologramType = hologramType;
        this.showStat = 10;
        this.leaderboardType = LbSecondaryType.ELO;
    }

    public Hologram(String name, HologramType hologramType) {
        this.name = name;
        this.hologramType = hologramType;
        this.leaderboardType = LbSecondaryType.ELO;

        this.getData();

        if (!this.isReadyToEnable())
            enabled = false;
    }

    public void getData() {
        if (config.isBoolean("holograms." + name + ".enabled")) {
            this.enabled = config.getBoolean("holograms." + name + ".enabled");
        } else {
            this.enabled = false;
        }

        if (config.isString("holograms." + name + ".lb-type")) {
            this.leaderboardType = LbSecondaryType.valueOf(config.getString("holograms." + name + ".lb-type"));
        }

        if (config.isSet("holograms." + name + ".location")) {
            Object objectLocation = config.get("holograms." + name + ".location");
            if (objectLocation instanceof Location) {
                this.baseLocation = (Location) objectLocation;
            }
        }

        if (config.isInt("holograms." + name + ".showStat")) {
            this.showStat = BackendManager.getInt("holograms." + name + ".showStat");
        }

        this.getAbstractData(config);
    }

    public abstract void getAbstractData(YamlConfiguration config);

    public void setData() {
        if (name == null) return;

        config.set("holograms." + name, null);
        config.set("holograms." + name + ".enabled", enabled);
        config.set("holograms." + name + ".type", hologramType.name());
        config.set("holograms." + name + ".lb-type", leaderboardType.name());
        config.set("holograms." + name + ".showStat", showStat);

        if (baseLocation != null) {
            config.set("holograms." + name + ".location", baseLocation);
        }

        setAbstractData(config);

        BackendManager.save();
    }

    public abstract void setAbstractData(YamlConfiguration config);

    public abstract boolean isReadyToEnable();

    public abstract Leaderboard getNextLeaderboard();

    public synchronized void updateContent() {
        // Prevent concurrent updates - if already updating, skip this call
        if (isUpdating) {
            return;
        }

        isUpdating = true;

        try {
            // Safety check to prevent errors if location is invalid
            if (baseLocation == null || baseLocation.getWorld() == null) {
                setEnabled(false);
                return;
            }

            Leaderboard leaderboard = this.getNextLeaderboard();
            if (leaderboard == null) {
                setSetupHologram(SetupHologramType.SETUP);
                return;
            }

            // Skip update if leaderboard is currently being refreshed to prevent flickering
            if (leaderboard.isUpdating()) {
                return;
            }

            if (leaderboard.isEmpty()) {
                setSetupHologram(SetupHologramType.NO_DISPLAY);
                return;
            }

            this.currentLB = leaderboard;

            List<String> lines = new ArrayList<>();
            switch (leaderboard.getMainType()) {
                case GLOBAL:
                    lines = new ArrayList<>(currentLB.getSecondaryType().getGlobalLines());
                    break;
                case LADDER:
                    lines = new ArrayList<>(currentLB.getSecondaryType().getLadderLines());
                    if (currentLB.getLadder() != null) {
                        lines.replaceAll(line -> line.replace("%ladder_name%", currentLB.getLadder().getName()));
                        lines.replaceAll(line -> line.replace("%ladder_displayName%", currentLB.getLadder().getDisplayName()));
                    }
                    break;
            }
            Collections.reverse(lines);

            this.updateHologram(lines, this.getPlacementStrings());
        } finally {
            // Always reset the flag, even if an exception occurs
            isUpdating = false;
        }
    }

    /**
     * Updates hologram by modifying existing armor stands instead of recreating them.
     * This prevents flickering during leaderboard updates.
     */
    private synchronized void updateHologram(final List<String> lines, final List<String> placements) {
        // Build the complete list of text lines to display with their spacing
        List<String> allLines = new ArrayList<>();
        List<Double> allSpacings = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isEmpty()) line = " ";

            if (line.contains("%top%")) {
                // Add all placement lines with standard spacing
                for (String topString : placements) {
                    allLines.add(topString);
                    allSpacings.add(currentLB.getSecondaryType().getLineSpacing());
                }
            } else {
                allLines.add(Common.mmToNormal(line));
                // Title line (last in reversed list) gets different spacing
                if (i == lines.size() - 1) {
                    allSpacings.add(currentLB.getSecondaryType().getTitleLineSpacing());
                } else {
                    allSpacings.add(currentLB.getSecondaryType().getLineSpacing());
                }
            }
        }

        // Remove invalid armor stands (dead or from wrong world)
        armorStands.removeIf(stand -> stand == null || stand.isDead() || !stand.isValid());

        int currentSize = armorStands.size();
        int requiredSize = allLines.size();

        // If we need more armor stands, create them
        if (requiredSize > currentSize) {
            Location location = baseLocation.clone();

            // Calculate location after existing armor stands
            for (int i = 0; i < currentSize && i < allSpacings.size(); i++) {
                location.subtract(0, -allSpacings.get(i), 0);
            }

            // Create new armor stands for additional lines
            for (int i = currentSize; i < requiredSize; i++) {
                double spacing = allSpacings.get(i);
                ArmorStand stand = createArmorStand(location, spacing);
                armorStands.add(stand);
            }
        }
        // If we have too many armor stands, remove the extras
        else if (requiredSize < currentSize) {
            for (int i = currentSize - 1; i >= requiredSize; i--) {
                ArmorStand stand = armorStands.remove(i);
                if (stand != null && !stand.isDead()) {
                    stand.remove();
                }
            }
        }

        // Update the text on all armor stands
        for (int i = 0; i < allLines.size() && i < armorStands.size(); i++) {
            ArmorStand stand = armorStands.get(i);
            if (stand != null && !stand.isDead()) {
                stand.setCustomName(allLines.get(i));
            }
        }
    }

    /**
     * Clears all tracked armor stands (used when switching to setup hologram)
     */
    private synchronized void clearHologram() {
        for (ArmorStand stand : armorStands) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        armorStands.clear();
    }

    private synchronized List<String> getPlacementStrings() {
        List<OfflinePlayer> topPlayers = new ArrayList<>();
        Map<OfflinePlayer, Integer> list = currentLB.getList();

        for (OfflinePlayer player : list.keySet()) {
            if (topPlayers.size() <= showStat)
                topPlayers.add(player);
            else
                break;
        }

        List<String> placementStrings = new ArrayList<>();
        for (int i = 0; i <= (showStat - 1); i++) {
            String rankNumber = String.valueOf((showStat - i));

            if (topPlayers.size() > ((showStat - 1) - i)) {
                OfflinePlayer target = topPlayers.get((showStat - 1) - i);
                Profile targetProfile = ProfileManager.getInstance().getProfile(target);

                if (targetProfile == null) {
                    placementStrings.add(NULL_STRING.replaceAll("%number%", rankNumber));
                } else {
                    Group group = targetProfile.getGroup();
                    String statNumber = String.valueOf(list.get(target));
                    Division division = targetProfile.getStats().getDivision();

                    placementStrings.add(StringUtil.CC(leaderboardType.getFormat()
                            .replaceAll("%placement%", rankNumber)
                            .replaceAll("%score%", statNumber)
                            .replaceAll("%player%", target.getName())
                            .replaceAll("%division%", (division != null ? Common.mmToNormal(division.getFullName()) : ""))
                            .replaceAll("%division_short%", (division != null ? Common.mmToNormal(division.getShortName()) : ""))
                            .replaceAll("%group%", (group != null ? group.getDisplayName() : ""))
                    ));
                }
            } else
                placementStrings.add(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.NULL-LINE").replaceAll("%number%", rankNumber));
        }
        return placementStrings;
    }

    public synchronized void setSetupHologram(SetupHologramType setupHologram) {
        if (baseLocation == null || baseLocation.getWorld() == null) {
            return;
        }

        // Clear existing hologram armor stands first
        clearHologram();

        // Additionally clear any orphaned armor stands at this location from previous sessions
        // This prevents duplicates from crashes or improper cleanup
        baseLocation.getWorld().getNearbyEntities(baseLocation, 2, 5, 2).stream()
                .filter(entity -> entity instanceof org.bukkit.entity.ArmorStand)
                .forEach(entity -> {
                    org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) entity;
                    // Remove armor stands that look like hologram markers
                    if (stand.isCustomNameVisible() && stand.getCustomName() != null) {
                        entity.remove();
                    }
                });

        Location location = baseLocation.clone();

        switch (setupHologram) {
            case SETUP:
                ArmorStand stand1 = createArmorStand(location, 0.3);
                stand1.setCustomName(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.SETUP-HOLO.TITLE"));
                armorStands.add(stand1);

                ArmorStand stand2 = createArmorStand(location, 0.3);
                stand2.setCustomName(StringUtil.CC(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.SETUP-HOLO.LINE").replaceAll("%name%", name)));
                armorStands.add(stand2);
                break;
            case NO_DISPLAY:
                ArmorStand stand3 = createArmorStand(location, 0.3);
                stand3.setCustomName(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.NOTHING-TO-DISPLAY"));
                armorStands.add(stand3);
                break;
        }
    }

    public synchronized void deleteHologram(boolean all) {
        if (baseLocation == null || baseLocation.getWorld() == null) {
            if (all) {
                hologramRunnable.cancel(false);
                HologramManager.getInstance().getHolograms().remove(this);
                HologramSetupManager.getInstance().removeHologramGUIs(this);
                config.set("holograms." + name, null);
                BackendManager.save();
            }
            return;
        }

        Collection<Entity> entities = baseLocation.getWorld().getNearbyEntities(baseLocation, 1, 6, 1);
        for (Entity entity : entities) {
            if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                ArmorStand stand = (ArmorStand) entity;
                if (!stand.isVisible()) {
                    entity.remove();
                }
            }
        }

        // Clear the tracked armor stands list
        armorStands.clear();

        if (all) {
            hologramRunnable.cancel(false);
            HologramManager.getInstance().getHolograms().remove(this);
            HologramSetupManager.getInstance().removeHologramGUIs(this);
            config.set("holograms." + name, null);
            BackendManager.save();
        }
    }

    /**
     * Creates a new armor stand at the specified location with proper settings
     */
    private static ArmorStand createArmorStand(@NotNull Location location, double lineHeight) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location.subtract(0, -lineHeight, 0), EntityType.ARMOR_STAND);

        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);

        // Set invulnerable and non-persistent (in modern versions) to prevent issues
        ClassImport.getClasses().getArenaUtil().setArmorStandInvulnerable(stand);

        return stand;
    }

    public void setEnabled(boolean enabled) {
        if (!enabled) {
            this.hologramRunnable.cancel(true);
            this.hologramRunnable = new HologramRunnable(this);
        } else {
            this.hologramRunnable.cancel(false);
            this.hologramRunnable = new HologramRunnable(this);
            this.hologramRunnable.begin();
        }

        this.enabled = enabled;

        GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).update();
        HologramSetupManager.getInstance().getHologramSetupGUIs().get(this).get(GUIType.Hologram_Main).update();

        if (HologramSetupManager.getInstance().getHologramSetupGUIs().get(this).containsKey(GUIType.Hologram_Ladder)) {
            HologramSetupManager.getInstance().getHologramSetupGUIs().get(this).get(GUIType.Hologram_Ladder).update();
        }
    }

}
