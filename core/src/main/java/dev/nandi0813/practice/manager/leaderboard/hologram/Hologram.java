package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.BackendManager;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.leaderboard.Leaderboard;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * COMPLETELY REWRITTEN Hologram system with strict state management.
 *
 * Architecture:
 * - Each hologram maintains a List<HologramLine> representing individual lines
 * - Each HologramLine manages exactly ONE ArmorStand entity with UUID tracking
 * - Full despawn() clears all lines before any spawn() operation
 * - Thread-safe operations with strict main-thread enforcement
 * - State tracking prevents duplicates and overlapping entities
 */
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

    // Current leaderboard being displayed
    protected Leaderboard currentLB;

    // Track current ladder for dynamic holograms to detect switches
    protected Ladder currentLadder;

    // NEW: Strict line-based management instead of loose ArmorStand list
    protected final List<HologramLine> lines = new ArrayList<>();

    // Thread safety: Prevent concurrent updates
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);

    // State tracking: Current hologram state
    private HologramState currentState = HologramState.UNINITIALIZED;

    // ========== CONSTRUCTORS ==========

    public Hologram(String name, Location baseLocation, HologramType hologramType) {
        this.name = name;
        this.baseLocation = baseLocation.clone().subtract(0, 2, 0);
        this.hologramType = hologramType;
        this.showStat = 10;
        this.leaderboardType = LbSecondaryType.ELO;
    }

    public Hologram(String name, HologramType hologramType) {
        this.name = name;
        this.hologramType = hologramType;
        this.leaderboardType = LbSecondaryType.ELO;

        this.getData();

        if (!this.isReadyToEnable()) {
            enabled = false;
        }
    }

    // ========== DATA PERSISTENCE ==========

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

    // ========== CORE HOLOGRAM MANAGEMENT ==========

    /**
     * STRICT DESPAWN: Removes ALL hologram lines and clears state.
     * MUST be called on main thread.
     * This is the ONLY way to clear a hologram - guarantees no duplicates.
     */
    public synchronized void despawn() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), this::despawn);
            return;
        }

        // Despawn all tracked lines
        for (HologramLine line : lines) {
            line.despawn();
        }
        lines.clear();

        // Additional safety: Clear any orphaned armor stands at this location
        if (baseLocation != null && baseLocation.getWorld() != null) {
            baseLocation.getWorld().getNearbyEntities(baseLocation, 2, 6, 2).stream()
                .filter(entity -> entity.getType() == EntityType.ARMOR_STAND)
                .filter(entity -> {
                    // Remove invisible armor stands (likely hologram remnants)
                    if (entity instanceof org.bukkit.entity.ArmorStand) {
                        org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) entity;
                        return !stand.isVisible() && stand.isCustomNameVisible();
                    }
                    return false;
                })
                .forEach(Entity::remove);
        }

        currentState = HologramState.DESPAWNED;
        currentLB = null;
        currentLadder = null;
    }

    /**
     * STRICT SPAWN: Creates hologram lines from scratch.
     * MUST be called on main thread.
     * Always calls despawn() first to prevent duplicates.
     *
     * @param textLines The lines of text to display (bottom to top order)
     * @param spacings The spacing for each line
     */
    private synchronized void spawn(@NotNull List<String> textLines, @NotNull List<Double> spacings) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> spawn(textLines, spacings));
            return;
        }

        if (baseLocation == null || baseLocation.getWorld() == null) {
            return;
        }

        // STRICT: Always despawn first to prevent any duplicates
        despawn();

        if (textLines.isEmpty()) {
            return;
        }

        // Create new hologram lines
        Location currentLoc = baseLocation.clone();

        for (int i = 0; i < textLines.size(); i++) {
            String text = textLines.get(i);
            if (text.isEmpty()) {
                text = " ";
            }

            double spacing = i < spacings.size() ? spacings.get(i) : 0.3;

            // Create and spawn the line
            HologramLine line = new HologramLine();
            line.spawn(currentLoc.clone(), text);
            lines.add(line);

            // Move up for next line
            currentLoc.subtract(0, -spacing, 0);
        }
    }

    /**
     * SMART UPDATE: Updates hologram content with minimal flickering.
     * Uses diff-based approach: only modifies lines that changed.
     * If line count changes significantly or ladder switches, does full respawn.
     *
     * @param textLines The new lines of text to display
     * @param spacings The spacing for each line
     */
    private synchronized void updateSmartly(@NotNull List<String> textLines, @NotNull List<Double> spacings) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> updateSmartly(textLines, spacings));
            return;
        }

        if (baseLocation == null || baseLocation.getWorld() == null) {
            return;
        }

        int currentLineCount = lines.size();
        int newLineCount = textLines.size();

        // If line count changes significantly (>2 difference), full respawn is safer
        boolean needsFullRespawn = Math.abs(currentLineCount - newLineCount) > 2;

        // Check if any lines are invalid (entities died/unloaded)
        boolean hasInvalidLines = lines.stream().anyMatch(line -> !line.isValid());

        if (needsFullRespawn || hasInvalidLines) {
            // Full respawn needed
            spawn(textLines, spacings);
            return;
        }

        // Diff-based update: modify existing lines, add/remove as needed

        // Update existing lines
        int minCount = Math.min(currentLineCount, newLineCount);
        for (int i = 0; i < minCount; i++) {
            HologramLine line = lines.get(i);
            String newText = textLines.get(i);
            if (!line.getText().equals(newText)) {
                line.updateText(newText);
            }
        }

        // Add new lines if needed
        if (newLineCount > currentLineCount) {
            Location currentLoc = baseLocation.clone();
            // Calculate where to start adding (after existing lines)
            for (int i = 0; i < currentLineCount; i++) {
                double spacing = i < spacings.size() ? spacings.get(i) : 0.3;
                currentLoc.subtract(0, -spacing, 0);
            }

            for (int i = currentLineCount; i < newLineCount; i++) {
                String text = textLines.get(i);
                if (text.isEmpty()) text = " ";

                double spacing = i < spacings.size() ? spacings.get(i) : 0.3;

                HologramLine line = new HologramLine();
                line.spawn(currentLoc.clone(), text);
                lines.add(line);

                currentLoc.subtract(0, -spacing, 0);
            }
        }
        // Remove extra lines if needed
        else if (newLineCount < currentLineCount) {
            // CRITICAL: Remove from the END (top of hologram) to prevent bottom lines from disappearing
            for (int i = currentLineCount - 1; i >= newLineCount; i--) {
                HologramLine line = lines.remove(i);
                line.despawn();
            }
        }
    }

    // ========== UPDATE LOGIC ==========

    /**
     * Main update method called by HologramRunnable.
     * Handles leaderboard changes and updates hologram content.
     * Thread-safe with atomic locking.
     */
    public synchronized void updateContent() {
        // Prevent concurrent updates - if already updating, skip
        if (!isUpdating.compareAndSet(false, true)) {
            return;
        }

        try {
            // Safety checks
            if (baseLocation == null || baseLocation.getWorld() == null) {
                setEnabled(false);
                return;
            }

            Leaderboard leaderboard = this.getNextLeaderboard();

            // No leaderboard available - show setup mode
            if (leaderboard == null) {
                if (currentState != HologramState.SETUP_MODE) {
                    setSetupHologram(SetupHologramType.SETUP);
                }
                return;
            }

            // Skip update if leaderboard is currently refreshing
            if (leaderboard.isUpdating()) {
                return;
            }

            // Leaderboard is empty - show "nothing to display"
            if (leaderboard.isEmpty()) {
                if (currentState != HologramState.NO_DISPLAY) {
                    setSetupHologram(SetupHologramType.NO_DISPLAY);
                }
                return;
            }

            // Check if ladder switched (for dynamic holograms)
            boolean ladderSwitched = false;
            if (hologramType == HologramType.LADDER_DYNAMIC) {
                Ladder newLadder = leaderboard.getLadder();
                if (currentLadder != null && newLadder != null && !currentLadder.equals(newLadder)) {
                    ladderSwitched = true;
                }
                currentLadder = newLadder;
            }

            this.currentLB = leaderboard;

            // Build text lines
            List<String> textLines = buildTextLines(leaderboard);
            List<Double> spacings = buildSpacings(textLines, leaderboard);

            // Update strategy: full respawn if ladder switched, smart update otherwise
            if (ladderSwitched || currentState != HologramState.DISPLAYING_LEADERBOARD) {
                spawn(textLines, spacings);
                currentState = HologramState.DISPLAYING_LEADERBOARD;
            } else {
                updateSmartly(textLines, spacings);
            }

        } finally {
            isUpdating.set(false);
        }
    }

    /**
     * Builds the text lines for display from leaderboard data.
     *
     * @param leaderboard The leaderboard to display
     * @return List of text lines (bottom to top)
     */
    private List<String> buildTextLines(@NotNull Leaderboard leaderboard) {
        List<String> lines = new ArrayList<>();

        switch (leaderboard.getMainType()) {
            case GLOBAL:
                lines = new ArrayList<>(leaderboard.getSecondaryType().getGlobalLines());
                break;
            case LADDER:
                lines = new ArrayList<>(leaderboard.getSecondaryType().getLadderLines());
                if (leaderboard.getLadder() != null) {
                    final String ladderName = leaderboard.getLadder().getName();
                    final String ladderDisplayName = leaderboard.getLadder().getDisplayName();
                    lines.replaceAll(line -> line
                        .replace("%ladder_name%", ladderName)
                        .replace("%ladder_displayName%", ladderDisplayName));
                }
                break;
        }

        // Reverse to get bottom-to-top order
        Collections.reverse(lines);

        // Expand %top% placeholder with placement strings
        List<String> expandedLines = new ArrayList<>();
        List<String> placementStrings = getPlacementStrings(leaderboard);

        for (String line : lines) {
            if (line.contains("%top%")) {
                expandedLines.addAll(placementStrings);
            } else {
                expandedLines.add(Common.mmToNormal(line));
            }
        }

        return expandedLines;
    }

    /**
     * Builds spacing values for each line.
     *
     * @param textLines The text lines
     * @param leaderboard The leaderboard (for spacing config)
     * @return List of spacing values matching textLines
     */
    private List<Double> buildSpacings(@NotNull List<String> textLines, @NotNull Leaderboard leaderboard) {
        List<Double> spacings = new ArrayList<>();
        double standardSpacing = leaderboard.getSecondaryType().getLineSpacing();
        double titleSpacing = leaderboard.getSecondaryType().getTitleLineSpacing();

        for (int i = 0; i < textLines.size(); i++) {
            // Last line (title) gets different spacing
            if (i == textLines.size() - 1) {
                spacings.add(titleSpacing);
            } else {
                spacings.add(standardSpacing);
            }
        }

        return spacings;
    }

    /**
     * Generates placement strings for top players.
     *
     * @param leaderboard The leaderboard
     * @return List of formatted placement strings
     */
    private List<String> getPlacementStrings(@NotNull Leaderboard leaderboard) {
        List<OfflinePlayer> topPlayers = new ArrayList<>();
        Map<OfflinePlayer, Integer> list = leaderboard.getList();

        for (OfflinePlayer player : list.keySet()) {
            if (topPlayers.size() < showStat) {
                topPlayers.add(player);
            } else {
                break;
            }
        }

        List<String> placementStrings = new ArrayList<>();
        for (int i = 0; i < showStat; i++) {
            String rankNumber = String.valueOf(showStat - i);

            if (topPlayers.size() > (showStat - 1 - i)) {
                OfflinePlayer target = topPlayers.get(showStat - 1 - i);
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
            } else {
                placementStrings.add(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.NULL-LINE")
                    .replaceAll("%number%", rankNumber));
            }
        }
        return placementStrings;
    }

    // ========== SETUP HOLOGRAMS ==========

    /**
     * Shows a setup/placeholder hologram.
     *
     * @param type The type of setup hologram to show
     */
    public synchronized void setSetupHologram(@NotNull SetupHologramType type) {
        if (baseLocation == null || baseLocation.getWorld() == null) {
            return;
        }

        List<String> lines = new ArrayList<>();
        List<Double> spacings = new ArrayList<>();

        switch (type) {
            case SETUP:
                lines.add(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.SETUP-HOLO.TITLE"));
                lines.add(StringUtil.CC(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.SETUP-HOLO.LINE")
                    .replaceAll("%name%", name)));
                spacings.add(0.3);
                spacings.add(0.3);
                currentState = HologramState.SETUP_MODE;
                break;
            case NO_DISPLAY:
                lines.add(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.NOTHING-TO-DISPLAY"));
                spacings.add(0.3);
                currentState = HologramState.NO_DISPLAY;
                break;
        }

        spawn(lines, spacings);
    }

    // ========== DELETION ==========

    /**
     * Deletes the hologram completely.
     *
     * @param all If true, removes from manager and deletes config
     */
    public synchronized void deleteHologram(boolean all) {
        // Despawn all entities
        despawn();

        // Additional cleanup: remove any nearby invisible armor stands
        if (baseLocation != null && baseLocation.getWorld() != null) {
            baseLocation.getWorld().getNearbyEntities(baseLocation, 1, 6, 1).stream()
                .filter(entity -> entity.getType() == EntityType.ARMOR_STAND)
                .filter(entity -> {
                    if (entity instanceof org.bukkit.entity.ArmorStand) {
                        org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) entity;
                        return !stand.isVisible();
                    }
                    return false;
                })
                .forEach(Entity::remove);
        }

        if (all) {
            hologramRunnable.cancel(false);
            HologramManager.getInstance().getHolograms().remove(this);
            HologramSetupManager.getInstance().removeHologramGUIs(this);
            config.set("holograms." + name, null);
            BackendManager.save();
        }
    }

    // ========== ENABLE/DISABLE ==========

    public void setEnabled(boolean enabled) {
        if (!enabled) {
            this.enabled = false;
            hologramRunnable.cancel(true);
        } else {
            if (this.isReadyToEnable()) {
                this.enabled = true;
                hologramRunnable.begin();
            }
        }
        this.setData();

        // Update relevant GUIs
        GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).update();
        if (HologramSetupManager.getInstance().getHologramSetupGUIs().containsKey(this)) {
            HologramSetupManager.getInstance().getHologramSetupGUIs().get(this).get(GUIType.Hologram_Main).update();
            if (HologramSetupManager.getInstance().getHologramSetupGUIs().get(this).containsKey(GUIType.Hologram_Ladder)) {
                HologramSetupManager.getInstance().getHologramSetupGUIs().get(this).get(GUIType.Hologram_Ladder).update();
            }
        }
    }
}



