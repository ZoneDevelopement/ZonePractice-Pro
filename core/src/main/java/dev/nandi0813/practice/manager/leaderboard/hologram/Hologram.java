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

    public Hologram(String name, Location baseLocation, HologramType hologramType) {
        this.name = name;
        this.baseLocation = baseLocation.subtract(0, 2, 0);
        this.hologramType = hologramType;
        this.showStat = 10;
        this.leaderboardType = LbSecondaryType.ELO;
    }

    // Loading the hologram from the config.
    public Hologram(String name, HologramType hologramType) {
        this.name = name;
        this.hologramType = hologramType;
        this.leaderboardType = LbSecondaryType.ELO;

        this.getData();

        if (!this.isReadyToEnable())
            enabled = false;

        if (enabled)
            hologramRunnable.begin();
        else
            setSetupHologram(SetupHologramType.SETUP);
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

    public void updateContent() {
        deleteHologram(false);

        Leaderboard leaderboard = this.getNextLeaderboard();
        if (leaderboard == null) {
            setSetupHologram(SetupHologramType.SETUP);
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

        this.spawnHologram(lines, this.getPlacementStrings());
    }

    private synchronized void spawnHologram(final List<String> lines, final List<String> placements) {
        Location location = baseLocation.clone();

        for (String line : lines) {
            if (line.isEmpty()) line = " ";

            if (line.contains("%top%")) {
                for (String topString : placements) {
                    ArmorStand stand = getHologram(location, currentLB.getSecondaryType().getLineSpacing());
                    stand.setCustomName(topString);
                }
            } else {
                ArmorStand stand;
                if (lines.indexOf(line) == lines.size() - 1) {
                    stand = getHologram(location, currentLB.getSecondaryType().getTitleLineSpacing());
                } else {
                    stand = getHologram(location, currentLB.getSecondaryType().getLineSpacing());
                }

                stand.setCustomName(Common.mmToNormal(line));
            }
        }
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
                    placementStrings.add(NULL_STRING.replace("%number%", rankNumber));
                } else {
                    Group group = targetProfile.getGroup();
                    String statNumber = String.valueOf(list.get(target));
                    Division division = targetProfile.getStats().getDivision();

                    placementStrings.add(StringUtil.CC(leaderboardType.getFormat()
                            .replace("%placement%", rankNumber)
                            .replace("%score%", statNumber)
                            .replace("%player%", target.getName())
                            .replace("%division%", (division != null ? Common.mmToNormal(division.getFullName()) : ""))
                            .replace("%division_short%", (division != null ? Common.mmToNormal(division.getShortName()) : ""))
                            .replace("%group%", (group != null ? group.getDisplayName() : ""))
                    ));
                }
            } else
                placementStrings.add(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.NULL-LINE").replace("%number%", rankNumber));
        }
        return placementStrings;
    }

    public void setSetupHologram(SetupHologramType setupHologram) {
        Location location = baseLocation.clone();

        switch (setupHologram) {
            case SETUP:
                ArmorStand stand1 = getHologram(location, 0.3);
                stand1.setCustomName(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.SETUP-HOLO.TITLE"));

                ArmorStand stand2 = getHologram(location, 0.3);
                stand2.setCustomName(StringUtil.CC(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.SETUP-HOLO.LINE").replace("%name%", name)));
                break;
            case NO_DISPLAY:
                ArmorStand stand3 = getHologram(location, 0.3);
                stand3.setCustomName(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.NOTHING-TO-DISPLAY"));
                break;
        }
    }

    public synchronized void deleteHologram(boolean all) {
        Collection<Entity> entities = baseLocation.getWorld().getNearbyEntities(baseLocation, 0, 6, 0);
        for (Entity entity : entities) {
            if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                entity.remove();
            }
        }

        if (all) {
            hologramRunnable.cancel(false);
            HologramManager.getInstance().getHolograms().remove(this);
            HologramSetupManager.getInstance().removeHologramGUIs(this);
            config.set("holograms." + name, null);
            BackendManager.save();
        }
    }

    private static ArmorStand getHologram(@NotNull Location location, double lineHeight) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location.subtract(0, -lineHeight, 0), EntityType.ARMOR_STAND);

        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);

        return stand;
    }

    public void setEnabled(boolean enabled) {
        if (!enabled) {
            this.hologramRunnable.cancel(true);
            this.hologramRunnable = new HologramRunnable(this);
            this.deleteHologram(false);
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
