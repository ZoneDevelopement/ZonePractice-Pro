package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.PermanentConfig;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class Spleef extends NormalLadder implements LadderHandle, CustomConfig {

    /** When true, breaking a snow block gives a snowball; snowballs can be thrown to destroy snow. */
    private boolean snowballMode = false;

    public Spleef(String name, LadderType type) {
        super(name, type);
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof BlockBreakEvent) {
            onSpleefBlockBreak((BlockBreakEvent) e, match);
            return true;
        } else if (e instanceof PlayerMoveEvent) {
            whenFall((PlayerMoveEvent) e, match);
            return true;
        } else if (e instanceof EntityDamageEvent) {
            onPlayerDamage((EntityDamageEvent) e);
        } else if (e instanceof ProjectileHitEvent projectileHitEvent) {
            onSnowballHit(projectileHitEvent, match);
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Block break — optionally give a snowball
    // -------------------------------------------------------------------------

    private void onSpleefBlockBreak(final @NotNull BlockBreakEvent e, final @NotNull Match match) {
        Player player = e.getPlayer();

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;
        if (match.getCurrentStat(player).isSet()) return;

        Block snow = e.getBlock();
        if (snow == null) return;
        if (!match.getArena().getCuboid().contains(snow)) return;

        e.setCancelled(true);
        if (snow.getType().equals(Material.SNOW_BLOCK)) {
            match.addBlockChange(ClassImport.createChangeBlock(snow));
            snow.setType(Material.AIR);

            // Give one snowball per block broken when snowball mode is active.
            // Uses the version-abstracted getSnowball() (SNOW_BALL on 1.8.8, SNOWBALL on modern).
            if (snowballMode) {
                player.getInventory().addItem(
                        new ItemStack(ClassImport.getClasses().getItemMaterialUtil().getSnowball(), 1));
                player.updateInventory();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Snowball hit — destroy the snow block it lands on
    // Uses BlockIterator on the snowball's velocity — same cross-version approach
    // as the Splegg event's egg wand, no Paper-only API needed.
    // -------------------------------------------------------------------------

    private void onSnowballHit(final @NotNull ProjectileHitEvent e, final @NotNull Match match) {
        if (!snowballMode) return;
        if (!(e.getEntity() instanceof Snowball snowball)) return;

        // Only handle snowballs that belong to this match.
        MetadataValue mv = snowball.getMetadata(PermanentConfig.FIGHT_ENTITY).stream().findFirst().orElse(null);
        if (mv == null || !(mv.value() instanceof Match hitMatch) || !hitMatch.equals(match)) return;

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        Block hitBlock = findHitBlock(snowball);
        if (hitBlock == null) return;
        if (!hitBlock.getType().equals(Material.SNOW_BLOCK)) return;
        if (!match.getArena().getCuboid().contains(hitBlock)) return;

        match.addBlockChange(ClassImport.createChangeBlock(hitBlock));
        hitBlock.setType(Material.AIR);
    }

    /**
     * Walks the snowball's trajectory using {@link BlockIterator} to find the first
     * non-air block it hit. Works on all server versions — same technique as Splegg.
     */
    @Nullable
    private static Block findHitBlock(@NotNull Snowball snowball) {
        try {
            BlockIterator it = new BlockIterator(
                    snowball.getWorld(),
                    snowball.getLocation().toVector(),
                    snowball.getVelocity().normalize(),
                    0.0D,
                    4);

            Block hit = null;
            while (it.hasNext()) {
                hit = it.next();
                if (hit.getType() != Material.AIR) return hit;
            }
            return hit;
        } catch (Exception ignored) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Fall into liquid → death
    // -------------------------------------------------------------------------

    private static void whenFall(final @NotNull PlayerMoveEvent e, final @NotNull Match match) {
        Player player = e.getPlayer();

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        Material block = player.getLocation().getBlock().getType();
        if (block.equals(Material.WATER) || block.equals(ClassImport.getClasses().getItemMaterialUtil().getWater())) {
            match.killPlayer(player, null, DeathCause.SPLEEF.getMessage());
        } else if (block.equals(Material.LAVA) || block.equals(ClassImport.getClasses().getItemMaterialUtil().getLava())) {
            match.killPlayer(player, null, DeathCause.SPLEEF.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Damage — cancel all except projectiles (no knockback from snowballs)
    // -------------------------------------------------------------------------

    private static void onPlayerDamage(final @NotNull EntityDamageEvent e) {
        if (e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
            e.setDamage(0);
        else
            e.setCancelled(true);
    }

    // -------------------------------------------------------------------------
    // CustomConfig — persist snowballMode in the ladder YAML file
    // -------------------------------------------------------------------------

    @Override
    public void setCustomConfig(YamlConfiguration config) {
        config.set("settings.spleef-snowball-mode", snowballMode);
    }

    @Override
    public void getCustomConfig(YamlConfiguration config) {
        if (config.isBoolean("settings.spleef-snowball-mode")) {
            snowballMode = config.getBoolean("settings.spleef-snowball-mode");
        }
    }
}
