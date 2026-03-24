package dev.nandi0813.practice.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class GoldenHead implements Listener {

    private final NamespacedKey goldenHeadKey = new NamespacedKey(ZonePractice.getInstance(), "golden_head_item");
    private ItemStack goldenHeadItem;
    private final List<PotionEffect> effects = new ArrayList<>();
    private int consumeCooldownSeconds;

    private final Map<UUID, Long> lastConsumeAt = new HashMap<>();
    private final Map<UUID, BukkitTask> cooldownActionBarTasks = new HashMap<>();

    public GoldenHead() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
        reload();
    }

    public void reload() {
        this.consumeCooldownSeconds = Math.max(0, ConfigManager.getInt("MATCH-SETTINGS.GOLDEN-HEAD.COOLDOWN"));

        ItemStack item = ConfigManager.getGuiItem("MATCH-SETTINGS.GOLDEN-HEAD.ITEM").get();
        if (item == null) {
            this.goldenHeadItem = new ItemStack(org.bukkit.Material.GOLDEN_APPLE);
        } else {
            this.goldenHeadItem = item;
        }

        applyCustomTexture(this.goldenHeadItem);
        markAsGoldenHead(this.goldenHeadItem);
        this.goldenHeadItem.setAmount(1);

        this.effects.clear();
        loadEffects();
    }

    public ItemStack getItem() {
        return this.goldenHeadItem.clone();
    }

    private void loadEffects() {
        for (String effect : ConfigManager.getList("MATCH-SETTINGS.GOLDEN-HEAD.EFFECTS")) {
            String[] split = effect.split("::");
            if (split.length != 3) continue;

            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(split[0].toLowerCase()));
            if (type == null) {
                type = Registry.EFFECT.get(NamespacedKey.minecraft(split[0].replace(' ', '_').toLowerCase()));
            }
            if (type == null) continue;

            if (StringUtil.isNotInteger(split[1])) continue;
            int duration = Integer.parseInt(split[1]);

            if (StringUtil.isNotInteger(split[2])) continue;
            int amplifier = Integer.parseInt(split[2]);
            if (amplifier < 1) continue;

            PotionEffect potionEffect = new PotionEffect(type, duration * 20, (amplifier - 1));
            effects.add(potionEffect);
        }
    }

    private void markAsGoldenHead(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        itemMeta.getPersistentDataContainer().set(goldenHeadKey, PersistentDataType.BYTE, (byte) 1);
        itemStack.setItemMeta(itemMeta);
    }

    public boolean isGoldenHead(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null && itemMeta.getPersistentDataContainer().has(goldenHeadKey, PersistentDataType.BYTE)) {
            return true;
        }

        return this.goldenHeadItem != null && itemStack.isSimilar(this.goldenHeadItem);
    }

    private void applyCustomTexture(ItemStack item) {
        String textureConfigValue = ConfigManager.getConfig().getString("MATCH-SETTINGS.GOLDEN-HEAD.TEXTURE", "").trim();
        if (textureConfigValue.isEmpty()) {
            return;
        }

        ItemMeta oldMeta = item.getItemMeta();
        item.setType(org.bukkit.Material.PLAYER_HEAD);

        SkullMeta skullMeta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(org.bukkit.Material.PLAYER_HEAD);
        if (skullMeta == null) {
            return;
        }

        if (oldMeta != null) {
            if (oldMeta.hasDisplayName()) {
                skullMeta.setDisplayName(oldMeta.getDisplayName());
            }
            if (oldMeta.hasLore()) {
                skullMeta.setLore(oldMeta.getLore());
            }
        }

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "GoldenHead");

        if (textureConfigValue.startsWith("http://") || textureConfigValue.startsWith("https://")) {
            String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureConfigValue + "\"}}}";
            textureConfigValue = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        }

        profile.setProperty(new ProfileProperty("textures", textureConfigValue));

        skullMeta.setPlayerProfile(profile);
        item.setItemMeta(skullMeta);
    }

    private long getRemainingCooldownSeconds(UUID playerId) {
        if (this.consumeCooldownSeconds < 1) {
            return 0;
        }

        Long lastUse = this.lastConsumeAt.get(playerId);
        if (lastUse == null) {
            return 0;
        }

        long elapsedSeconds = (System.currentTimeMillis() - lastUse) / 1000L;
        long remaining = this.consumeCooldownSeconds - elapsedSeconds;
        return Math.max(0, remaining);
    }

    private void startCooldownActionBar(Profile profile) {
        if (this.consumeCooldownSeconds < 1) {
            return;
        }

        if (profile.getActionBar().isLock() && !this.cooldownActionBarTasks.containsKey(profile.getUuid())) {
            return;
        }

        UUID playerId = profile.getUuid();

        if (!this.cooldownActionBarTasks.containsKey(playerId)) {
            profile.getActionBar().createActionBar();
        }

        BukkitTask existingTask = this.cooldownActionBarTasks.remove(playerId);
        if (existingTask != null) {
            existingTask.cancel();
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(ZonePractice.getInstance(), () -> {
            Player onlinePlayer = profile.getPlayer().getPlayer();
            if (onlinePlayer == null || !onlinePlayer.isOnline()) {
                profile.getActionBar().cancelActionBar();
                BukkitTask removed = this.cooldownActionBarTasks.remove(playerId);
                if (removed != null) {
                    removed.cancel();
                }
                return;
            }

            long remaining = getRemainingCooldownSeconds(playerId);
            if (remaining <= 0) {
                profile.getActionBar().cancelActionBar();
                BukkitTask removed = this.cooldownActionBarTasks.remove(playerId);
                if (removed != null) {
                    removed.cancel();
                }
                return;
            }

            profile.getActionBar().setMessage("<gold>🕒 <yellow>" + remaining + "s");
            profile.getActionBar().send();
        }, 0L, 20L);

        this.cooldownActionBarTasks.put(playerId, task);
    }

    @EventHandler
    public void onGoldenHeadConsume(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        ItemStack item = e.getItem();
        if (item == null) return;
        if (!isGoldenHead(item)) return;

        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        long remainingSeconds = getRemainingCooldownSeconds(player.getUniqueId());
        if (remainingSeconds > 0) {
            e.setCancelled(true);
            startCooldownActionBar(profile);
            return;
        }

        e.setCancelled(true);
        this.lastConsumeAt.put(player.getUniqueId(), System.currentTimeMillis());
        startCooldownActionBar(profile);

        int amount = item.getAmount();

        if (amount == 1)
            player.getInventory().setItemInMainHand(null);
        else
            item.setAmount(amount - 1);

        for (PotionEffect effect : effects) {
            boolean activate = true;
            for (PotionEffect active : player.getActivePotionEffects()) {
                if (!effect.getType().equals(active.getType()))
                    continue;

                if (effect.getAmplifier() < active.getAmplifier()) {
                    activate = false;
                    break;
                }
                if (effect.getDuration() < active.getDuration()) {
                    activate = false;
                    break;
                }
            }

            if (activate)
                player.addPotionEffect(effect);
        }

        player.updateInventory();
    }

}