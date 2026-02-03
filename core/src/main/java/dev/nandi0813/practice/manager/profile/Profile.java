package dev.nandi0813.practice.manager.profile;

import dev.nandi0813.practice.manager.fight.match.util.CustomKit;
import dev.nandi0813.practice.manager.gui.guis.customladder.PlayerCustomKitSelector;
import dev.nandi0813.practice.manager.gui.guis.profile.ProfileSettingsGui;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.profile.enums.ProfileWorldTime;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.manager.profile.group.GroupManager;
import dev.nandi0813.practice.manager.profile.statistics.ProfileStat;
import dev.nandi0813.practice.module.interfaces.actionbar.ActionBar;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
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

    private final UUID uuid;
    private final OfflinePlayer player;
    private final ProfileFile file;
    private final ProfileStat stats;
    private Group group;

    private Component prefix;
    private NamedTextColor nameColor;
    private Component suffix;

    private long firstJoin;
    private long lastJoin;

    private ProfileStatus status;
    private boolean spectatorMode;
    private boolean party;
    private boolean hideSpectators;

    private boolean staffMode;
    private boolean staffChat;
    private boolean hideFromPlayers;
    private Player followTarget;

    private boolean duelRequest;
    private boolean sidebar;
    private boolean hidePlayers;
    private boolean partyInvites;
    private boolean allowSpectate;
    private boolean privateMessages;
    private ProfileWorldTime worldTime;
    private boolean flying;

    private int allowedCustomKits;
    private final Map<NormalLadder, Map<Integer, CustomKit>> unrankedCustomKits = new HashMap<>();
    private final Map<NormalLadder, Map<Integer, CustomKit>> rankedCustomKits = new HashMap<>();

    // Unranked & Ranked & Event left daily
    private final List<Profile> ignoredPlayers = new ArrayList<>();
    private int unrankedLeft = 0;
    private int rankedLeft = 0;
    private int eventStartLeft = 0;

    private RankedBan rankedBan = new RankedBan();
    private ProfileSettingsGui settingsGui;
    private ActionBar actionBar = ClassImport.createActionBarClass(this);

    // Custom ladder
    private PlayerCustomKitSelector playerCustomKitSelector;
    private final List<CustomLadder> customLadders = new ArrayList<>();
    private CustomLadder selectedCustomLadder;

    public Profile(UUID uuid, OfflinePlayer player) {
        this.uuid = uuid;
        this.player = player;
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

    public void saveData() {
        this.rankedBan.set(file.getConfig(), "ranked-ban");

        for (CustomLadder customLadder : customLadders) {
            customLadder.setData();
        }
        if (this.selectedCustomLadder != null) {
            this.file.getConfig().set("selected-custom-ladder", customLadders.indexOf(this.selectedCustomLadder));
        }

        stats.setData(false);
        file.setData();
    }

    public void getData() {
        file.getData();
        stats.getData();

        this.rankedBan.get(file.getConfig(), "ranked-ban");

        if (this.file.getConfig().isConfigurationSection("player-custom-kit")) {
            this.customLadders.clear();
            for (String ladder : this.file.getConfig().getConfigurationSection("player-custom-kit").getKeys(false)) {
                try {
                    int i = Integer.parseInt(ladder);
                    if (i < 0 || i > 5) {
                        continue;
                    }

                    this.customLadders.add(new CustomLadder(this, "player-custom-kit." + i, i + 1));
                } catch (NumberFormatException e) {
                    if (this.file.getConfig().isConfigurationSection("player-custom-kit")) {
                        CustomLadder oldLadderFormat = new CustomLadder(this, "player-custom-kit", 1);
                        this.customLadders.add(new CustomLadder(oldLadderFormat, this, "player-custom-kit.0"));

                        this.file.getConfig().set("player-custom-kit", null);
                        this.file.saveFile();
                    }
                    break;
                }
            }

            if (!this.customLadders.isEmpty()) {
                if (this.file.getConfig().isInt("selected-custom-ladder")) {
                    int index = this.file.getConfig().getInt("selected-custom-ladder");
                    if (index < this.customLadders.size() && index >= 0) {
                        this.selectedCustomLadder = this.customLadders.get(index);
                    }
                }
            }
        }
    }

    public void checkGroup() {
        Player online = Bukkit.getPlayer(uuid);
        if (online == null || !online.isOnline()) return;

        Group newGroup = GroupManager.getInstance().getGroup(online);

        // If newGroup is null (shouldn't happen with our fix, but safety check)
        // or if the group has changed, update it
        if (newGroup == null) {
            Common.sendConsoleMMMessage("<red>Warning: Could not determine group for " + online.getName());
            return;
        }

        if (group == newGroup) return;

        try {
            this.setGroup(newGroup);
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Failed to set group for " + online.getName() + "! Error: " + e.getMessage());
        }
    }

    public int getCustomKitPerm() {
        Player onlinePlayer = player.getPlayer();

        if (onlinePlayer == null) {
            return 0;
        }

        if (this.group != null) {
            return this.group.getModifiableKitLimit();
        }

        return -1;
    }

    public void setGroup(Group group) throws IllegalArgumentException {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null");
        }

        this.group = group;
        this.unrankedLeft = group.getUnrankedLimit();
        this.rankedLeft = group.getRankedLimit();
        this.eventStartLeft = group.getEventStartLimit();

        while (this.customLadders.size() < this.group.getCustomKitLimit()) {
            this.customLadders.add(new CustomLadder(this, "player-custom-kit." + customLadders.size(), this.customLadders.size() + 1));
        }

        while (this.customLadders.size() > this.group.getCustomKitLimit()) {
            this.customLadders.remove(this.customLadders.size() - 1);
        }

        this.playerCustomKitSelector = new PlayerCustomKitSelector(this);
    }

    public void setSelectedCustomLadder(CustomLadder customLadder) {
        if (customLadder == null) {
            throw new IllegalArgumentException("Custom ladder cannot be null.");
        }

        if (!customLadders.contains(customLadder)) {
            throw new IllegalArgumentException("Custom ladder not found in profile.");
        }

        this.selectedCustomLadder = customLadder;
    }

}
