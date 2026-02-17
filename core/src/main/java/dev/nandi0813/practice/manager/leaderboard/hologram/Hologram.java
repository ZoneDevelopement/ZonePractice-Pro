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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Abstract base class for leaderboard holograms.
 *
 * <p>Architecture:</p>
 * <ul>
 *   <li>Each hologram maintains a List of HologramLines</li>
 *   <li>Smart updates only modify changed content (no flicker)</li>
 *   <li>Thread-safe with atomic state tracking</li>
 *   <li>Auto-recovery for externally removed armor stands</li>
 * </ul>
 */
@Getter
public abstract class Hologram {

    // Configuration
    protected static final YamlConfiguration config = BackendManager.getConfig();
    private static final String NULL_LINE_FORMAT = ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.NULL-LINE");
    private static final double DEFAULT_SPACING = 0.3;

    // Core properties
    protected final String name;
    protected Location baseLocation;
    protected boolean enabled;

    @Setter protected HologramType hologramType;
    @Setter protected LbSecondaryType leaderboardType;
    @Setter protected HologramRunnable hologramRunnable;
    @Setter protected int showStat;

    // State tracking
    protected Leaderboard currentLB;
    protected Ladder currentLadder;
    protected final List<HologramLine> lines = new ArrayList<>();
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private HologramState currentState = HologramState.UNINITIALIZED;

    // ==================== CONSTRUCTORS ====================

    protected Hologram(String name, Location baseLocation, HologramType hologramType) {
        this.name = name;
        this.baseLocation = baseLocation.clone().subtract(0, 2, 0);
        this.hologramType = hologramType;
        this.hologramRunnable = new HologramRunnable(this);
        this.showStat = 10;
        this.leaderboardType = LbSecondaryType.ELO;
    }

    protected Hologram(String name, HologramType hologramType) {
        this.name = name;
        this.hologramType = hologramType;
        this.hologramRunnable = new HologramRunnable(this);
        this.leaderboardType = LbSecondaryType.ELO;
        this.getData();

        if (!this.isReadyToEnable()) {
            enabled = false;
        }
    }

    // ==================== ABSTRACT METHODS ====================

    public abstract void getAbstractData(YamlConfiguration config);
    public abstract void setAbstractData(YamlConfiguration config);
    public abstract boolean isReadyToEnable();
    public abstract Leaderboard getNextLeaderboard();

    // ==================== DATA PERSISTENCE ====================

    public void getData() {
        enabled = config.getBoolean("holograms." + name + ".enabled", false);

        if (config.isString("holograms." + name + ".lb-type")) {
            leaderboardType = LbSecondaryType.valueOf(config.getString("holograms." + name + ".lb-type"));
        }

        if (config.isSet("holograms." + name + ".location")) {
            Object loc = config.get("holograms." + name + ".location");
            if (loc instanceof Location) {
                baseLocation = (Location) loc;
            }
        }

        showStat = config.getInt("holograms." + name + ".showStat", 10);
        getAbstractData(config);
    }

    public void setData() {
        if (name == null) return;

        String path = "holograms." + name;
        config.set(path, null);
        config.set(path + ".enabled", enabled);
        config.set(path + ".type", hologramType.name());
        config.set(path + ".lb-type", leaderboardType.name());
        config.set(path + ".showStat", showStat);

        if (baseLocation != null) {
            config.set(path + ".location", baseLocation);
        }

        setAbstractData(config);
        BackendManager.save();
    }

    // ==================== CORE MANAGEMENT ====================

