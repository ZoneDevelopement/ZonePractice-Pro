package dev.nandi0813.practice.manager.fight.event.util;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.event.EventSetupManager;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.NumberUtil;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public enum EventUtil {
    ;

    public static void changeStatus(EventData eventData, Player player) {
        try {
            eventData.setEnabled(!eventData.isEnabled());

            GUIManager.getInstance().getGuis().get(GUIType.Event_Host).update();
            GUIManager.getInstance().getGuis().get(GUIType.Event_Summary).update();
            EventSetupManager.getInstance().getEventSetupGUIs().get(eventData).get(GUIType.Event_Main).update();

            Common.sendMMMessage(player, "<green>Event successfully " + (eventData.isEnabled() ? "enabled" : "disabled") + "!");
        } catch (Exception e) {
            Common.sendMMMessage(player, "<red>" + e.getMessage());
        }
    }

    public static void setEventSpectatorInventory(Player player) {
        ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.SPECTATE);
        PlayerUtil.clearPlayer(player, false, true, false);

        InventoryManager.getInstance().setInventory(player, Inventory.InventoryType.SPECTATE_EVENT);
    }

    public static void sendCompassTracker(Player player) {
        Player target = getClosestTarget(player);
        if (target != null) {
            player.setCompassTarget(target.getLocation());
            double distance = NumberUtil.roundDouble(player.getLocation().distance(target.getLocation()));

            Profile profile = ProfileManager.getInstance().getProfile(player);
            profile.getActionBar().setActionBar(LanguageManager.getString("EVENT.COMPASS-TRACKER-ACTIONBAR")
                            .replaceAll("%target%", target.getName())
                            .replaceAll("%distance%", String.valueOf(distance)),
                    5);
        }
    }

    public static Player getClosestTarget(Player player) {
        Player target = null;
        double distance = Double.POSITIVE_INFINITY;
        List<Entity> near = player.getNearbyEntities(50, 50, 50);

        for (Entity entity : near) {
            if (!(entity instanceof Player) || entity == player) continue;

            ProfileStatus entityStatus = ProfileManager.getInstance().getProfile(entity).getStatus();
            if (!entityStatus.equals(ProfileStatus.EVENT) && !entityStatus.equals(ProfileStatus.MATCH)) continue;

            double distanceTo = player.getLocation().distance(entity.getLocation());

            if (distanceTo < distance) {
                distance = distanceTo;
                target = (Player) entity;
            }
        }

        return target;
    }

}
