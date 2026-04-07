package dev.nandi0813.practice.manager.fight.match.bot;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.bot.neural.BotSpawnerUtil;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.nandi0813.practice.manager.nametag.NametagManager;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.server.sound.SoundManager;
import dev.nandi0813.practice.manager.server.sound.SoundType;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

@Getter
public class BotMatch extends Match implements Team {

    public static final String BOT_DISPLAY_NAME = "PvP Bot";

    private NPC botNpc;
    private int spawnedRoundNumber = -1;

    private final Player player;
    private Object matchWinner;

    public BotMatch(Ladder ladder, Arena arena, Player player, int winsNeeded) {
        super(ladder, arena, Collections.singletonList(player), winsNeeded);

        this.type   = MatchType.BOT_DUEL;
        this.player = player;

        NametagManager.getInstance().setNametag(
                player,
                TeamEnum.TEAM1.getPrefix(),
                TeamEnum.TEAM1.getNameColor(),
                TeamEnum.TEAM1.getSuffix(),
                20);
    }

    @Override
    public void startNextRound() {
        BotMatchRound round = new BotMatchRound(this, this.rounds.size() + 1);
        this.rounds.put(round.getRoundNumber(), round);
        despawnBot();

        for (String line : LanguageManager.getList("MATCH.BOT-DUEL.MATCH-START")) {
            this.sendMessage(
                    line.replace("%ladder%", ladder.getDisplayName())
                        .replace("%arena%",  arena.getDisplayName())
                        .replace("%player%", player.getName()),
                    false);
        }

        round.startRound();
        spawnBot(arena.getPosition2() != null ? arena.getPosition2() : arena.getPosition1(), round.getRoundNumber());
    }

    @Override
    public BotMatchRound getCurrentRound() {
        return (BotMatchRound) this.rounds.get(this.rounds.size());
    }

    @Override
    public int getWonRounds(Player player) {
        int won = 0;
        for (Round r : this.rounds.values()) {
            if (((BotMatchRound) r).getRoundWinner() == player) won++;
        }
        return won;
    }

    @Override
    public void teleportPlayer(Player player) {
        player.teleport(arena.getPosition1());
    }

