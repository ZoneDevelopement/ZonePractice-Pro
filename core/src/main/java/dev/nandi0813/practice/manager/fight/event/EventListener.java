package dev.nandi0813.practice.manager.fight.event;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.Brackets;
import dev.nandi0813.practice.manager.fight.event.events.ffa.lms.LMS;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.tnttag.TNTTag;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.event.util.EventUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.EnderpearlRunnable;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {

    private final EventManager eventManager;

    public EventListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onTrackerUse(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        if (!e.getAction().equals(Action.RIGHT_CLICK_AIR) && !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Event event = EventManager.getInstance().getEventByPlayer(player);
        if (!event.getStatus().equals(EventStatus.LIVE)) {
            return;
        }

        ItemStack item = ClassImport.getClasses().getPlayerUtil().getItemInUse(player, EventManager.PLAYER_TRACKER.getType());
        if (item == null) {
            return;
        }

        if (!item.equals(EventManager.PLAYER_TRACKER)) {
            return;
        }

        e.setCancelled(true);
        EventUtil.sendCompassTracker(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = EventManager.getInstance().getEventByPlayer(player);
        if (!(event instanceof Brackets) && !(event instanceof LMS)) {
            e.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onEnderPearlLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof EnderPearl enderPearl)) {
            return;
        }

        if (!(enderPearl.getShooter() instanceof Player player)) {
            return;
        }

        Event event = EventManager.getInstance().getEventByPlayer(player);
        if (event == null) {
            return;
        }

        if (!event.getStatus().equals(EventStatus.LIVE)) {
            e.setCancelled(true);
        } else {
            int duration = ConfigManager.getInt("EVENT.ENDERPEARL-COOLDOWN");
            if (duration <= 0) return;

            if (!PlayerCooldown.isActive(player, CooldownObject.ENDER_PEARL)) {
                EnderpearlRunnable enderPearlCountdown = new EnderpearlRunnable(player, duration);
                enderPearlCountdown.begin();
            } else {
                e.setCancelled(true);

                Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("EVENT.ENDERPEARL-COOLDOWN"), PlayerCooldown.getLeftInDouble(player, CooldownObject.ENDER_PEARL)));
                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Entity entity = e.getEntity();

        if (entity.hasMetadata(TNTTag.TNT_TAG_TNT_METADATA)) {
            e.blockList().clear();
        }
    }


    /**
     * Event listeners interface
     */

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile == null) {
            return;
        }

        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = eventManager.getEventByPlayer(player);
        if (event == null) {
            return;
        }

        eventManager.getEventListeners().get(event.getType()).onEntityDamage(event, e);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile == null) {
            return;
        }

        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = eventManager.getEventByPlayer(player);
        if (event == null) {
            return;
        }

        eventManager.getEventListeners().get(event.getType()).onEntityDamageByEntity(event, e);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = eventManager.getEventByPlayer(player);
        if (event == null) {
            return;
        }

        eventManager.getEventListeners().get(event.getType()).onProjectileLaunch(event, e);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = eventManager.getEventByPlayer(player);
        if (event == null) {
            return;
        }

        eventManager.getEventListeners().get(event.getType()).onPlayerQuit(event, e);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = eventManager.getEventByPlayer(player);
        if (event == null) {
            return;
        }

        eventManager.getEventListeners().get(event.getType()).onPlayerMove(event, e);
    }

    @EventHandler
    public void onPlayerEggThrow(final PlayerEggThrowEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = eventManager.getEventByPlayer(player);
        if (event == null) {
            return;
        }

        eventManager.getEventListeners().get(event.getType()).onPlayerEggThrow(event, e);
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = eventManager.getEventByPlayer(player);
        if (event == null) {
            return;
        }

        eventManager.getEventListeners().get(event.getType()).onPlayerDropItem(event, e);

        if (!e.isCancelled()) {
            Entity drop = e.getItemDrop();
            event.getFightChange().addEntityChange(drop);
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = eventManager.getEventByPlayer(player);
        if (event == null) {
            return;
        }

        eventManager.getEventListeners().get(event.getType()).onPlayerInteract(event, e);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.EVENT)) {
            return;
        }

        Event event = eventManager.getEventByPlayer(player);
        if (event == null) {
            return;
        }

        eventManager.getEventListeners().get(event.getType()).onInventoryClick(event, e);
    }

}
