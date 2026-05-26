package dev.nandi0813.practice.manager.profile;

import dev.nandi0813.practice.event.ProfileStatusChangeEvent;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.util.CustomKit;
import dev.nandi0813.practice.manager.gui.guis.customladder.PlayerCustomKitSelector;
import dev.nandi0813.practice.manager.gui.guis.profile.ProfileSettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsData;
import dev.nandi0813.practice.manager.profile.enums.ProfilePrefixVisibility;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.profile.enums.ProfileWorldTime;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.manager.profile.group.GroupManager;
import dev.nandi0813.practice.manager.profile.statistics.ProfileStat;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.actionbar.ActionBar;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
@Setter
public class Profile {

    // Identity
    private final UUID uuid;
    private final OfflinePlayer player;
    private final ProfileFile file;
    private final ProfileStat stats;
    private Group group;

    // Name display
    private Component prefix;
    private String nameTemplate;
    private NamedTextColor nameColor;
    private Component suffix;

    // Join timestamps
    private long firstJoin;
    private long lastJoin;

    // Online state
    private ProfileStatus status;
    private boolean spectatorMode;
    private boolean party;
    private boolean hideSpectators;

    // Staff state
    private boolean staffMode;
    private boolean staffChat;
    private boolean hideFromPlayers;
    private Player followTarget;

    // Player settings
    private boolean duelRequest;
    private boolean sidebar;
    private boolean hidePlayers;
    private boolean partyInvites;
    private boolean allowSpectate;
    private boolean privateMessages;
    private ProfileWorldTime worldTime = ProfileWorldTime.DAY;
    private boolean flying;
    private ProfilePrefixVisibility prefixVisibility = ProfilePrefixVisibility.PREFIX_AND_SUFFIX;

    // Custom kits
    private int allowedCustomKits;
    private final Map<NormalLadder, Map<Integer, CustomKit>> unrankedCustomKits = new HashMap<>();
    private final Map<NormalLadder, Map<Integer, CustomKit>> rankedCustomKits = new HashMap<>();

    // Daily limits
    private final List<Profile> ignoredPlayers = new ArrayList<>();
    private int unrankedLeft;
    private int rankedLeft;
    private int eventStartLeft;
    private int partyBroadcastLeft;

    // Misc
    private RankedBan rankedBan = new RankedBan();
    private ProfileSettingsGui settingsGui;
    private ActionBar actionBar;

    // Cosmetics
    private CosmeticsData cosmeticsData = new CosmeticsData();

    // Custom ladder
    private PlayerCustomKitSelector playerCustomKitSelector;
    private final List<CustomLadder> customLadders = new ArrayList<>();
    private CustomLadder selectedCustomLadder;
    private boolean fullDataLoaded;

    public Profile(UUID uuid, OfflinePlayer player) {
        this.uuid = uuid;
        // Never pin a live Player instance here; always resolve online player from UUID.
        this.player = (player instanceof Player)
                ? Bukkit.getOfflinePlayer(uuid)
                : Objects.requireNonNullElseGet(player, () -> Bukkit.getOfflinePlayer(uuid));
        this.status = ProfileStatus.OFFLINE;
        this.file = new ProfileFile(this);
        this.stats = new ProfileStat(this);
    }

    public Profile(UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getOfflinePlayer(uuid);
        this.status = ProfileStatus.OFFLINE;
        this.file = new ProfileFile(this);
        this.stats = new ProfileStat(this);
    }

    /**
     * Resolves the currently connected player for this profile by UUID.
     * Returns null when the player is offline.
     */
    public Player getOnlinePlayer() {
        return Bukkit.getPlayer(uuid);
    }

    // Data persistence

    public void saveData() {
        if (!fullDataLoaded) {
            return;
        }

        rankedBan.saveToConfig(file.getConfig(), "ranked-ban");

        saveCustomLadders();

        stats.setData(false);
        file.setData();
    }

    private void saveCustomLadders() {
        for (CustomLadder customLadder : customLadders) {
            customLadder.setData();
        }
        if (selectedCustomLadder != null) {
            file.getConfig().set("selected-custom-ladder", customLadders.indexOf(selectedCustomLadder));
        }
    }

    public void getData() {
        stats.getLadderStats().clear();
        file.getData();
        stats.getData();

        rankedBan.loadFromConfig(file.getConfig(), "ranked-ban");

        loadCustomLadders();

        fullDataLoaded = true;
    }

    private void loadCustomLadders() {
        if (!file.getConfig().isConfigurationSection("player-custom-kit")) return;

        customLadders.clear();
        for (String key : Objects.requireNonNull(
                file.getConfig().getConfigurationSection("player-custom-kit")).getKeys(false)) {
            if (!tryLoadCustomLadder(key)) break;
        }

        loadSelectedCustomLadder();
    }

    private boolean tryLoadCustomLadder(String key) {
        try {
            int i = Integer.parseInt(key);
            if (i < 0 || i > 5) return true;

            customLadders.add(new CustomLadder(this, "player-custom-kit." + i, i + 1));
            return true;
        } catch (NumberFormatException e) {
            if (file.getConfig().isConfigurationSection("player-custom-kit")) {
                CustomLadder oldFormat = new CustomLadder(this, "player-custom-kit", 1);
                customLadders.add(new CustomLadder(oldFormat, this, "player-custom-kit.0"));
                file.getConfig().set("player-custom-kit", null);
                file.saveFile();
            }
            return false;
        }
    }

