package dev.nandi0813.practice_1_8_8;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.module.interfaces.*;
import dev.nandi0813.practice_1_8_8.interfaces.LadderUtil;
import dev.nandi0813.practice_1_8_8.interfaces.MatchTntListener;
import dev.nandi0813.practice_1_8_8.interfaces.PlayerHiderUtil;
import dev.nandi0813.practice_1_8_8.listener.ArenaListener;
import dev.nandi0813.practice_1_8_8.listener.FFAListener;
import dev.nandi0813.practice_1_8_8.listener.MatchListener;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class Classes implements dev.nandi0813.practice.module.util.Classes {

    public WorldCreate worldCreate = new dev.nandi0813.practice_1_8_8.interfaces.WorldCreate();
    public PlayerHiderInterface playerHiderUtil = new PlayerHiderUtil();
    public ItemMaterialUtil itemMaterialUtil = new dev.nandi0813.practice_1_8_8.interfaces.ItemMaterialUtil();
    public LadderUtil ladderUtil = new LadderUtil();
    public ItemCreateUtil itemCreateUtil = new dev.nandi0813.practice_1_8_8.interfaces.ItemCreateUtil();
    public PlayerUtil playerUtil = new dev.nandi0813.practice_1_8_8.interfaces.PlayerUtil();
    public ArenaUtil arenaUtil = new dev.nandi0813.practice_1_8_8.interfaces.ArenaUtil();
    public ArenaCopyUtil arenaCopyUtil = new dev.nandi0813.practice_1_8_8.interfaces.ArenaCopyUtil();

    public BedUtil bedUtil = new dev.nandi0813.practice_1_8_8.interfaces.BedUtil();
    public EntityHider entityHider = new dev.nandi0813.practice_1_8_8.interfaces.EntityHider(ZonePractice.getInstance(), EntityHider.Policy.BLACKLIST);
    public StatisticListener statisticListener = new dev.nandi0813.practice_1_8_8.interfaces.StatisticListener();
    public ConfigItemProvider configItemProvider = new dev.nandi0813.practice_1_8_8.interfaces.LegacyConfigItemProvider();

    public Class<?> changedBlockClass = dev.nandi0813.practice_1_8_8.interfaces.ChangedBlock.class;
    public Class<?> kitDataClass = dev.nandi0813.practice_1_8_8.interfaces.KitData.class;
    public Class<?> actionBarClass = dev.nandi0813.practice_1_8_8.interfaces.ActionBar.class;

    public Classes() {
        Bukkit.getServer().getPluginManager().registerEvents(arenaCopyUtil, ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(statisticListener, ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new MatchListener(), ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new FFAListener(), ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new MatchTntListener(), ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new ArenaListener(), ZonePractice.getInstance());

        Bukkit.getServer().getPluginManager().registerEvents(new EPCountdownListener(), ZonePractice.getInstance());
    }

}
