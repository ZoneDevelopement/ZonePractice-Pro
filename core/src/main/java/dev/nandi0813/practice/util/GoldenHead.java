package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class GoldenHead implements Listener {

    private final ItemStack goldenHeadItem;
    private final List<PotionEffect> effects = new ArrayList<>();

    public GoldenHead() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());

        goldenHeadItem = getItem();
        goldenHeadItem.setAmount(1);

        getEffects();
    }

    public ItemStack getItem() {
        return ConfigManager.getGuiItem("MATCH-SETTINGS.GOLDEN-HEAD.ITEM").get();
    }

    public void getEffects() {
        for (String effect : ConfigManager.getList("MATCH-SETTINGS.GOLDEN-HEAD.EFFECTS")) {
            String[] split = effect.split("::");
            if (split.length != 3) continue;

            PotionEffectType type = PotionEffectType.getByName(split[0]);
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

    @EventHandler
    public void onGoldenHeadConsume(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        ItemStack item = e.getItem();
        if (item == null) return;
        if (!item.hasItemMeta()) return;
        if (!item.getItemMeta().equals(goldenHeadItem.getItemMeta())) return;

        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
                return;
        }

        e.setCancelled(true);

        int amount = item.getAmount();

        if (amount == 1)
            player.setItemInHand(null);
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
                player.addPotionEffect(effect, true);
        }

        player.updateInventory();
    }

}
