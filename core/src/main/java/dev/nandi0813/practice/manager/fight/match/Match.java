package dev.nandi0813.practice.manager.fight.match;

import dev.nandi0813.api.Event.Match.MatchEndEvent;
import dev.nandi0813.api.Event.Match.MatchStartEvent;
import dev.nandi0813.api.Event.Spectate.End.MatchSpectateEndEvent;
import dev.nandi0813.api.Event.Spectate.Start.MatchSpectateStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.NormalArena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.*;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.match.util.MatchFightPlayer;
import dev.nandi0813.practice.manager.fight.match.util.MatchUtil;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.guis.MatchStatsGui;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.RespawnableLadder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.ScoringLadder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.module.interfaces.ChangedBlock;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public abstract class Match extends BukkitRunnable implements Spectatable, dev.nandi0813.api.Interface.Match {

    // Match ID
    protected final String id;
    protected MatchType type;

    // Basic match variables
    protected final NormalArena arena;
    protected final Ladder ladder;
    protected final Cuboid sideBuildLimit;

    // Duration
    protected int duration = 0;

    // Round
    protected final int winsNeeded;
    protected final Map<Integer, Round> rounds = new HashMap<>();

    // Player variables
    protected final Map<Player, MatchFightPlayer> matchPlayers = new HashMap<>();
    protected final List<Player> players; // List of the in game players
    protected final Map<UUID, MatchStatsGui> matchStatsGuis = new HashMap<>();

    // Spectator variables
    private boolean allowSpectators = true;
    protected final List<Player> spectators = new ArrayList<>(); // List of the spectators

    // Fight change
    private final FightChangeOptimized fightChange;

    @Setter
    protected MatchStatus status;

    protected Match(final Ladder ladder, final Arena arena, final List<Player> players, final int winsNeeded) {
        this.id = MatchUtil.getMatchID();
        this.arena = arena.getAvailableArena();
        this.ladder = ladder;
        this.winsNeeded = winsNeeded;
        this.players = new ArrayList<>(players);
        for (Player player : players) {
            this.matchPlayers.put(player, new MatchFightPlayer(player, this));
            this.addPlayerToBelowName(player);
        }
        this.fightChange = new FightChangeOptimized(arena.getCuboid());

        if (arena.getSideBuildLimit() > 0)
            this.sideBuildLimit = MatchUtil.getSideBuildLimitCube(this.arena.getCuboid().clone(), arena.getSideBuildLimit());
        else
            this.sideBuildLimit = null;
    }

    public void startMatch() {
        MatchStartEvent matchStartEvent = new MatchStartEvent(this);
        Bukkit.getPluginManager().callEvent(matchStartEvent);
        if (matchStartEvent.isCancelled()) return;

        if (this.ladder.isBuild()) {
            this.arena.setAvailable(false);
        }

        for (Player player : this.players) {
            Profile profile = ProfileManager.getInstance().getProfile(player);

            profile.setStatus(ProfileStatus.MATCH);

            if (!profile.isAllowSpectate() && this.allowSpectators) {
                this.allowSpectators = false;
            }

            PlayerUtil.setFightPlayer(player);

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!this.players.contains(online)) {
                    PlayerHider.getInstance().hidePlayer(player, online, true);
                }
            }

            this.entityVanish(player);
        }

        this.status = MatchStatus.START;
        this.startNextRound();
        this.runTaskTimerAsynchronously(ZonePractice.getInstance(), 0, 20L);
    }

    public void sendMessage(String message, boolean spectator) {
        for (Player player : this.players) {
            Common.sendMMMessage(player, message);
        }

        if (spectator) {
            for (Player specPlayer : this.spectators) {
                Common.sendMMMessage(specPlayer, message);
            }
        }
    }

    public void entityVanish(Player player) {
        if (!ladder.isBuild()) {
            if (players.contains(player)) {
                for (Entity entity : arena.getCuboid().getEntities()) {
                    if (!(entity instanceof Player)) {
                        ClassImport.getClasses().getEntityHider().hideEntity(player, entity);
                    }
                }
            } else if (spectators.contains(player)) {
                for (Entity entity : arena.getCuboid().getEntities()) {
                    if (!(entity instanceof Player)) {
                        if (fightChange.containsEntity(entity))
                            ClassImport.getClasses().getEntityHider().showEntity(player, entity);
                        else
                            ClassImport.getClasses().getEntityHider().hideEntity(player, entity);
                    }
                }
            }
        }
    }

    public abstract void startNextRound();

    public abstract Round getCurrentRound();

    public abstract int getWonRounds(Player player);

    public abstract void teleportPlayer(Player player);

    public void killPlayer(Player player, Player killer, String deathMessage) {
        if (this.getCurrentStat(player).isSet())
            return;

        deathMessage = TeamUtil.replaceTeamNames(deathMessage, player, this instanceof Team team ? team.getTeam(player) : TeamEnum.FFA);
        matchPlayers.get(player).die(deathMessage, this.getCurrentStat(player));

        if (ladder instanceof NormalLadder) {
            if (killer != null) {
                matchPlayers.get(killer).getProfile().getStats().getLadderStat((NormalLadder) ladder).increaseKills();
            }
            matchPlayers.get(player).getProfile().getStats().getLadderStat((NormalLadder) ladder).increaseDeaths();
        }

        killPlayer(player, deathMessage);
    }

    protected abstract void killPlayer(Player player, String deathMessage);

    public abstract void removePlayer(Player player, boolean quit);

    public abstract Object getMatchWinner();

    public abstract boolean isEndMatch();

    public void endMatch() {
        if (ZonePractice.getInstance().isEnabled()) {
            Round round = this.getCurrentRound();
            if (!round.getRoundStatus().equals(RoundStatus.END)) {
                this.getCurrentRound().endRound();
            }
        }

        Bukkit.getPluginManager().callEvent(new MatchEndEvent(this));

        for (Player player : new ArrayList<>(players))
            removePlayer(player, false);

        for (Player spectator : new ArrayList<>(spectators))
            removeSpectator(spectator);

        // Reset the arena.
        resetMap();

        this.cancel();

        if (ZonePractice.getInstance().isEnabled()) {
            for (UUID uuid : rounds.get(1).getStatistics().keySet()) {
                matchStatsGuis.put(uuid, new MatchStatsGui(this, uuid));
            }
        }

        // Set arena to available
        this.arena.setAvailable(true);
    }

    /*
     * Statistics methods
     */
    public Statistic getCurrentStat(Player player) {
        return this.getCurrentRound().getStatistics().getOrDefault(
                ProfileManager.getInstance().getUuids().get(player), null);
    }

    /*
     * Ladder behavior helper methods
     * These methods provide convenient access to ladder-specific behaviors
     * through the new interface system.
     */

    /**
     * Checks if the current ladder supports player respawning.
     *
     * @return true if the ladder implements RespawnableLadder
     */
    public boolean isRespawnableLadder() {
        return ladder instanceof RespawnableLadder;
    }

    /**
     * Checks if the current ladder has custom scoring mechanics.
     *
     * @return true if the ladder implements ScoringLadder
     */
    public boolean isScoringLadder() {
        return ladder instanceof ScoringLadder;
    }

    /**
     * Gets the ladder as a RespawnableLadder if applicable.
     *
     * @return Optional containing the RespawnableLadder, or empty if not applicable
     */
    public Optional<RespawnableLadder> asRespawnableLadder() {
        return ladder instanceof RespawnableLadder r ? Optional.of(r) : Optional.empty();
    }

    /**
     * Gets the ladder as a ScoringLadder if applicable.
     *
     * @return Optional containing the ScoringLadder, or empty if not applicable
     */
    public Optional<ScoringLadder> asScoringLadder() {
        return ladder instanceof ScoringLadder s ? Optional.of(s) : Optional.empty();
    }

    /**
     * Handles player death using the ladder's respawn mechanics.
     * Returns the death result which indicates how the death should be processed.
     *
     * @param player The player who died
     * @return DeathResult indicating the outcome, or ELIMINATED if ladder doesn't support respawning
     */
    public DeathResult handleLadderDeath(Player player) {
        if (ladder instanceof RespawnableLadder respawnableLadder) {
            return respawnableLadder.handlePlayerDeath(player, this, getCurrentRound());
        }
        return DeathResult.ELIMINATED;
    }

    /**
     * Checks if the round should end based on ladder-specific scoring.
     *
     * @param triggerPlayer The player who triggered the scoring check
     * @return true if the round should end based on scoring conditions
     */
    public boolean shouldEndRoundByScoring(Player triggerPlayer) {
        if (ladder instanceof ScoringLadder scoringLadder) {
            return scoringLadder.shouldEndRound(this, getCurrentRound(), triggerPlayer);
        }
        return false;
    }

    /*
     * Spectator methods
     */
    private final Random random = new Random();

    public void addSpectator(Player player, Player target, boolean teleport, boolean message) {
        if (this.spectators.contains(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("SPECTATE.MATCH.ALREADY-SPECTATING"));
            return;
        }

        if (this.status.equals(MatchStatus.OVER)) {
            Common.sendMMMessage(player, LanguageManager.getString("SPECTATE.MATCH.MATCH-ENDED"));
            return;
        }

        if (!isAllowSpectators() && !player.hasPermission("zpp.bypass.spectate")) {
            Common.sendMMMessage(player, LanguageManager.getString("SPECTATE.MATCH.CANT-SPECTATE"));
            return;
        }

        // If the target is given and the target is not in the match, return
        if (target != null && !this.players.contains(target)) return;

        // Call the match spectate start event
        MatchSpectateStartEvent event = new MatchSpectateStartEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        // If the spectator was spectating a match, remove.
        Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(player);
        if (spectatable != null) {
            spectatable.removeSpectator(player);
        }

        this.spectators.add(player);
        SpectatorManager.getInstance().getSpectators().put(player, this);
        this.addPlayerToBelowName(player);

        if (message) {
            sendMessage(LanguageManager.getString("SPECTATE.MATCH.SPECTATE-START").replace("%player%", player.getName()), true);
        }

        entityVanish(player);

        if (target != null) {
            player.teleport(target);
        } else {
            player.teleport(players.get(random.nextInt(players.size())));
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.isStaffMode()) {
            InventoryManager.getInstance().setStaffModeInventory(player);
            player.setFlySpeed((float) InventoryManager.STAFF_SPECTATOR_SPEED / 10);
        } else {
            InventoryManager.getInstance().setInventory(player, Inventory.InventoryType.SPECTATE_MATCH);
        }

        SpectatorManager.getInstance().getSpectatorMenuGui().update();
    }

    public void removeSpectator(Player player) {
        if (!spectators.contains(player)) return;

        this.spectators.remove(player);
        SpectatorManager.getInstance().getSpectators().remove(player);
        this.removePlayerFromBelowName(player);

        if (!status.equals(MatchStatus.END) && !status.equals(MatchStatus.OVER)) {
            Bukkit.getPluginManager().callEvent(new MatchSpectateEndEvent(player, this));

            SpectatorManager.getInstance().getSpectatorMenuGui().update();
        }

        if (ZonePractice.getInstance().isEnabled() && player.isOnline()) {
            InventoryManager.getInstance().setLobbyInventory(player, true);
        }
    }

    public void addBlockChange(ChangedBlock changedBlock) {
        fightChange.addBlockChange(changedBlock);
    }

    public void addEntityChange(Entity entity) {
        fightChange.addEntityChange(entity);

        if (!ladder.isBuild()) {
            for (Player player : MatchManager.getInstance().getHidePlayers(this)) {
                ClassImport.getClasses().getEntityHider().hideEntity(player, entity);
            }
        }
    }

    public void addEntityChange(@NotNull List<Entity> entities) {
        for (Entity entity : entities)
            addEntityChange(entity);
    }

    public void resetMap() {
        // Make sure that the players can safely spawn back to the starting position.
        for (Location location : this.arena.getStandingLocations()) {
            MatchUtil.safePlayerTeleportBlock(location.getBlock().getRelative(BlockFace.DOWN));
        }

        if (ZonePractice.getInstance().isEnabled()) {
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                    fightChange.rollback(300, 100), 2L);
        } else {
            fightChange.rollback(300, 100);
        }
    }

    public List<Player> getPeople() {
        List<Player> people = new ArrayList<>();
        people.addAll(players);
        people.addAll(spectators);
        return people;
    }

    public void addPlayerToBelowName(Player player) {
        if (!this.ladder.isHealthBelowName()) {
            return;
        }

        MatchManager.getInstance().getBelowNameManager().initForUser(player);
    }

    public void removePlayerFromBelowName(Player player) {
        if (!this.ladder.isHealthBelowName()) {
            return;
        }

        MatchManager.getInstance().getBelowNameManager().cleanUpForUser(player);
    }

    @Override
    public void run() {
        Round currentRound = this.getCurrentRound();
        if (currentRound != null && currentRound.getRoundStatus().equals(RoundStatus.LIVE)) {
            this.duration++;

            if (ladder instanceof NormalLadder) {
                if (ladder.getMaxDuration() - 30 == this.duration) {
                    this.sendMessage(LanguageManager.getString("MATCH.MATCH-OVER-IN-30"), true);
                } else if (ladder.getMaxDuration() == this.duration) {
                    Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
                        this.setStatus(MatchStatus.END);
                        this.endMatch();
                    });
                }
            }
        }
    }

    public String getFormattedTime() {
        return StringUtil.formatMillisecondsToMinutes(duration * 1000L);
    }

    @Override
    public GUIItem getSpectatorMenuItem() {
        return GUIFile.getGuiItem("GUIS.SPECTATOR-MENU.ICONS.MATCH-ICON")
                .setMaterial(ladder.getIcon().getType())
                .setDamage(ladder.getIcon().getDurability())
                .replace("%match_id%", id)
                .replace("%weight_class%", ((this instanceof Duel && ((Duel) this).isRanked()) ? WeightClass.RANKED.getName() : WeightClass.UNRANKED.getName()))
                .replace("%match_type%", type.getName(false))
                .replace("%ladder%", ladder.getDisplayName())
                .replace("%arena%", arena.getDisplayName())
                .replace("%round%", String.valueOf(getCurrentRound().getRoundNumber()))
                .replace("%duration%", getCurrentRound().getFormattedTime())
                .replace("%roundDuration%", getCurrentRound().getFormattedTime())
                .replace("%matchDuration%", this.getFormattedTime())
                .replace("%spectators%", String.valueOf(spectators.size()));
    }

    @Override
    public boolean canDisplay() {
        return status.equals(MatchStatus.LIVE) && ladder instanceof NormalLadder;
    }

    @Override
    public Cuboid getCuboid() {
        return arena.getCuboid();
    }

}
