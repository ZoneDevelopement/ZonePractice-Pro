package dev.nandi0813.practice.manager.duel.bot;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.NormalArena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.duel.DuelManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.fight.match.listener.BotDeathListener;
import dev.nandi0813.practice.manager.fight.match.type.duel.BotDuel;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PvPBotManager {

    private static PvPBotManager instance;

    public static PvPBotManager getInstance() {
        if (instance == null) {
            instance = new PvPBotManager();
        }
        return instance;
    }

    private PvPBotManager() {
    }

    private boolean citizensReady;

    private final Map<UUID, NPC> spawnedBots = new HashMap<>();
    private final Map<UUID, UUID> botProfiles = new HashMap<>();

    public void initialize() {
        this.citizensReady = Bukkit.getPluginManager().isPluginEnabled("Citizens");
        if (!citizensReady) {
            Common.sendConsoleMMMessage("<yellow>[PvPBot] Citizens was not found. Bot duels are disabled.");
            return;
        }

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PvPBotTrait.class).withName(PvPBotTrait.TRAIT_NAME));
        Bukkit.getPluginManager().registerEvents(new BotDeathListener(), ZonePractice.getInstance());
    }

    public void shutdown() {
        for (NPC npc : new ArrayList<>(spawnedBots.values())) {
            if (npc.isSpawned()) {
                npc.despawn();
            }
            npc.destroy();
        }

        for (UUID botProfileId : new ArrayList<>(botProfiles.values())) {
            unregisterBotProfile(botProfileId);
        }

        spawnedBots.clear();
        botProfiles.clear();
    }

    public boolean startBotDuel(Player player) {
        if (!citizensReady) {
            Common.sendMMMessage(player, "<red>Citizens is required for bot duels.");
            return false;
        }

        NormalLadder ladder = LadderManager.getInstance().getEnabledLadders()
                .stream()
                .filter(value -> value.getMatchTypes().contains(MatchType.DUEL))
                .filter(value -> value.getKitData() != null && value.getKitData().isSet())
                .filter(value -> !value.getAvailableArenas().isEmpty())
                .findFirst()
                .orElse(null);

        if (ladder == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.DUEL.NO-AVAILABLE-ARENA"));
            return false;
        }

        Arena arena = LadderUtil.getAvailableArena(ladder);
        if (arena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.DUEL.NO-AVAILABLE-ARENA"));
            return false;
        }

        return startBotDuel(player, ladder, arena, ladder.getRounds());
    }

    public boolean startBotDuel(Player player, Ladder ladder, Arena arena, int rounds) {
        if (!citizensReady) {
            Common.sendMMMessage(player, "<red>Citizens is required for bot duels.");
            return false;
        }

        if (!(ladder instanceof NormalLadder normalLadder)) {
            Common.sendMMMessage(player, "<red>Only normal ladders are supported for bot duels.");
            return false;
        }

        Arena selectedArena = arena != null ? arena : LadderUtil.getAvailableArena(ladder);
        if (selectedArena == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.DUEL.NO-AVAILABLE-ARENA"));
            return false;
        }

        NormalArena activeArena = selectedArena.getAvailableArena();
        if (activeArena == null || activeArena.getPosition1() == null || activeArena.getPosition2() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.DUEL.NO-AVAILABLE-ARENA"));
            return false;
        }

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Bot_" + player.getName());
        npc.setProtected(false);

        Location npcSpawn = activeArena.getPosition2().clone();
        if (!npc.spawn(npcSpawn) || !(npc.getEntity() instanceof Player botPlayer)) {
            npc.destroy();
            Common.sendMMMessage(player, "<red>Could not spawn bot NPC.");
            return false;
        }

        UUID botProfileId = registerBotProfile(botPlayer);
        spawnedBots.put(botPlayer.getUniqueId(), npc);
        botProfiles.put(botPlayer.getUniqueId(), botProfileId);

        equipNpcFromKit(npc, normalLadder);

         PvPBotTrait trait = npc.getOrAddTrait(PvPBotTrait.class);
         trait.setControllerPlayerId(player.getUniqueId());
         trait.setTargetPlayerId(player.getUniqueId());
         trait.setArenaBounds(activeArena.getCuboid());

         // Apply the selected bot difficulty from DuelManager
         PvPBotTrait.BotDifficulty difficulty = DuelManager.getInstance().getBotDifficulty(player);
         trait.setDifficulty(difficulty);

        BotDuel duel = new BotDuel(ladder, selectedArena, player, botPlayer, npc, rounds);
        duel.startMatch();

        Common.sendMMMessage(player, "<green>Bot duel started against <yellow>" + npc.getName() + "</yellow>.");
        return true;
    }

    public void cleanupBot(BotDuel duel) {
        Player botPlayer = duel.getBotPlayer();
        if (botPlayer == null) {
            return;
        }

        UUID botId = botPlayer.getUniqueId();
        NPC npc = spawnedBots.remove(botId);
        if (npc != null) {
            if (npc.isSpawned()) {
                npc.despawn();
            }
            npc.destroy();
        }

        UUID botProfileId = botProfiles.remove(botId);
        if (botProfileId != null) {
            unregisterBotProfile(botProfileId);
        }
    }

    private UUID registerBotProfile(Player botPlayer) {
        UUID botId = botPlayer.getUniqueId();
        ProfileManager profileManager = ProfileManager.getInstance();

        profileManager.getUuids().put(botPlayer, botId);
        if (!profileManager.getProfiles().containsKey(botId)) {
            profileManager.getProfiles().put(botId, new Profile(botId));
        }

        return botId;
    }

    private void unregisterBotProfile(UUID botProfileId) {
        ProfileManager profileManager = ProfileManager.getInstance();
        profileManager.getProfiles().remove(botProfileId);
        profileManager.getUuids().entrySet().removeIf(entry -> Objects.equals(entry.getValue(), botProfileId));
    }

    private void equipNpcFromKit(NPC npc, NormalLadder ladder) {
        Equipment equipment = npc.getOrAddTrait(Equipment.class);

        ItemStack[] armor = ladder.getKitData().getArmor();
        if (armor != null && armor.length >= 4) {
            equipment.set(Equipment.EquipmentSlot.BOOTS, cloneOrAir(armor[0]));
            equipment.set(Equipment.EquipmentSlot.LEGGINGS, cloneOrAir(armor[1]));
            equipment.set(Equipment.EquipmentSlot.CHESTPLATE, cloneOrAir(armor[2]));
            equipment.set(Equipment.EquipmentSlot.HELMET, cloneOrAir(armor[3]));
        }

        ItemStack weapon = findBestWeapon(ladder.getKitData().getStorage());
        equipment.set(Equipment.EquipmentSlot.HAND, cloneOrAir(weapon));
    }

    private ItemStack findBestWeapon(ItemStack[] storage) {
        if (storage == null) {
            return new ItemStack(Material.IRON_SWORD);
        }

        ItemStack fallback = null;
        for (ItemStack itemStack : storage) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }

            Material type = itemStack.getType();
            String materialName = type.name();

            if (materialName.endsWith("SWORD")) {
                return itemStack;
            }

            if (fallback == null && materialName.endsWith("AXE")) {
                fallback = itemStack;
            }
        }

        return fallback != null ? fallback : new ItemStack(Material.IRON_SWORD);
    }

    private ItemStack cloneOrAir(ItemStack itemStack) {
        return itemStack == null ? new ItemStack(Material.AIR) : itemStack.clone();
    }
}

