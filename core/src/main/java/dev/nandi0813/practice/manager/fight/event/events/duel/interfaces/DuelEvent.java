package dev.nandi0813.practice.manager.fight.event.events.duel.interfaces;

import dev.nandi0813.api.Event.Event.EventEndEvent;
import dev.nandi0813.api.Event.Spectate.Start.EventSpectateStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.event.runnables.DurationRunnable;
import dev.nandi0813.practice.manager.fight.event.runnables.StartRunnable;
import dev.nandi0813.practice.manager.fight.event.util.EventUtil;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public abstract class DuelEvent extends Event {

    private static final Random random = new Random();

    private final String LANGUAGE_PATH;
    private final List<DuelFight> fights = new ArrayList<>();
    private final Map<Player, Player> duelList = new HashMap<>();
    private final Map<Player, Player> spectating = new HashMap<>();
    @Setter
    private int round;

    private StartRunnable startRunnable;
    private DurationRunnable durationRunnable;

    public DuelEvent(Object starter, DuelEventData eventData, String LANGUAGE_PATH) {
        super(starter, eventData);
        this.LANGUAGE_PATH = LANGUAGE_PATH;
    }

    @Override
    protected void customStart() {
        this.startNextRound();
    }

    protected void startNextRound() {
        if (this.round != 0) {
            this.fightChange.rollback(100, 50);
        }

        this.round++;

        this.getNextFights(leftPlayer ->
        {
            sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".ROUND-DRAWN").replace("%round%", String.valueOf(round)), true);

            for (DuelFight fight : fights) {
                Player player1 = fight.getPlayers().get(0);
                Player player2 = fight.getPlayers().get(1);

                for (Player eventPlayer : players) {
                    if (player1 != eventPlayer && player2 != eventPlayer) {
                        PlayerHider.getInstance().hidePlayer(player1, eventPlayer, false);
                        PlayerHider.getInstance().hidePlayer(player2, eventPlayer, false);
                    }

                    if (eventPlayer != leftPlayer)
                        PlayerUtil.setFightPlayer(eventPlayer);
                }

                PlayerHider.getInstance().showPlayer(player1, player2);
                PlayerHider.getInstance().showPlayer(player2, player1);

                this.teleport(player1, this.getEventData().getLocation1());
                this.teleport(player2, this.getEventData().getLocation2());
            }

            if (leftPlayer != null) {
                this.sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".PLAYER-OUT-OF-ROUND").replace("%player%", leftPlayer.getName()), true);
                this.addSpectator(leftPlayer, getRandomFightPlayer(), true, false);
            }

            for (Player spectator : this.getSpectators()) {
                if (this.spectating.containsKey(spectator)) {
                    Player target = spectating.get(spectator);

                    if (this.isInFight(target))
                        this.addSpectator(spectator, target, true, false);
                    else {
                        Common.sendMMMessage(spectator, LanguageManager.getString(LANGUAGE_PATH + ".SPECTATOR-TARGET-NOT-PLAYING").replace("%target%", target.getName()));
                        this.addSpectator(spectator, this.getRandomFightPlayer(), true, false);
                    }
                } else
                    this.addSpectator(spectator, this.getRandomFightPlayer(), true, false);
            }

            // Set the status to start, countdown until the match is live.
            this.status = EventStatus.START;
            this.getStartRunnable().begin();
        });
    }

    public abstract void teleport(final Player player, final Location location);

    @Override
    public void handleStartRunnable(StartRunnable startRunnable) {
        int seconds = startRunnable.getSeconds();

        if (seconds == 0) {
            startRunnable.cancel();

            this.getDurationRunnable().begin();
            this.status = EventStatus.LIVE;
        } else {
            if (seconds <= 5) {
                this.sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".ROUND-STARTING")
                                .replace("%seconds%", String.valueOf(seconds))
                                .replace("%secondName%", (seconds == 1 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC"))),
                        true);
            }

            startRunnable.decreaseTime();
        }
    }

    @Override
    public void handleDurationRunnable(DurationRunnable durationRunnable) {
        int seconds = durationRunnable.getSeconds();

        if (seconds == 0) {
            durationRunnable.cancel();

            if (!this.status.equals(EventStatus.END)) {
                List<DuelFight> fights = new ArrayList<>(this.fights);
                for (DuelFight fight : fights)
                    fight.endFight(null);
            }
        } else {
            if (seconds <= 5 || seconds == 30 || seconds == 60)
                sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".ROUND-ENDING")
                                .replace("%seconds%", String.valueOf(seconds))
                                .replace("%secondName%", (seconds == 1 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC"))),
                        true);

            durationRunnable.decreaseTime();
        }
    }

    @Override
    public void killPlayer(Player player, boolean teleport) {
        DuelFight duelFight = this.getFight(player);
        if (duelFight != null) {
            duelFight.endFight(player);
        }
    }

    @Override
    public boolean checkIfEnd() {
        if (players.size() == 1) {
            this.winner = players.stream().findFirst().get();
            return true;
        } else return players.isEmpty();
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
            this.sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".WON-EVENT").replace("%winner%", this.winner.getName()), true);

            for (String cmd : this.eventData.getType().getWinnerCMD()) {
                ServerManager.runConsoleCommand(cmd.replace("%player%", this.winner.getName()));
            }
        } else
            this.sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".NO-WINNER"), true);
    }

    @Override
    public DuelEventData getEventData() {
        return (DuelEventData) eventData;
    }

    public boolean isInFight(Player player) {
        for (DuelFight fight : fights)
            if (fight.getPlayers().contains(player))
                return true;
        return false;
    }

    public DuelFight getFight(Player player) {
        for (DuelFight fight : fights)
            if (fight.getPlayers().contains(player))
                return fight;
        return null;
    }

    public Player getRandomFightPlayer() {
        for (Player player : players)
            if (this.isInFight(player))
                return player;
        return null;
    }

    private void getNextFights(NextFightCallback callback) {
        fights.clear();
        duelList.clear();
        Collections.shuffle(players);

        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            for (Player player : players) {
                if (!isInFight(player)) {
                    List<Player> notInFight = new ArrayList<>();
                    for (Player enemy : players) {
                        if (player != enemy && !isInFight(enemy))
                            notInFight.add(enemy);
                    }

                    if (notInFight.isEmpty()) {
                        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> callback.onNextFight(player));
                        return;
                    }

                    Player enemy = notInFight.get(random.nextInt(notInFight.size()));

                    List<Player> fightPlayers = new ArrayList<>();
                    fightPlayers.add(player);
                    fightPlayers.add(enemy);

                    fights.add(new DuelFight(this, fightPlayers));

                    duelList.put(player, enemy);
                    duelList.put(enemy, player);
                }
            }

            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> callback.onNextFight(null));
        });
    }

    @Override
    public void addSpectator(Player spectator, Player target, boolean teleport, boolean message) {
        if (!players.contains(spectator)) {
            EventSpectateStartEvent event = new EventSpectateStartEvent(spectator, this);
            Bukkit.getPluginManager().callEvent(event);
        }

        DuelFight fight;
        if (target != null)
            fight = this.getFight(target);
        else
            fight = this.getFight(this.getRandomFightPlayer());

        if (fight == null) {
            if (target != null) {
                Common.sendMMMessage(spectator, LanguageManager.getString("COMMAND.SPECTATE.TARGET-NOT-PLAYING").replace("%target%", target.getName()));
            }
            return;
        }

        // Teleport the player to the spectator location.
        if (target != null)
            spectator.teleport(target);
        else
            spectator.teleport(eventData.getCuboid().getCenter());

        if (players.contains(spectator)) {
            PlayerUtil.clearPlayer(spectator, true, true, false);
        } else {
            this.addSpectator(spectator);
            fight.getSpectators().add(spectator);

            EventUtil.setEventSpectatorInventory(spectator);

            if (message && !this.status.equals(EventStatus.END)) {
                sendMessage(LanguageManager.getString(LANGUAGE_PATH + ".STARTED-SPECTATING").replace("%spectator%", spectator.getName()), true);
            }
        }

        for (Player eventPlayer : players) {
            PlayerHider.getInstance().hidePlayer(eventPlayer, spectator, false);

            if (fight.getPlayers().contains(eventPlayer))
                PlayerHider.getInstance().showPlayer(spectator, eventPlayer);
            else
                PlayerHider.getInstance().hidePlayer(spectator, eventPlayer, false);
        }
    }

    @Override
    public StartRunnable getStartRunnable() {
        if (this.startRunnable == null) {
            this.startRunnable = new StartRunnable(this);
        } else if (!this.startRunnable.isRunning() && this.startRunnable.isHasRun()) {
            this.startRunnable = new StartRunnable(this);
        }

        return this.startRunnable;
    }

    @Override
    public DurationRunnable getDurationRunnable() {
        if (this.durationRunnable == null) {
            this.durationRunnable = new DurationRunnable(this);
        } else if (!this.durationRunnable.isRunning() && this.durationRunnable.isHasRun()) {
            this.durationRunnable = new DurationRunnable(this);
        }

        return this.durationRunnable;
    }

}