    private void loadSelectedCustomLadder() {
        if (customLadders.isEmpty()) return;
        if (!file.getConfig().isInt("selected-custom-ladder")) return;

        int index = file.getConfig().getInt("selected-custom-ladder");
        if (index >= 0 && index < customLadders.size()) {
            selectedCustomLadder = customLadders.get(index);
        }
    }

    public synchronized void loadStatsOnlyData() {
        file.reloadFile();
        stats.getLadderStats().clear();

        if (file.getConfig().isLong("join.first"))
            setFirstJoin(file.getConfig().getLong("join.first"));

        if (file.getConfig().isLong("join.latest"))
            setLastJoin(file.getConfig().getLong("join.latest"));

        stats.getData();
        fullDataLoaded = false;
    }

    public synchronized void ensureFullDataLoaded() {
        if (fullDataLoaded) return;

        getData();
    }

    public synchronized void demoteToStatsOnly() {
        settingsGui = null;
        playerCustomKitSelector = null;
        selectedCustomLadder = null;
        customLadders.clear();
        unrankedCustomKits.clear();
        rankedCustomKits.clear();
        ignoredPlayers.clear();
        followTarget = null;
        actionBar = null;
        fullDataLoaded = false;
    }

    // Group management

    public void checkGroup() {
        Player online = getOnlinePlayer();
        if (online == null || !online.isOnline()) return;

        Group newGroup = GroupManager.getInstance().getGroup(online);
        if (newGroup == null) {
            Common.sendConsoleMMMessage("<red>Warning: Could not determine group for " + online.getName() + ". Assigning default (lowest weighted) group to them.");
            return;
        }

        if (group == newGroup) return;

        try {
            setGroup(newGroup);
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Failed to set group for " + online.getName() + "! Error: " + e.getMessage());
        }
    }

    public int getCustomKitPerm() {
        Player onlinePlayer = getOnlinePlayer();
        if (onlinePlayer == null) return 0;

        if (group != null) return group.getModifiableKitLimit();
        return -1;
    }

    public void setGroup(Group group) throws IllegalArgumentException {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null");
        }

        this.group = group;
        unrankedLeft = group.getUnrankedLimit();
        rankedLeft = group.getRankedLimit();
        eventStartLeft = group.getEventStartLimit();
        partyBroadcastLeft = group.getPartyBroadcastLimit();

        refreshPartyLimit();
        syncCustomLadderCount();

        // Invalidate the selector so it gets recreated on next access (lazy-loading)
        playerCustomKitSelector = null;
    }

    private void refreshPartyLimit() {
        Player onlinePlayer = getOnlinePlayer();
        if (onlinePlayer == null) return;

        Party partyObj = PartyManager.getInstance().getParty(onlinePlayer);
        if (partyObj != null && onlinePlayer.equals(partyObj.getLeader())) {
            partyObj.refreshMaxPlayerLimitForLeader();
        }
    }

    private void syncCustomLadderCount() {
        while (customLadders.size() < group.getCustomKitLimit()) {
            customLadders.add(new CustomLadder(this, "player-custom-kit." + customLadders.size(), customLadders.size() + 1));
        }
        while (customLadders.size() > group.getCustomKitLimit()) {
            customLadders.removeLast();
        }
    }

    // Lazy-loaded accessors

    public PlayerCustomKitSelector getPlayerCustomKitSelector() {
        if (playerCustomKitSelector == null) {
            playerCustomKitSelector = new PlayerCustomKitSelector(this);
        }
        return playerCustomKitSelector;
    }

    public ActionBar getActionBar() {
        if (actionBar == null) {
            actionBar = new ActionBar(this);
        }
        return actionBar;
    }

    // Setters with side effects

    public void setSelectedCustomLadder(CustomLadder customLadder) {
        if (customLadder == null)
            throw new IllegalArgumentException("Custom ladder cannot be null.");
        if (!customLadders.contains(customLadder))
            throw new IllegalArgumentException("Custom ladder not found in profile.");

        selectedCustomLadder = customLadder;
    }

    public void setStatus(ProfileStatus status) {
        ProfileStatus previous = this.status;
        this.status = status;

        Bukkit.getPluginManager().callEvent(new ProfileStatusChangeEvent(this, previous, status));

        invalidateRematchIfLeftLobby(previous, status);
    }

    private void invalidateRematchIfLeftLobby(ProfileStatus previous, ProfileStatus status) {
        boolean wasIdle = previous == ProfileStatus.LOBBY || previous == ProfileStatus.SPECTATE;
        boolean isActive = status != ProfileStatus.LOBBY && status != ProfileStatus.SPECTATE && status != ProfileStatus.OFFLINE;

        if (!wasIdle || !isActive) return;

        Player online = getOnlinePlayer();
        if (online != null && online.isOnline()) {
            MatchManager.getInstance().invalidateRematchByPlayer(online);
        }
    }

}
