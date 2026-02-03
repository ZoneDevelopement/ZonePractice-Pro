package dev.nandi0813.practice.manager.profile.group;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.sidebar.SidebarManager;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class GroupManager extends ConfigFile {

    private static GroupManager instance;

    public static GroupManager getInstance() {
        if (instance == null)
            instance = new GroupManager();
        return instance;
    }

    private List<Group> groups = new ArrayList<>();

    private GroupManager() {
        super("", "groups");
        loadGroups();
    }

    public void loadGroups() {
        for (String groupName : this.config.getConfigurationSection("GROUPS").getKeys(false)) {
            String chatFormat = null;
            if (this.config.isSet("GROUPS." + groupName + ".CHAT-FORMAT"))
                chatFormat = this.getString("GROUPS." + groupName + ".CHAT-FORMAT");

            List<Component> sidebarExtension = new ArrayList<>();
            if (SidebarManager.getInstance().isList("GROUP-EXTENSIONS." + groupName)) {
                for (String line : SidebarManager.getInstance().getList("GROUP-EXTENSIONS." + groupName)) {
                    sidebarExtension.add(ZonePractice.getMiniMessage().deserialize(line));
                }
            }

            Group group = new Group(
                    groupName,
                    this.getString("GROUPS." + groupName + ".NAME"),
                    this.getInt("GROUPS." + groupName + ".WEIGHT"),
                    this.getInt("GROUPS." + groupName + ".UNRANKED-PER-DAY"),
                    this.getInt("GROUPS." + groupName + ".RANKED-PER-DAY"),
                    this.getInt("GROUPS." + groupName + ".EVENT-START-PER-DAY"),
                    this.getInt("GROUPS." + groupName + ".CUSTOM-KIT"),
                    this.getInt("GROUPS." + groupName + ".MODIFIABLE-KIT-PER-LADDER"),
                    ZonePractice.getMiniMessage().deserialize(this.getString("GROUPS." + groupName + ".LOBBY-NAMETAG.PREFIX")),
                    NamedTextColor.NAMES.valueOr(this.getString("GROUPS." + groupName + ".LOBBY-NAMETAG.NAME-COLOR").toLowerCase(), NamedTextColor.WHITE),
                    ZonePractice.getMiniMessage().deserialize(this.getString("GROUPS." + groupName + ".LOBBY-NAMETAG.SUFFIX")),
                    this.getInt("GROUPS." + groupName + ".LOBBY-NAMETAG.SORT-PRIORITY"),
                    chatFormat,
                    sidebarExtension);

            groups.add(group);
        }

        List<Group> sortedList = new ArrayList<>(groups);
        sortedList.sort(Comparator.comparing(Group::getWeight));
        groups = sortedList;
    }

    public Group getGroup(String name) {
        for (Group group : groups)
            if (group.getName().equalsIgnoreCase(name))
                return group;
        return null;
    }

    public Group getGroup(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile.getPlayer().isOp()) {
            try {
                profile.setGroup(groups.get(groups.size() - 1));
            } catch (Exception e) {
                Common.sendConsoleMMMessage("<red>Failed to set group for " + profile.getPlayer().getName() + "! Error: " + e.getMessage());
            }
            return null;
        }

        Group currentGroup = profile.getGroup();

        // If player is not OP and has a current group, validate they still have permission for it
        if (currentGroup != null && currentGroup.getPermission() != null && !player.hasPermission(currentGroup.getPermission())) {
            currentGroup = null;
        }

        // Find the highest weight group the player has permission for
        for (Group group : groups) {
            if (currentGroup != null && currentGroup.getWeight() > group.getWeight()) continue;
            if (group.getPermission() == null) continue;

            if (player.hasPermission(group.getPermission()))
                currentGroup = group;
        }

        // If player has no permissions for any group, assign them to the lowest weighted group (default)
        if (currentGroup == null && !groups.isEmpty()) {
            currentGroup = groups.get(0); // First group in sorted list = lowest weight
        }

        return currentGroup;
    }

    @Override
    public void setData() {
    }

    @Override
    public void getData() {
    }
}
