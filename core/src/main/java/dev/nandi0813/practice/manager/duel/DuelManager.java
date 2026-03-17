package dev.nandi0813.practice.manager.duel;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class DuelManager {

    private static DuelManager instance;

    public static DuelManager getInstance() {
        if (instance == null)
            instance = new DuelManager();
        return instance;
    }

    private DuelManager() {
    }

    private final Map<Player, Player> pendingRequestTarget = new HashMap<>();
    private final Map<Player, List<DuelRequest>> requests = new HashMap<>();

    public boolean isRequested(Player sender, Player target) {
        if (requests.containsKey(target))
            for (DuelRequest request : requests.get(target))
                if (request.getSender().equals(sender))
                    return true;
        return false;
    }

    public void sendRequest(DuelRequest request) {
        List<DuelRequest> requests;
        Player sender = request.getSender();
        Player target = request.getTarget();

        if (getRequests().containsKey(target))
            requests = new ArrayList<>(getRequests().get(target));
        else
            requests = new ArrayList<>();

        requests.removeIf(oldRequest -> oldRequest.getSender().equals(request.getSender()));

        requests.add(request);
        getRequests().put(target, requests);

        sender.closeInventory();

        request.sendRequest();

        new BukkitRunnable() {
            @Override
            public void run() {
                List<DuelRequest> targetRequests = getRequests().get(target);
                if (targetRequests == null) {
                    return;
                }

                boolean removed = targetRequests.remove(request);
                if (targetRequests.isEmpty()) {
                    getRequests().remove(target);
                }

                if (removed) {
                    request.handleExpiry();
                }
            }
        }.runTaskLater(ZonePractice.getInstance(), 20L * ConfigManager.getInt("MATCH-SETTINGS.DUEL.INVITATION-EXPIRY"));
    }

}