    @Override
    protected void killPlayer(Player player, String deathMessage) {
        DeathResult result = handleLadderDeath(player);

        switch (result) {
            case TEMPORARY_DEATH:
                asRespawnableLadder().ifPresent(r ->
                        new dev.nandi0813.practice.manager.fight.match.util.TempKillPlayer(
                                getCurrentRound(), player, r.getRespawnTime()));
                dev.nandi0813.practice.manager.fight.util.PlayerUtil.clearInventory(player);
                player.setHealth(20);
                SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_TEMP_DEATH).play(this.getPeople());
                break;

            case ELIMINATED:
            default:
                this.getCurrentStat(player).end(true);
                PlayerUtil.setFightPlayer(player);
                dev.nandi0813.practice.manager.fight.util.PlayerUtil.clearInventory(player);
                player.setHealth(20);
                SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());
                getCurrentRound().endRound();
                break;
        }
    }

    @Override
    public void removePlayer(Player player, boolean quit) {
        if (!players.contains(player)) return;

        players.remove(player);
        MatchManager.getInstance().getPlayerMatches().remove(player);

        if (players.isEmpty()) {
            despawnBot();
        }

        if (quit && !this.status.equals(MatchStatus.END) && !this.status.equals(MatchStatus.OVER)) {
            this.getCurrentStat(player).end(true);
            this.sendMessage(
                    TeamUtil.replaceTeamNames(
                            LanguageManager.getString("MATCH.BOT-DUEL.PLAYER-LEFT"),
                            player, TeamEnum.TEAM1),
                    true);
            getCurrentRound().endRound();
        }

        this.removePlayerFromBelowName(player);

        if (ZonePractice.getInstance().isEnabled() && player.isOnline()) {
            dev.nandi0813.practice.manager.profile.Profile profile =
                    ProfileManager.getInstance().getProfile(player);
            profile.setUnrankedLeft(profile.getUnrankedLeft() - 1);
            InventoryManager.getInstance().setLobbyInventory(player, true);
        }
    }

    @Override
    public Object getMatchWinner() { return matchWinner; }

    @Override
    public boolean isEndMatch() {
        if (this.status.equals(MatchStatus.END)) return true;
        if (this.players.isEmpty()) { this.matchWinner = null; return true; }
        if (getWonRounds(player) >= winsNeeded) { this.matchWinner = player; return true; }

        int botWins = 0;
        for (Round r : rounds.values()) {
            if (((BotMatchRound) r).getRoundWinner() == null) botWins++;
        }
        if (botWins >= winsNeeded) { this.matchWinner = null; return true; }

        return false;
    }

    @Override
    public void endMatch() {
        try {
            super.endMatch();
        } finally {
            despawnBot();
        }
    }

    @Override
    public TeamEnum getTeam(Player player) { return TeamEnum.TEAM1; }

    private void spawnBot(Location spawnLoc, int roundNumber) {
        if (spawnedRoundNumber == roundNumber && botNpc != null && botNpc.isSpawned()) {
            return;
        }

        despawnBot();
        NPC npc = BotSpawnerUtil.spawnNeuralBot(ZonePractice.getInstance(), spawnLoc, player, this);
        spawnedRoundNumber = roundNumber;
        this.botNpc = npc;

        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> applyLoadoutAfterSpawn(npc));

        if (npc.getEntity() != null) {
            for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    ZonePractice.getEntityHider().hideEntity(online, npc.getEntity());
                }
            }
        }
    }

    private void applyLoadoutAfterSpawn(NPC npc) {
        if (botNpc == null || botNpc != npc || !npc.isSpawned()) {
            return;
        }
        if (!(npc.getEntity() instanceof Player botPlayer)) {
            return;
        }

        ItemStack[] storage = ladder.getKitData().getStorage();
        if (storage != null) {
            for (int i = 0; i < Math.min(storage.length, 36); i++) {
                botPlayer.getInventory().setItem(i, cloneItem(storage[i]));
            }
        }

        ItemStack[] armor = ladder.getKitData().getArmor();
        if (armor != null) {
            Equipment equipment = npc.getOrAddTrait(Equipment.class);
            equipment.set(Equipment.EquipmentSlot.BOOTS, cloneArmorSlot(armor, 0));
            equipment.set(Equipment.EquipmentSlot.LEGGINGS, cloneArmorSlot(armor, 1));
            equipment.set(Equipment.EquipmentSlot.CHESTPLATE, cloneArmorSlot(armor, 2));
            equipment.set(Equipment.EquipmentSlot.HELMET, cloneArmorSlot(armor, 3));

            // Keep Bukkit inventory in sync for combat state export.
            ItemStack[] clonedArmor = new ItemStack[4];
            for (int i = 0; i < Math.min(armor.length, 4); i++) {
                clonedArmor[i] = cloneItem(armor[i]);
            }
            botPlayer.getInventory().setArmorContents(clonedArmor);
        }
    }

    private void despawnBot() {
        if (botNpc == null) return;
        final NPC npc = botNpc;
        botNpc = null;
        spawnedRoundNumber = -1;

        Runnable cleanup = () -> {
            if (npc.isSpawned()) {
                npc.despawn();
            }
            npc.destroy();
            net.citizensnpcs.api.CitizensAPI.getNPCRegistry().deregister(npc);
        };

        if (Bukkit.isPrimaryThread()) {
            cleanup.run();
        } else {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), cleanup);
        }
    }

    private static ItemStack cloneArmorSlot(ItemStack[] armor, int index) {
        if (armor == null || index < 0 || index >= armor.length) {
            return null;
        }
        return cloneItem(armor[index]);
    }

    private static ItemStack cloneItem(ItemStack itemStack) {
        return itemStack == null ? null : itemStack.clone();
    }

    public void onBotDied(Player humanWinner) {
        if (!status.equals(MatchStatus.LIVE)) {
            return;
        }
        SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());
        getCurrentRound().setRoundWinner(humanWinner != null ? humanWinner : player);
        getCurrentRound().endRound();
    }
}

