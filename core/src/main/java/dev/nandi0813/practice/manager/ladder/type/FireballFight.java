package dev.nandi0813.practice.manager.ladder.type;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.fight.util.ListenerUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.BedFight;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.FireballRunnable;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import lombok.Getter;
import lombok.Setter;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;
import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

@Getter
@Setter
public class FireballFight extends BedFight implements CustomConfig, LadderHandle {

    private double fireballCooldown;
    private static final String FIREBALL_COOLDOWN_PATH = "fireball-cooldown";

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
    }

    @Override
    public void getCustomConfig(YamlConfiguration config) {
        if (config.isDouble(FIREBALL_COOLDOWN_PATH)) {
            this.fireballCooldown = config.getDouble(FIREBALL_COOLDOWN_PATH);
            if (this.fireballCooldown < 0.5 || this.fireballCooldown > 15)
                this.fireballCooldown = 1.5;
        } else
            this.fireballCooldown = 1.5;
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
        ItemStack fireballItem = ClassImport.getClasses().getPlayerUtil().getItemInUse(player, ClassImport.getClasses().getItemMaterialUtil().getFireball());
        if (fireballItem == null) {
            return;
        }

        e.setCancelled(true);

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        if (ladder.fireballCooldown <= 0) return;

        if (!PlayerCooldown.isActive(player, CooldownObject.FIREBALL_FIGHT_FIREBALL)) {
            FireballRunnable fireballRunnable = new FireballRunnable(player, ladder.fireballCooldown);
            fireballRunnable.begin();
        } else {
            Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("MATCH.COOLDOWN.FIREBALL"), PlayerCooldown.getLeftInDouble(player, CooldownObject.FIREBALL_FIGHT_FIREBALL)));
            return;
        }

        final Fireball fireball = ClassImport.getClasses().getPlayerUtil().shootFireball(player, FIREBALL_SPEED);
        fireball.setMetadata(FIGHT_ENTITY, new FixedMetadataValue(ZonePractice.getInstance(), match));
        fireball.setMetadata(FIREBALL_FIGHT_FIREBALL, new FixedMetadataValue(ZonePractice.getInstance(), match));
        fireball.setIsIncendiary(false);
        fireball.setShooter(player);

        fireballItem.setAmount(fireballItem.getAmount() - 1);
        if (fireballItem.getAmount() == 0) {
            ClassImport.getClasses().getPlayerUtil().setItemInUseIf(
                    player,
                    ClassImport.getClasses().getItemMaterialUtil().getFireball(),
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
            ClassImport.getClasses().getLadderUtil().placeTnt(e, match);
        } else {
            block.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(e));

            Block underBlock = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
            if (ClassImport.getClasses().getArenaUtil().turnsToDirt(underBlock))
                match.addBlockChange(ClassImport.createChangeBlock(underBlock));
        }
    }

    private static void onEntityDamageByEntity(final @NotNull EntityDamageByEntityEvent e, final @NotNull Match match) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (e.getDamager() instanceof Fireball fireball) {
            if (!fireball.hasMetadata(FIREBALL_FIGHT_FIREBALL)) {
                return;
            }

            MetadataValue mv = BlockUtil.getMetadata(fireball, FIREBALL_FIGHT_FIREBALL);
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
            ClassImport.getClasses().getPlayerUtil().applyFireballKnockback(player, fireball);
        } else if (e.getDamager() instanceof TNTPrimed tnt) {
            MetadataValue mv = BlockUtil.getMetadata(tnt, FIREBALL_FIGHT_TNT);
            if (ListenerUtil.checkMetaData(mv)) {
                return;
            }

            MetadataValue mv2 = BlockUtil.getMetadata(tnt, FIREBALL_FIGHT_TNT_SHOOTER);
            if (mv2 == null) {
                return;
            }

            if (mv2.value() instanceof Player shooter) {
                if (shooter == player || !TeamUtil.isSaveTeamMate(match, shooter, player)) {
                    ClassImport.getClasses().getPlayerUtil().applyTntKnockback(player, tnt);
                    e.setDamage(0);
                } else if (TeamUtil.isSaveTeamMate(match, shooter, player)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    private static final float FIREBALL_YIELD = (float) ConfigManager.getDouble("MATCH-SETTINGS.FIREBALL-FIGHT.FIREBALL-YIELD");
    private static void onFireballHit(final @NotNull ProjectileHitEvent e) {
        Entity entity = e.getEntity();

        if (!entity.hasMetadata(FIREBALL_FIGHT_FIREBALL)) {
            return;
        }

        if (!(entity instanceof Fireball fireball)) {
            return;
        }

        fireball.setYield(FIREBALL_YIELD);
    }

    private static void onEntityExplode(@NotNull EntityExplodeEvent e) {
        Entity entity = e.getEntity();

        if (entity.hasMetadata(FIREBALL_FIGHT_FIREBALL)) {
            e.blockList().clear();
        } else if (entity.hasMetadata(FIREBALL_FIGHT_TNT) && entity.hasMetadata(FIREBALL_FIGHT_TNT_SHOOTER)) {
            MetadataValue mv = BlockUtil.getMetadata(entity, FIREBALL_FIGHT_TNT);
            if (ListenerUtil.checkMetaData(mv)) return;

            if (!(mv.value() instanceof Match match)) return;
            if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

            e.blockList().removeIf(block ->
                    !ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(match.getLadder(), block) &&
                            !block.getType().equals(Material.TNT) &&
                            !block.hasMetadata(PLACED_IN_FIGHT)
            );

            for (Block block : e.blockList())
                BlockUtil.breakBlock(match, block);
        }
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

}
