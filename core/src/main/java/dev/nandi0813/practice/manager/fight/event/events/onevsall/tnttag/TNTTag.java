package dev.nandi0813.practice.manager.fight.event.events.onevsall.tnttag;

import dev.nandi0813.api.Event.Event.EventEndEvent;
import dev.nandi0813.api.Event.Spectate.Start.EventSpectateStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.event.runnables.DurationRunnable;
import dev.nandi0813.practice.manager.fight.event.runnables.StartRunnable;
import dev.nandi0813.practice.manager.fight.event.util.EventUtil;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
public class TNTTag extends Event {

    private static final Random random = new Random();

    private int round;

    @Setter
    private List<Player> taggedPlayers = new ArrayList<>();

    private StartRunnable startRunnable;
    private DurationRunnable durationRunnable;

    public TNTTag(Object starter, TNTTagData eventData) {
        super(starter, eventData);
    }

    @Override
    public TNTTagData getEventData() {
        return (TNTTagData) eventData;
    }

    public void increaseRounds() {
        this.round++;
    }

    @Override
    public void killPlayer(Player player, boolean teleport) {
        if (!this.players.contains(player)) {
            return;
        }

        this.sendExplosion(player.getLocation());
        this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.TNTTAG.BLOWN-UP").replaceAll("%player%", player.getName()), true);

        this.taggedPlayers.remove(player);
        this.players.remove(player);
        if (player.isOnline()) {
            this.addSpectator(player, null, teleport, false);
        }

        this.checkIfEnd();
    }

    @Override
    protected void customStart() {
        for (Player player : players) {
            this.teleportPlayer(player);
            this.loadInv(player);

            for (Player eventPlayer : players) {
                if (!eventPlayer.equals(player)) {
                    PlayerHider.getInstance().showPlayer(player, eventPlayer);
                }
            }
        }

        this.getStartRunnable().begin();
        this.increaseRounds();
    }

    private void loadInv(Player player) {
        PlayerUtil.clearPlayer(player, true, false, true);
        setSpeedPotion(player, 1);
    }

    @Override
    public void handleStartRunnable(StartRunnable startRunnable) {
        int seconds = this.getStartRunnable().getSeconds();

        if (seconds == 0) {
            this.getStartRunnable().cancel();
            this.status = EventStatus.LIVE;
            this.nextRound(round != 1);
        } else if (seconds <= 5) {
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.TNTTAG.ROUND-STARTING")
                            .replaceAll("%seconds%", String.valueOf(seconds))
                            .replaceAll("%secondName%", (seconds == 1 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC")))
                    , true);
        }

        startRunnable.decreaseTime();
    }

    public void nextRound(boolean teleport) {
        List<Player> taggers = this.getRandomTaggers();

        this.getDurationRunnable().begin();

        if (teleport) {
            for (Player player : players) {
                this.teleportPlayer(player);
                this.loadInv(player);
            }
        }

        for (Player tagger : taggers) {
            this.setTag(null, tagger);
        }

        this.increaseRounds();
    }

    @Override
    public void handleDurationRunnable(DurationRunnable durationRunnable) {
        int seconds = durationRunnable.getSeconds();

        if (seconds == 0) {
            durationRunnable.cancel();

            for (Player player : new ArrayList<>(taggedPlayers)) {
                killPlayer(player, false);
            }

            // If the event hasn't ended yet, start a new round.
            if (!this.getStatus().equals(EventStatus.END)) {
                this.getStartRunnable().begin();
            }
        } else {
            durationRunnable.decreaseTime();
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

        if (winner != null) {
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.TNTTAG.WON-EVENT").replaceAll("%winner%", winner.getName()), true);

            for (String cmd : eventData.getType().getWinnerCMD())
                ServerManager.runConsoleCommand(cmd.replaceAll("%player%", winner.getName()));
        } else {
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.TNTTAG.NO-WINNER"), true);
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
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.TNTTAG.STARTED-SPECTATING").replaceAll("%spectator%", spectator.getName()), true);
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

    public void teleportPlayer(Player player) {
        player.teleport(eventData.getSpawns().get(random.nextInt(eventData.getSpawns().size())));
    }

    public void setTag(Player tagger, Player tagged) {
        if (tagger != null) {
            this.taggedPlayers.remove(tagger);
            this.loadInv(tagger);

            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.TNTTAG.PLAYER-TAGGED-PLAYER")
                            .replaceAll("%player%", tagger.getName())
                            .replaceAll("%target%", tagged.getName())
                    , true
            );
        } else {
            this.sendMessage(LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.TNTTAG.START-TAGGER").replaceAll("%player%", tagged.getName()), true);
        }

        tagged.getInventory().setItem(0, ConfigManager.getGuiItem("EVENT.TNTTAG.TAGGED-ITEM").get());
        tagged.getInventory().setItem(1, EventManager.PLAYER_TRACKER);
        tagged.getInventory().setHelmet(new ItemStack(Material.TNT));

        setSpeedPotion(tagged, 3);
        this.taggedPlayers.add(tagged);
    }

    public List<Player> getRandomTaggers() {
        int taggerCount;
        List<Player> taggers = new ArrayList<>();

        if (players.size() >= 50)
            taggerCount = 6;
        else if (players.size() >= 20)
            taggerCount = 4;
        else if (players.size() >= 11)
            taggerCount = 3;
        else if (players.size() >= 6)
            taggerCount = 2;
        else
            taggerCount = 1;

        for (int i = 0; i < 100; i++) {
            int randomPlayerNumber = random.nextInt(players.size());

            if (taggers.size() == taggerCount)
                break;
            else {
                Player tagger = players.get(randomPlayerNumber);

                if (!taggers.contains(tagger))
                    taggers.add(tagger);
            }
        }
        return taggers;
    }


    private static void setSpeedPotion(Player player, int amplifier) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000 * 20, amplifier));
    }

    public static final String TNT_TAG_TNT_METADATA = "ZPP_TNT_TAG_TNT";

    private void sendExplosion(Location location) {
        TNTPrimed tnt = (TNTPrimed) location.getWorld().spawnEntity(location.subtract(-0.5, 0, -0.5), EntityType.PRIMED_TNT);
        tnt.setMetadata(TNT_TAG_TNT_METADATA, new FixedMetadataValue(ZonePractice.getInstance(), this));
        tnt.setFuseTicks(1);
    }

}