    /**
     * Despawns all hologram lines and clears state.
     */
    public synchronized void despawn() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), this::despawn);
            return;
        }

        lines.forEach(HologramLine::despawn);
        lines.clear();

        currentState = HologramState.DESPAWNED;
        currentLB = null;
        currentLadder = null;
    }

    /**
     * Smart update that modifies content without flickering.
     */
    private synchronized void updateSmartly(@NotNull List<String> textLines, @NotNull List<Double> spacings) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> updateSmartly(textLines, spacings));
            return;
        }

        if (baseLocation == null || baseLocation.getWorld() == null || textLines.isEmpty()) {
            return;
        }

        List<Location> positions = calculatePositions(textLines.size(), spacings);

        if (lines.isEmpty()) {
            // First spawn - create all lines
            spawnNewLines(textLines, positions);
        } else {
            // Update existing lines
            updateExistingLines(textLines, positions);
        }
    }

    private List<Location> calculatePositions(int count, List<Double> spacings) {
        List<Location> positions = new ArrayList<>();
        Location loc = baseLocation.clone();

        for (int i = 0; i < count; i++) {
            positions.add(loc.clone());
            double spacing = i < spacings.size() ? spacings.get(i) : DEFAULT_SPACING;
            loc.add(0, spacing, 0);
        }

        return positions;
    }

    private void spawnNewLines(List<String> textLines, List<Location> positions) {
        for (int i = 0; i < textLines.size(); i++) {
            String text = textLines.get(i).isEmpty() ? " " : textLines.get(i);
            HologramLine line = new HologramLine();
            line.spawn(positions.get(i), text);
            lines.add(line);
        }
    }

    private void updateExistingLines(List<String> textLines, List<Location> positions) {
        int minCount = Math.min(lines.size(), textLines.size());

        // Update existing lines
        for (int i = 0; i < minCount; i++) {
            String text = textLines.get(i).isEmpty() ? " " : textLines.get(i);
            lines.get(i).updateText(text);
        }

        // Add new lines if needed
        if (textLines.size() > lines.size()) {
            for (int i = lines.size(); i < textLines.size(); i++) {
                String text = textLines.get(i).isEmpty() ? " " : textLines.get(i);
                HologramLine line = new HologramLine();
                line.spawn(positions.get(i), text);
                lines.add(line);
            }
        }
        // Remove extra lines if needed
        else if (textLines.size() < lines.size()) {
            for (int i = lines.size() - 1; i >= textLines.size(); i--) {
                lines.remove(i).despawn();
            }
        }
    }

    // ==================== UPDATE LOGIC ====================

    /**
     * Main update method - handles leaderboard changes and content updates.
     */
    public synchronized void updateContent() {
        if (!isUpdating.compareAndSet(false, true)) {
            return;
        }

        try {
            if (baseLocation == null || baseLocation.getWorld() == null) {
                return;
            }

            Leaderboard leaderboard = getNextLeaderboard();

            if (leaderboard == null) {
                handleNoLeaderboard();
                return;
            }

            if (leaderboard.isEmpty()) {
                handleEmptyLeaderboard();
                return;
            }

            if (hologramType == HologramType.LADDER_DYNAMIC) {
                currentLadder = leaderboard.getLadder();
            }

            currentLB = leaderboard;
            List<String> textLines = buildTextLines(leaderboard);
            List<Double> spacings = buildSpacings(textLines.size(), leaderboard);

            updateSmartly(textLines, spacings);
            currentState = HologramState.DISPLAYING_LEADERBOARD;

        } finally {
            isUpdating.set(false);
        }
    }

    private void handleNoLeaderboard() {
        if (lines.isEmpty() && currentState != HologramState.SETUP_MODE) {
            setSetupHologram(SetupHologramType.SETUP);
        }
    }

    private void handleEmptyLeaderboard() {
        if (lines.isEmpty() && currentState != HologramState.NO_DISPLAY) {
            setSetupHologram(SetupHologramType.NO_DISPLAY);
        }
    }

    // ==================== TEXT BUILDING ====================

    private List<String> buildTextLines(@NotNull Leaderboard leaderboard) {
        List<String> configLines = getConfigLines(leaderboard);

        if (configLines.isEmpty()) {
            return List.of("§cNo config lines");
        }

        Collections.reverse(configLines);

        List<String> expandedLines = new ArrayList<>();
        List<String> placementStrings = buildPlacementStrings(leaderboard);

        for (String line : configLines) {
            if (line.contains("%top%")) {
                expandedLines.addAll(placementStrings);
            } else {
                expandedLines.add(Common.mmToNormal(line));
            }
        }

        return expandedLines.isEmpty() ? List.of("§cNo data") : expandedLines;
    }

    private List<String> getConfigLines(Leaderboard leaderboard) {
        List<String> lines = switch (leaderboard.getMainType()) {
            case GLOBAL -> new ArrayList<>(leaderboard.getSecondaryType().getGlobalLines());
            case LADDER -> {
                List<String> ladderLines = new ArrayList<>(leaderboard.getSecondaryType().getLadderLines());
                if (leaderboard.getLadder() != null) {
                    String ladderName = leaderboard.getLadder().getName();
                    String displayName = leaderboard.getLadder().getDisplayName();
                    ladderLines.replaceAll(line -> line
                            .replace("%ladder_name%", ladderName)
                            .replace("%ladder_displayName%", displayName));
                }
                yield ladderLines;
            }
        };
        return lines;
    }

    private List<Double> buildSpacings(int lineCount, Leaderboard leaderboard) {
        double standardSpacing = leaderboard.getSecondaryType().getLineSpacing();
        double titleSpacing = leaderboard.getSecondaryType().getTitleLineSpacing();

        return IntStream.range(0, lineCount)
                .mapToObj(i -> i == lineCount - 1 ? titleSpacing : standardSpacing)
                .collect(Collectors.toList());
    }

    private List<String> buildPlacementStrings(Leaderboard leaderboard) {
        Map<OfflinePlayer, Integer> playerStats = leaderboard.getList();
        List<OfflinePlayer> topPlayers = playerStats.keySet().stream()
                .limit(showStat)
                .collect(Collectors.toList());

        List<String> placements = new ArrayList<>();

        for (int i = 0; i < showStat; i++) {
            int rank = showStat - i;
            int playerIndex = showStat - 1 - i;

            if (playerIndex < topPlayers.size()) {
                placements.add(formatPlayerEntry(topPlayers.get(playerIndex), playerStats, rank));
            } else {
                placements.add(NULL_LINE_FORMAT.replace("%number%", String.valueOf(rank)));
            }
        }

        return placements;
    }

    private String formatPlayerEntry(OfflinePlayer player, Map<OfflinePlayer, Integer> stats, int rank) {
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile == null) {
            return NULL_LINE_FORMAT.replace("%number%", String.valueOf(rank));
        }

        Group group = profile.getGroup();
        Division division = profile.getStats().getDivision();

        return StringUtil.CC(leaderboardType.getFormat()
                .replace("%placement%", String.valueOf(rank))
                .replace("%score%", String.valueOf(stats.get(player)))
                .replace("%player%", player.getName())
                .replace("%division%", division != null ? Common.mmToNormal(division.getFullName()) : "")
                .replace("%division_short%", division != null ? Common.mmToNormal(division.getShortName()) : "")
                .replace("%group%", group != null ? group.getDisplayName() : ""));
    }

    // ==================== SETUP HOLOGRAMS ====================

    /**
     * Shows a setup/placeholder hologram.
     */
    public synchronized void setSetupHologram(@NotNull SetupHologramType type) {
        if (baseLocation == null || baseLocation.getWorld() == null) {
            return;
        }

        // Don't replace existing content
        if (!lines.isEmpty()) {
            currentState = type == SetupHologramType.SETUP ? HologramState.SETUP_MODE : HologramState.NO_DISPLAY;
            return;
        }

        List<String> setupLines = switch (type) {
            case SETUP -> {
                currentState = HologramState.SETUP_MODE;
                yield List.of(
                        ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.SETUP-HOLO.TITLE"),
                        StringUtil.CC(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.SETUP-HOLO.LINE")
                                .replace("%name%", name))
                );
            }
            case NO_DISPLAY -> {
                currentState = HologramState.NO_DISPLAY;
                yield List.of(ConfigManager.getString("LEADERBOARD.HOLOGRAM.FORMAT.NOTHING-TO-DISPLAY"));
            }
        };

        List<Double> spacings = setupLines.stream().map(l -> DEFAULT_SPACING).collect(Collectors.toList());
        updateSmartly(setupLines, spacings);
    }

    // ==================== DELETION ====================

    /**
     * Deletes the hologram completely.
     */
    public synchronized void deleteHologram(boolean removeFromManager) {
        despawn();

        if (removeFromManager) {
            hologramRunnable.cancel(false);
            HologramManager.getInstance().getHolograms().remove(this);
            HologramSetupManager.getInstance().removeHologramGUIs(this);
            config.set("holograms." + name, null);
            BackendManager.save();
        }
    }

    // ==================== ENABLE/DISABLE ====================

    public void setEnabled(boolean enabled) {
        if (enabled && isReadyToEnable()) {
            this.enabled = true;
            hologramRunnable.begin();
        } else {
            this.enabled = false;
            hologramRunnable.cancel(true);
        }

        setData();
        updateGUIs();
    }

    private void updateGUIs() {
        GUIManager.getInstance().searchGUI(GUIType.Hologram_Summary).update();

        var setupGUIs = HologramSetupManager.getInstance().getHologramSetupGUIs();
        if (setupGUIs.containsKey(this)) {
            setupGUIs.get(this).get(GUIType.Hologram_Main).update();
            if (setupGUIs.get(this).containsKey(GUIType.Hologram_Ladder)) {
                setupGUIs.get(this).get(GUIType.Hologram_Ladder).update();
            }
        }
    }
}
