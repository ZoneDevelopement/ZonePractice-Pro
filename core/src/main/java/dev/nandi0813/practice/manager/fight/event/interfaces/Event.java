package dev.nandi0813.practice.manager.fight.event.interfaces;

import dev.nandi0813.api.Event.Event.EventEndEvent;
import dev.nandi0813.api.Event.Event.EventStartEvent;
import dev.nandi0813.api.Event.Spectate.End.EventSpectateEndEvent;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.runnables.DurationRunnable;
import dev.nandi0813.practice.manager.fight.event.runnables.EndRunnable;
import dev.nandi0813.practice.manager.fight.event.runnables.StartRunnable;
import dev.nandi0813.practice.manager.fight.event.runnables.queue.QueueRunnable;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class Event implements Spectatable, dev.nandi0813.api.Interface.Event {

    protected final EventData eventData;
    @Getter
    protected final EventType type;
    @Getter
    protected final Object starter;
    @Getter
    protected final List<Player> players;
    @Getter
    protected final FightChangeOptimized fightChange;
    // Runnable
    @Getter
    protected final QueueRunnable queueRunnable;
    @Getter
    private final List<Player> spectators;
    @Getter
    private final EndRunnable endRunnable;
    @Getter
    protected EventStatus status;
    @Getter
    protected Player winner;
    @Getter
    protected int startPlayerCount;

    public Event(final Object starter, final EventData eventData) {
        this.starter = starter;
        this.eventData = eventData;
        this.type = eventData.getType();
        this.status = EventStatus.COLLECTING;

        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.fightChange = new FightChangeOptimized(this);

        this.queueRunnable = new QueueRunnable(this);
        this.endRunnable = new EndRunnable(this);
    }

    public abstract EventData getEventData();

    public void addPlayer(final Player player) throws IllegalStateException {
        if (this.players.size() >= this.eventData.getMaxPlayer()) {
            throw new IllegalStateException(LanguageManager.getString("EVENT.EVENT-FULL"));
        }

        if (!this.status.equals(EventStatus.COLLECTING)) {
            throw new IllegalStateException(LanguageManager.getString("EVENT.EVENT-ALREADY-STARTED"));
        }

        this.players.add(player);
        this.sendMessage(LanguageManager.getString("EVENT.PLAYER-JOINED").replace("%player%", player.getName()).replace("%playerSize%", String.valueOf(players.size())).replace("%maxPlayerSize%", String.valueOf(eventData.getMaxPlayer())), false);

        InventoryManager.getInstance().setEventQueueInventory(player);
        ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.QUEUE);

        if (eventData.getMinPlayer() == players.size()) {
            queueRunnable.setStarting();
        }
        if (eventData.getMaxPlayer() == players.size()) {
            queueRunnable.setStartingMaxPlayers();
        }
    }

    public void removePlayer(Player player, boolean message) {
        if (!this.players.contains(player)) {
            return;
        }

        this.players.remove(player);

        if (this.status.equals(EventStatus.COLLECTING)) {
            this.getQueueRunnable().playerLeave();

            if (message) {
                this.sendMessage(LanguageManager.getString("EVENT.PLAYER-QUIT").replace("%player%", player.getName()), false);
            }

            InventoryManager.getInstance().setLobbyInventory(player, false);
        } else {
            this.killPlayer(player, false);

            if (message && !status.equals(EventStatus.END)) {
                this.sendMessage(LanguageManager.getString("EVENT.PLAYER-QUIT").replace("%player%", player.getName()), true);
            }

            if (status.equals(EventStatus.START) || status.equals(EventStatus.LIVE)) {
                this.checkIfEnd();
            }

            if (player.isOnline()) {
                InventoryManager.getInstance().setLobbyInventory(player, true);
            }
        }
    }

    public abstract void killPlayer(Player player, boolean teleport);

    public void startQueue() {
        EventStartEvent event = new EventStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (!this.status.equals(EventStatus.COLLECTING)) {
            return;
        }

        this.queueRunnable.begin();
        SpectatorManager.getInstance().getSpectatorMenuGui().update();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (starter instanceof Player starterPlayer)
                Common.sendMMMessage(online, LanguageManager.getString("EVENT.EVENT-QUEUE-STARTED-PLAYER").replace("%player%", starterPlayer.getName()).replace("%event%", type.getName()));
            else
                Common.sendMMMessage(online, LanguageManager.getString("EVENT.EVENT-QUEUE-STARTED-CONSOLE").replace("%consoleName%", LanguageManager.getString("CONSOLE-NAME")).replace("%event%", type.getName()));
        }

        if (starter instanceof Player) {
            this.addPlayer((Player) starter);
        }
    }

    public void stopQueue() {
        if (this.status != EventStatus.COLLECTING) {
            return;
        }

        EventEndEvent event = new EventEndEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        SpectatorManager.getInstance().getSpectatorMenuGui().update();

        this.cancelAllRunnable();
        sendMessage(LanguageManager.getString("EVENT.EVENT-CANT-START-ONTIME"), false);

        for (Player player : new ArrayList<>(players))
            removePlayer(player, false);

        EventManager.getInstance().getEvents().remove(this);
    }

    public void start() {
        this.status = EventStatus.START;

        for (Player player : this.players) {
            ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.EVENT);
        }

        this.startPlayerCount = this.players.size();

        this.customStart();

        SpectatorManager.getInstance().getSpectatorMenuGui().update();
    }

    protected abstract void customStart();

    public abstract void handleStartRunnable(final StartRunnable startRunnable);

    public abstract void handleDurationRunnable(final DurationRunnable durationRunnable);

    public boolean checkIfEnd() {
        if (players.size() == 1) {
            this.winner = players.stream().findFirst().get();
            endEvent();
            SpectatorManager.getInstance().getSpectatorMenuGui().update();
            return true;
        } else if (players.isEmpty()) {
            endEvent();
            SpectatorManager.getInstance().getSpectatorMenuGui().update();
            return true;
        }
        return false;
    }

    public abstract void endEvent();

    public void forceEnd(final Player player) {
        if (player != null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.EVENT.PLAYER-END-MSG"));
            sendMessage(LanguageManager.getString("COMMAND.STAFF.ARGUMENTS.FORCE-END.EVENT.MATCH-END-MSG").replace("%player%", player.getName()), true);
        }

        // Store current status before changing it
        boolean wasLive = !this.status.equals(EventStatus.COLLECTING);

        // Cancel all runnables first to prevent interference
        this.cancelAllRunnable();

        // Remove all players and spectators
        this.removeAll();

        // Set status to END
        this.status = EventStatus.END;

        // Rollback fight changes if event was live
        if (wasLive) {
            this.getFightChange().rollback(100, 50);
        }

        // Remove from event manager
        EventManager.getInstance().getEvents().remove(this);

        // Update spectator GUI
        SpectatorManager.getInstance().getSpectatorMenuGui().update();
    }

    public void removeAll() {
        for (Player player : new ArrayList<>(this.players))
            this.removePlayer(player, false);

        for (Player spectator : new ArrayList<>(this.spectators))
            this.removeSpectator(spectator);
    }

    @Override
    public void removeSpectator(Player player) {
        if (!this.removeSpectatorFromList(player)) {
            return;
        }

        if (!status.equals(EventStatus.END))
            sendMessage(LanguageManager.getString("EVENT.PLAYER-STOPPED-SPECTATING").replace("%player%", player.getName()), true);

        InventoryManager.getInstance().setLobbyInventory(player, !status.equals(EventStatus.COLLECTING));
        SpectatorManager.getInstance().getSpectatorMenuGui().update();

        EventSpectateEndEvent event = new EventSpectateEndEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public boolean canDisplay() {
        switch (status) {
            case COLLECTING, END -> {
                return false;
            }
            case LIVE, START -> {
                return true;
            }
        }

        return false;
    }

    @Override
    public GUIItem getSpectatorMenuItem() {
        return GUIFile.getGuiItem("GUIS.SPECTATOR-MENU.ICONS.EVENT-ICON")
                .setMaterial(eventData.getIcon().getMaterial()).setDamage(eventData.getIcon().getDamage()).replace("%event_type%", type.getName())
                .replace("%event_duration%", this.getDurationRunnable().getFormattedTime())
                .replace("%players%", String.valueOf(players.size())).replace("%spectators%", String.valueOf(spectators.size()));

    }

    @Override
    public boolean isBuild() {
        return false;
    }

    @Override
    public Cuboid getCuboid() {
        return eventData.getCuboid();
    }

    @Override
    public void sendMessage(final String message, final boolean spectator) {
        for (Player player : players)
            Common.sendMMMessage(player, message);

        if (spectator) for (Player player : spectators)
            Common.sendMMMessage(player, message);
    }

    public abstract StartRunnable getStartRunnable();

    public abstract DurationRunnable getDurationRunnable();

    public void cancelAllRunnable() {
        if (this.queueRunnable != null) this.queueRunnable.cancel();
        if (this.getStartRunnable() != null) this.getStartRunnable().cancel();
        if (this.getDurationRunnable() != null) this.getDurationRunnable().cancel();
        if (this.endRunnable != null) this.endRunnable.cancel();
    }

    public boolean addSpectator(Player player) {
        SpectatorManager.getInstance().getSpectators().put(player, this);

        if (this.spectators.contains(player))
            return false;

        this.spectators.add(player);

        return true;
    }

    public boolean removeSpectatorFromList(Player player) {
        if (!this.spectators.contains(player))
            return false;

        this.spectators.remove(player);
        SpectatorManager.getInstance().getSpectators().remove(player);

        return true;
    }

}
