package dev.nandi0813.practice.manager.duel.bot;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Cuboid;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;

public class PvPBotTrait extends Trait {

    public static final String TRAIT_NAME = "pvp_bot";

    public PvPBotTrait() {
        super(TRAIT_NAME);
    }

    private static final int ATTACK_COOLDOWN_TICKS = 10;
    private static final int GOLDEN_APPLE_CONSUME_TICKS = 32;
    private static final int HEALING_RETREAT_DURATION_TICKS = 40;
    private static final double STRAFING_SPEED = 0.25D;
    private static final double ARENA_EDGE_BUFFER = 2.5D;
    private static final double ARENA_EDGE_PULL = 0.12D;
    private static final double ARENA_OUTSIDE_PULL = 0.22D;
    private static final double MAX_HORIZONTAL_SPEED = 0.65D;

    @Setter
    private UUID controllerPlayerId;
    @Getter
    @Setter
    private UUID targetPlayerId;
    @Setter
    private Cuboid arenaBounds;

    @Setter
    private BotDifficulty difficulty = BotDifficulty.NORMAL;

    private int localTick = 0;
    private int lastAttackTick = 0;
    private int wTapRecoverTick = 0;
    private int shieldBlockTicks = 0;
    private int strafeTimer = 0;
    private int strafeDirection = 1;
    private ItemStack previousMainHand = null;

    private BotState currentState = BotState.CHASING;
    private int stateTickCounter = 0;

    private static final Random RANDOM = new Random();

    private double getAttackReach() {
        return switch (difficulty) {
            case NOOB -> 2.0D;
            case EASY -> 2.5D;
            case NORMAL -> 2.8D;
            case HARD, EXPERT -> 3.0D;
        };
    }

    private double getStrafeChance() {
        return switch (difficulty) {
            case NOOB -> 0.0D;
            case EASY -> 0.10D;
            case NORMAL -> 0.30D;
            case HARD -> 0.60D;
            case EXPERT -> 0.85D;
        };
    }

    private double getJumpChance() {
        return switch (difficulty) {
            case NOOB, EASY -> 0.0D;
            case NORMAL -> 0.10D;
            case HARD -> 0.40D;
            case EXPERT -> 1.0D;
        };
    }

    private double getShieldChance() {
        return switch (difficulty) {
            case NOOB -> 0.0D;
            case EASY -> 0.05D;
            case NORMAL -> 0.15D;
            case HARD -> 0.40D;
            case EXPERT -> 0.85D;
        };
    }

    private double getAxeSwapChance() {
        return switch (difficulty) {
            case NOOB, EASY -> 0.0D;
            case NORMAL -> 0.30D;
            case HARD -> 0.80D;
            case EXPERT -> 1.0D;
        };
    }

    private double getHealingThreshold() {
        return switch (difficulty) {
            case NOOB -> 3.0D;
            case EASY -> 6.0D;
            case NORMAL -> 8.0D;
            case HARD, EXPERT -> 10.0D;
        };
    }

    private double getHealingSpeedMultiplier() {
        return switch (difficulty) {
            case NOOB -> 0.5D;
            case EASY -> 0.75D;
            case NORMAL -> 1.0D;
            case HARD -> 1.2D;
            case EXPERT -> 1.5D;
        };
    }

    private boolean shouldRespectAttackCooldown() {
        return difficulty != BotDifficulty.NOOB && difficulty != BotDifficulty.EASY;
    }

    private boolean shouldUseWTapping() {
        return difficulty == BotDifficulty.HARD || difficulty == BotDifficulty.EXPERT;
    }

    @Override
    public void run() {
        localTick++;
        stateTickCounter++;

        if (!npc.isSpawned()) {
            return;
        }

        if (!(npc.getEntity() instanceof LivingEntity botEntity)) {
            return;
        }

        // Do not run movement/combat logic while death animation is playing.
        if (botEntity.isDead() || botEntity.getHealth() <= 0.0D) {
            disableLookClose();
            return;
        }

        Player target = resolveTarget();
        if (target == null) {
            disableLookClose();
            return;
        }

        updateLookCloseTarget(target);

        double distanceSquared = botEntity.getLocation().distanceSquared(target.getLocation());
        double distance = Math.sqrt(distanceSquared);

        double healthThreshold = getHealingThreshold();
        double currentHealth = botEntity.getHealth();
        boolean lowHealth = currentHealth <= healthThreshold;

        switch (currentState) {
            case CHASING:
                handleChasingState(botEntity, target, distance, lowHealth);
                break;
            case ATTACKING:
                handleAttackingState(botEntity, target, distance, lowHealth);
                break;
            case HEALING:
                handleHealingState(botEntity, target);
                break;
        }

        if (wTapRecoverTick > 0) {
            wTapRecoverTick--;
        }

        keepBotInsideArena(botEntity);
    }

