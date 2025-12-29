package dev.nandi0813.practice.manager.fight.event.events.ffa.oitc;

import dev.nandi0813.api.Event.Event.EventEndEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.events.ffa.interfaces.FFAEvent;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OITC extends FFAEvent {

    private static final int DEFAULT_PLAYER_LIFE = ConfigManager.getInt("EVENT.OITC.SETTINGS.PLAYER-LIFE");

    private final Map<Player, Integer> playerPoints = new HashMap<>();
    private final Map<Player, Integer> playerLives = new HashMap<>();

    public OITC(Object starter, OITCData eventData) {
        super(starter, eventData, "COMMAND.EVENT.ARGUMENTS.OITC");
    }

    @Override
    protected void customCustomStart() {
        for (Player player : this.players) {
            playerPoints.put(player, 0);
            playerLives.put(player, DEFAULT_PLAYER_LIFE);
        }
    }

    @Override
    protected void loadInventory(Player player) {
        PlayerUtil.clearPlayer(player, true, false, true);
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        player.getInventory().addItem(new ItemStack(Material.BOW));
        player.getInventory().addItem(EventManager.PLAYER_TRACKER);
        player.getInventory().setItem(8, new ItemStack(Material.ARROW));
        player.updateInventory();
    }

    @Override
    public OITCData getEventData() {
        return (OITCData) this.eventData;
    }

    @Override
    public void killPlayer(Player player, boolean teleport) {
        if (!this.players.contains(player)) {
            return;
        }

        int newLife = playerLives.get(player) - 1;

        if (playerLives.get(player) == 1) {
            players.remove(player);

            for (Player eventPlayer : players)
                this.givePoints(eventPlayer, 5);

            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.OITC.PLAYER-DIED")
                            .replaceAll("%player%", player.getName())
                            .replaceAll("%startPlayerCount%", String.valueOf(playerLives.size()))
                            .replaceAll("%playerCount%", String.valueOf(players.size()))
                    , true);

            this.addSpectator(player, null, teleport, false);
            this.checkIfEnd();
        } else {
            this.teleport(player);
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.OITC.LIFE-LEFT").replaceAll("%life%", String.valueOf(newLife)));
        }

        playerLives.replace(player, newLife);
    }

    public void givePoints(Player player, int point) {
        if (playerPoints.containsKey(player)) {
            playerPoints.replace(player, playerPoints.get(player) + point);
        }

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.OITC.POINTS-RECEIVED").replaceAll("%point%", String.valueOf(point)));
    }

    @Override
    public void endEvent() {
        if (this.status.equals(EventStatus.END)) {
            return;
        }

        EventEndEvent event = new EventEndEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        this.cancelAllRunnable();
        this.status = EventStatus.END;
        if (ZonePractice.getInstance().isEnabled()) {
            this.getEndRunnable().begin();
        } else {
            this.getEndRunnable().end();
        }

        if (winner != null) {
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.OITC.WON-EVENT")
                            .replaceAll("%winner%", winner.getName())
                            .replaceAll("%points%", String.valueOf(playerPoints.get(winner)))
                    , true);

            for (String cmd : eventData.getType().getWinnerCMD())
                ServerManager.runConsoleCommand(cmd.replaceAll("%player%", winner.getName()));
        } else
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.OITC.NO-WINNER"), true);
    }

    public Player getHighestPointPlayer() {
        Map<Player, Integer> points = getOrder(playerPoints);

        for (Player player : points.keySet())
            return player;

        return null;
    }

    public Map<Player, Integer> getOrder(Map<Player, Integer> map) {
        return PlayerUtil.sortByValue(map);
    }

}
