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
            this.buildRollback = new BuildRollback(new FightChangeOptimized(this));
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

    public void removePlayer(Player player) {
        if (!players.containsKey(player))
            return;

        Bukkit.getPluginManager().callEvent(new FFARemovePlayerEvent(this, player));

        this.sendMessage(LanguageManager.getString("FFA.GAME.PLAYER-LEAVE").replace("%player%", player.getName()), true);

        players.remove(player);
        fightPlayers.remove(player);
        statistics.remove(player);

        InventoryManager.getInstance().setLobbyInventory(player, true);
        SpectatorManager.getInstance().getSpectatorMenuGui().update();
    }

    public void killPlayer(Player player, Player killer, String deathMessage) {
        if (!players.containsKey(player))
            return;

        fightPlayers.get(player).die(deathMessage, statistics.get(player));
        fightPlayers.get(player).getProfile().getStats().getLadderStat(players.get(player)).increaseDeaths();

        if (killer != null) {
            fightPlayers.get(killer).getProfile().getStats().getLadderStat(players.get(killer)).increaseKills();

            if (arena.isReKitAfterKill()) {
                KitUtil.loadDefaultLadderKit(killer, TeamEnum.FFA, players.get(killer));
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
                .setDamage(arena.getIcon().getDurability())
                .replace("%players%", String.valueOf(players.size()))
                .replace("%spectators%", String.valueOf(spectators.size()))
                .replace("%arena%", arena.getDisplayName())
                .replace("%build_status%", arena.isBuild() ? BUILD_ON : BUILD_OFF);
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
