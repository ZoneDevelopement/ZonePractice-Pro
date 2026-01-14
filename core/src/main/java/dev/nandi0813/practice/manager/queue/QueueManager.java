package dev.nandi0813.practice.manager.queue;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.module.util.ClassImport;
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
        int size = 0;
        for (Queue queue : queues)
            if (queue.getLadder().equals(ladder) && queue.isRanked() == ranked)
                size++;
        return size;
    }

    public int getQueueSize(final Ladder ladder) {
        int size = 0;
        for (Queue queue : queues)
            if (queue.getLadder().equals(ladder))
                size++;
        return size;
    }

    public void createUnrankedQueue(Player player, NormalLadder ladder) {
        if (ladder.isFrozen()) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.UNRANKED.LADDER-FROZEN").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        if (!ladder.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.UNRANKED.LADDER-DISABLED").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        player.closeInventory();
        Queue queue = new Queue(player, ladder, false);
        queue.startQueue();
    }

    public void createRankedQueue(Player player, NormalLadder ladder) {
        if (ladder.isFrozen()) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.RANKED.LADDER-FROZEN").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        if (!ladder.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.RANKED.LADDER-DISABLED").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        if (ClassImport.getClasses().getPlayerUtil().getPing(player) > ConfigManager.getInt("QUEUE.RANKED.MAX-PING")) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.RANKED.HIGH-PING"));
            player.closeInventory();
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile.getRankedBan().isBanned()) {
            Common.sendMMMessage(player, LanguageManager.getString("QUEUES.RANKED.BANNED")
                    .replace("%banner%", profile.getRankedBan().getBanner() == null ? "Console" : profile.getRankedBan().getBanner().getPlayer().getName())
                    .replace("%reason%", profile.getRankedBan().getReason() == null ? LanguageManager.getString("QUEUES.RANKED.NO-REASON") : profile.getRankedBan().getReason()));

            player.closeInventory();
            return;
        }

        player.closeInventory();
        Queue queue = new Queue(player, ladder, true);
        queue.startQueue();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Queue queue = this.getQueue(player);

        if (queue != null)
            queue.endQueue(false, null);
    }

}
