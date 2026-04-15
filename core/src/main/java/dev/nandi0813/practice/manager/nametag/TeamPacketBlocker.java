package dev.nandi0813.practice.manager.nametag;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class TeamPacketBlocker {

    private static TeamPacketBlocker instance;

    @Getter
    private boolean tabPluginPresent = false;

    @Getter
    private boolean tabScoreboardTeamsEnabled = false;

    @Getter
    private boolean nametagSystemDisabled = false;

    @Getter
    private TabIntegration tabIntegration = null;

    private TeamPacketBlocker() {
    }

    public static TeamPacketBlocker getInstance() {
        if (instance == null) {
            instance = new TeamPacketBlocker();
        }
        return instance;
    }

    public void register() {
        Plugin tabPlugin = Bukkit.getPluginManager().getPlugin("TAB");
        tabPluginPresent = tabPlugin != null && tabPlugin.isEnabled();

        if (!tabPluginPresent) {
            return;
        }

        tabScoreboardTeamsEnabled = false;
        nametagSystemDisabled = false;

        try {
            tabIntegration = new TabIntegration();
        } catch (Throwable ignored) {
            tabIntegration = null;
        }
    }

    public void unregister() {
        tabIntegration = null;
        tabPluginPresent = false;
        tabScoreboardTeamsEnabled = false;
        nametagSystemDisabled = false;
    }

    @SuppressWarnings ( "unused" )
    public void registerOurTeam(String teamName) {
    }

    @SuppressWarnings ( "unused" )
    public void unregisterOurTeam(String teamName) {
    }

    @SuppressWarnings ( "unused" )
    public boolean isOurTeam(String teamName) {
        return false;
    }

}
