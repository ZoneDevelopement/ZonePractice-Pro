package dev.nandi0813.practice.manager.fight.belowname;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.module.util.ClassImport;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BelowNameManager implements PacketListener {

    private static BelowNameManager instance;

    public static BelowNameManager getInstance() {
        if (instance == null) {
            instance = new BelowNameManager();
        }
        return instance;
    }

    private final String objectiveName = "ZPP_BELOW_NAME";
    private final Component displayName = Component.text(ChatColor.RED + "♥");

    private final Map<Player, User> registeredUsers = Collections.synchronizedMap(new HashMap<>());

    private BelowNameManager() {
        this.initIndicators();
    }

    private final Runnable hpUpdate = () -> {
        for (Map.Entry<Player, User> entry : registeredUsers.entrySet()) {
            for (Entity other : entry.getKey().getNearbyEntities(20, 20, 20)) {
                if (!(other instanceof Player otherPlayer)) {
                    continue;
                }

                double health = ClassImport.getClasses().getPlayerUtil().getPlayerHealth(otherPlayer);
                int hp = (int) Math.round(health * 10);

                Component formattedHealth = Component.text(String.format("%.1f", health));

                entry.getValue().sendPacket(new WrapperPlayServerUpdateScore(
                        otherPlayer.getName(),
                        WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                        objectiveName,
                        hp,
                        formattedHealth,
                        null
                ));
            }
        }
    };

    public void initIndicators() {
        Bukkit.getScheduler().runTaskTimer(ZonePractice.getInstance(), hpUpdate, 0, 5L);
    }

    public void initForUser(Player player) {
        if (registeredUsers.containsKey(player)) {
            return;
        }

        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        user.sendPacket(new WrapperPlayServerScoreboardObjective(
                objectiveName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                displayName,
                WrapperPlayServerScoreboardObjective.RenderType.INTEGER,
                null
        ));
        user.sendPacket(new WrapperPlayServerDisplayScoreboard(2, objectiveName));
        registeredUsers.put(player, user);
    }

    public void cleanUpForUser(Player player) {
        if (!registeredUsers.containsKey(player)) {
            return;
        }

        registeredUsers.get(player).sendPacket(new WrapperPlayServerScoreboardObjective(objectiveName, WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE, displayName, null));
        registeredUsers.remove(player);
    }
}