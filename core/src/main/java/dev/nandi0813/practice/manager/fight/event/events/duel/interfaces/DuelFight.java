package dev.nandi0813.practice.manager.fight.event.events.duel.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelFight {

    private final DuelEvent duelEvent;
    private boolean ended;

    @Getter
    private final List<Player> players;
    @Getter
    private final List<Player> spectators = new ArrayList<>();

    public DuelFight(final DuelEvent duelEvent, final List<Player> players) {
        this.duelEvent = duelEvent;
        this.players = players;
        this.ended = false;
    }

    public void endFight(final Player loser) {
        if (this.ended) {
            return;
        } else {
            this.ended = true;
        }

        // Final fights should end immediately without the "next round" countdown.
        if (this.isFinalFightResult(loser)) {
            this.finalizeFightEnd(loser);
            return;
        }

        int delayTicks = EventManager.getInstance().getPostKillDelayTicks();
        if (delayTicks <= 0) {
            this.finalizeFightEnd(loser);
            return;
        }

        int delaySeconds = Math.max(1, delayTicks / 20);
        new BukkitRunnable() {
            private int secondsLeft = delaySeconds;

            @Override
            public void run() {
                if (duelEvent.getStatus().equals(EventStatus.END)) {
                    cancel();
                    return;
                }

                if (secondsLeft <= 0) {
                    cancel();
                    finalizeFightEnd(loser);
                    return;
                }

                sendPostKillCountdownTitle(secondsLeft);
                secondsLeft--;
            }
        }.runTaskTimer(ZonePractice.getInstance(), 0L, 20L);
    }

    private void finalizeFightEnd(final Player loser) {
        if (this.duelEvent.getStatus().equals(EventStatus.END)) {
            return;
        }

        this.sendEndTitles(loser);
        this.duelEvent.getFights().remove(this);

        if (loser == null) {
            for (Player player : players) {
                this.duelEvent.sendMessage(LanguageManager.getString(duelEvent.getLANGUAGE_PATH() + ".PLAYER-OUT").replace("%player%", player.getName()), true);
                this.duelEvent.getPlayers().remove(player);
                this.duelEvent.getSpectators().add(player);
            }
        } else {
            this.duelEvent.sendMessage(LanguageManager.getString(duelEvent.getLANGUAGE_PATH() + ".PLAYER-OUT").replace("%player%", loser.getName()), true);
            this.duelEvent.getPlayers().remove(loser);
            this.duelEvent.getSpectators().add(loser);
            this.sendMessage(LanguageManager.getString(duelEvent.getLANGUAGE_PATH() + ".WON-FIGHT").replace("%player%", getOtherPlayer(loser).getName()));
        }

        if (!this.duelEvent.checkIfEnd()) {
            if (this.duelEvent.getFights().isEmpty()) {
                this.duelEvent.startNextRound();
            } else {
                List<Player> forward = new ArrayList<>(this.players);
                forward.addAll(this.spectators);

                for (Player player : forward) {
                    this.duelEvent.addSpectator(player, this.duelEvent.getRandomFightPlayer(), true, true);
                    Common.sendMMMessage(player, LanguageManager.getString(duelEvent.getLANGUAGE_PATH() + ".SPECTATOR-FORWARDED"));
                }
            }
        } else {
            if (loser != null) {
                Player winner = getOtherPlayer(loser);
                if (winner != null && !this.duelEvent.getPlayers().contains(winner)) {
                    this.duelEvent.getPlayers().add(winner);
                }
                this.duelEvent.getSpectators().remove(winner);
            }

            this.duelEvent.endEvent();
        }
    }

    private void sendEndTitles(final Player loser) {
        if (loser == null || this.duelEvent.getStatus().equals(EventStatus.END)) {
            return;
        }

        Player winner = getOtherPlayer(loser);
        if (winner == null) {
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%winner%", winner.getName());

        if (this.isFinalFightResult(loser)) {
            EventManager.getInstance().sendConfiguredEventTitle(
                    winner,
                    "EVENT.DUEL.EVENT-MATCH-WON-TITLE",
                    "EVENT.DUEL.EVENT-MATCH-WON-SUBTITLE",
                    placeholders
            );
            return;
        }

        EventManager.getInstance().sendConfiguredEventTitle(
                winner,
                "EVENT.DUEL.EVENT-ROUND-WON-TITLE",
                "EVENT.DUEL.EVENT-ROUND-WON-SUBTITLE",
                placeholders
        );

        EventManager.getInstance().sendConfiguredEventTitle(
                loser,
                "EVENT.DUEL.EVENT-ROUND-LOST-TITLE",
                "EVENT.DUEL.EVENT-ROUND-LOST-SUBTITLE",
                placeholders
        );

        String spectatorTitle = LanguageManager.getString("EVENT.DUEL.EVENT-FIGHT-WON-TITLE").replace("%winner%", winner.getName());
        for (Player spectator : this.getSpectatorRecipients(winner, loser)) {
            EventManager.getInstance().sendRawEventTitle(spectator, spectatorTitle, "");
        }
    }

    private void sendPostKillCountdownTitle(int secondsLeft) {
        String title = LanguageManager.getString("EVENT.DUEL.POST-KILL-COUNTDOWN-TITLE");
        String subtitle = LanguageManager.getString("EVENT.DUEL.POST-KILL-COUNTDOWN-SUBTITLE")
                .replace("%seconds%", String.valueOf(secondsLeft))
                .replace("%secondName%", (secondsLeft == 1 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC")));

        for (Player player : this.getTitleRecipients()) {
            EventManager.getInstance().sendRawEventTitle(player, title, subtitle);
        }
    }

    private List<Player> getTitleRecipients() {
        Set<Player> recipients = new LinkedHashSet<>();
        recipients.addAll(this.players);
        recipients.addAll(this.spectators);
        recipients.addAll(this.duelEvent.getSpectators());
        return new ArrayList<>(recipients);
    }

    private boolean isFinalFightResult(final Player loser) {
        int remainingPlayers = this.duelEvent.getPlayers().size();
        remainingPlayers -= (loser == null ? this.players.size() : 1);
        return remainingPlayers <= 1;
    }

    private List<Player> getSpectatorRecipients(Player winner, Player loser) {
        List<Player> recipients = new ArrayList<>(this.duelEvent.getSpectators());
        recipients.addAll(this.spectators);
        recipients.remove(winner);
        recipients.remove(loser);
        return recipients;
    }

    public Player getOtherPlayer(Player player) {
        for (Player fightPlayer : players)
            if (!fightPlayer.equals(player))
                return fightPlayer;
        return null;
    }

    public void sendMessage(String message) {
        List<Player> messageTo = new ArrayList<>();
        messageTo.addAll(players);
        messageTo.addAll(spectators);

        for (Player player : messageTo)
            Common.sendMMMessage(player, message);
    }

}
