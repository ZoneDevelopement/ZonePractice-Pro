package dev.nandi0813.practice.manager.ladder.abstraction.playercustom;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.playerkit.PlayerKitManager;
import dev.nandi0813.practice.manager.playerkit.guis.MainGUI;
import dev.nandi0813.practice.manager.profile.Profile;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CustomLadder extends Ladder {

    private static final String DEFAULT_NAME = PlayerKitManager.getInstance().getString("DEFAULT-SETTINGS.NAME");
    private static final boolean DEFAULT_REGEN = PlayerKitManager.getInstance().getBoolean("DEFAULT-SETTINGS.REGENERATION");
    private static final boolean DEFAULT_HUNGER = PlayerKitManager.getInstance().getBoolean("DEFAULT-SETTINGS.HUNGER");
    private static final boolean DEFAULT_BUILD = PlayerKitManager.getInstance().getBoolean("DEFAULT-SETTINGS.BUILD");
    private static final int DEFAULT_ROUNDS = PlayerKitManager.getInstance().getInt("DEFAULT-SETTINGS.ROUNDS");
    private static final int DEFAULT_HITDELAY = PlayerKitManager.getInstance().getInt("DEFAULT-SETTINGS.HITDELAY");
    private static final int DEFAULT_EP_COOLDOWN = PlayerKitManager.getInstance().getInt("DEFAULT-SETTINGS.EP_COOLDOWN");
    private static final int DEFAULT_GA_COOLDOWN = PlayerKitManager.getInstance().getInt("DEFAULT-SETTINGS.GA_COOLDOWN");

    private static final String NAME_PATH = ".settings.name";
    private static final String REGEN_PATH = ".settings.regen";
    private static final String HUNGER_PATH = ".settings.hunger";
    private static final String BUILD_PATH = ".settings.build";
    private static final String ROUNDS_PATH = ".settings.rounds";
    private static final String HITDELAY_PATH = ".settings.hit-delay";
    private static final String KNOCKBACK_PATH = ".settings.knockback";
    private static final String EP_COOLDOWN_PATH = ".settings.ep-cooldown";
    private static final String GA_COOLDOWN_PATH = ".settings.ga-cooldown";
    private static final String KIT_DATA_PATH = ".kit-data";

    private final MainGUI mainGUI;

    private static final List<MatchType> MATCH_TYPES = new ArrayList<>();
    static {
        for (String matchType : PlayerKitManager.getInstance().getList("MATCH-TYPES"))
            MATCH_TYPES.add(MatchType.valueOf(matchType));
    }

    private final YamlConfiguration config;
    private final String mapPath;

    public CustomLadder(final Profile profile, final String mapPath, final int id) {
        super(DEFAULT_NAME.replace("%id%", String.valueOf(id)), LadderType.BASIC);

        this.config = profile.getFile().getConfig();
        this.mapPath = mapPath;

        this.setDisplayName(this.getName());
        this.setRegen(DEFAULT_REGEN);
        this.setHunger(DEFAULT_HUNGER);
        this.setBuild(DEFAULT_BUILD);
        this.setRounds(DEFAULT_ROUNDS);
        this.setHitDelay(DEFAULT_HITDELAY);
        this.setEnderPearlCooldown(DEFAULT_EP_COOLDOWN);
        this.setGoldenAppleCooldown(DEFAULT_GA_COOLDOWN);
        this.matchTypes = new ArrayList<>(MATCH_TYPES);

        this.getData();
        this.mainGUI = new MainGUI(this);
    }

    public CustomLadder(final CustomLadder customLadder, final Profile profile, final String mapPath) {
        super(customLadder);

        this.config = profile.getFile().getConfig();
        this.mapPath = mapPath;

        this.mainGUI = new MainGUI(this);
    }

    @Override
    public boolean isReadyToEnable() {
        return kitData.isSet() && !matchTypes.isEmpty();
    }

    public void getData() {
        if (config.isString(mapPath + NAME_PATH)) displayName = config.getString(mapPath + NAME_PATH);
        if (config.isBoolean(mapPath + REGEN_PATH)) regen = config.getBoolean(mapPath + REGEN_PATH);
        if (config.isBoolean(mapPath + HUNGER_PATH)) hunger = config.getBoolean(mapPath + HUNGER_PATH);
        if (config.isBoolean(mapPath + BUILD_PATH)) this.setBuild(config.getBoolean(mapPath + BUILD_PATH));
        if (config.isInt(mapPath + ROUNDS_PATH)) rounds = config.getInt(mapPath + ROUNDS_PATH);
        if (config.isInt(mapPath + HITDELAY_PATH)) hitDelay = config.getInt(mapPath + HITDELAY_PATH);
        if (config.isString(mapPath + KNOCKBACK_PATH)) ladderKnockback.get(config.getString(mapPath + KNOCKBACK_PATH));
        if (config.isInt(mapPath + EP_COOLDOWN_PATH)) enderPearlCooldown = config.getInt(mapPath + EP_COOLDOWN_PATH);
        if (config.isInt(mapPath + GA_COOLDOWN_PATH)) goldenAppleCooldown = config.getInt(mapPath + GA_COOLDOWN_PATH);

        kitData.getData(config, mapPath + KIT_DATA_PATH);
    }

    @Override
    public List<Arena> getArenas() {
        List<Arena> arenas = new ArrayList<>();
        for (Arena arena : ArenaManager.getInstance().getNormalArenas()) {
            if (arena.isEnabled() && arena.isAllowCustomKitOnMap()) {
                if (arena.getAssignedLadderTypes().contains(this.type)) {
                    arenas.add(arena);
                }
            }
        }
        return arenas;
    }

    public void setData() {
        config.set(mapPath + NAME_PATH, displayName);
        config.set(mapPath + REGEN_PATH, regen);
        config.set(mapPath + HUNGER_PATH, hunger);
        config.set(mapPath + BUILD_PATH, build);
        config.set(mapPath + ROUNDS_PATH, rounds);
        config.set(mapPath + HITDELAY_PATH, hitDelay);
        config.set(mapPath + KNOCKBACK_PATH, ladderKnockback.get());
        config.set(mapPath + EP_COOLDOWN_PATH, enderPearlCooldown);
        config.set(mapPath + GA_COOLDOWN_PATH, goldenAppleCooldown);

        kitData.saveData(config, mapPath + KIT_DATA_PATH);
    }

    @Override
    public void setBuild(boolean build) {
        if (build)
            this.setType(LadderType.BUILD);
        else
            this.setType(LadderType.BASIC);

        this.build = build;
    }

    @Override
    public boolean isEnabled() {
        return isReadyToEnable();
    }

}
