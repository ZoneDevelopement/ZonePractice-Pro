package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.duel.DuelManager;
import dev.nandi0813.practice.manager.duel.DuelRequest;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.inventory.inventories.LobbyInventory;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RematchRequest {

    @Getter
    private final List<Player> players = new ArrayList<>();
    @Getter
    private final Ladder ladder;
    private final int rounds;

    private boolean isRequested = false;

    public RematchRequest(Match match) {
        this.players.addAll(match.getPlayers());
        this.ladder = match.getLadder();
        this.rounds = match.getWinsNeeded();

        setInventories();
        startRunnable();
    }

    public void sendRematchRequest(Player sender) {
        Player target = getOtherPlayer(sender);
        if (!target.isOnline()) {
            Common.sendMMMessage(sender, LanguageManager.getString("MATCH.REMATCH-REQUEST.TARGET-OFFLINE"));
            return;
        }

        Profile targetProfile = ProfileManager.getInstance().getProfile(target);

        if (isRequested) {
            Common.sendMMMessage(sender, LanguageManager.getString("MATCH.REMATCH-REQUEST.ALREADY-SENT"));
            return;
        }

        if ((targetProfile.getStatus().equals(ProfileStatus.LOBBY) || targetProfile.getStatus().equals(ProfileStatus.EDITOR) || targetProfile.getStatus().equals(ProfileStatus.SPECTATE)) && !targetProfile.isParty()) {
            if (!targetProfile.isDuelRequest()) {
                Common.sendMMMessage(sender, LanguageManager.getString("MATCH.REMATCH-REQUEST.TARGET-DONT-ACCEPT").replace("%target%", target.getName()));
                return;
            }

            DuelRequest request = new DuelRequest(sender, target, ladder, null, rounds);
            DuelManager.getInstance().sendRequest(request);

            isRequested = true;
        } else
            Common.sendMMMessage(sender, LanguageManager.getString("MATCH.REMATCH-REQUEST.CANT-SEND-ANYMORE").replace("%target%", target.getName()));
    }

    public Player getOtherPlayer(Player player) {
        for (Player player1 : players)
            if (player1 != player)
                return player1;
        return null;
    }

    public void setInventories() {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
        {
            for (Player player : this.players) {
                if (!player.isOnline()) return;

                Inventory inventory = InventoryManager.getInstance().getPlayerInventory(player);
                if (inventory instanceof LobbyInventory lobbyInventory) {
                    lobbyInventory.addRematchItem(player);
                }
            }
        }, 5L);
    }

    public void startRunnable() {
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                {
                    MatchManager.getInstance().getRematches().remove(this);

                    for (Player player : players) {
                        if (!player.isOnline()) return;

                        Profile profile = ProfileManager.getInstance().getProfile(player);
                        if (!profile.getStatus().equals(ProfileStatus.LOBBY)) return;

                        Location lobby = ServerManager.getLobby();
                        if (lobby != null && lobby.getWorld() != null && !player.getLocation().getWorld().equals(lobby.getWorld())) {
                            return;
                        }

                        InventoryManager.getInstance().setLobbyInventory(player, false);
                    }
                },
                ConfigManager.getInt("MATCH-SETTINGS.REMATCH.EXPIRE-TIME") * 20L);
    }

}
