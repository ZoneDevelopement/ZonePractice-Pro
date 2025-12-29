package dev.nandi0813.practice.manager.fight.event.events.onevsall.juggernaut;

import dev.nandi0813.api.Event.Event.EventEndEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.events.ffa.interfaces.FFAEvent;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.module.interfaces.KitData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class Juggernaut extends FFAEvent {

    private Player juggernaut;

    public Juggernaut(Object starter, JuggernautData eventData) {
        super(starter, eventData, "COMMAND.EVENT.ARGUMENTS.JUGGERNAUT");
    }

    @Override
    protected void customCustomStart() {
        this.juggernaut = this.players.get(random.nextInt(this.players.size()));
        this.startPlayerCount = this.startPlayerCount - 1;
        this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.JUGGERNAUT-ANNOUNCE").replaceAll("%player%", juggernaut.getName()), false);
    }

    @Override
    protected void loadInventory(Player player) {
        KitData kitData;
        if (player.equals(juggernaut)) {
            kitData = this.getEventData().getJuggernautKitData();
        } else {
            kitData = this.getEventData().getPlayerKitData();
        }
        kitData.loadKitData(player, true);
    }

    @Override
    public void killPlayer(Player player, boolean teleport) {
        if (!this.players.contains(player)) {
            return;
        }

        if (this.juggernaut != player) {
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.PLAYER-DIED")
                            .replaceAll("%player%", player.getName())
                            .replaceAll("%startPlayerCount%", String.valueOf(this.getStartPlayerCount()))
                            .replaceAll("%playerCount%", String.valueOf(this.players.size() - 2))
                    , true);
        } else {
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.JUGGERNAUT-DIED").replaceAll("%player%", juggernaut.getName()), true);
        }

        this.players.remove(player);
        if (player != null && player.isOnline()) {
            addSpectator(player, null, teleport, false);
        }

        if (this.juggernaut != player) {
            if (players.size() == 1) {
                this.winner = juggernaut;
                this.endEvent();
            }
        } else {
            this.winner = players.stream().findAny().isPresent() ? players.stream().findAny().get() : null;
            this.endEvent();
        }
    }

    @Override
    public void endEvent() {
        EventEndEvent event = new EventEndEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        this.cancelAllRunnable();
        this.status = EventStatus.END;

        if (ZonePractice.getInstance().isEnabled()) {
            this.getEndRunnable().begin();
        } else {
            this.getEndRunnable().end();
        }

        if (this.winner != null) {
            if (this.winner == juggernaut) {
                this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.JUGGERNAUT-WON").replaceAll("%player%", winner.getName()), true);

                for (String cmd : eventData.getType().getWinnerCMD())
                    ServerManager.runConsoleCommand(cmd.replaceAll("%player%", juggernaut.getName()));
            } else {
                this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.ATTACKERS-WON"), true);

                for (Player player : players) {
                    if (player != juggernaut) {
                        for (String cmd : eventData.getType().getWinnerCMD()) {
                            ServerManager.runConsoleCommand(cmd.replaceAll("%player%", player.getName()));
                        }
                    }
                }
            }
        } else
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.NO-WINNER"), true);
    }


    @Override
    public JuggernautData getEventData() {
        return (JuggernautData) eventData;
    }
}
