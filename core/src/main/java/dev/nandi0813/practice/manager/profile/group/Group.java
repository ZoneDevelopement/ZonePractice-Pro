package dev.nandi0813.practice.manager.profile.group;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

@Getter
public class Group {

    private final String name;
    private final String displayName;
    private final int weight;
    private final String permission;

    private final int unrankedLimit;
    private final int rankedLimit;
    private final int eventStartLimit;

    private final int customKitLimit;
    private final int modifiableKitLimit;

    // Nametag stuff
    private final Component prefix;
    private final NamedTextColor nameColor;
    private final Component suffix;
    private final int sortPriority;

    private final String chatFormat;

    // Set up in the sidebar.yml file
    private final List<Component> sidebarExtension;

    public Group(String name, String displayName, int weight, int unrankedLimit, int rankedLimit, int eventStartLimit, int customKitLimit, int modifiableKitLimit, Component prefix, NamedTextColor nameColor, Component suffix, int sortPriority, String chatFormat, List<Component> sidebarExtension) {
        this.name = name;
        this.displayName = displayName;

        this.weight = weight;
        this.permission = "zpp.group." + name.toLowerCase();
        this.registerPermission();

        this.unrankedLimit = unrankedLimit;
        this.rankedLimit = rankedLimit;
        this.eventStartLimit = eventStartLimit;

        if (customKitLimit < 0 || customKitLimit > 5) {
            this.customKitLimit = 0;
        } else {
            this.customKitLimit = customKitLimit;
        }

        if (modifiableKitLimit < 0 || modifiableKitLimit > 4) {
            this.modifiableKitLimit = 0;
        } else {
            this.modifiableKitLimit = modifiableKitLimit;
        }

        this.prefix = prefix;
        this.nameColor = nameColor;
        this.suffix = suffix;
        this.sortPriority = sortPriority;

        this.chatFormat = chatFormat;

        this.sidebarExtension = sidebarExtension;
    }

    public void registerPermission() {
        Permission perm = new Permission(
                this.permission,
                LanguageManager.getString("PROFILE.GROUP-PERMISSION-NAME").replace("%group%", name),
                PermissionDefault.FALSE);
        Bukkit.getPluginManager().addPermission(perm);
    }

}
