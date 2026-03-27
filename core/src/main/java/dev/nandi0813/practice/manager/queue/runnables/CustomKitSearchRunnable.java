package dev.nandi0813.practice.manager.queue.runnables;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.queue.CustomKitQueueManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.actionbar.ActionBarPriority;
import dev.nandi0813.practice.util.interfaces.Runnable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CustomKitSearchRunnable extends Runnable {

    private static final int MAX_JOIN_SEARCH_TIME = ConfigManager.getInt("QUEUE.UNRANKED.MAX-QUEUE-TIME");
    private static final String ACTION_BAR_ID = "queue";

    @Getter
    private final UUID playerUuid;
    @Getter
    private final SearchMode mode;
    private final String hostedKitName;
    @Getter
    private final long startedAtMs;

    public CustomKitSearchRunnable(UUID playerUuid, SearchMode mode, String hostedKitName) {
        super(0, 20L, false);
        this.playerUuid = playerUuid;
        this.mode = mode;
        this.hostedKitName = hostedKitName;
        this.startedAtMs = System.currentTimeMillis();
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            CustomKitQueueManager.getInstance().getJoinSearches().remove(playerUuid);
            cancel();
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null || profile.getStatus() != ProfileStatus.QUEUE) {
            CustomKitQueueManager.getInstance().cancelJoinSearch(player, false, false);
            return;
        }

        if (mode == SearchMode.JOIN_OTHERS && CustomKitQueueManager.getInstance().tryJoinAnyHostedQueue(player)) {
            return;
        }

        if (mode == SearchMode.HOST && CustomKitQueueManager.getInstance().tryMatchHostedQueue(player)) {
            return;
        }

        if (seconds >= MAX_JOIN_SEARCH_TIME) {
            if (mode == SearchMode.HOST) {
                CustomKitQueueManager.getInstance().cancelHostedQueue(player, false, true);
            }

            CustomKitQueueManager.getInstance().cancelJoinSearch(player, false, false);
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.NO-MATCH-IN-TIME")
                    .replace("%maxTime%", String.valueOf(MAX_JOIN_SEARCH_TIME)));
            return;
        }

        String searchingActionbar = mode == SearchMode.HOST
                ? LanguageManager.getString("QUEUES.CUSTOM.HOSTING-ACTIONBAR")
                : LanguageManager.getString("QUEUES.CUSTOM.SEARCHING-ACTIONBAR");

        if (searchingActionbar.isEmpty()) {
            searchingActionbar = mode == SearchMode.HOST
                    ? "<yellow>Hosting custom kit <gold>%kit% <gray>%elapsed%"
                    : "<yellow>Searching hosted custom kits... <gray>%elapsed%";
        }

        profile.getActionBar().setMessage(ACTION_BAR_ID,
                searchingActionbar
                        .replace("%elapsed%", StringUtil.formatMillisecondsToMinutes(seconds * 1000L))
                        .replace("%kit%", hostedKitName == null ? "-" : hostedKitName),
                -1,
                ActionBarPriority.NORMAL);

        seconds++;
    }

    public long getElapsedSeconds() {
        return (System.currentTimeMillis() - startedAtMs) / 1000L;
    }

    public enum SearchMode {
        HOST,
        JOIN_OTHERS
    }
}
