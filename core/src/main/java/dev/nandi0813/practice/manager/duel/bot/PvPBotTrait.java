package dev.nandi0813.practice.manager.duel.bot;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Cuboid;
import lombok.Setter;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SplashPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class PvPBotTrait extends Trait {

    public static final String TRAIT_NAME = "pvp_bot";

    private static final double ATTACK_REACH = 3.1D;
    private static final double HEAL_HEALTH_THRESHOLD = 9.0D;
    private static final int GAPPEL_USE_TICKS = 32;
    private static final int SPLASH_POTION_COOLDOWN_TICKS = 60;

    @Setter
    private UUID controllerPlayerId;
    @Setter
    private UUID targetPlayerId;
    @Setter
    private Cuboid arenaBounds;

    private int localTick;
    private int nextAttackTick;
    private int nextPotionTick;
    private boolean consumingHeal;

    public PvPBotTrait() {
        super(TRAIT_NAME);
    }

    @Override
    public void run() {
        localTick++;

        if (!npc.isSpawned()) {
            return;
        }

        if (!(npc.getEntity() instanceof LivingEntity botEntity)) {
            return;
        }

        Player target = resolveTarget();
        if (target == null) {
            return;
        }

        keepNavigatorAggressive(target);

        double distanceSquared = botEntity.getLocation().distanceSquared(target.getLocation());

        if (botEntity instanceof Player botPlayer) {
            botPlayer.setSprinting(distanceSquared > (ATTACK_REACH * ATTACK_REACH));
        }

        if (botEntity.getHealth() <= HEAL_HEALTH_THRESHOLD && !consumingHeal) {
            consumeHealing(botEntity);
        }

        if (!consumingHeal && localTick >= nextAttackTick && distanceSquared <= (ATTACK_REACH * ATTACK_REACH)) {
            performMeleeAttack(botEntity, target);
        }

        if (!consumingHeal && localTick >= nextPotionTick && distanceSquared > 10.0D && distanceSquared < 49.0D) {
            throwSplashPotion(botEntity, target);
            nextPotionTick = localTick + SPLASH_POTION_COOLDOWN_TICKS;
        }
    }

    private Player resolveTarget() {
        if (targetPlayerId != null) {
            Player directTarget = Bukkit.getPlayer(targetPlayerId);
            if (isValidArenaTarget(directTarget)) {
                return directTarget;
            }
        }

        if (controllerPlayerId != null) {
            Player fallback = Bukkit.getPlayer(controllerPlayerId);
            if (isValidArenaTarget(fallback)) {
                return fallback;
            }
        }

        return null;
    }

    private boolean isValidArenaTarget(Player player) {
        if (player == null || !player.isOnline() || player.isDead()) {
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null || profile.getStatus() != ProfileStatus.MATCH) {
            return false;
        }

        return arenaBounds == null || arenaBounds.contains(player.getLocation());
    }

    private void keepNavigatorAggressive(Player target) {
        Navigator navigator = npc.getNavigator();
        if (!navigator.isNavigating() || navigator.getEntityTarget() == null || navigator.getEntityTarget().getTarget() != target) {
            navigator.setTarget(target, true);
        }
    }

    private void performMeleeAttack(LivingEntity botEntity, Player target) {
        botEntity.swingMainHand();

        double baseDamage = getWeaponDamage(readMainHand());
        target.damage(baseDamage, botEntity);
        applyKnockback(botEntity.getLocation(), target);

        int attackCooldownTicks = getAttackCooldown(readMainHand());
        nextAttackTick = localTick + attackCooldownTicks;
    }

    private ItemStack readMainHand() {
        if (npc.getEntity() instanceof Player botPlayer) {
            ItemStack hand = botPlayer.getInventory().getItemInMainHand();
            return hand == null ? new ItemStack(Material.AIR) : hand;
        }

        return new ItemStack(Material.AIR);
    }

    private int getAttackCooldown(ItemStack itemStack) {
        Material material = itemStack.getType();
        if (material.name().endsWith("AXE")) {
            return 20;
        }
        if (material.name().endsWith("SWORD")) {
            return 11;
        }
        return 12;
    }

    private double getWeaponDamage(ItemStack itemStack) {
        return switch (itemStack.getType()) {
            case WOODEN_SWORD, GOLDEN_SWORD -> 5.0D;
            case STONE_SWORD -> 6.0D;
            case IRON_SWORD -> 7.0D;
            case DIAMOND_SWORD -> 8.0D;
            case NETHERITE_SWORD -> 9.0D;
            case WOODEN_AXE, GOLDEN_AXE -> 7.0D;
            case STONE_AXE -> 9.0D;
            case IRON_AXE -> 9.0D;
            case DIAMOND_AXE -> 10.0D;
            case NETHERITE_AXE -> 11.0D;
            default -> 1.0D;
        };
    }

    private void applyKnockback(Location source, Player target) {
        Vector knockback = target.getLocation().toVector().subtract(source.toVector());
        if (knockback.lengthSquared() <= 0.0001D) {
            return;
        }

        knockback.normalize().multiply(0.42D).setY(0.36D);
        target.setVelocity(knockback);
    }

    private void consumeHealing(LivingEntity botEntity) {
        consumingHeal = true;

        ItemStack previousHand = readMainHand();
        if (npc.getEntity() instanceof Player botPlayer) {
            botPlayer.getInventory().setItemInMainHand(new ItemStack(Material.GOLDEN_APPLE));
        }

        botEntity.swingMainHand();

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (!npc.isSpawned() || !(npc.getEntity() instanceof LivingEntity currentEntity)) {
                consumingHeal = false;
                return;
            }

            double maxHealth = currentEntity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null
                    ? currentEntity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()
                    : 20.0D;
            double healed = Math.min(maxHealth, currentEntity.getHealth() + 8.0D);
            currentEntity.setHealth(healed);
            currentEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 1, true, true));

            if (npc.getEntity() instanceof Player botPlayer) {
                botPlayer.getInventory().setItemInMainHand(previousHand);
            }

            consumingHeal = false;
        }, GAPPEL_USE_TICKS);
    }

    private void throwSplashPotion(LivingEntity botEntity, Player target) {
        botEntity.swingMainHand();

        SplashPotion potion = botEntity.getWorld().spawn(botEntity.getEyeLocation(), SplashPotion.class);

        ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        if (potionMeta != null) {
            potionMeta.setBasePotionType(PotionType.HARMING);
            itemStack.setItemMeta(potionMeta);
        }

        potion.setItem(itemStack);

        Vector direction = target.getEyeLocation().toVector().subtract(botEntity.getEyeLocation().toVector()).normalize();
        potion.setVelocity(direction.multiply(0.65D).setY(0.24D));
    }
}


