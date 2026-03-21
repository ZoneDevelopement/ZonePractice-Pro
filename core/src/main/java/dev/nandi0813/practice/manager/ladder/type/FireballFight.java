package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.util.*;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.BedFight;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.util.LadderUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;
import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

@Getter
@Setter
public class FireballFight extends BedFight implements CustomConfig, LadderHandle {

    private static final double TNT_VELOCITY_HORIZONTAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.TNT.HORIZONTAL");
    private static final double TNT_VELOCITY_VERTICAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.TNT.VERTICAL");

    private static final double FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.FIREBALL.HORIZONTAL");
    private static final double FB_VELOCITY_VERTICAL_MULTIPLICATIVE = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.EXPLOSION.FIREBALL.VERTICAL");

    private double fireballCooldown;
    private boolean fireballBlockDestroy;
    private static final String FIREBALL_COOLDOWN_PATH = "fireball-cooldown";
    private static final String FIREBALL_BLOCK_DESTROY_PATH = "fireball-block-destroy";

    public FireballFight(String name, LadderType type) {
        super(name, type);
    }

    @Override
    public String getRespawnLanguagePath() {
        return "FIREBALL-FIGHT";
    }

    @Override
    public void setCustomConfig(YamlConfiguration config) {
        config.set(FIREBALL_COOLDOWN_PATH, fireballCooldown);
        config.set(FIREBALL_BLOCK_DESTROY_PATH, fireballBlockDestroy);
    }

    @Override
    public void getCustomConfig(YamlConfiguration config) {
        if (config.isDouble(FIREBALL_COOLDOWN_PATH)) {
            this.fireballCooldown = config.getDouble(FIREBALL_COOLDOWN_PATH);
            if (this.fireballCooldown < 0.5 || this.fireballCooldown > 15)
                this.fireballCooldown = 1.5;
        } else
            this.fireballCooldown = 1.5;

        this.fireballBlockDestroy = config.getBoolean(FIREBALL_BLOCK_DESTROY_PATH, false);
    }

    @Override
    public boolean handleEvents(Event e, Match match) {
        if (e instanceof EntityDamageEvent) {
            onPlayerDamage((EntityDamageEvent) e, match);

            if (e instanceof EntityDamageByEntityEvent)
                onEntityDamageByEntity((EntityDamageByEntityEvent) e, match);

            return true;
        } else if (e instanceof PlayerInteractEvent) {
            onFireballLaunch((PlayerInteractEvent) e, match, this);
            onTntClick((PlayerInteractEvent) e);
            return true;
        } else if (e instanceof ProjectileHitEvent) {
            onFireballHit((ProjectileHitEvent) e);
            return true;
        } else if (e instanceof EntityExplodeEvent) {
            onEntityExplode((EntityExplodeEvent) e);
            return true;
        } else if (e instanceof org.bukkit.event.block.BlockExplodeEvent) {
            onBlockExplode((org.bukkit.event.block.BlockExplodeEvent) e, match);
            return true;
        } else if (e instanceof PlayerDropItemEvent) {
            onItemDrop((PlayerDropItemEvent) e);
            return true;
        } else if (e instanceof PlayerMoveEvent) {
            onPlayerMove((PlayerMoveEvent) e, match);
            return true;
        } else if (e instanceof BlockBreakEvent) {
            onBedDestroy((BlockBreakEvent) e, match);
            return true;
        } else if (e instanceof BlockPlaceEvent) {
            onBlockPlace((BlockPlaceEvent) e, match);
            return true;
        }
        return false;
    }

    private static final String FIREBALL_FIGHT_FIREBALL = "ZONEPRACTICE_PRO_MATCH_FIREBALL";
    public static final String FIREBALL_FIGHT_TNT = "ZONEPRACTICE_PRO_MATCH_FIREBALL_TNT";
    public static final String FIREBALL_FIGHT_TNT_SHOOTER = "ZONEPRACTICE_PRO_MATCH_FIREBALL_TNT_SHOOTER";
    private static final double FIREBALL_SPEED = ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.FIREBALL-SPEED");

