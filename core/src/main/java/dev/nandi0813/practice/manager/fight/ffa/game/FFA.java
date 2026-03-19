package dev.nandi0813.practice.manager.fight.ffa.game;

import dev.nandi0813.api.Event.FFARemovePlayerEvent;
import dev.nandi0813.api.Event.Spectate.End.FFASpectateEndEvent;
import dev.nandi0813.api.Event.Spectate.Start.FFASpectateStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.util.KitUtil;
import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class FFA implements Spectatable, dev.nandi0813.api.Interface.FFA {

    private static final Random random = new Random();

    private final Map<Player, NormalLadder> players = new HashMap<>();
    private final Map<Player, FightPlayer> fightPlayers = new HashMap<>();
    private final Map<Player, Statistic> statistics = new HashMap<>();

    private final List<Player> spectators = new ArrayList<>();
    private final FFAArena arena;
    private final LadderSelector ladderSelectorGui;

    private boolean build;
    private BuildRollback buildRollback;

    private boolean open;

    /** Tracks the last player that dealt damage to another player, for void-kill attribution. */
    private final Map<UUID, UUID> lastAttackerMap = new HashMap<>();
    /** Timestamp (ms) of the last attacker hit, keyed by victim UUID. */
    private final Map<UUID, Long> lastAttackerTime = new HashMap<>();
    /** How long (ms) a last-attacker is considered valid for void attribution. */
    private static final long LAST_ATTACKER_EXPIRY_MS = 4_000L;

    public FFA(FFAArena arena) {
        this.arena = arena;
        this.build = arena.isBuild();
        this.ladderSelectorGui = new LadderSelector(this);
        this.open = false;
    }

    public void open() {
        if (this.open) {
            return;
        }

        if (!this.arena.isEnabled()) {
            return;
        }

        this.build = this.arena.isBuild();
        this.open = true;

        if (this.build) {
            this.buildRollback = new BuildRollback(new FightChangeOptimized(this), this::teleportStuckSpectatorsAfterRollback);
            this.buildRollback.begin();
        }

        SpectatorManager.getInstance().getSpectatorMenuGui().update();
        this.ladderSelectorGui.update();
    }

    public void close(String message) {
        if (!this.open) {
            return;
        }

        if (this.build) {
            this.buildRollback.cancel();
            this.buildRollback = null;
        }

        if (!ZonePractice.getInstance().isEnabled())
            return;

        if (message != null) {
            this.sendMessage(message, true);
        }

        this.open = false;

        for (Player player : new ArrayList<>(players.keySet()))
            removePlayer(player);
        for (Player spectator : new ArrayList<>(spectators))
            removeSpectator(spectator);

        SpectatorManager.getInstance().getSpectatorMenuGui().update();
    }

    public void addPlayer(Player player, NormalLadder ladder) {
        if (players.containsKey(player))
            return;

        players.put(player, ladder);
        fightPlayers.put(player, new FightPlayer(player, this));
        statistics.put(player, new Statistic(ProfileManager.getInstance().getUuids().get(player)));

        // Hide the spectators
        for (Player spectator : this.spectators) {
            PlayerHider.getInstance().hidePlayer(player, spectator, false);
            PlayerHider.getInstance().showPlayer(spectator, player);
        }

        // Show other players
        for (Player ffaPlayer : this.players.keySet()) {
            if (!ffaPlayer.equals(player)) {
                PlayerHider.getInstance().showPlayer(ffaPlayer, player);
                PlayerHider.getInstance().showPlayer(player, ffaPlayer);
            }
        }

        teleportPlayer(player);
        this.sendMessage(LanguageManager.getString("FFA.GAME.PLAYER-JOIN").replace("%player%", player.getName()), true);

        PlayerUtil.setFightPlayer(player);
        KitUtil.loadDefaultLadderKit(player, TeamEnum.FFA, players.get(player));

        ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.FFA);
        SpectatorManager.getInstance().getSpectatorMenuGui().update();
    }

    public void changePlayerLadder(Player player, NormalLadder ladder) {
        if (!players.containsKey(player) || ladder == null)
            return;

        players.put(player, ladder);
        PlayerUtil.setFightPlayer(player);
        KitUtil.loadDefaultLadderKit(player, TeamEnum.FFA, ladder);
    }

    public void removePlayer(Player player) {
        if (!players.containsKey(player))
            return;

        Bukkit.getPluginManager().callEvent(new FFARemovePlayerEvent(this, player));

        this.sendMessage(LanguageManager.getString("FFA.GAME.PLAYER-LEAVE").replace("%player%", player.getName()), true);

        // Remove in-flight ender pearls to prevent the player from being
        // teleported back to the arena world after they have left.
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof EnderPearl pearl && player.equals(pearl.getShooter())) {
                pearl.remove();
            }
        }

        players.remove(player);
        fightPlayers.remove(player);
        statistics.remove(player);

        InventoryManager.getInstance().setLobbyInventory(player, true);
        SpectatorManager.getInstance().getSpectatorMenuGui().update();
    }

    public void killPlayer(Player player, Player killer, String deathMessage) {
        if (!players.containsKey(player))
            return;

        // If no explicit killer and the death message is the plain void message,
        // check whether a recent attacker should be credited instead.
        if (killer == null) {
            Player lastAttacker = getLastAttacker(player);
            if (lastAttacker != null && deathMessage != null
                    && deathMessage.equals(dev.nandi0813.practice.manager.fight.util.DeathCause.VOID.getMessage())) {
                killer = lastAttacker;
                deathMessage = dev.nandi0813.practice.manager.fight.util.DeathCause.VOID_BY_PLAYER
                        .getMessage()
                        .replace("%killer%", killer.getName());
            }
        }

        fightPlayers.get(player).die(deathMessage, statistics.get(player));
        fightPlayers.get(player).getProfile().getStats().getLadderStat(players.get(player)).increaseDeaths();

        if (killer != null) {
            fightPlayers.get(killer).getProfile().getStats().getLadderStat(players.get(killer)).increaseKills();

            playDeathEffect(killer, player);

            if (arena.isReKitAfterKill()) {
                KitUtil.loadDefaultLadderKit(killer, TeamEnum.FFA, players.get(killer));
            }

            if (arena.isHealthResetOnKill()) {
                applyHealthResetOnKill(killer);
            }
        }

        if (arena.isLobbyAfterDeath()) {
            this.removePlayer(player);
        } else {
            PlayerUtil.setFightPlayer(player);
            KitUtil.loadDefaultLadderKit(player, TeamEnum.FFA, players.get(player));

            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                    teleportPlayer(player), 1L);
        }
    }

    private void playDeathEffect(Player killer, Player victim) {
        if (killer == null || victim == null) {
            return;
        }

        try {
            Profile killerProfile = fightPlayers.containsKey(killer)
                    ? fightPlayers.get(killer).getProfile()
                    : ProfileManager.getInstance().getProfile(killer);

            if (killerProfile == null || killerProfile.getCosmeticsData() == null) {
                return;
            }

            var deathEffect = killerProfile.getCosmeticsData().getDeathEffect();
            if (deathEffect == null) {
                return;
            }

            List<Player> viewers = new ArrayList<>(players.keySet());
            viewers.addAll(spectators);
            deathEffect.play(victim.getLocation(), viewers);
        } catch (Exception ignored) {
            // Cosmetic effects should never break FFA kill handling.
        }
    }

    private void applyHealthResetOnKill(Player killer) {
        AttributeInstance maxHealth = killer.getAttribute(Attribute.MAX_HEALTH);
        double maxHealthValue = maxHealth != null ? maxHealth.getValue() : 20.0D;
        killer.setHealth(Math.max(1.0D, maxHealthValue));
        killer.setFoodLevel(20);
        killer.setSaturation(20.0F);
        killer.setFireTicks(0);
        killer.setFallDistance(0.0F);
    }

    /**
     * Records that {@code attacker} last hit {@code victim}.
     * Called from damage listeners so void deaths can be attributed correctly.
     */
    public void recordAttack(Player victim, Player attacker) {
        if (victim == attacker) return;

        lastAttackerMap.put(victim.getUniqueId(), attacker.getUniqueId());
        lastAttackerTime.put(victim.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Returns the last player who hit {@code victim} within the expiry window,
     * or {@code null} if there is none.
     */
    public @org.jetbrains.annotations.Nullable Player getLastAttacker(Player victim) {
        Long time = lastAttackerTime.get(victim.getUniqueId());
        if (time == null || System.currentTimeMillis() - time > LAST_ATTACKER_EXPIRY_MS) return null;
        UUID attackerUuid = lastAttackerMap.get(victim.getUniqueId());
        if (attackerUuid == null) return null;
        for (Player p : players.keySet()) {
            if (attackerUuid.equals(p.getUniqueId())) return p;
        }
        return null;
    }

    public void teleportPlayer(Player player) {
        player.teleport(arena.getFfaPositions().get(random.nextInt(arena.getFfaPositions().size())));
    }

    public void sendMessage(String message, boolean spectator) {
        for (Player player : players.keySet()) {
            Common.sendMMMessage(player, message);
        }
        if (spectator) {
            for (Player spectatorPlayer : spectators) {
                Common.sendMMMessage(spectatorPlayer, message);
            }
        }
    }

    private void teleportStuckSpectatorsAfterRollback() {
        if (!this.open || !this.build || this.spectators.isEmpty()) {
            return;
        }

        List<Player> activePlayers = new ArrayList<>(this.players.keySet());

        for (Player spectator : new ArrayList<>(this.spectators)) {
            if (spectator == null || !spectator.isOnline()) {
                continue;
            }

            if (!dev.nandi0813.practice.manager.fight.util.PlayerUtil.isPlayerStuck(spectator)) {
                continue;
            }

            if (!activePlayers.isEmpty()) {
                spectator.teleport(activePlayers.get(random.nextInt(activePlayers.size())));
            } else if (!this.arena.getFfaPositions().isEmpty()) {
                spectator.teleport(this.arena.getFfaPositions().get(random.nextInt(this.arena.getFfaPositions().size())));
            } else {
                spectator.teleport(this.arena.getCuboid().getCenter().add(0, 1, 0));
            }
        }
    }

    @Override
    public FightChangeOptimized getFightChange() {
        if (this.getBuildRollback() == null)
            return null;
        return this.getBuildRollback().getFightChange();
    }

    @Override
    public void addSpectator(Player player, Player target, boolean teleport, boolean message) {
        if (this.spectators.contains(player)) {
            return;
        }

        FFASpectateStartEvent ffaSpectateStartEvent = new FFASpectateStartEvent(player, this);
        Bukkit.getPluginManager().callEvent(ffaSpectateStartEvent);
        if (ffaSpectateStartEvent.isCancelled()) {
            return;
        }

        // If the spectator was spectating another match/FFA, remove them first.
        Spectatable previousSpectatable = SpectatorManager.getInstance().getSpectators().get(player);
        if (previousSpectatable != null) {
            previousSpectatable.removeSpectator(player);
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        this.spectators.add(player);
        SpectatorManager.getInstance().getSpectators().put(player, this);
        profile.setStatus(ProfileStatus.SPECTATE);

        // Hide spectator from players.
        for (Player eventPlayer : this.players.keySet()) {
            PlayerHider.getInstance().hidePlayer(eventPlayer, player, false);
            PlayerHider.getInstance().showPlayer(player, eventPlayer);
        }

        // Hide spectators from each other.
        for (Player eventSpectator : this.spectators) {
            if (!eventSpectator.equals(player)) {
                PlayerHider.getInstance().hidePlayer(eventSpectator, player, false);
                PlayerHider.getInstance().hidePlayer(player, eventSpectator, false);
            }
        }

        if (target != null && this.players.containsKey(target)) {
            player.teleport(target);
        } else {
            if (players.isEmpty()) {
                this.teleportPlayer(player);
            } else {
                player.teleport(new ArrayList<>(players.keySet()).get(random.nextInt(players.size())));
            }
        }

        if (profile.isStaffMode()) {
            InventoryManager.getInstance().setStaffModeInventory(player);
            player.setFlySpeed((float) InventoryManager.STAFF_SPECTATOR_SPEED / 10);
        } else
            InventoryManager.getInstance().setInventory(player, Inventory.InventoryType.SPECTATE_FFA);

        if (message) {
            this.sendMessage(LanguageManager.getString("FFA.GAME.SPECTATE-START").replace("%player%", player.getName()), true);
        }

        SpectatorManager.getInstance().getSpectatorMenuGui().update();
    }

    @Override
    public void removeSpectator(Player player) {
        if (!this.spectators.contains(player)) {
            return;
        }

        this.spectators.remove(player);
        SpectatorManager.getInstance().getSpectators().remove(player);

        if (ZonePractice.getInstance().isEnabled() && player.isOnline()) {
            InventoryManager.getInstance().setLobbyInventory(player, true);
        }

        SpectatorManager.getInstance().getSpectatorMenuGui().update();

        FFASpectateEndEvent ffaSpectateEndEvent = new FFASpectateEndEvent(player, this);
        Bukkit.getPluginManager().callEvent(ffaSpectateEndEvent);
    }

    private static final String BUILD_ON = GUIFile.getString("GUIS.SPECTATOR-MENU.ICONS.FFA-ICON.BUILD-STATUS.ENABLED");
    private static final String BUILD_OFF = GUIFile.getString("GUIS.SPECTATOR-MENU.ICONS.FFA-ICON.BUILD-STATUS.DISABLED");

    @Override
    public GUIItem getSpectatorMenuItem() {
        return GUIFile.getGuiItem("GUIS.SPECTATOR-MENU.ICONS.FFA-ICON")
                .setMaterial(arena.getIcon().getType())
                .setDamage(Common.getItemDamage(arena.getIcon()))
                .replace("%players%", String.valueOf(players.size()))
                .replace("%spectators%", String.valueOf(spectators.size()))
                .replace("%arena%", arena.getDisplayName())
                .replace("%build_status%", arena.isBuild() ? BUILD_ON : BUILD_OFF);
    }

    @Override
    public List<Player> getActivePlayerList() {
        return new ArrayList<>(players.keySet());
    }

    @Override
    public boolean canDisplay() {
        return this.open;
    }

    @Override
    public boolean isBuild() {
        return this.build;
    }

    @Override
    public Cuboid getCuboid() {
        return arena.getCuboid();
    }

}
