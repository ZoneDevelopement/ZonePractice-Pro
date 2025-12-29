package dev.nandi0813.practice.manager.fight.event.enums;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.module.util.ClassImport;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public enum EventType {

    LMS(
            10,
            ConfigManager.getString("EVENT.LMS.NAME"),
            ClassImport.getClasses().getItemCreateUtil().createItem("&eLast Man Standing", Material.DIAMOND_SWORD),
            15,
            LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.LMS.BROADCAST"),
            ConfigManager.getList("EVENT.LMS.WINNER-COMMAND"),
            40,
            15 * 60,
            30 * 60,
            15,
            10,
            50
    ),
    OITC(
            11,
            ConfigManager.getString("EVENT.OITC.NAME"),
            ClassImport.getClasses().getItemCreateUtil().createItem("&6One In The Chamber", Material.BOW),
            15,
            LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.OITC.BROADCAST"),
            ConfigManager.getList("EVENT.OITC.WINNER-COMMAND"),
            40,
            15 * 60,
            30 * 60,
            20,
            10,
            30
    ),
    TNTTAG(
            12,
            ConfigManager.getString("EVENT.TNTTAG.NAME"),
            ClassImport.getClasses().getItemCreateUtil().createItem("&cTNT Tag", Material.TNT),
            15,
            LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.TNTTAG.BROADCAST"),
            ConfigManager.getList("EVENT.TNTTAG.WINNER-COMMAND"),
            30,
            10 * 60,
            30, // TNT Explode time
            5,
            12,
            20
    ),
    BRACKETS(
            13,
            ConfigManager.getString("EVENT.BRACKETS.NAME"),
            ClassImport.getClasses().getItemCreateUtil().createItem("&aBrackets", Material.POTION, Short.valueOf("34")),
            15,
            LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.BRACKETS.BROADCAST"),
            ConfigManager.getList("EVENT.BRACKETS.WINNER-COMMAND"),
            60,
            15 * 60,
            8 * 60,
            5,
            10,
            60
    ),
    SUMO(
            14,
            ConfigManager.getString("EVENT.SUMO.NAME"),
            ClassImport.getClasses().getItemCreateUtil().createItem("&6Sumo", Material.STICK),
            15,
            LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SUMO.BROADCAST"),
            ConfigManager.getList("EVENT.SUMO.WINNER-COMMAND"),
            45,
            10 * 60,
            3 * 60,
            3,
            20,
            100
    ),
    SPLEGG(
            15,
            ConfigManager.getString("EVENT.SPLEGG.NAME"),
            ClassImport.getClasses().getItemCreateUtil().createItem("&bSplegg", Material.EGG),
            15,
            LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.SPLEGG.BROADCAST"),
            ConfigManager.getList("EVENT.SPLEGG.WINNER-COMMAND"),
            40,
            12 * 60,
            15 * 60,
            10,
            20,
            50
    ),
    JUGGERNAUT(
            16,
            ConfigManager.getString("EVENT.JUGGERNAUT.NAME"),
            ClassImport.getClasses().getItemCreateUtil().createItem("&6Juggernaut", Material.GOLDEN_APPLE),
            15,
            LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.JUGGERNAUT.BROADCAST"),
            ConfigManager.getList("EVENT.JUGGERNAUT.WINNER-COMMAND"),
            60,
            15 * 60,
            30 * 60,
            15,
            20,
            50
    );


    private final int guiSlot;
    private final String name;
    private final ItemStack icon;
    private final int broadcastInterval;
    private final String broadcastMSG;
    private final List<String> winnerCMD;
    private final int waitBeforeStart;
    private final int maxQueueTime;
    private final int duration;
    private final int startTime;
    private final int minPlayer;
    private final int maxPlayer;

    EventType(int guiSlot, String name, ItemStack icon, int broadcastInterval, String broadcastMSG, List<String> winnerCMD, int waitBeforeStart, int maxQueueTime, int duration, int startTime, int minPlayer, int maxPlayer) {
        this.guiSlot = guiSlot;
        this.name = name;
        this.icon = icon;
        this.broadcastInterval = broadcastInterval;
        this.broadcastMSG = broadcastMSG;
        this.winnerCMD = winnerCMD;
        this.waitBeforeStart = waitBeforeStart; // Once the required player has entered the queue the countdown starts from here. (seconds)
        this.maxQueueTime = maxQueueTime; // If the event doesn't start before this time expires, the event stops. (minutes)
        this.duration = duration; // The maximum duration of the event in minutes. (minutes)
        this.startTime = startTime; // The time before the players can start killing each other.
        this.minPlayer = minPlayer; // Minimum players that needs to join for the event to start.
        this.maxPlayer = maxPlayer; // Maximum player that can join the event.
    }

}
