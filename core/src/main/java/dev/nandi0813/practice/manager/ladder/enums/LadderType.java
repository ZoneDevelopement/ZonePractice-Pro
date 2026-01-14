package dev.nandi0813.practice.manager.ladder.enums;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.ladder.type.*;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum LadderType {

    BASIC(
            LanguageManager.getString("LADDER.LADDER-TYPES.BASIC.NAME"),
            Material.DIRT,
            false,
            true,
            LanguageManager.getList("LADDER.LADDER-TYPES.BASIC.DESCRIPTION"),
            Basic.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.START_MOVING,
                            SettingType.DROP_INVENTORY_TEAM,
                            SettingType.EDITABLE,
                            SettingType.ENDER_PEARL_COOLDOWN,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.MULTI_ROUND_START_COUNTDOWN,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.MAX_DURATION,
                            SettingType.START_COUNTDOWN,
                            SettingType.HEALTH_BELOW_NAME
                    )
            ),
            false,
            false
    ),
    BUILD(
            LanguageManager.getString("LADDER.LADDER-TYPES.BUILD.NAME"),
            Material.STONE_PICKAXE,
            true,
            true,
            LanguageManager.getList("LADDER.LADDER-TYPES.BUILD.DESCRIPTION"),
            Build.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.START_MOVING,
                            SettingType.DROP_INVENTORY_TEAM,
                            SettingType.EDITABLE,
                            SettingType.ENDER_PEARL_COOLDOWN,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.MULTI_ROUND_START_COUNTDOWN,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.MAX_DURATION,
                            SettingType.START_COUNTDOWN,
                            SettingType.TNT_FUSE_TIME,
                            SettingType.HEALTH_BELOW_NAME
                    )
            ),
            false,
            false
    ),
    SUMO(
            LanguageManager.getString("LADDER.LADDER-TYPES.SUMO.NAME"),
            Material.STICK,
            false,
            true,
            LanguageManager.getList("LADDER.LADDER-TYPES.SUMO.DESCRIPTION"),
            Sumo.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.DROP_INVENTORY_TEAM,
                            SettingType.EDITABLE,
                            SettingType.ENDER_PEARL_COOLDOWN,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.MAX_DURATION,
                            SettingType.START_COUNTDOWN,
                            SettingType.HEALTH_BELOW_NAME
                    )
            ),
            false,
            false
    ),
    BOXING(
            LanguageManager.getString("LADDER.LADDER-TYPES.BOXING.NAME"),
            Material.DIAMOND_CHESTPLATE,
            false,
            true,
            LanguageManager.getList("LADDER.LADDER-TYPES.BOXING.DESCRIPTION"),
            Boxing.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.START_MOVING,
                            SettingType.EDITABLE,
                            SettingType.ENDER_PEARL_COOLDOWN,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.MULTI_ROUND_START_COUNTDOWN,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.START_COUNTDOWN,
                            SettingType.MAX_DURATION,
                            SettingType.BOXING_HITS
                    )
            ),
            false,
            false
    ),
    PEARL_FIGHT(
            LanguageManager.getString("LADDER.LADDER-TYPES.PEARL-FIGHT.NAME"),
            Material.ENDER_PEARL,
            true,
            true,
            LanguageManager.getList("LADDER.LADDER-TYPES.PEARL-FIGHT.DESCRIPTION"),
            PearlFight.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.START_MOVING,
                            SettingType.EDITABLE,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.START_COUNTDOWN,
                            SettingType.MAX_DURATION,
                            SettingType.TEMP_BUILD_DELAY,
                            SettingType.TNT_FUSE_TIME
                    )
            ),
            false,
            false
    ),
    SPLEEF(
            LanguageManager.getString("LADDER.LADDER-TYPES.SPLEEF.NAME"),
            ClassImport.getClasses().getItemMaterialUtil().getIronShovel(),
            true,
            true,
            LanguageManager.getList("LADDER.LADDER-TYPES.SPLEEF.DESCRIPTION"),
            Spleef.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.START_MOVING,
                            SettingType.REGENERATION,
                            SettingType.HUNGER,
                            SettingType.MULTI_ROUND_START_COUNTDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.KNOCKBACK,
                            SettingType.WEIGHT_CLASS,
                            SettingType.ROUNDS,
                            SettingType.MAX_DURATION,
                            SettingType.START_COUNTDOWN
                    )
            ),
            false,
            false
    ),
    SKYWARS(
            LanguageManager.getString("LADDER.LADDER-TYPES.SKYWARS.NAME"),
            ClassImport.getClasses().getItemMaterialUtil().getEyeOfEnder(),
            true,
            true,
            LanguageManager.getList("LADDER.LADDER-TYPES.SKYWARS.DESCRIPTION"),
            SkyWars.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.MULTI_ROUND_START_COUNTDOWN,
                            SettingType.DROP_INVENTORY_TEAM,
                            SettingType.EDITABLE,
                            SettingType.ENDER_PEARL_COOLDOWN,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.START_COUNTDOWN,
                            SettingType.MAX_DURATION,
                            SettingType.SKYWARS_LOOT,
                            SettingType.TNT_FUSE_TIME,
                            SettingType.HEALTH_BELOW_NAME
                    )
            ),
            false,
            false
    ),
    BEDWARS(
            LanguageManager.getString("LADDER.LADDER-TYPES.BEDWARS.NAME"),
            ClassImport.getClasses().getItemMaterialUtil().getRedBed(),
            true,
            false,
            LanguageManager.getList("LADDER.LADDER-TYPES.BEDWARS.DESCRIPTION"),
            BedWars.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.MULTI_ROUND_START_COUNTDOWN,
                            SettingType.EDITABLE,
                            SettingType.ENDER_PEARL_COOLDOWN,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.START_COUNTDOWN,
                            SettingType.MAX_DURATION,
                            SettingType.RESPAWN_TIME,
                            SettingType.TNT_FUSE_TIME,
                            SettingType.HEALTH_BELOW_NAME
                    )
            ),
            true,
            false
    ),
    FIREBALL_FIGHT(
            LanguageManager.getString("LADDER.LADDER-TYPES.FIREBALL-FIGHT.NAME"),
            ClassImport.getClasses().getItemMaterialUtil().getFireball(),
            true,
            false,
            LanguageManager.getList("LADDER.LADDER-TYPES.FIREBALL-FIGHT.DESCRIPTION"),
            FireballFight.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.MULTI_ROUND_START_COUNTDOWN,
                            SettingType.EDITABLE,
                            SettingType.ENDER_PEARL_COOLDOWN,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.START_COUNTDOWN,
                            SettingType.RESPAWN_TIME,
                            SettingType.MAX_DURATION,
                            SettingType.FIREBALL_COOLDOWN,
                            SettingType.TNT_FUSE_TIME,
                            SettingType.HEALTH_BELOW_NAME
                    )
            ),
            true,
            false
    ),
    BRIDGES(
            LanguageManager.getString("LADDER.LADDER-TYPES.BRIDGES.NAME"),
            ClassImport.getClasses().getItemMaterialUtil().getStainedClay(),
            true,
            false,
            LanguageManager.getList("LADDER.LADDER-TYPES.BRIDGES.DESCRIPTION"),
            Bridges.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.MULTI_ROUND_START_COUNTDOWN,
                            SettingType.EDITABLE,
                            SettingType.ENDER_PEARL_COOLDOWN,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.MAX_DURATION,
                            SettingType.START_COUNTDOWN,
                            SettingType.RESPAWN_TIME,
                            SettingType.TNT_FUSE_TIME,
                            SettingType.HEALTH_BELOW_NAME
                    )
            ),
            false,
            true
    ),
    BATTLE_RUSH(
            LanguageManager.getString("LADDER.LADDER-TYPES.BATTLE-RUSH.NAME"),
            ClassImport.getClasses().getItemMaterialUtil().getLilyPad(),
            true,
            false,
            LanguageManager.getList("LADDER.LADDER-TYPES.BATTLE-RUSH.DESCRIPTION"),
            BattleRush.class,
            new ArrayList<>(
                    Arrays.asList(
                            SettingType.MULTI_ROUND_START_COUNTDOWN,
                            SettingType.EDITABLE,
                            SettingType.ENDER_PEARL_COOLDOWN,
                            SettingType.GOLDEN_APPLE_COOLDOWN,
                            SettingType.HIT_DELAY,
                            SettingType.HUNGER,
                            SettingType.KNOCKBACK,
                            SettingType.WEIGHT_CLASS,
                            SettingType.REGENERATION,
                            SettingType.ROUNDS,
                            SettingType.START_COUNTDOWN,
                            SettingType.RESPAWN_TIME,
                            SettingType.MAX_DURATION,
                            SettingType.TEMP_BUILD_DELAY,
                            SettingType.TNT_FUSE_TIME,
                            SettingType.HEALTH_BELOW_NAME
                    )
            ),
            false,
            true
    );

    private final String name;
    private final List<String> description;

    @Getter
    private final Material icon;
    @Getter
    private final boolean build;
    @Getter
    private final boolean isPartyFFASupported;
    @Getter
    private final Class<?> classInstance;
    @Getter
    private final List<SettingType> settingTypes;
    @Getter
    private final boolean bed;
    @Getter
    private final boolean portal;

    LadderType(String name, Material icon, boolean build, boolean isPartyFFASupported, List<String> description, Class<?> classInstance, List<SettingType> settingTypes, boolean bed, boolean portal) {
        this.name = name;
        this.icon = icon;
        this.build = build;
        this.isPartyFFASupported = isPartyFFASupported;
        this.description = description;
        this.classInstance = classInstance;
        this.settingTypes = settingTypes;
        this.bed = bed;
        this.portal = portal;
    }

    public String getName() {
        return Common.mmToNormal(this.name);
    }

    public List<String> getDescription() {
        return Common.mmToNormal(this.description);
    }

}
