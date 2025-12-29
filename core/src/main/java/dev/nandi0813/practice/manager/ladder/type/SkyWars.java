package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.util.MatchUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.ItemSerializationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Setter
public class SkyWars extends NormalLadder implements CustomConfig, LadderHandle {

    private ItemStack[] skyWarsLoot;
    private static final String SKYWARS_LOOT_PATH = "skywars-loot";

    public SkyWars(String name, LadderType type) {
        super(name, type);
        this.startMove = false;
    }

    @Override
    public boolean isReadyToEnable() {
        return icon != null && kitData.isSet() && !matchTypes.isEmpty() && skyWarsLoot != null;
    }

    @Override
    public void setCustomConfig(YamlConfiguration config) {
        config.set(SKYWARS_LOOT_PATH, ItemSerializationUtil.itemStackArrayToBase64(this.skyWarsLoot));
    }

    @Override
    public void getCustomConfig(YamlConfiguration config) {
        if (config.isString(SKYWARS_LOOT_PATH))
            this.skyWarsLoot = ItemSerializationUtil.itemStackArrayFromBase64(config.getString(SKYWARS_LOOT_PATH));
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof PlayerInteractEvent) {
            onSkyWarsChestLoot((PlayerInteractEvent) e, match, this);
            return true;
        }
        return false;
    }

    private static void onSkyWarsChestLoot(final @NotNull PlayerInteractEvent e, final @NotNull Match match, final @NotNull SkyWars ladder) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Player player = e.getPlayer();
        Block chest = e.getClickedBlock();

        if (chest == null || !chest.getType().equals(Material.CHEST)) return;

        e.setCancelled(true);

        if (match.getCurrentStat(player).isSet()) return;
        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        List<ItemStack> loot = MatchUtil.getRandomSkyWarsLoot(ladder);
        if (loot.isEmpty()) return;

        for (ItemStack item : loot) {
            if (item != null && !item.getType().equals(Material.AIR))
                match.addEntityChange(player.getWorld().dropItemNaturally(chest.getLocation(), item));
        }

        match.addBlockChange(ClassImport.createChangeBlock(chest));

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                chest.setType(Material.AIR), 2L);
    }

}
