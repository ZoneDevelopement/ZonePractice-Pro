package dev.nandi0813.practice.manager.fight.event.events.ffa.interfaces;

import dev.nandi0813.api.Event.Event.EventEndEvent;
import dev.nandi0813.api.Event.Spectate.Start.EventSpectateStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.manager.fight.event.interfaces.FullRunnableInterface;
import dev.nandi0813.practice.manager.fight.event.runnables.DurationRunnable;
import dev.nandi0813.practice.manager.fight.event.runnables.StartRunnable;
import dev.nandi0813.practice.manager.fight.event.util.EventUtil;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;

public abstract class FFAEvent extends FullRunnableInterface {

    protected static final Random random = new Random();
    private final String LANGUAGE_PATH;

    public FFAEvent(Object starter, EventData eventData, String languagePath) {
        super(starter, eventData);
        this.LANGUAGE_PATH = languagePath;
    }

    public void teleport(final Player player) {
        int i = random.nextInt(eventData.getSpawns().size());
        player.teleport(eventData.getSpawns().get(i));

        loadInventory(player);
    }

    @Override
    protected void customStart() {
        this.status = EventStatus.START;
        this.getStartRunnable().begin();
        this.customCustomStart(); // Juggernaut miatt itt kell lennie, mert utána jön a load INV.

        for (Player player : this.players) {
            for (Player target : this.players) {
                if (player != target) {
                    PlayerHider.getInstance().showPlayer(player, target);
                }
            }

            this.teleport(player);
        }
    }

    protected abstract void customCustomStart();

    @Override
    public void handleStartRunnable(StartRunnable startRunnable) {
        int seconds = startRunnable.getSeconds();

        if (seconds == 0) {
            startRunnable.cancel();
            this.status = EventStatus.LIVE;
            this.getDurationRunnable().begin();
        } else if (seconds % 10 == 0 || seconds <= 3) {
            sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".GAME-STARTING")
                            .replaceAll("%seconds%", String.valueOf(seconds))
                            .replaceAll("%secondName%", (seconds == 1 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC"))),
                    true);
        }

        startRunnable.decreaseTime();
    }

    @Override
    public void handleDurationRunnable(DurationRunnable durationRunnable) {
        if (durationRunnable.getSeconds() == 0) {
            if (!getStatus().equals(EventStatus.END)) {
                endEvent();
            } else {
                durationRunnable.cancel();
            }
        } else
            durationRunnable.decreaseTime();
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
            this.sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".WON-EVENT").replaceAll("%winner%", winner.getName()), true);

            for (String cmd : eventData.getType().getWinnerCMD())
                ServerManager.runConsoleCommand(cmd.replaceAll("%player%", winner.getName()));
        } else
            this.sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".NO-WINNER"), true);
    }

    @Override
    public void killPlayer(Player player, boolean teleport) {
        if (!this.players.contains(player)) {
            return;
        }

        this.sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".PLAYER-DIED")
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%startPlayerCount%", String.valueOf(this.getStartPlayerCount()))
                        .replaceAll("%playerCount%", String.valueOf(players.size() - 1))
                , true);

        this.players.remove(player);
        if (player.isOnline()) {
            this.addSpectator(player, null, teleport, false);
        }

        this.checkIfEnd();
    }

    @Override
    public void addSpectator(Player spectator, Player target, boolean teleport, boolean message) {
        EventSpectateStartEvent event = new EventSpectateStartEvent(spectator, this);
        Bukkit.getPluginManager().callEvent(event);

        if (target == null && !this.players.isEmpty()) {
            target = this.players.get(random.nextInt(this.players.size()));
        }

        if (teleport) {
            if (target != null) {
                spectator.teleport(target);
            } else {
                spectator.teleport(this.getEventData().getCuboid().getCenter());
            }
        }

        this.addSpectator(spectator);
        EventUtil.setEventSpectatorInventory(spectator);

        if (message && !this.status.equals(EventStatus.END)) {
            sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".STARTED-SPECTATING").replaceAll("%spectator%", spectator.getName()), true);
        }

        for (Player eventPlayer : players) {
            PlayerHider.getInstance().hidePlayer(eventPlayer, spectator, false);
        }

        for (Player eventSpectator : this.getSpectators()) {
            if (!eventSpectator.equals(spectator)) {
                PlayerHider.getInstance().hidePlayer(eventSpectator, spectator, false);
                PlayerHider.getInstance().hidePlayer(spectator, eventSpectator, false);
            }
        }
    }

    protected abstract void loadInventory(Player player);

}