    private void handleChasingState(LivingEntity botEntity, Player target, double distance, boolean lowHealth) {
        if (distance <= getAttackReach() + 1.0D) {
            currentState = BotState.ATTACKING;
            stateTickCounter = 0;
            return;
        }

        if (lowHealth && hasHealingItems()) {
            currentState = BotState.HEALING;
            stateTickCounter = 0;
            return;
        }

        keepNavigatorAggressive(target);

        if (botEntity instanceof Player botPlayer) {
            botPlayer.setSprinting(true);
        }
    }

    private void handleAttackingState(LivingEntity botEntity, Player target, double distance, boolean lowHealth) {
        double attackReach = getAttackReach();

        if (lowHealth && hasHealingItems()) {
            currentState = BotState.HEALING;
            stateTickCounter = 0;
            return;
        }

        if (botEntity.getHealth() <= 2.0D) {
            equipTotemsOfUndying(botEntity);
        }

        if (distance > attackReach + 1.5D) {
            currentState = BotState.CHASING;
            stateTickCounter = 0;
            Navigator navigator = npc.getNavigator();
            navigator.setTarget(target, true);
            return;
        }

        if (target.isBlocking() && RANDOM.nextDouble() < getAxeSwapChance()) {
            handleShieldTargeting(botEntity, target);
        }

        if (shieldBlockTicks > 0) {
            shieldBlockTicks--;
        } else if (shouldRaiseShield(botEntity, distance)) {
            raiseShield(botEntity);
        }

        if (canAttack()) {
            boolean isCriticalOpportunity = isCriticalHitReady(botEntity);
            performMeleeAttack(botEntity, target, isCriticalOpportunity);
            lastAttackTick = localTick;
        }

        applyStrafeMovement(botEntity, target, distance);
        keepNavigatorAggressive(target);
    }

    private void handleHealingState(LivingEntity botEntity, Player target) {
        double maxHealth = getMaxHealth(botEntity);
        double currentHealth = botEntity.getHealth();
        double healthPercentage = currentHealth / maxHealth;

        if (healthPercentage > 0.75D) {
            currentState = BotState.ATTACKING;
            stateTickCounter = 0;
            Navigator navigator = npc.getNavigator();
            navigator.setPaused(false);
            navigator.setTarget(target, true);
            return;
        }

        if (stateTickCounter == 1) {
            Navigator navigator = npc.getNavigator();
            navigator.setPaused(true);

            if (botEntity instanceof Player botPlayer) {
                botPlayer.setSprinting(true);
            }
        }

        int retreatDuration = (int) (HEALING_RETREAT_DURATION_TICKS / getHealingSpeedMultiplier());
        if (stateTickCounter <= retreatDuration) {
            retreatFromPlayer(botEntity, target);
        } else {
            if (stateTickCounter == retreatDuration + 1) {
                attemptHealing(botEntity);
            }

            int consumeDuration = (int) (GOLDEN_APPLE_CONSUME_TICKS / getHealingSpeedMultiplier());
            if (stateTickCounter > retreatDuration + consumeDuration) {
                currentState = BotState.ATTACKING;
                stateTickCounter = 0;
                Navigator navigator = npc.getNavigator();
                navigator.setPaused(false);
                navigator.setTarget(target, true);

                if (botEntity instanceof Player botPlayer) {
                    botPlayer.setSprinting(true);
                }
            }
        }
    }

