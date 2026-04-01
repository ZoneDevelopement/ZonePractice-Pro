package dev.nandi0813.practice.telemetry.collector;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.telemetry.ServerFingerprintUtil;
import dev.nandi0813.practice.telemetry.transport.ai.AiTrainingMatchPayload;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AiTrainingCollector {

    private static final int SCHEMA_VERSION = 1;
    private static final Set<LadderType> ALLOWED_LADDER_TYPES = Set.of(LadderType.BASIC, LadderType.BUILD, LadderType.BOXING, LadderType.SUMO);
    private static final long CAPTURE_INTERVAL_TICKS = 2L;
    private static final int CACHED_REFRESH_SAMPLES_FAR = 2; // 2 samples x 2 ticks = 4 ticks
    private static final double COMBAT_DISTANCE_SQUARED = 64.0D;

    private final Match match;
    private final String matchId;
    private final String ladderName;
    private final String ladderType;
    private final String arenaName;
    private final String arenaType;
    private final String serverHash;

    private final List<JSONObject> rows = Collections.synchronizedList(new ArrayList<>());
    private final Map<UUID, PlayerFrameState> playerState = new ConcurrentHashMap<>();

    private BukkitTask samplerTask;
    private long lastTimestamp;

    public AiTrainingCollector(Match match) {
        this.match = match;
        this.matchId = normalizeMatchId(match.getId());
        this.ladderName = match.getLadder().getName();
        this.ladderType = normalizeLadderType(match);
        this.arenaName = match.getArena().getName();
        this.arenaType = match.getLadder().isBuild() ? "build" : "flat";
        this.serverHash = ServerFingerprintUtil.getServerId();
    }

    public static boolean isSupportedLadder(Match match) {
        if (match == null || match.getLadder() == null || match.getLadder().getType() == null) {
            return false;
        }

        return ALLOWED_LADDER_TYPES.contains(match.getLadder().getType());
    }

    public void start() {
        if (samplerTask != null) {
            return;
        }

        samplerTask = ZonePractice.getInstance().getServer().getScheduler().runTaskTimer(
                ZonePractice.getInstance(),
                this::captureFrame,
                1L,
                CAPTURE_INTERVAL_TICKS
        );
    }

    public void stop() {
        if (samplerTask != null) {
            samplerTask.cancel();
            samplerTask = null;
        }
    }

    public void markLmb(Player player) {
        state(player).inputLmb = true;
    }

    public void markRmb(Player player) {
        state(player).inputRmb = true;
    }

    public void markInventoryOpen(Player player) {
        state(player).inputInventoryOpen = true;
    }

    public void incrementBlocksPlaced(Player player) {
        state(player).blocksPlaced += 1;
    }

    public void addDamageDealt(Player player, double amount) {
        state(player).damageDealt += Math.max(0.0D, amount);
    }

    public void addDamageTaken(Player player, double amount) {
        state(player).damageTaken += Math.max(0.0D, amount);
    }

    public AiTrainingMatchPayload toPayload() {
        return new AiTrainingMatchPayload(
                SCHEMA_VERSION,
                matchId,
                ladderName,
                ladderType,
                arenaName,
                arenaType,
                serverHash,
                System.currentTimeMillis(),
                new ArrayList<>(rows)
        );
    }

    public boolean hasRows() {
        return !rows.isEmpty();
    }

    private void captureFrame() {
        for (Player player : match.getPlayers()) {
            if (player == null || !player.isOnline()) {
                continue;
            }

            PlayerFrameState frameState = state(player);
            Player target = resolveTarget(player);
            Location currentLocation = player.getLocation();

            JSONObject row = new JSONObject();
            row.put("timestamp", nextTimestamp());
            row.put("playerName", player.getName());
            row.put("ladderName", ladderName);
            row.put("ladderType", ladderType);
            row.put("arenaName", arenaName);
            row.put("arenaType", arenaType);
            row.put("serverHash", serverHash);

            row.put("health", player.getHealth() + player.getAbsorptionAmount());
            row.put("posX", currentLocation.getX());
            row.put("posY", currentLocation.getY());
            row.put("posZ", currentLocation.getZ());
            row.put("velX", player.getVelocity().getX());
            row.put("velY", player.getVelocity().getY());
            row.put("velZ", player.getVelocity().getZ());
            row.put("yaw", currentLocation.getYaw());
            row.put("pitch", currentLocation.getPitch());
            row.put("isOnGround", player.isOnGround());

            MovementInput movementInput = resolveMovementInput(currentLocation, frameState.previousLocation, frameState.previousOnGround);
            row.put("inputForward", movementInput.forward);
            row.put("inputBackward", movementInput.backward);
            row.put("inputLeft", movementInput.left);
            row.put("inputRight", movementInput.right);
            row.put("inputJump", movementInput.jump);
            row.put("inputSneak", player.isSneaking());
            row.put("inputSprint", player.isSprinting());
            row.put("inputLmb", frameState.inputLmb);
            row.put("inputRmb", frameState.inputRmb);
            row.put("inputSlot", player.getInventory().getHeldItemSlot());
            row.put("inputInventoryOpen", frameState.inputInventoryOpen || isInventoryOpen(player));

            row.put("foodLevel", Math.min(20, Math.max(0, player.getFoodLevel())));
            row.put("totalArmor", getTotalArmor(player));
            row.put("helmetArmor", getHelmetArmor(player));
            row.put("chestplateArmor", getChestplateArmor(player));
            row.put("leggingsArmor", getLeggingsArmor(player));
            row.put("bootsArmor", getBootsArmor(player));
            row.put("attackCooldown", getAttackCooldownPercent(player));
            row.put("isUsingItem", isUsingItem(player));
            row.put("itemUseDuration", getItemUseDuration(player));

            PlayerInventory inventory = player.getInventory();
            row.put("selectedSlot", inventory.getHeldItemSlot());
            row.put("mainHandItem", serializeItem(inventory.getItemInMainHand()));
            row.put("offHandItem", serializeItem(inventory.getItemInOffHand()));
            row.put("hotbar0", serializeItem(inventory.getItem(0)));
            row.put("hotbar1", serializeItem(inventory.getItem(1)));
            row.put("hotbar2", serializeItem(inventory.getItem(2)));
            row.put("hotbar3", serializeItem(inventory.getItem(3)));
            row.put("hotbar4", serializeItem(inventory.getItem(4)));
            row.put("hotbar5", serializeItem(inventory.getItem(5)));
            row.put("hotbar6", serializeItem(inventory.getItem(6)));
            row.put("hotbar7", serializeItem(inventory.getItem(7)));
            row.put("hotbar8", serializeItem(inventory.getItem(8)));
            row.put("inventoryBag", serializeInventoryBag(inventory));

            row.put("hasSpeed", hasPotion(player, "SPEED"));
            row.put("hasSlowness", hasPotion(player, "SLOW", "SLOWNESS"));
            row.put("hasStrength", hasPotion(player, "INCREASE_DAMAGE", "STRENGTH"));
            row.put("hasWeakness", hasPotion(player, "WEAKNESS"));
            row.put("hasRegeneration", hasPotion(player, "REGENERATION"));
            row.put("hasPoison", hasPotion(player, "POISON"));

            writeTargetState(row, player, target);
            boolean inCombatRange = isInCombatRange(player, target);
            ObstacleState obstacleState = resolveObstacleState(player, frameState, inCombatRange);
            ThreatState threatState = resolveThreatState(player, frameState, inCombatRange);
            writeObstacleState(row, obstacleState);
            writeThreatState(row, threatState);

            row.put("damageDealt", consumeDamageDealt(frameState));
            row.put("damageTaken", consumeDamageTaken(frameState));
            row.put("blocksPlaced", consumeBlocksPlaced(frameState));

            rows.add(row);

            frameState.previousLocation = currentLocation.clone();
            frameState.previousOnGround = player.isOnGround();
            frameState.inputLmb = false;
            frameState.inputRmb = false;
            frameState.inputInventoryOpen = false;
            frameState.samplesCaptured += 1;
        }
    }

    private static boolean isInCombatRange(Player player, Player target) {
        if (target == null) {
            return false;
        }
        return target.getLocation().distanceSquared(player.getLocation()) <= COMBAT_DISTANCE_SQUARED;
    }

    private static boolean shouldRefreshCachedState(PlayerFrameState frameState, boolean inCombatRange) {
        if (inCombatRange) {
            return true;
        }
        return frameState.samplesCaptured % CACHED_REFRESH_SAMPLES_FAR == 0;
    }

    private static ObstacleState resolveObstacleState(Player player, PlayerFrameState frameState, boolean inCombatRange) {
        if (frameState.obstacleState == null || shouldRefreshCachedState(frameState, inCombatRange)) {
            frameState.obstacleState = computeObstacleState(player);
        }
        return frameState.obstacleState;
    }

    private static ThreatState resolveThreatState(Player player, PlayerFrameState frameState, boolean inCombatRange) {
        if (frameState.threatState == null || shouldRefreshCachedState(frameState, inCombatRange)) {
            frameState.threatState = computeThreatState(player);
        }
        return frameState.threatState;
    }

    private PlayerFrameState state(Player player) {
        return playerState.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerFrameState(player.getLocation().clone(), player.isOnGround()));
    }

    private long nextTimestamp() {
        long now = System.currentTimeMillis();
        if (now <= lastTimestamp) {
            now = lastTimestamp + 1;
        }
        lastTimestamp = now;
        return now;
    }

    private int consumeBlocksPlaced(PlayerFrameState frameState) {
        int delta = Math.max(0, frameState.blocksPlaced - frameState.blocksPlacedSent);
        frameState.blocksPlacedSent = frameState.blocksPlaced;
        return delta;
    }

    private double consumeDamageDealt(PlayerFrameState frameState) {
        double delta = Math.max(0.0D, frameState.damageDealt - frameState.damageDealtSent);
        frameState.damageDealtSent = frameState.damageDealt;
        return delta;
    }

    private double consumeDamageTaken(PlayerFrameState frameState) {
        double delta = Math.max(0.0D, frameState.damageTaken - frameState.damageTakenSent);
        frameState.damageTakenSent = frameState.damageTaken;
        return delta;
    }

    private static MovementInput resolveMovementInput(Location current, Location previous, boolean previousOnGround) {
        if (previous == null) {
            return MovementInput.NONE;
        }

        double dx = current.getX() - previous.getX();
        double dz = current.getZ() - previous.getZ();
        double yaw = Math.toRadians(current.getYaw());

        double forwardComponent = -Math.sin(yaw) * dx + Math.cos(yaw) * dz;
        double strafeComponent = Math.cos(yaw) * dx + Math.sin(yaw) * dz;
        double threshold = 0.03D;

        boolean forward = forwardComponent > threshold;
        boolean backward = forwardComponent < -threshold;
        boolean left = strafeComponent > threshold;
        boolean right = strafeComponent < -threshold;
        boolean jump = !previousOnGround && current.getY() > previous.getY();

        return new MovementInput(forward, backward, left, right, jump);
    }

    private static boolean isInventoryOpen(Player player) {
        try {
            return player.getOpenInventory().getTopInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static double getTotalArmor(Player player) {
        try {
            var armor = player.getAttribute(Attribute.ARMOR);
            if (armor != null) {
                return armor.getValue();
            }
        } catch (Exception ignored) {
        }
        return 0.0D;
    }

    private static float getHelmetArmor(Player player) {
        return getArmorPieceValue(player.getInventory().getHelmet());
    }

    private static float getChestplateArmor(Player player) {
        return getArmorPieceValue(player.getInventory().getChestplate());
    }

    private static float getLeggingsArmor(Player player) {
        return getArmorPieceValue(player.getInventory().getLeggings());
    }

    private static float getBootsArmor(Player player) {
        return getArmorPieceValue(player.getInventory().getBoots());
    }

    private static float getArmorPieceValue(ItemStack armorPiece) {
        if (armorPiece == null || armorPiece.getType() == Material.AIR) {
            return 0.0f;
        }

        // Return base armor points per item type
        // Based on Minecraft's armor point values
        switch (armorPiece.getType()) {
            // Leather armor (1 point each)
            case LEATHER_HELMET -> { return 1.0f; }
            case LEATHER_CHESTPLATE -> { return 3.0f; }
            case LEATHER_LEGGINGS -> { return 2.0f; }
            case LEATHER_BOOTS -> { return 1.0f; }

            // Iron armor
            case IRON_HELMET -> { return 2.0f; }
            case IRON_CHESTPLATE -> { return 6.0f; }
            case IRON_LEGGINGS -> { return 5.0f; }
            case IRON_BOOTS -> { return 2.0f; }

            // Golden armor
            case GOLDEN_HELMET -> { return 2.0f; }
            case GOLDEN_CHESTPLATE -> { return 5.0f; }
            case GOLDEN_LEGGINGS -> { return 3.0f; }
            case GOLDEN_BOOTS -> { return 1.0f; }

            // Diamond armor
            case DIAMOND_HELMET -> { return 3.0f; }
            case DIAMOND_CHESTPLATE -> { return 8.0f; }
            case DIAMOND_LEGGINGS -> { return 6.0f; }
            case DIAMOND_BOOTS -> { return 3.0f; }

            // Netherite armor
            case NETHERITE_HELMET -> { return 3.0f; }
            case NETHERITE_CHESTPLATE -> { return 8.0f; }
            case NETHERITE_LEGGINGS -> { return 6.0f; }
            case NETHERITE_BOOTS -> { return 3.0f; }

            // Chain armor
            case CHAINMAIL_HELMET -> { return 2.0f; }
            case CHAINMAIL_CHESTPLATE -> { return 5.0f; }
            case CHAINMAIL_LEGGINGS -> { return 4.0f; }
            case CHAINMAIL_BOOTS -> { return 1.0f; }

            default -> { return 0.0f; }
        }
    }

    private static double getAttackCooldown(Player player) {
        try {
            return player.getAttackCooldown();
        } catch (Exception ignored) {
            return 1.0D;
        }
    }

    private static int getAttackCooldownPercent(Player player) {
        double cooldown = getAttackCooldown(player);
        double clamped = Math.max(0.0D, Math.min(1.0D, cooldown));
        return (int) Math.round(clamped * 100.0D);
    }

    private static boolean isUsingItem(Player player) {
        try {
            Object value = player.getClass().getMethod("isHandRaised").invoke(player);
            if (value instanceof Boolean result) {
                return result;
            }
        } catch (Exception ignored) {
        }

        try {
            Object value = player.getClass().getMethod("isBlocking").invoke(player);
            if (value instanceof Boolean result) {
                return result;
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    private static int getItemUseDuration(Player player) {
        try {
            Object value = player.getClass().getMethod("getItemUseRemainingTime").invoke(player);
            if (value instanceof Integer result) {
                return Math.max(0, result);
            }
        } catch (Exception ignored) {
        }

        return 0;
    }

    private static boolean hasPotion(Player player, String... aliases) {
        for (String alias : aliases) {
            PotionEffectType type = PotionEffectType.getByName(alias);
            if (type != null && player.hasPotionEffect(type)) {
                return true;
            }
        }
        return false;
    }

    private static String serializeItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return "AIR";
        }

        return itemStack.getType().name() + "x" + itemStack.getAmount();
    }

    private static String serializeInventoryBag(PlayerInventory inventory) {
        StringBuilder builder = new StringBuilder();
        ItemStack[] storage = inventory.getStorageContents();
        for (int i = 0; i < storage.length; i++) {
            if (i > 0) {
                builder.append(';');
            }
            builder.append(i).append('=').append(serializeItem(storage[i]));
        }
        return builder.toString();
    }

    private static void writeTargetState(JSONObject row, Player player, Player target) {
        if (target == null) {
            row.put("targetDistance", 0.0D);
            row.put("targetRelX", 0.0D);
            row.put("targetRelY", 0.0D);
            row.put("targetRelZ", 0.0D);
            row.put("targetYaw", 0.0F);
            row.put("targetPitch", 0.0F);
            row.put("targetVelX", 0.0D);
            row.put("targetVelY", 0.0D);
            row.put("targetVelZ", 0.0D);
            row.put("targetHealth", 0.0D);
            return;
        }

        Vector delta = target.getLocation().toVector().subtract(player.getLocation().toVector());
        row.put("targetDistance", target.getLocation().distance(player.getLocation()));
        row.put("targetRelX", delta.getX());
        row.put("targetRelY", delta.getY());
        row.put("targetRelZ", delta.getZ());
        row.put("targetYaw", target.getLocation().getYaw());
        row.put("targetPitch", target.getLocation().getPitch());
        row.put("targetVelX", target.getVelocity().getX());
        row.put("targetVelY", target.getVelocity().getY());
        row.put("targetVelZ", target.getVelocity().getZ());
        row.put("targetHealth", target.getHealth() + target.getAbsorptionAmount());
    }

    private static ObstacleState computeObstacleState(Player player) {
        double rayForward = rayDistance(player, 0.0F);
        double rayLeft = rayDistance(player, -90.0F);
        double rayRight = rayDistance(player, 90.0F);
        double rayBackward = rayDistance(player, 180.0F);
        double distanceToGround = distanceToGround(player);

        Block targetBlock = resolveTargetBlock(player);
        if (targetBlock == null) {
            return new ObstacleState(rayForward, rayLeft, rayRight, rayBackward, distanceToGround, 0, 0, 0);
        }

        return new ObstacleState(
                rayForward,
                rayLeft,
                rayRight,
                rayBackward,
                distanceToGround,
                targetBlock.getX(),
                targetBlock.getY(),
                targetBlock.getZ()
        );
    }

    private static void writeObstacleState(JSONObject row, ObstacleState obstacleState) {
        row.put("rayForward", obstacleState.rayForward());
        row.put("rayLeft", obstacleState.rayLeft());
        row.put("rayRight", obstacleState.rayRight());
        row.put("rayBackward", obstacleState.rayBackward());
        row.put("distanceToGround", obstacleState.distanceToGround());
        row.put("lookingAtBlockX", obstacleState.lookingAtBlockX());
        row.put("lookingAtBlockY", obstacleState.lookingAtBlockY());
        row.put("lookingAtBlockZ", obstacleState.lookingAtBlockZ());
    }

    private static Block resolveTargetBlock(Player player) {
        try {
            return player.getTargetBlockExact(6);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double rayDistance(Player player, float yawOffset) {
        Location eye = player.getEyeLocation();
        Location adjustedEye = eye.clone();
        adjustedEye.setYaw(eye.getYaw() + yawOffset);
        adjustedEye.setPitch(0.0F);

        Vector direction = adjustedEye.getDirection().normalize();
        RayTraceResult result = player.getWorld().rayTraceBlocks(eye, direction, 6.0D);
        if (result == null) {
            return 6.0D;
        }

        return result.getHitPosition().distance(eye.toVector());
    }

    private static double distanceToGround(Player player) {
        Location location = player.getLocation();
        World world = player.getWorld();
        int minY = world.getMinHeight();

        for (int y = location.getBlockY(); y >= minY; y--) {
            Material material = world.getBlockAt(location.getBlockX(), y, location.getBlockZ()).getType();
            if (!material.isAir()) {
                return Math.max(0.0D, location.getY() - (y + 1));
            }
        }

        return Math.max(0.0D, location.getY() - minY);
    }

    private static ThreatState computeThreatState(Player player) {
        Entity nearestProjectile = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Entity entity : player.getNearbyEntities(16.0D, 8.0D, 16.0D)) {
            if (!(entity instanceof Projectile projectile)) {
                continue;
            }

            if (projectile.getShooter() instanceof Player shooter && shooter.equals(player)) {
                continue;
            }

            double currentDistanceSquared = entity.getLocation().distanceSquared(player.getLocation());
            if (currentDistanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = currentDistanceSquared;
                nearestProjectile = entity;
            }
        }

        if (nearestProjectile == null) {
            return new ThreatState(0.0D, 0.0D, 0.0D);
        }

        Vector delta = nearestProjectile.getLocation().toVector().subtract(player.getLocation().toVector());
        return new ThreatState(delta.getX(), delta.getY(), delta.getZ());
    }

    private static void writeThreatState(JSONObject row, ThreatState threatState) {
        row.put("nearestProjectileDx", threatState.nearestProjectileDx());
        row.put("nearestProjectileDy", threatState.nearestProjectileDy());
        row.put("nearestProjectileDz", threatState.nearestProjectileDz());
    }

    private Player resolveTarget(Player player) {
        Player nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Player candidate : match.getPlayers()) {
            if (candidate == null || !candidate.isOnline() || candidate.equals(player)) {
                continue;
            }

            try {
                double currentDistanceSquared = candidate.getLocation().distanceSquared(player.getLocation());
                if (currentDistanceSquared < nearestDistanceSquared) {
                    nearestDistanceSquared = currentDistanceSquared;
                    nearest = candidate;
                }
            } catch (Exception ignored) {}
        }

        return nearest;
    }

    private static String normalizeLadderType(Match match) {
        LadderType ladderType = match.getLadder().getType();
        if (ladderType == null) {
            return "";
        }
        return ladderType.name().toLowerCase(Locale.ROOT);
    }

    private static String normalizeMatchId(String rawMatchId) {
        if (rawMatchId == null || rawMatchId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        try {
            return UUID.fromString(rawMatchId).toString();
        } catch (IllegalArgumentException ignored) {
            return UUID.nameUUIDFromBytes(rawMatchId.getBytes(StandardCharsets.UTF_8)).toString();
        }
    }

    private record MovementInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump) {
        private static final MovementInput NONE = new MovementInput(false, false, false, false, false);
    }

    private record ObstacleState(
            double rayForward,
            double rayLeft,
            double rayRight,
            double rayBackward,
            double distanceToGround,
            int lookingAtBlockX,
            int lookingAtBlockY,
            int lookingAtBlockZ
    ) {
    }

    private record ThreatState(
            double nearestProjectileDx,
            double nearestProjectileDy,
            double nearestProjectileDz
    ) {
    }

    private static final class PlayerFrameState {
        private Location previousLocation;
        private boolean previousOnGround;
        private boolean inputLmb;
        private boolean inputRmb;
        private boolean inputInventoryOpen;
        private long samplesCaptured;
        private ObstacleState obstacleState;
        private ThreatState threatState;

        private int blocksPlaced;
        private int blocksPlacedSent;
        private double damageDealt;
        private double damageDealtSent;
        private double damageTaken;
        private double damageTakenSent;

        private PlayerFrameState(Location previousLocation, boolean previousOnGround) {
            this.previousLocation = previousLocation;
            this.previousOnGround = previousOnGround;
        }
    }
}


