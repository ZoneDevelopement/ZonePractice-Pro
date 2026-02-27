package dev.nandi0813.practice.manager.ladder.abstraction;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.util.LadderKnockback;
import dev.nandi0813.practice.module.interfaces.KitData;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Ladder {

    protected final String name;
    @Setter
    protected String displayName;
    protected ItemStack icon;
    @Setter
    protected LadderType type;
    @Setter
    protected boolean enabled;

    // Settings the admins/players can set.
    @Setter
    protected KitData kitData;
    protected final LadderKnockback ladderKnockback;
    @Setter
    protected boolean regen = true;
    @Setter
    protected boolean hunger = true;
    @Setter
    protected boolean build;
    @Setter
    protected int hitDelay = 20;
    @Setter
    protected int rounds = 1;
    @Setter
    protected int enderPearlCooldown = ConfigManager.getInt("MATCH-SETTINGS.ENDERPEARL.COOLDOWN");
    @Setter
    protected int goldenAppleCooldown = 0;
    @Setter
    protected int fireworkRocketCooldown = ConfigManager.getInt("MATCH-SETTINGS.FIREWORK-ROCKET.COOLDOWN");

    protected List<MatchType> matchTypes = new ArrayList<>();

    // Cannot be set by player in custom
    @Setter
    protected int startCountdown = 3;
    @Setter
    protected int tntFuseTime = 4;
    @Setter
    protected int maxDuration = 600;
    @Setter
    protected boolean multiRoundStartCountdown = true; // Ha azt irja nincs hasznalva buggos
    @Setter
    protected boolean dropInventoryPartyGames = false; // Ha azt irja nincs hasznalva buggos
    @Setter
    protected boolean startMove = true;
    @Setter
    protected boolean healthBelowName = false;
    @Setter
    protected boolean resetBuildAfterRound = false;
    @Setter
    protected boolean breakAllBlocks = false;

    protected Ladder(String name, LadderType type) {
        this.name = name;
        this.displayName = name;
        this.type = type;
        this.kitData = ClassImport.createKitData();
        this.ladderKnockback = new LadderKnockback();
    }

    protected Ladder(Ladder ladder) {
        this.name = ladder.getName();
        this.displayName = ladder.getDisplayName();
        if (ladder.getIcon() != null) {
            this.icon = ladder.getIcon().clone();
        }
        this.kitData = ClassImport.createKitData(ladder.getKitData());
        this.type = ladder.getType();
        this.enabled = ladder.isEnabled();
        this.ladderKnockback = new LadderKnockback(ladder.getLadderKnockback());
        this.regen = ladder.isRegen();
        this.hunger = ladder.isHunger();
        this.build = ladder.isBuild();
        this.hitDelay = ladder.getHitDelay();
        this.rounds = ladder.getRounds();
        this.enderPearlCooldown = ladder.getEnderPearlCooldown();
        this.goldenAppleCooldown = ladder.getGoldenAppleCooldown();
        this.fireworkRocketCooldown = ladder.getFireworkRocketCooldown();
        this.matchTypes = new ArrayList<>(ladder.getMatchTypes());
        this.startCountdown = ladder.getStartCountdown();
        this.tntFuseTime = ladder.getTntFuseTime();
        this.maxDuration = ladder.getMaxDuration();
        this.multiRoundStartCountdown = ladder.isMultiRoundStartCountdown();
        this.dropInventoryPartyGames = ladder.isDropInventoryPartyGames();
        this.startMove = ladder.isStartMove();
        this.healthBelowName = ladder.isHealthBelowName();
        this.resetBuildAfterRound = ladder.isResetBuildAfterRound();
        this.breakAllBlocks = ladder.isBreakAllBlocks();
    }

    public abstract List<Arena> getArenas();

    public List<Arena> getAvailableArenas() {
        List<Arena> arenas = new ArrayList<>();
        for (Arena arena : getArenas())
            if (arena.getAvailableArena() != null)
                arenas.add(arena);
        return arenas;
    }

    public void setIcon(final ItemStack icon) {
        if (icon == null || icon.getType().equals(Material.AIR)) {
            return;
        }

        this.icon = icon.clone();

        if (icon.hasItemMeta() && icon.getItemMeta().getDisplayName() != null && !icon.getItemMeta().getDisplayName().equalsIgnoreCase(" "))
            this.displayName = StringUtil.CC(icon.getItemMeta().getDisplayName());
        else
            this.displayName = name;
    }

    public ItemStack getIcon() {
        if (this.icon == null) return null;
        return this.icon.clone();
    }

    public abstract void setData();

    public abstract void getData();

    public abstract boolean isReadyToEnable();

}
