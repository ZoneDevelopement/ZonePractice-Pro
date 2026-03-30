package dev.nandi0813.practice.manager.queue;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class QueueManager implements Listener {

    private static final String MULTI_QUEUE_CONFIG_PATH = "QUEUE.MULTI";
    private static final String MULTI_QUEUE_PERMISSION_DEFAULT = "zpp.queue.multi";

    private static QueueManager instance;

    public static QueueManager getInstance() {
        if (instance == null)
            instance = new QueueManager();
        return instance;
    }

    private final List<Queue> queues = Collections.synchronizedList(new ArrayList<>());

    private QueueManager() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    public Queue getQueue(final Player queuePlayer) {
        for (Queue queue : queues)
            if (queue.getPlayer() == queuePlayer) return queue;
        return null;
    }

    public int getQueueSize(final Ladder ladder, final boolean ranked) {
        if (!(ladder instanceof NormalLadder normalLadder)) {
            return 0;
        }

        int size = 0;
        for (Queue queue : queues)
            if (queue.isRanked() == ranked && queue.isQueued(normalLadder))
                size++;
        return size;
    }

    public int getQueueSize(final Ladder ladder) {
        if (!(ladder instanceof NormalLadder normalLadder)) {
            return 0;
        }

        int size = 0;
        for (Queue queue : queues)
            if (queue.isQueued(normalLadder))
                size++;
        return size;
    }

    public void createUnrankedQueue(Player player, NormalLadder ladder) {
        createUnrankedQueue(player, ladder, false);
    }

    public void createUnrankedQueue(Player player, NormalLadder ladder, boolean keepInventoryOpen) {
        if (ladder.isFrozen()) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.UNRANKED.LADDER-FROZEN").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        if (!ladder.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.UNRANKED.LADDER-DISABLED").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        toggleQueue(player, ladder, false, keepInventoryOpen);
    }

    public void createRankedQueue(Player player, NormalLadder ladder) {
        createRankedQueue(player, ladder, false);
    }

    public void createRankedQueue(Player player, NormalLadder ladder, boolean keepInventoryOpen) {
        if (ladder.isFrozen()) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.RANKED.LADDER-FROZEN").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        if (!ladder.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.RANKED.LADDER-DISABLED").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        if (PlayerUtil.getPing(player) > ConfigManager.getInt("QUEUE.RANKED.MAX-PING")) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.RANKED.HIGH-PING"));
            if (!keepInventoryOpen) {
                player.closeInventory();
            }
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!player.hasPermission("zpp.bypass.ranked.requirements")) {
            Division requirement = DivisionManager.getInstance().getMinimumForRanked();
            if (requirement != null && !DivisionManager.getInstance().meetsMinimumForRanked(profile)) {
                sendRankedProgressMessage(player, profile, requirement);

                if (!keepInventoryOpen) {
                    player.closeInventory();
                }
                return;
            }
        }

        if (profile.getRankedLeft() <= 0 && !player.hasPermission("zpp.bypass.ranked.limit")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.RANKED.NO-RANKED-LEFT"));
            if (!keepInventoryOpen) {
                player.closeInventory();
            }
            return;
        }

        if (profile.getRankedBan().isBanned()) {
            String bannerName = "Console";
            if (profile.getRankedBan().getBanner() != null && profile.getRankedBan().getBanner().getPlayer() != null) {
                bannerName = profile.getRankedBan().getBanner().getPlayer().getName();
            }

            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.RANKED.BANNED")
                    .replace("%banner%", bannerName != null ? bannerName : "")
                    .replace("%reason%", profile.getRankedBan().getReason() == null ? LanguageManager.getString("QUEUES.RANKED.NO-REASON") : profile.getRankedBan().getReason()));

            if (!keepInventoryOpen) {
                player.closeInventory();
            }
            return;
        }

        toggleQueue(player, ladder, true, keepInventoryOpen);
    }

    public void endAllQueuesForPlayer(Player player, boolean foundMatch, String message) {
        Queue queue = getQueue(player);
        if (queue != null) {
            queue.endQueue(foundMatch, message);
        }
    }

    public boolean isMultiQueueAllowed(Player player) {
        if (!ConfigManager.getBoolean(MULTI_QUEUE_CONFIG_PATH + ".ENABLED")) {
            return false;
        }

        String permission = getMultiQueuePermissionNode();
        return player.hasPermission(permission);
    }

    private String getMultiQueuePermissionNode() {
        String path = MULTI_QUEUE_CONFIG_PATH + ".PERMISSION";
        String permission = ConfigManager.getString(path);
        return permission.isEmpty() ? MULTI_QUEUE_PERMISSION_DEFAULT : permission;
    }

    private int getMultiQueueLimit() {
        return ConfigManager.getInt(MULTI_QUEUE_CONFIG_PATH + ".MAX-LADDERS", 3);
    }

    private void toggleQueue(Player player, NormalLadder ladder, boolean ranked, boolean keepInventoryOpen) {
        Queue existingQueue = getQueue(player);
        boolean allowMultiQueue = isMultiQueueAllowed(player);
        boolean preserveOpenInventory = keepInventoryOpen && allowMultiQueue;

        if (existingQueue != null && existingQueue.isRanked() != ranked) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.MULTI.CANT-MIX-TYPES"));
            return;
        }

        if (existingQueue == null) {
            if (!preserveOpenInventory) {
                player.closeInventory();
            }

            Queue queue = new Queue(player, ladder, ranked, preserveOpenInventory);
            queue.startQueue();
            return;
        }

        if (!allowMultiQueue) {
            if (existingQueue.isQueued(ladder)) {
                endAllQueuesForPlayer(player, false, LanguageManager.getString("QUEUES.QUEUE-STOPPED")
                        .replace("%weightClass%", ranked ? WeightClass.RANKED.getMMName() : WeightClass.UNRANKED.getMMName())
                        .replace("%ladder%", ladder.getDisplayName()));
                return;
            }

            endAllQueuesForPlayer(player, false, null);

            player.closeInventory();

            Queue queue = new Queue(player, ladder, ranked, false);
            queue.startQueue();
            return;
        }

        if (existingQueue.isQueued(ladder)) {
            existingQueue.removeLadder(ladder);

            if (existingQueue.getQueuedLadders().isEmpty()) {
                endAllQueuesForPlayer(player, false, LanguageManager.getString("QUEUES.QUEUE-STOPPED")
                        .replace("%weightClass%", ranked ? WeightClass.RANKED.getMMName() : WeightClass.UNRANKED.getMMName())
                        .replace("%ladder%", ladder.getDisplayName()));
            } else {
                Common.sendMMMessage(player, LanguageManager.getString("QUEUES.QUEUE-STOPPED")
                        .replace("%weightClass%", ranked ? WeightClass.RANKED.getMMName() : WeightClass.UNRANKED.getMMName())
                        .replace("%ladder%", ladder.getDisplayName()));
            }
            return;
        }

        int limit = getMultiQueueLimit();
        if (limit > 0 && existingQueue.getQueuedLadders().size() >= limit) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.MULTI.MAX-LADDERS").replace("%max%", String.valueOf(limit)));
            return;
        }

        existingQueue.addLadder(ladder);
        Common.sendMMMessage(player, LanguageManager.getString("QUEUES.QUEUE-START")
                .replace("%weightClass%", ranked ? WeightClass.RANKED.getMMName() : WeightClass.UNRANKED.getMMName())
                .replace("%ladder%", ladder.getDisplayName()));
    }

    private void sendRankedProgressMessage(Player player, Profile profile, Division requirement) {
        int currentExp = profile.getStats().getExperience();
        int requiredExp = requirement.getExperience();
        int currentWins = profile.getStats().getGlobalWins();
        int requiredWins = requirement.getWin();

        Common.sendMMMessage(player, "");
        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.RANKED.PROGRESS-HEADER"));

        // Experience progress
        int expProgress = Math.min(100, (int) ((double) currentExp / requiredExp * 100));
        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.RANKED.PROGRESS-EXP")
                .replace("%current%", String.valueOf(currentExp))
                .replace("%required%", String.valueOf(requiredExp))
                .replace("%percent%", String.valueOf(expProgress))
        );

        // Wins progress (if wins are counted)
        if (DivisionManager.getInstance().isCOUNT_BY_WINS()) {
            int winsProgress = Math.min(100, (int) ((double) currentWins / requiredWins * 100));
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.RANKED.PROGRESS-WINS")
                    .replace("%current%", String.valueOf(currentWins))
                    .replace("%required%", String.valueOf(requiredWins))
                    .replace("%percent%", String.valueOf(winsProgress))
            );
        }

        Common.sendMMMessage(player, "");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        endAllQueuesForPlayer(player, false, null);
    }

}
