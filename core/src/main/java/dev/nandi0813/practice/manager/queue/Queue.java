package dev.nandi0813.practice.manager.queue;

import dev.nandi0813.api.Event.Queue.QueueEndEvent;
import dev.nandi0813.api.Event.Queue.QueueStartEvent;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.queue.runnables.RankedSearchRunnable;
import dev.nandi0813.practice.manager.queue.runnables.SearchRunnable;
import dev.nandi0813.practice.manager.queue.runnables.UnrankedSearchRunnable;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.interfaces.Runnable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Queue extends Runnable implements dev.nandi0813.api.Interface.Queue {

    private static final int UNRANKED_MAX_QUEUE_TIME = ConfigManager.getInt("QUEUE.UNRANKED.MAX-QUEUE-TIME");
    private static final int RANKED_MAX_QUEUE_TIME = ConfigManager.getInt("QUEUE.RANKED.MAX-QUEUE-TIME");

    private final QueueManager queueManager = QueueManager.getInstance();

    @Getter
    @Setter
    private Player player;
    @Getter
    private final Profile profile;
    @Getter
    @Setter
    private NormalLadder ladder;
    @Getter
    @Setter
    private boolean ranked;
    @Getter
    @Setter
    private int range;
    @Getter
    private SearchRunnable searchRunnable;

    @Getter
    @Setter // Just for testing purposes
    private int duration;

    public Queue(Player player, NormalLadder ladder, boolean ranked) {
        super(0, 20L, false);
        this.player = player;
        this.profile = ProfileManager.getInstance().getProfile(player);
        this.ladder = ladder;
        this.ranked = ranked;
    }

    public void startQueue() {
        // Call QueueStartEvent
        QueueStartEvent queueStartEvent = new QueueStartEvent(this);
        Bukkit.getPluginManager().callEvent(queueStartEvent);
        if (queueStartEvent.isCancelled()) return;

        // Add the queue to the queueManager
        this.queueManager.getQueues().add(this);

        // Begins the duration timer
        this.begin();

        // Set the player's inventory to the match queue inventory
        InventoryManager.getInstance().setMatchQueueInventory(player);

        // Send the player a message that they have started queueing
        Common.sendMMMessage(player, LanguageManager.getString("QUEUES.QUEUE-START")
                .replace("%weightClass%", (ranked ? WeightClass.RANKED.getMMName() : WeightClass.UNRANKED.getMMName()))
                .replace("%ladder%", ladder.getDisplayName())
        );

        // Start the queue based on the queue type
        if (this.ranked)
            startRankedQueue();
        else
            startUnrankedQueue();
    }

    public void startRankedQueue() {
        this.searchRunnable = new RankedSearchRunnable(this);
        this.searchRunnable.begin();

        GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
    }

    public void startUnrankedQueue() {
        Division division = profile.getStats().getDivision();

        if (division == null || !ConfigManager.getBoolean("QUEUE.UNRANKED.DIVISION-SEARCH.ENABLED")) {
            for (Queue queue : queueManager.getQueues()) {
                if (queue == queueManager.getQueue(player)) continue;
                if (queue.getPlayer() == player) continue;
                if (queue.getLadder() != ladder) continue;
                if (queue.isRanked()) continue;

                this.cancel();
                this.startMatch(queue);
                return;
            }
        } else {
            this.searchRunnable = new UnrankedSearchRunnable(this);
            this.searchRunnable.begin();
        }

        GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();
    }

    public void startMatch(Queue queue) {
        // Check if the ladder is frozen or disabled
        if (ladder.getAvailableArenas().isEmpty()) {
            queue.endQueue(false, LanguageManager.getString("QUEUES.NO-AVAILABLE-ARENA"));
            this.endQueue(false, LanguageManager.getString("QUEUES.NO-AVAILABLE-ARENA"));
            return;
        }

        // Get an available arena
        Arena arena = LadderUtil.getAvailableArena(ladder);
        if (arena == null) {
            queue.endQueue(false, LanguageManager.getString("QUEUES.NO-AVAILABLE-ARENA"));
            this.endQueue(false, LanguageManager.getString("QUEUES.NO-AVAILABLE-ARENA"));
            return;
        }

        queue.endQueue(true, LanguageManager.getString("QUEUES.QUEUE-STOPPED")
                .replace("%weightClass%", (ranked ? WeightClass.RANKED.getMMName() : WeightClass.UNRANKED.getMMName()))
                .replace("%ladder%", ladder.getDisplayName()));

        this.endQueue(true, LanguageManager.getString("QUEUES.QUEUE-STOPPED")
                .replace("%weightClass%", (ranked ? WeightClass.RANKED.getMMName() : WeightClass.UNRANKED.getMMName()))
                .replace("%ladder%", ladder.getDisplayName()));

        Duel duel = new Duel(ladder, arena, Arrays.asList(player, queue.getPlayer()), ranked, ladder.getRounds());
        duel.startMatch();
    }

    public void endQueue(boolean foundMatch, final String message) {
        // Call QueueEndEvent
        QueueEndEvent queueEndEvent = new QueueEndEvent(this);
        Bukkit.getPluginManager().callEvent(queueEndEvent);

        // Remove the queue from the queueManager
        this.queueManager.getQueues().remove(this);

        // Cancel the timers
        if (this.searchRunnable != null) {
            this.searchRunnable.cancel();
        }

        this.cancel();

        // Set the player's inventory to the lobby inventory
        if (!foundMatch && player.isOnline()) {
            InventoryManager.getInstance().setLobbyInventory(player, false);
        }

        // Send the player a message that they have stopped queueing
        if (message != null) {
            Common.sendMMMessage(player, message);
        }

        // Update the GUIs
        if (ranked) {
            GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
        } else {
            GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();
        }
    }

    @Override
    public void run() {
        if (ladder.isFrozen() || !ladder.isEnabled()) {
            this.endQueue(false, LanguageManager.getString("QUEUES.LADDER-FROZEN").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        int maxQueueTime;
        if (ranked) {
            maxQueueTime = RANKED_MAX_QUEUE_TIME;
        } else {
            maxQueueTime = UNRANKED_MAX_QUEUE_TIME;
        }

        if (seconds >= maxQueueTime) {
            this.endQueue(false, LanguageManager.getString("QUEUES.NO-MATCH-IN-TIME").replace("%maxTime%", String.valueOf(maxQueueTime)));
            return;
        }

        seconds++;
    }

    public String getFormattedDuration() {
        return StringUtil.formatMillisecondsToMinutes(seconds * 1000L);
    }

}
