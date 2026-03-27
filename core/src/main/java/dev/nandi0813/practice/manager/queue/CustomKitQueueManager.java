package dev.nandi0813.practice.manager.queue;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.event.ProfileStatusChangeEvent;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.queue.runnables.CustomKitSearchRunnable;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class CustomKitQueueManager implements Listener {

    private static final String ACTION_BAR_ID = "queue";

    private static CustomKitQueueManager instance;

    public static CustomKitQueueManager getInstance() {
        if (instance == null) {
            instance = new CustomKitQueueManager();
        }
        return instance;
    }

    private CustomKitQueueManager() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    @Getter
    private final Map<UUID, HostedCustomKitQueue> hostedQueues = new LinkedHashMap<>();
    @Getter
    private final Map<UUID, CustomKitSearchRunnable> joinSearches = new LinkedHashMap<>();

    public boolean isCustomKitQueueEnabled() {
        Object enabledValue = ConfigManager.get("QUEUE.UNRANKED.CUSTOM-KIT.ENABLED");
        if (enabledValue == null) {
            enabledValue = ConfigManager.get("QUEUE.UNRANKED.CUSTOM-KIT-QUEUE-ENABLED");
        }

        // Keep feature enabled by default to avoid breaking old configs that don't have the new key yet.
        if (enabledValue == null) {
            return true;
        }

        return Boolean.parseBoolean(String.valueOf(enabledValue));
    }

    public List<CustomLadder> getHostableKits(Profile profile) {
        List<CustomLadder> hostable = new ArrayList<>();
        if (profile == null) {
            return hostable;
        }

        for (CustomLadder customLadder : profile.getCustomLadders()) {
            if (customLadder != null && customLadder.isEnabled()) {
                hostable.add(customLadder);
            }
        }

        return hostable;
    }

    public HostedCustomKitQueue getHostedQueue(Player host) {
        if (host == null) {
            return null;
        }

        return hostedQueues.get(host.getUniqueId());
    }

    public CustomKitSearchRunnable getJoinSearch(Player joiner) {
        if (joiner == null) {
            return null;
        }

        return joinSearches.get(joiner.getUniqueId());
    }

    public boolean hostQueue(Player host, CustomLadder customLadder) {
        if (host == null || customLadder == null) {
            return false;
        }

        if (!isCustomKitQueueEnabled()) {
            Common.sendMMMessage(host, LanguageManager.getString("CANT-USE-COMMAND"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(host);
        if (profile == null || profile.getStatus() != ProfileStatus.LOBBY) {
            Common.sendMMMessage(host, LanguageManager.getString("CANT-USE-COMMAND"));
            return false;
        }

        if (!customLadder.isEnabled()) {
            Common.sendMMMessage(host, LanguageManager.getString("QUEUES.CUSTOM.KIT-NOT-READY"));
            return false;
        }

        if (customLadder.getAvailableArenas().isEmpty()) {
            Common.sendMMMessage(host, LanguageManager.getString("QUEUES.NO-AVAILABLE-ARENA"));
            return false;
        }

        hostedQueues.put(host.getUniqueId(), new HostedCustomKitQueue(host.getUniqueId(), customLadder, System.currentTimeMillis()));

        InventoryManager.getInstance().setMatchQueueInventory(host);

        CustomKitSearchRunnable hostSearch = joinSearches.get(host.getUniqueId());
        if (hostSearch != null) {
            hostSearch.cancel();
        }

        hostSearch = new CustomKitSearchRunnable(host.getUniqueId(), CustomKitSearchRunnable.SearchMode.HOST, customLadder.getDisplayName());
        joinSearches.put(host.getUniqueId(), hostSearch);
        hostSearch.begin();

        Common.sendMMMessage(host, LanguageManager.getString("QUEUES.CUSTOM.HOSTED").replace("%kit%", customLadder.getDisplayName()));

        tryMatchHostedQueue(host);
        return true;
    }

    public boolean startJoinSearch(Player joiner) {
        if (joiner == null) {
            return false;
        }

        if (!isCustomKitQueueEnabled()) {
            Common.sendMMMessage(joiner, LanguageManager.getString("CANT-USE-COMMAND"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(joiner);
        if (profile == null || profile.getStatus() != ProfileStatus.LOBBY) {
            Common.sendMMMessage(joiner, LanguageManager.getString("CANT-USE-COMMAND"));
            return false;
        }

        if (joinSearches.containsKey(joiner.getUniqueId())) {
            return true;
        }

        InventoryManager.getInstance().setMatchQueueInventory(joiner);

        CustomKitSearchRunnable search = new CustomKitSearchRunnable(joiner.getUniqueId(), CustomKitSearchRunnable.SearchMode.JOIN_OTHERS, null);
        joinSearches.put(joiner.getUniqueId(), search);
        search.begin();

        Common.sendMMMessage(joiner, LanguageManager.getString("QUEUES.CUSTOM.WAITING-FOR-HOST"));
        return true;
    }

    public boolean cancelHostedQueue(Player host, boolean sendMessage, boolean setLobbyInventory) {
        if (host == null) {
            return false;
        }

        HostedCustomKitQueue removed = hostedQueues.remove(host.getUniqueId());
        if (removed == null) {
            return false;
        }

        if (host.isOnline()) {
            cancelJoinSearch(host, false, false);

            if (setLobbyInventory) {
                InventoryManager.getInstance().setLobbyInventory(host, false);
            }

            clearQueueActionBar(host);

            if (sendMessage) {
                Common.sendMMMessage(host, LanguageManager.getString("QUEUES.CUSTOM.CANCELLED"));
            }
        }

        return true;
    }

    public boolean cancelJoinSearch(Player joiner, boolean sendMessage, boolean setLobbyInventory) {
        if (joiner == null) {
            return false;
        }

        CustomKitSearchRunnable search = joinSearches.remove(joiner.getUniqueId());
        if (search == null) {
            return false;
        }

        search.cancel();

        if (joiner.isOnline()) {
            if (setLobbyInventory) {
                InventoryManager.getInstance().setLobbyInventory(joiner, false);
            }

            clearQueueActionBar(joiner);

            if (sendMessage) {
                Common.sendMMMessage(joiner, LanguageManager.getString("QUEUES.CUSTOM.SEARCH-CANCELLED"));
            }
        }

        return true;
    }

    public boolean tryMatchHostedQueue(Player host) {
        HostedCustomKitQueue hostedQueue = hostedQueues.get(host.getUniqueId());
        if (hostedQueue == null) {
            return false;
        }

        for (CustomKitSearchRunnable search : new ArrayList<>(joinSearches.values())) {
            if (search.getMode() != CustomKitSearchRunnable.SearchMode.JOIN_OTHERS) {
                continue;
            }

            Player joiner = Bukkit.getPlayer(search.getPlayerUuid());
            if (joiner == null || !joiner.isOnline()) {
                continue;
            }

            Profile joinerProfile = ProfileManager.getInstance().getProfile(joiner);
            if (joinerProfile == null || joinerProfile.getStatus() != ProfileStatus.QUEUE) {
                cancelJoinSearch(joiner, false, false);
                continue;
            }

            if (startHostedMatch(host, joiner, hostedQueue)) {
                return true;
            }
        }

        return false;
    }

    public boolean tryJoinAnyHostedQueue(Player joiner) {
        for (HostedCustomKitQueue hostedQueue : new ArrayList<>(hostedQueues.values())) {
            Player host = Bukkit.getPlayer(hostedQueue.hostUuid());
            if (!isJoinable(host, joiner, hostedQueue)) {
                continue;
            }

            if (startHostedMatch(host, joiner, hostedQueue)) {
                return true;
            }
        }

        return false;
    }

    private boolean startHostedMatch(Player host, Player joiner, HostedCustomKitQueue selectedQueue) {
        if (selectedQueue == null || host == null || joiner == null) {
            return false;
        }

        CustomLadder hostedKit = selectedQueue.customLadder();
        Arena arena = LadderUtil.getAvailableArena(hostedKit);
        if (arena == null) {
            hostedQueues.remove(host.getUniqueId());
            Common.sendMMMessage(host, LanguageManager.getString("QUEUES.NO-AVAILABLE-ARENA"));
            InventoryManager.getInstance().setLobbyInventory(host, false);
            return false;
        }

        hostedQueues.remove(host.getUniqueId());
        cancelJoinSearch(host, false, false);
        cancelJoinSearch(joiner, false, false);

        clearQueueActionBar(host);
        clearQueueActionBar(joiner);

        host.closeInventory();
        joiner.closeInventory();

        Common.sendMMMessage(host, LanguageManager.getString("QUEUES.CUSTOM.MATCH-FOUND").replace("%player%", joiner.getName()));
        Common.sendMMMessage(joiner, LanguageManager.getString("QUEUES.CUSTOM.JOINED").replace("%player%", host.getName()).replace("%kit%", hostedKit.getDisplayName()));

        Duel duel = new Duel(hostedKit, arena, List.of(host, joiner), false, hostedKit.getRounds());
        duel.startMatch();
        return true;
    }

    private boolean isJoinable(Player host, Player joiner, HostedCustomKitQueue hostedQueue) {
        if (host == null || !host.isOnline()) {
            if (hostedQueue != null) {
                hostedQueues.remove(hostedQueue.hostUuid());
            }
            return false;
        }

        if (host.equals(joiner)) {
            return false;
        }

        Profile hostProfile = ProfileManager.getInstance().getProfile(host);
        if (hostProfile == null || hostProfile.getStatus() != ProfileStatus.QUEUE) {
            hostedQueues.remove(hostedQueue.hostUuid());
            return false;
        }

        if (hostedQueue.customLadder() == null || !hostedQueue.customLadder().isEnabled()) {
            hostedQueues.remove(hostedQueue.hostUuid());
            InventoryManager.getInstance().setLobbyInventory(host, false);
            Common.sendMMMessage(host, LanguageManager.getString("QUEUES.CUSTOM.KIT-NOT-READY"));
            return false;
        }

        return true;
    }

    private void clearQueueActionBar(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile != null) {
            profile.getActionBar().removeMessage(ACTION_BAR_ID);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        cancelHostedQueue(e.getPlayer(), false, false);
        cancelJoinSearch(e.getPlayer(), false, false);
    }

    @EventHandler
    public void onProfileStatusChange(ProfileStatusChangeEvent e) {
        if (e.getOldStatus() != ProfileStatus.QUEUE) {
            return;
        }

        if (e.getNewStatus() == ProfileStatus.QUEUE) {
            return;
        }

        Player player = e.getProfile().getPlayer().getPlayer();
        if (player == null) {
            return;
        }

        cancelHostedQueue(player, false, false);
        cancelJoinSearch(player, false, false);
    }

    public record HostedCustomKitQueue(UUID hostUuid, CustomLadder customLadder, long startedAtMs) {


        public long getElapsedSeconds() {
            return (System.currentTimeMillis() - startedAtMs) / 1000L;
        }
    }
}

