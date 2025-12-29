package dev.nandi0813.practice.manager.gui.guis;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class MatchStatsGui extends GUI {

    private final Match match;
    private final UUID uuid;
    private final OfflinePlayer player;
    private final Map<Integer, Statistic> stats = new HashMap<>();

    public MatchStatsGui(Match match, UUID uuid) {
        super(GUIType.MatchStatGui);
        this.match = match;
        this.uuid = uuid;
        this.player = Bukkit.getOfflinePlayer(uuid);

        for (Round round : match.getRounds().values())
            stats.put(round.getRoundNumber(), round.getStatistics().get(uuid));

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        if (!ZonePractice.getInstance().isEnabled()) return;

        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            int round = 1;
            while (stats.containsKey(round)) {
                String title;
                if (match.getLadder().getRounds() == 1)
                    title = GUIFile.getString("GUIS.MATCH-STATISTICS.TITLE.SINGLE-ROUND").replace("%player%", player.getName());
                else
                    title = GUIFile.getString("GUIS.MATCH-STATISTICS.TITLE.MULTIPLE-ROUND").replace("%player%", player.getName()).replace("%round%", String.valueOf(round));
                gui.put(round, InventoryUtil.createInventory(title, 6));

                Statistic roundStatistic = stats.get(round);
                Inventory inventory = gui.get(round);

                // Inventory Content
                List<ItemStack> inventoryContent = Arrays.asList(roundStatistic.getEndInventory());
                List<ItemStack> firstLine = new ArrayList<>();
                int healthPotionsLeft = 0;
                for (int i = 0; i < 36; i++) {
                    if (i < 9)
                        firstLine.add(inventoryContent.get(i));

                    if (i < 27) inventory.setItem(i, inventoryContent.get(i + 9));
                    else inventory.setItem(i, firstLine.get(i - 27));

                    if (inventoryContent.get(i) != null && inventoryContent.get(i).getType().equals(Material.POTION) && inventoryContent.get(i).getDurability() == 16421)
                        healthPotionsLeft++;
                }

                // Armor Content
                List<ItemStack> armor = Arrays.asList(roundStatistic.getEndArmor());
                for (int i = 36; i <= 39; i++)
                    inventory.setItem(i, armor.get(i - 36));

                if (roundStatistic.getPotionThrown() != 0)
                    inventory.setItem(47, getPotionItem(roundStatistic, healthPotionsLeft));
                inventory.setItem(48, getHealthItem(roundStatistic.getEndHeart()));
                inventory.setItem(49, getFoodItem(roundStatistic.getEndHunger()));
                inventory.setItem(50, getEffectItem(roundStatistic));
                inventory.setItem(51, getStatsItem(roundStatistic));

                if (match.getLadder().getRounds() != 1)
                    inventory.setItem(53, getNextRoundItem(stats.containsKey(round + 1) ? round + 1 : 1));

                round++;
            }

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int round = inGuiPlayers.get(player);

        e.setCancelled(true);

        if (e.getRawSlot() == 53 && e.getCurrentItem() != null)
            player.performCommand("matchinv " + match.getId() + " " + uuid + " " + (stats.containsKey(round + 1) ? round + 1 : 1));
    }


    private ItemStack getPotionItem(Statistic roundStatistic, int healthPotionsLeft) {
        return GUIFile.getGuiItem("GUIS.MATCH-STATISTICS.ICONS.POTION")
                .replaceAll("%potion_left%", String.valueOf(healthPotionsLeft))
                .replaceAll("%potion_thrown%", String.valueOf(roundStatistic.getPotionThrown()))
                .replaceAll("%potion_missed%", String.valueOf(roundStatistic.getPotionMissed()))
                .replaceAll("%potion_accuracy%", String.valueOf(roundStatistic.getPotionAccuracy()))
                .setAmount(healthPotionsLeft >= 1 && healthPotionsLeft <= 64 ? healthPotionsLeft : 1)
                .get();
    }

    private ItemStack getHealthItem(double endHearth) {
        return GUIFile.getGuiItem("GUIS.MATCH-STATISTICS.ICONS.HEALTH")
                .replaceAll("%end_hearth%", String.valueOf(endHearth))
                .get();
    }

    private ItemStack getFoodItem(double endHunger) {
        return GUIFile.getGuiItem("GUIS.MATCH-STATISTICS.ICONS.HUNGER")
                .replaceAll("%end_hunger%", String.valueOf(endHunger))
                .get();
    }

    private ItemStack getEffectItem(Statistic roundStatistic) {
        if (!roundStatistic.getEndPotionEffects().isEmpty()) {
            List<String> effects = new ArrayList<>();
            for (PotionEffect potionEffect : roundStatistic.getEndPotionEffects()) {
                effects.add(GUIFile.getString("GUIS.MATCH-STATISTICS.ICONS.EFFECT.HAS-EFFECT.FORMAT")
                        .replaceAll("%name%", StringUtils.capitalize(potionEffect.getType().getName().replace("_", " ").toLowerCase()))
                        .replaceAll("%amplifier%", String.valueOf(potionEffect.getAmplifier() + 1))
                        .replaceAll("%time%", StringUtil.formatMillisecondsToMinutes((potionEffect.getDuration() / 20) * 1000L))
                );
            }

            List<String> lore = new ArrayList<>();
            for (String line : GUIFile.getStringList("GUIS.MATCH-STATISTICS.ICONS.EFFECT.HAS-EFFECT.ICON.LORE")) {
                if (line.contains("%effects%"))
                    lore.addAll(effects);
                else
                    lore.add(line);
            }

            ItemStack item = GUIFile.getGuiItem("GUIS.MATCH-STATISTICS.ICONS.EFFECT.HAS-EFFECT.ICON").get();
            ItemMeta itemMeta = item.getItemMeta();

            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            return item;
        } else
            return GUIFile.getGuiItem("GUIS.MATCH-STATISTICS.ICONS.EFFECT.NO-EFFECT").get();
    }

    private ItemStack getStatsItem(Statistic roundStatistic) {
        return GUIFile.getGuiItem("GUIS.MATCH-STATISTICS.ICONS.STATS")
                .replaceAll("%total_hits%", String.valueOf(roundStatistic.getHit()))
                .replaceAll("%total_hits_received%", String.valueOf(roundStatistic.getGetHit()))
                .replaceAll("%longest_combo%", String.valueOf(roundStatistic.getLongestCombo()))
                .replaceAll("%avarage_cps%", String.valueOf(roundStatistic.getAverageCPS()))
                .get();
    }

    private ItemStack getNextRoundItem(int nextRound) {
        return GUIFile.getGuiItem("GUIS.MATCH-STATISTICS.ICONS.VIEW-ROUND")
                .replaceAll("%round%", String.valueOf(nextRound))
                .get();
    }

}
