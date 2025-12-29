package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.type.partyffa.PartyFFA;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.type.SkyWars;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.NumberUtil;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public enum MatchUtil {
    ;

    public static String getMatchID() {
        return "match-" + System.currentTimeMillis() + NumberUtil.getRandomNumber(100, 999);
    }

    public static boolean isLadderBedRelated(Ladder ladder) {
        LadderType ladderType = ladder.getType();
        return ladderType.equals(LadderType.BEDWARS) || ladderType.equals(LadderType.FIREBALL_FIGHT);
    }

    public static Cuboid getSideBuildLimitCube(Cuboid baseCube, int limit) {
        baseCube = baseCube.expand(Cuboid.CuboidDirection.North, -limit);
        baseCube = baseCube.expand(Cuboid.CuboidDirection.South, -limit);
        baseCube = baseCube.expand(Cuboid.CuboidDirection.West, -limit);
        return baseCube.expand(Cuboid.CuboidDirection.East, -limit);
    }

    /*
    public static HashMap<Integer, RoundStatistic> getOldMatchStatistics(UUID uuid, YamlConfiguration config)
    {
            HashMap<Integer, RoundStatistic> playerStat = new HashMap<>();

            for (int i = 1; i < 100; i++)
            {
                if (config.isConfigurationSection("stats." + i + "." + uuid))
                {
                    RoundStatistic roundStat = new RoundStatistic(uuid);
                    roundStat.setSet(true);
                    roundStat.setAverageCPS(config.getDouble("stats." + i + "." + uuid + ".averagecps"));
                    roundStat.setHit(config.getInt("stats." + i + "." + uuid + ".hit"));
                    roundStat.setGetHit(config.getInt("stats." + i + "." + uuid + ".gethit"));
                    roundStat.setLongestCombo(config.getInt("stats." + i + "." + uuid + ".longestcombo"));
                    roundStat.setEndHeart(config.getDouble("stats." + i + "." + uuid + ".heart"));
                    roundStat.setEndHunger(config.getDouble("stats." + i + "." + uuid + ".hunger"));
                    roundStat.setEndPotionEffects((List<PotionEffect>) config.getList("stats." + i + "." + uuid + ".potions"));
                    roundStat.setEndArmor(ItemSerializationUtil.itemStackArrayFromBase64(config.getString("stats." + i + "." + uuid + ".armor")));
                    roundStat.setEndInventory(ItemSerializationUtil.itemStackArrayFromBase64(config.getString("stats." + i + "." + uuid + ".inventory")));

                    playerStat.put(i, roundStat);
                }
                else
                    break;
            }

            return playerStat;
    }
     */
    public static List<ItemStack> getRandomSkyWarsLoot(SkyWars ladder) {
        if (ladder.getSkyWarsLoot() == null) return Collections.emptyList();

        List<ItemStack> allLoot = new ArrayList<>(Arrays.asList(ladder.getSkyWarsLoot().clone()));
        Collections.shuffle(allLoot);
        int random = (int) ((Math.random() * (10 - 4)) + 4);

        List<ItemStack> actualLoot = new ArrayList<>();
        for (int i = 0; i < random; i++)
            if (allLoot.get(i) != null) actualLoot.add(allLoot.get(i));

        return actualLoot;
    }

    public static int getRandomElo() {
        String[] changeInterval = ConfigManager.getString("QUEUE.RANKED.ELO-CHANGE").split("-");
        int min = Integer.parseInt(changeInterval[0]);
        int max = Integer.parseInt(changeInterval[1]);

        return (int) ((Math.random() * (max - min)) + min);
    }

    public static void safePlayerTeleportBlock(Block block) {
        if (block != null && block.getType().equals(Material.AIR))
            block.setType(Material.BEDROCK);
    }

    public static Player getBoxingTopPlayer(PartyFFA partyFFA, int rank) {
        if (partyFFA.getPlayers().size() < rank) return null;

        Map<Player, Integer> boxingHits = new HashMap<>();
        for (Player player : partyFFA.getPlayers()) {
            Statistic roundStatistic = partyFFA.getCurrentStat(player);
            if (roundStatistic != null)
                boxingHits.put(player, roundStatistic.getHit());
        }

        if (boxingHits.size() < rank) return null;

        return new ArrayList<>(PlayerUtil.sortByValue(boxingHits).keySet()).get(rank - 1);
    }

}
