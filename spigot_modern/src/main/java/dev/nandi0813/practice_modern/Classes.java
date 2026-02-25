package dev.nandi0813.practice_modern;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.module.interfaces.*;
import dev.nandi0813.practice_modern.interfaces.MatchTntListener;
import dev.nandi0813.practice_modern.interfaces.PlayerHiderUtil;
import dev.nandi0813.practice_modern.listener.*;
import dev.nandi0813.practice_modern.modern_version.ItemOffHand;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class Classes implements dev.nandi0813.practice.module.util.Classes {

    public WorldCreate worldCreate = new dev.nandi0813.practice_modern.interfaces.WorldCreate();
    public PlayerHiderInterface playerHiderUtil = new PlayerHiderUtil();
    public ItemMaterialUtil itemMaterialUtil = new dev.nandi0813.practice_modern.interfaces.ItemMaterialUtil();
    public LadderUtil ladderUtil = new dev.nandi0813.practice_modern.interfaces.LadderUtil();
    public ItemCreateUtil itemCreateUtil = new dev.nandi0813.practice_modern.interfaces.ItemCreateUtil();
    public PlayerUtil playerUtil = new dev.nandi0813.practice_modern.interfaces.PlayerUtil();
    public ArenaUtil arenaUtil = new dev.nandi0813.practice_modern.interfaces.ArenaUtil();
    public ArenaCopyUtil arenaCopyUtil = new dev.nandi0813.practice_modern.interfaces.ArenaCopy.ArenaCopyUtil();

    public BedUtil bedUtil = new dev.nandi0813.practice_modern.interfaces.BedUtil();
    public EntityHider entityHider = new dev.nandi0813.practice_modern.interfaces.EntityHider(ZonePractice.getInstance(), EntityHider.Policy.BLACKLIST);
    public StatisticListener statisticListener = new dev.nandi0813.practice_modern.interfaces.StatisticListener();
    public ConfigItemProvider configItemProvider = new dev.nandi0813.practice_modern.interfaces.ModernConfigItemProvider();

    public Class<?> changedBlockClass = dev.nandi0813.practice_modern.interfaces.ChangedBlock.class;
    public Class<?> kitDataClass = dev.nandi0813.practice_modern.interfaces.KitData.class;
    public Class<?> actionBarClass = dev.nandi0813.practice_modern.interfaces.ActionBar.class;

    public Classes() {
        Bukkit.getServer().getPluginManager().registerEvents(arenaCopyUtil, ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(statisticListener, ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new MatchListener(), ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new FFAListener(), ZonePractice.getInstance());

        // Only 1.20 stuff
        Bukkit.getServer().getPluginManager().registerEvents(new ItemOffHand(), ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new MatchTntListener(), ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new ArenaListener(), ZonePractice.getInstance());

        Bukkit.getServer().getPluginManager().registerEvents(new EPCountdownListener(), ZonePractice.getInstance());
        Bukkit.getServer().getPluginManager().registerEvents(new FireworkRocketCooldownListener(), ZonePractice.getInstance());
    }

}