    private static void onTntClick(final @NotNull PlayerInteractEvent e) {
        Block clickedBlock = e.getClickedBlock();
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && clickedBlock != null) {
            if (clickedBlock.getType().equals(Material.TNT)) {
                e.setCancelled(true);
            }
        }
    }

    private static void onFireballLaunch(final @NotNull PlayerInteractEvent e, final Match match, final FireballFight ladder) {
        Player player = e.getPlayer();
        Action action = e.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack fireballItem = PlayerUtil.getItemInUse(player, Material.FIRE_CHARGE);
        if (fireballItem == null) {
            return;
        }

        e.setCancelled(true);

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        if (ladder.fireballCooldown <= 0) return;

        if (!ModernItemCooldownHandler.handleFireballMatch(player, ladder.fireballCooldown)) {
            return;
        }

        final Fireball fireball = PlayerUtil.shootFireball(player, FIREBALL_SPEED);
        BlockUtil.setMetadata(fireball, FIGHT_ENTITY, match);
        BlockUtil.setMetadata(fireball, FIREBALL_FIGHT_FIREBALL, match);
        fireball.setIsIncendiary(false);
        fireball.setShooter(player);

        fireballItem.setAmount(fireballItem.getAmount() - 1);
        if (fireballItem.getAmount() == 0) {
            PlayerUtil.setItemInUseIf(
                    player,
                    Material.FIRE_CHARGE,
                    null);
        }
        player.updateInventory();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (fireball.isDead() || fireball.isValid()) {
                    cancel();
                    return;
                }

                if (!match.getArena().getCuboid().contains(fireball.getLocation())) {
                    fireball.remove();
                    cancel();
                }
            }
        };

        runnable.runTaskTimer(ZonePractice.getInstance(), 40L, 20L);
    }

    private static void onBlockPlace(final @NotNull BlockPlaceEvent e, final @NotNull Match match) {
        Block block = e.getBlockPlaced();
        if (block.getType().equals(Material.TNT)) {
            LadderUtil.placeTnt(e, match);
        } else {
            BlockUtil.setMetadata(block, PLACED_IN_FIGHT, match);
            match.addBlockChange(new ChangedBlock(e));

            Block underBlock = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
            if (ArenaUtil.turnsToDirt(underBlock))
                match.getFightChange().addArenaBlockChange(new ChangedBlock(underBlock));
        }
    }

    private static void onEntityDamageByEntity(final @NotNull EntityDamageByEntityEvent e, final @NotNull Match match) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (e.getDamager() instanceof Fireball fireball) {
            if (!BlockUtil.hasMetadata(fireball, FIREBALL_FIGHT_FIREBALL)) {
                return;
            }

            Match mv = BlockUtil.getMetadata(fireball, FIREBALL_FIGHT_FIREBALL, Match.class);
            if (ListenerUtil.checkMetaData(mv)) {
                return;
            }

            if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            if (fireball.getShooter() instanceof Player shooter) {
                if (TeamUtil.isSaveTeamMate(match, shooter, player)) {
                    e.setCancelled(true);
                    return;
                }
            }

            e.setDamage(0);
            applyFireballKnockback(player, fireball);
        } else if (e.getDamager() instanceof TNTPrimed tnt) {
            Match mv = BlockUtil.getMetadata(tnt, FIREBALL_FIGHT_TNT, Match.class);
            if (ListenerUtil.checkMetaData(mv)) {
                return;
            }

            Player shooter = BlockUtil.getMetadata(tnt, FIREBALL_FIGHT_TNT_SHOOTER, Player.class);
            if (shooter == null) {
                return;
            }

            if (shooter == player || !TeamUtil.isSaveTeamMate(match, shooter, player)) {
                applyTntKnockback(player, tnt);
                e.setDamage(0);
            } else if (TeamUtil.isSaveTeamMate(match, shooter, player)) {
                e.setCancelled(true);
            }
        }
    }

    private static final float FIREBALL_YIELD = (float) ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.FIREBALL-YIELD");


    private static void onFireballHit(final @NotNull ProjectileHitEvent e) {
        Entity entity = e.getEntity();

        if (!BlockUtil.hasMetadata(entity, FIREBALL_FIGHT_FIREBALL)) {
            return;
        }

        if (!(entity instanceof Fireball fireball)) {
            return;
        }

        fireball.setYield(FIREBALL_YIELD);
    }

    private static void onEntityExplode(@NotNull EntityExplodeEvent e) {
        Entity entity = e.getEntity();

        if (BlockUtil.hasMetadata(entity, FIREBALL_FIGHT_FIREBALL)) {
            Match match = BlockUtil.getMetadata(entity, FIREBALL_FIGHT_FIREBALL, Match.class);
            if (ListenerUtil.checkMetaData(match)) {
                e.blockList().clear();
                return;
            }

            if (!(match.getLadder() instanceof FireballFight fireballFight) || !fireballFight.isFireballBlockDestroy()) {
                // Setting disabled — no block destruction from fireball, but don't cancel so the sound plays.
                e.blockList().clear();
                return;
            }

            if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
                e.blockList().clear();
                return;
            }

            // Setting enabled — only destroy player-placed blocks, not arena blocks.
            e.blockList().removeIf(block -> !BlockUtil.hasMetadata(block, PLACED_IN_FIGHT));
        } else if (BlockUtil.hasMetadata(entity, FIREBALL_FIGHT_TNT) && BlockUtil.hasMetadata(entity, FIREBALL_FIGHT_TNT_SHOOTER)) {
            Match match = BlockUtil.getMetadata(entity, FIREBALL_FIGHT_TNT, Match.class);
            if (ListenerUtil.checkMetaData(match)) return;

            if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
                e.blockList().clear();
                return;
            }

            // Filter to only destroyable blocks — MatchTntListener will track + break them naturally.
            e.blockList().removeIf(block -> {
                if (block.getType().equals(Material.TNT)) return false;                     // keep → chain-explodes
                if (ArenaUtil.containsDestroyableBlock(match.getLadder(), block)) return false; // keep → destroyable
                if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) return false;                       // keep → player placed
                if (BlockUtil.hasMetadata(block.getRelative(0, 1, 0), PLACED_IN_FIGHT)) return true;  // remove → support under player block
                return true;                                                                 // remove → pure arena block
            });
        }
    }

    private static void onBlockExplode(@NotNull org.bukkit.event.block.BlockExplodeEvent e, @NotNull Match match) {
        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.blockList().clear();
            return;
        }

        // Filter to only destroyable blocks — MatchTntListener will track + break them naturally.
        e.blockList().removeIf(block -> {
            if (block.getType().equals(Material.TNT)) return false;                     // keep → chain-explodes
            if (ArenaUtil.containsDestroyableBlock(match.getLadder(), block)) return false; // keep → destroyable
            if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) return false;                       // keep → player placed
            if (BlockUtil.hasMetadata(block.getRelative(0, 1, 0), PLACED_IN_FIGHT)) return true;  // remove → support under player block
            return true;                                                                 // remove → pure arena block
        });
    }

    private static void onPlayerDamage(final @NotNull EntityDamageEvent e, final Match match) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) e.setCancelled(true);
        if (match.getCurrentStat(player).isSet()) return;

        EntityDamageEvent.DamageCause cause = e.getCause();
        boolean voidDamage = cause.equals(EntityDamageEvent.DamageCause.VOID);

        if (cause.equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
            e.setDamage(0);
        } else if (cause.equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelled(true);
        } else if (voidDamage) {
            e.setDamage(0);

            match.killPlayer(player, null, DeathCause.VOID.getMessage());
        }
    }

    private static void applyFireballKnockback(final Player player, final Fireball fireball) {
        final Location playerLoc = player.getLocation();
        final Location fireballLoc = fireball.getLocation();

        final float yield = fireball.getYield() > 0 ? fireball.getYield() : 1.0f;

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            double dx = playerLoc.getX() - fireballLoc.getX();
            double dz = playerLoc.getZ() - fireballLoc.getZ();

            // Use only horizontal distance for factor calculation so that jumping
            // (which only increases vertical separation) does not reduce the knockback strength.
            double effectiveDistance = Math.sqrt(dx * dx + dz * dz);

            double safeDistance = 0.8;
            double factor = 1.0;

            if (effectiveDistance > safeDistance) {
                double impactRadius = yield * 2.5;
                double decayRange = impactRadius - safeDistance;

                if (decayRange <= 0.1) decayRange = 1.0;

                factor = 1.0 - ((effectiveDistance - safeDistance) / decayRange);
            }

            if (factor <= 0.1) return;
            if (factor > 1) factor = 1;

            // Compute horizontal direction separately so the vertical component
            // of the direction vector doesn't steal from horizontal velocity.
            double horizontalLen = Math.sqrt(dx * dx + dz * dz);
            double horizDirX;
            double horizDirZ;

            if (horizontalLen < 0.001) {
                // Fireball is almost directly below – pick the player's facing direction
                Vector facing = playerLoc.getDirection();
                double facingLen = Math.sqrt(facing.getX() * facing.getX() + facing.getZ() * facing.getZ());
                if (facingLen < 0.001) {
                    horizDirX = 0;
                    horizDirZ = 0;
                } else {
                    horizDirX = facing.getX() / facingLen;
                    horizDirZ = facing.getZ() / facingLen;
                }
            } else {
                horizDirX = dx / horizontalLen;
                horizDirZ = dz / horizontalLen;
            }

            // Apply a slight reduction when airborne so it's weaker than grounded, but not drastically
            double airMultiplier = player.isOnGround() ? 1.0 : 0.8;

            Vector velocity = new Vector(
                    horizDirX * FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor * airMultiplier,
                    FB_VELOCITY_VERTICAL_MULTIPLICATIVE * factor,
                    horizDirZ * FB_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor * airMultiplier
            );

            player.setVelocity(velocity);
        }, 1L);
    }

    private static void applyTntKnockback(Player player, TNTPrimed tnt) {
        final Location playerLoc = player.getLocation();
        final Location tntLoc = tnt.getLocation();
        final float yield = tnt.getYield();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            double distance = playerLoc.distance(tntLoc);

            double impactRadius = (yield > 0 ? yield : 4.0) * 2.0;
            double factor = 1.0 - (distance / impactRadius);

            if (factor <= 0.1) return;
            if (factor > 1) factor = 1;

            Vector direction = playerLoc.toVector().subtract(tntLoc.toVector());

            if (direction.lengthSquared() == 0) {
                direction = new Vector(0, 0.1, 0);
            } else {
                direction.normalize();
            }

            Vector velocity = new Vector(
                    direction.getX() * TNT_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor,
                    TNT_VELOCITY_VERTICAL_MULTIPLICATIVE * factor,
                    direction.getZ() * TNT_VELOCITY_HORIZONTAL_MULTIPLICATIVE * factor
            );

            player.setVelocity(velocity);
        }, 1L);
    }

}