    private void applyStrafeMovement(LivingEntity botEntity, Player target, double distance) {
        strafeTimer++;

        if (strafeTimer > 15 + RANDOM.nextInt(20)) {
            strafeDirection = RANDOM.nextBoolean() ? 1 : -1;
            strafeTimer = 0;
        }

        double strafeChance = getStrafeChance();
        double jumpChance = getJumpChance();

        if (botEntity.isOnGround() && RANDOM.nextDouble() < strafeChance) {
            Vector directionToTarget = target.getLocation().toVector().subtract(botEntity.getLocation().toVector()).normalize();
            Vector perpendicular = new Vector(-directionToTarget.getZ(), 0, directionToTarget.getX()).multiply(strafeDirection);
            Vector velocity = botEntity.getVelocity();

            if (distance < 2.3D && difficulty.ordinal() >= BotDifficulty.NORMAL.ordinal()) {
                velocity.add(directionToTarget.multiply(-0.20D));
            } else if (distance > 2.8D) {
                velocity.add(directionToTarget.multiply(0.15D));
            }

            velocity.setX(velocity.getX() * 0.6 + perpendicular.getX() * STRAFING_SPEED);
            velocity.setZ(velocity.getZ() * 0.6 + perpendicular.getZ() * STRAFING_SPEED);
            botEntity.setVelocity(velocity);
        }

        int cooldown = calculateAttackCooldown();
        int ticksSinceLast = localTick - lastAttackTick;
        if (ticksSinceLast >= cooldown - 4 && botEntity.isOnGround() && distance <= 3.5D && RANDOM.nextDouble() < jumpChance) {
            botEntity.setVelocity(botEntity.getVelocity().setY(0.42D));
        }
    }

    private void retreatFromPlayer(LivingEntity botEntity, Player target) {
        Vector retreatDirection = botEntity.getLocation().toVector()
                .subtract(target.getLocation().toVector())
                .normalize()
                .multiply(0.4D);

        Vector currentVelocity = botEntity.getVelocity();
        currentVelocity.add(retreatDirection);
        botEntity.setVelocity(currentVelocity);
    }

    private void attemptHealing(LivingEntity botEntity) {
        if (consumeItem(Material.SPLASH_POTION) != null) {
            useSplashPotion(botEntity);
            return;
        }

        ItemStack goldenApple = consumeItem(Material.ENCHANTED_GOLDEN_APPLE, Material.GOLDEN_APPLE);
        if (goldenApple != null) {
            useGoldenApple(botEntity, goldenApple.getType());
            return;
        }

        currentState = BotState.ATTACKING;
        stateTickCounter = 0;
    }

    private void useSplashPotion(LivingEntity botEntity) {
        if (!(botEntity instanceof Player botPlayer)) {
            return;
        }

        previousMainHand = botPlayer.getInventory().getItemInMainHand();
        ItemStack splashPotion = new ItemStack(Material.SPLASH_POTION);
        botPlayer.getInventory().setItemInMainHand(splashPotion);

        Location originalLoc = botEntity.getLocation();
        Location lookDown = originalLoc.clone();
        lookDown.setPitch(90f);
        botEntity.teleport(lookDown);

        botEntity.swingMainHand();

        org.bukkit.entity.ThrownPotion thrownPotion = botEntity.getWorld().spawn(
                botEntity.getEyeLocation(),
                org.bukkit.entity.ThrownPotion.class
        );

        ItemStack potionItem = new ItemStack(Material.SPLASH_POTION);
        org.bukkit.inventory.meta.PotionMeta potionMeta = (org.bukkit.inventory.meta.PotionMeta) potionItem.getItemMeta();
        if (potionMeta != null) {
            potionMeta.setBasePotionType(org.bukkit.potion.PotionType.HEALING);
            potionItem.setItemMeta(potionMeta);
        }

        thrownPotion.setItem(potionItem);
        thrownPotion.setVelocity(new Vector(0, -0.5D, 0));

        botEntity.getWorld().playSound(botEntity.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.5f, 1.0f);

        double maxHealth = getMaxHealth(botEntity);
        double newHealth = Math.min(maxHealth, botEntity.getHealth() + 8.0D);
        botEntity.setHealth(newHealth);

        botEntity.teleport(originalLoc);

        if (previousMainHand != null) {
            botPlayer.getInventory().setItemInMainHand(previousMainHand);
        }
    }

