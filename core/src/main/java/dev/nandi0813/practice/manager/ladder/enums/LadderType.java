package dev.nandi0813.practice.manager.ladder.enums;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.ladder.type.*;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

/**
 * Enum defining all available ladder types in the practice plugin.
 * Uses LadderTypeConfig builder for cleaner configuration.
 */
public enum LadderType {

    BASIC(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.BASIC.NAME",
            Material.DIRT,
                    "LADDER.LADDER-TYPES.BASIC.DESCRIPTION",
                    Basic.class
            )
            .withMovementSettings()
            .withTeamSettings()
            .withCommonSettings()
            .withPearlSettings()
    ),

    BUILD(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.BUILD.NAME",
            Material.STONE_PICKAXE,
                    "LADDER.LADDER-TYPES.BUILD.DESCRIPTION",
                    Build.class
            )
            .withBuild()
            .withMovementSettings()
            .withTeamSettings()
            .withCommonSettings()
            .withPearlSettings()
            .withBuildSettings()
    ),

    SUMO(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.SUMO.NAME",
            Material.STICK,
                    "LADDER.LADDER-TYPES.SUMO.DESCRIPTION",
                    Sumo.class
            )
            .withTeamSettings()
            .withCommonSettings()
            .withPearlSettings()
    ),

    BOXING(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.BOXING.NAME",
            Material.DIAMOND_CHESTPLATE,
                    "LADDER.LADDER-TYPES.BOXING.DESCRIPTION",
                    Boxing.class
            )
            .withMovementSettings()
            .withTeamSettings()
            .withCommonSettings()
            .withPearlSettings()
            .withSetting(SettingType.BOXING_HITS)
    ),

    PEARL_FIGHT(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.PEARL-FIGHT.NAME",
            Material.ENDER_PEARL,
                    "LADDER.LADDER-TYPES.PEARL-FIGHT.DESCRIPTION",
                    PearlFight.class
            )
            .withBuild()
            .withMovementSettings()
            .withCommonSettings()
            .withSettings(
                    SettingType.GOLDEN_APPLE_COOLDOWN,
                    SettingType.TEMP_BUILD_DELAY
            )
            .withBuildSettings()
    ),

    SPLEEF(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.SPLEEF.NAME",
            ClassImport.getClasses().getItemMaterialUtil().getIronShovel(),
                    "LADDER.LADDER-TYPES.SPLEEF.DESCRIPTION",
                    Spleef.class
            )
            .withBuild()
            .withMovementSettings()
            .withSettings(
                    SettingType.REGENERATION,
                    SettingType.HUNGER,
                    SettingType.MULTI_ROUND_START_COUNTDOWN,
                    SettingType.HIT_DELAY,
                    SettingType.KNOCKBACK,
                    SettingType.WEIGHT_CLASS,
                    SettingType.ROUNDS,
                    SettingType.MAX_DURATION,
                    SettingType.START_COUNTDOWN,
                    SettingType.SPLEEF_SNOWBALL_MODE
            )
    ),

    SKYWARS(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.SKYWARS.NAME",
            ClassImport.getClasses().getItemMaterialUtil().getEyeOfEnder(),
                    "LADDER.LADDER-TYPES.SKYWARS.DESCRIPTION",
                    SkyWars.class
            )
            .withBuild()
            .withTeamSettings()
            .withCommonSettings()
            .withPearlSettings()
            .withBuildSettings()
            .withSetting(SettingType.SKYWARS_LOOT)
    ),

    BEDWARS(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.BEDWARS.NAME",
            ClassImport.getClasses().getItemMaterialUtil().getRedBed(),
                    "LADDER.LADDER-TYPES.BEDWARS.DESCRIPTION",
                    BedWars.class
            )
            .withBuild()
            .withBed()
            .noPartyFFA()
            .withRespawnSettings()
            .withCommonSettings()
            .withPearlSettings()
            .withBuildSettings()
    ),

    FIREBALL_FIGHT(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.FIREBALL-FIGHT.NAME",
            ClassImport.getClasses().getItemMaterialUtil().getFireball(),
                    "LADDER.LADDER-TYPES.FIREBALL-FIGHT.DESCRIPTION",
                    FireballFight.class
            )
            .withBuild()
            .withBed()
            .noPartyFFA()
            .withRespawnSettings()
            .withCommonSettings()
            .withPearlSettings()
            .withBuildSettings()
            .withSetting(SettingType.FIREBALL_COOLDOWN)
    ),

    BRIDGES(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.BRIDGES.NAME",
            ClassImport.getClasses().getItemMaterialUtil().getStainedClay(),
                    "LADDER.LADDER-TYPES.BRIDGES.DESCRIPTION",
                    Bridges.class
            )
            .withBuild()
            .withPortal()
            .noPartyFFA()
            .withRespawnSettings()
            .withCommonSettings()
            .withPearlSettings()
            .withBuildSettings()
            .withSetting(SettingType.RESET_BUILD_AFTER_ROUND)
    ),

    BATTLE_RUSH(LadderTypeConfig.builder(
                    "LADDER.LADDER-TYPES.BATTLE-RUSH.NAME",
            ClassImport.getClasses().getItemMaterialUtil().getLilyPad(),
                    "LADDER.LADDER-TYPES.BATTLE-RUSH.DESCRIPTION",
                    BattleRush.class
            )
            .withBuild()
            .withPortal()
            .noPartyFFA()
            .withRespawnSettings()
            .withCommonSettings()
            .withPearlSettings()
            .withBuildSettings()
            .withSetting(SettingType.TEMP_BUILD_DELAY)
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

    LadderType(LadderTypeConfig config) {
        this.name = LanguageManager.getString(config.getNameKey());
        this.icon = config.getIcon();
        this.build = config.isBuild();
        this.isPartyFFASupported = config.isPartyFFASupported();
        this.description = LanguageManager.getList(config.getDescriptionKey());
        this.classInstance = config.getClassInstance();
        this.settingTypes = config.getSettingTypes();
        this.bed = config.isHasBed();
        this.portal = config.isHasPortal();
    }

    public String getName() {
        return Common.mmToNormal(this.name);
    }

    public List<String> getDescription() {
        return Common.mmToNormal(this.description);
    }

}
