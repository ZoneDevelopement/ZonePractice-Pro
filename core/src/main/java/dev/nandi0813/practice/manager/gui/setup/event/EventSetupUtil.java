package dev.nandi0813.practice.manager.gui.setup.event;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum EventSetupUtil {
    ;

    // Marker item
    public static ItemStack getMarkerItem(EventData eventData) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&eEvent: &b" + eventData.getType().getName());
        lore.add("");
        lore.add("&e&lLEFT-CLICK &7marks the first corner.");
        lore.add("&6&lRIGHT-CLICK &7marks the second corner.");
        lore.add("");
        lore.add("&c&lNote: &7This can't be undone.");
        return ClassImport.getClasses().getItemCreateUtil().createItem("&bCorner Marker", Material.STICK, lore);
    }

    public static ItemStack getBroadcastIntervalItem(final int broadcastInterval) {
        return GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.BROADCAST-INTERVAL").replaceAll("%broadcastInterval%", String.valueOf(broadcastInterval)).get();
    }

    public static ItemStack getWaitBeforeStartItem(final int waitBeforeStart) {
        return GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.WAIT-BEFORE-START").replaceAll("%waitBeforeStart%", String.valueOf(waitBeforeStart)).get();
    }

    public static ItemStack getMaxQueueTimeItem(int queueTime) {
        return GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.MAX-QUEUE-TIME").replaceAll("%queueTime%", String.valueOf(queueTime / 60)).get();
    }

    public static ItemStack getDurationItem(EventData eventData) {
        return switch (eventData.getType()) {
            case TNTTAG ->
                    GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.DURATION.TNTTAG").replaceAll("%explodeTime%", String.valueOf(eventData.getDuration())).get();
            case BRACKETS, SUMO ->
                    GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.DURATION.SUMO&BRACKETS").replaceAll("%roundDuration%", String.valueOf(eventData.getDuration() / 60)).get();
            default ->
                    GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.DURATION.OTHER").replaceAll("%duration%", String.valueOf(eventData.getDuration() / 60)).get();
        };
    }

    public static ItemStack getStartTimeItem(int startTime) {
        return GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.START-TIME").replaceAll("%startTime%", String.valueOf(startTime)).get();
    }

    public static ItemStack getMinPlayerItem(int minPlayer) {
        return GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.MIN-PLAYER").replaceAll("%minPlayer%", String.valueOf(minPlayer)).get();
    }

    public static ItemStack getMaxPlayerItem(int maxPlayer) {
        return GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.MAX-PLAYER").replaceAll("%maxPlayer%", String.valueOf(maxPlayer)).get();
    }

}