    private void useGoldenApple(LivingEntity botEntity, Material consumedType) {
        if (!(botEntity instanceof Player botPlayer)) {
            return;
        }

        previousMainHand = botPlayer.getInventory().getItemInMainHand();
        ItemStack goldenApple = new ItemStack(consumedType);
        botPlayer.getInventory().setItemInMainHand(goldenApple);

        botPlayer.setWalkSpeed(0.1f);
        botEntity.swingMainHand();
        botEntity.getWorld().playSound(botEntity.getLocation(), Sound.ENTITY_GENERIC_EAT, 0.5f, 1.0f);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (!npc.isSpawned() || !(npc.getEntity() instanceof LivingEntity currentEntity)) {
                return;
            }

            currentEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, true, false));
            currentEntity.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120, 0, true, false));

            double maxHealth = getMaxHealth(currentEntity);
            double newHealth = Math.min(maxHealth, currentEntity.getHealth() + 4.0D);
            currentEntity.setHealth(newHealth);

            if (currentEntity instanceof Player currentPlayer) {
                currentPlayer.getInventory().setItemInMainHand(previousMainHand != null ? previousMainHand : new ItemStack(Material.AIR));
                currentPlayer.setWalkSpeed(0.2f);
            }
        }, GOLDEN_APPLE_CONSUME_TICKS);
    }

    private void performMeleeAttack(LivingEntity botEntity, Player target, boolean isCritical) {
        ItemStack mainHand = readMainHand();
        double baseDamage = getWeaponDamage(mainHand);

        if (isCritical && (difficulty == BotDifficulty.NORMAL || difficulty == BotDifficulty.HARD || difficulty == BotDifficulty.EXPERT)) {
            baseDamage *= 1.5D;
        } else if (isCritical && (difficulty == BotDifficulty.NOOB || difficulty == BotDifficulty.EASY)) {
            isCritical = false;
        }

        botEntity.swingMainHand();
        target.damage(baseDamage, botEntity);

        double knockbackMultiplier = isCritical ? 1.0D : 0.5D;
        applyKnockback(botEntity.getLocation(), target, knockbackMultiplier);

        if (shouldUseWTapping() && botEntity instanceof Player botPlayer) {
            botPlayer.setSprinting(false);
            wTapRecoverTick = 2;

            Vector kb = target.getLocation().toVector().subtract(botEntity.getLocation().toVector()).normalize();
            target.setVelocity(target.getVelocity().add(kb.multiply(0.15D)));

            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
                if (npc.isSpawned() && npc.getEntity() instanceof Player bp) {
                    bp.setSprinting(true);
                }
            }, 2L);
        }
    }

    private void applyKnockback(Location source, Player target, double knockbackMultiplier) {
        Vector knockback = target.getLocation().toVector().subtract(source.toVector());
        if (knockback.lengthSquared() <= 0.0001D) {
            return;
        }

        knockback.normalize().multiply(0.42D * knockbackMultiplier).setY(0.36D);
        target.setVelocity(knockback);
    }

    private ItemStack consumeItem(Material... materials) {
        Inventory inventory = npc.getOrAddTrait(Inventory.class);
        if (inventory == null) {
            return null;
        }

        if (!(npc.getEntity() instanceof Player botPlayer)) {
            return null;
        }

        for (int i = 0; i < botPlayer.getInventory().getSize(); i++) {
            ItemStack item = botPlayer.getInventory().getItem(i);
            if (item != null && item.getAmount() > 0) {
                boolean matches = false;
                for (Material material : materials) {
                    if (item.getType() == material) {
                        matches = true;
                        break;
                    }
                }

                if (!matches) {
                    continue;
                }

                ItemStack result = item.clone();
                result.setAmount(1);

                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    botPlayer.getInventory().setItem(i, null);
                }

                return result;
            }
        }

        return null;
    }

    private boolean hasHealingItems() {
        if (!(npc.getEntity() instanceof Player botPlayer)) {
            return false;
        }

        for (int i = 0; i < botPlayer.getInventory().getSize(); i++) {
            ItemStack item = botPlayer.getInventory().getItem(i);
            if (item != null && item.getAmount() > 0) {
                Material type = item.getType();
                if (type == Material.SPLASH_POTION || type == Material.GOLDEN_APPLE || type == Material.ENCHANTED_GOLDEN_APPLE) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canAttack() {
        if (!shouldRespectAttackCooldown()) {
            int spamClickInterval = 2 + RANDOM.nextInt(2);
            return localTick - lastAttackTick >= spamClickInterval && wTapRecoverTick == 0;
        }

        int cooldownTicks = calculateAttackCooldown();
        return localTick - lastAttackTick >= cooldownTicks && wTapRecoverTick == 0;
    }

    private boolean isCriticalHitReady(LivingEntity botEntity) {
        if (botEntity.getVelocity().getY() >= 0) {
            return false;
        }

        if (botEntity instanceof Player botPlayer) {
            return !botPlayer.isSprinting();
        }

        return true;
    }

    private void handleShieldTargeting(LivingEntity botEntity, Player target) {
        if (!(botEntity instanceof Player botPlayer)) {
            return;
        }

        ItemStack axe = findItemInInventory(Material.DIAMOND_AXE, Material.IRON_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.WOODEN_AXE);
        if (axe == null) {
            return;
        }

        ItemStack currentHand = botPlayer.getInventory().getItemInMainHand();
        if (currentHand.getType().name().endsWith("SWORD")) {
            previousMainHand = currentHand;
            botPlayer.getInventory().setItemInMainHand(axe);

            botEntity.swingMainHand();
            target.damage(0.5D, botEntity);

            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
                if (npc.isSpawned() && npc.getEntity() instanceof Player bp) {
                    bp.getInventory().setItemInMainHand(previousMainHand != null ? previousMainHand : new ItemStack(Material.IRON_SWORD));
                }
            }, 5L);
        }
    }

    private boolean shouldRaiseShield(LivingEntity botEntity, double distance) {
        double shieldChance = getShieldChance();

        if (shieldChance <= 0.0D) {
            return false;
        }

        double shieldDistance = 4.0D + shieldChance;
        if (distance > shieldDistance) {
            return false;
        }

        if (!(botEntity instanceof Player botPlayer)) {
            return false;
        }

        ItemStack offHand = botPlayer.getInventory().getItemInOffHand();
        if (offHand.getType() != Material.SHIELD) {
            return false;
        }

        return RANDOM.nextDouble() < shieldChance;
    }

    private void raiseShield(LivingEntity botEntity) {
        if (!(botEntity instanceof Player botPlayer)) {
            return;
        }

        previousMainHand = botPlayer.getInventory().getItemInMainHand();
        botPlayer.setSneaking(true);
        shieldBlockTicks = 10;

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (npc.isSpawned() && npc.getEntity() instanceof Player bp) {
                bp.setSneaking(false);
            }
        }, 10L);
    }

    private void equipTotemsOfUndying(LivingEntity botEntity) {
        if (!(botEntity instanceof Player botPlayer)) {
            return;
        }

        ItemStack totem = findItemInInventory(Material.TOTEM_OF_UNDYING);
        if (totem == null) {
            return;
        }

        botPlayer.getInventory().setItemInOffHand(totem);
    }

    private ItemStack findItemInInventory(Material... materials) {
        if (!(npc.getEntity() instanceof Player botPlayer)) {
            return null;
        }

        for (int i = 0; i < botPlayer.getInventory().getSize(); i++) {
            ItemStack item = botPlayer.getInventory().getItem(i);
            if (item != null && item.getAmount() > 0) {
                for (Material material : materials) {
                    if (item.getType() == material) {
                        return item;
                    }
                }
            }
        }

        return null;
    }

    private int calculateAttackCooldown() {
        return ATTACK_COOLDOWN_TICKS;
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
        if (profile == null || (profile.getStatus() != ProfileStatus.MATCH && profile.getStatus() != ProfileStatus.STARTING)) {
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

    private void updateLookCloseTarget(Player target) {
        LookClose lookClose = npc.getOrAddTrait(LookClose.class);
        lookClose.lookClose(true);
        lookClose.setRandomLook(false);
        lookClose.setRange(64.0D);
        lookClose.setDisableWhileNavigating(false);
    }

    private void disableLookClose() {
        if (!npc.hasTrait(LookClose.class)) {
            return;
        }

        LookClose lookClose = npc.getOrAddTrait(LookClose.class);
        lookClose.lookClose(false);
    }

    private void keepBotInsideArena(LivingEntity botEntity) {
        if (arenaBounds == null) {
            return;
        }

        Location location = botEntity.getLocation();
        if (location.getWorld() == null || !arenaBounds.getWorld().equals(location.getWorld())) {
            return;
        }

        double minX = arenaBounds.getLowerX() + 0.5D;
        double maxX = arenaBounds.getUpperX() + 0.5D;
        double minZ = arenaBounds.getLowerZ() + 0.5D;
        double maxZ = arenaBounds.getUpperZ() + 0.5D;

        Vector velocity = botEntity.getVelocity();
        boolean outsideArena = location.getX() < minX || location.getX() > maxX || location.getZ() < minZ || location.getZ() > maxZ;

        if (outsideArena) {
            Location corrected = location.clone();
            corrected.setX(Math.clamp(location.getX(), minX, maxX));
            corrected.setZ(Math.clamp(location.getZ(), minZ, maxZ));
            botEntity.teleport(corrected);

            Vector pullToCenter = arenaBounds.getCenter().toVector().subtract(corrected.toVector());
            pullToCenter.setY(0);
            if (pullToCenter.lengthSquared() > 0.0001D) {
                pullToCenter.normalize().multiply(ARENA_OUTSIDE_PULL);
                botEntity.setVelocity(new Vector(pullToCenter.getX(), Math.max(velocity.getY(), 0.08D), pullToCenter.getZ()));
            }
            return;
        }

        boolean nearEdge = location.getX() < minX + ARENA_EDGE_BUFFER
                || location.getX() > maxX - ARENA_EDGE_BUFFER
                || location.getZ() < minZ + ARENA_EDGE_BUFFER
                || location.getZ() > maxZ - ARENA_EDGE_BUFFER;

        if (!nearEdge) {
            return;
        }

        Vector pullToCenter = arenaBounds.getCenter().toVector().subtract(location.toVector());
        pullToCenter.setY(0);
        if (pullToCenter.lengthSquared() > 0.0001D) {
            pullToCenter.normalize().multiply(ARENA_EDGE_PULL);
            velocity.add(pullToCenter);
        }

        if (location.getX() < minX + ARENA_EDGE_BUFFER && velocity.getX() < 0) {
            velocity.setX(velocity.getX() * 0.25D + ARENA_EDGE_PULL);
        } else if (location.getX() > maxX - ARENA_EDGE_BUFFER && velocity.getX() > 0) {
            velocity.setX(velocity.getX() * 0.25D - ARENA_EDGE_PULL);
        }

        if (location.getZ() < minZ + ARENA_EDGE_BUFFER && velocity.getZ() < 0) {
            velocity.setZ(velocity.getZ() * 0.25D + ARENA_EDGE_PULL);
        } else if (location.getZ() > maxZ - ARENA_EDGE_BUFFER && velocity.getZ() > 0) {
            velocity.setZ(velocity.getZ() * 0.25D - ARENA_EDGE_PULL);
        }

        double horizontalSpeed = Math.sqrt((velocity.getX() * velocity.getX()) + (velocity.getZ() * velocity.getZ()));
        if (horizontalSpeed > MAX_HORIZONTAL_SPEED) {
            double scale = MAX_HORIZONTAL_SPEED / horizontalSpeed;
            velocity.setX(velocity.getX() * scale);
            velocity.setZ(velocity.getZ() * scale);
        }

        botEntity.setVelocity(velocity);
    }

    private ItemStack readMainHand() {
        if (npc.getEntity() instanceof Player botPlayer) {
            return botPlayer.getInventory().getItemInMainHand();
        }

        return new ItemStack(Material.AIR);
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

    private double getMaxHealth(LivingEntity entity) {
        org.bukkit.attribute.AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double maxH = maxHealthAttr.getValue();
            return maxH > 0 ? maxH : 20.0D;
        }
        return 20.0D;
    }

    private enum BotState {
        CHASING,
        ATTACKING,
        HEALING
    }

    public enum BotDifficulty {
        NOOB,
        EASY,
        NORMAL,
        HARD,
        EXPERT
    }
}