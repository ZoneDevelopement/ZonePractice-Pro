package dev.nandi0813.practice.manager.playerdisplay.tab;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.util.PAPIUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class TabList {

    private final Player player;

    public TabList(Player player) {
        this.player = player;
        this.setTab();
    }

    public void setTab() {
        WrapperPlayServerPlayerListHeaderAndFooter packet
                = new WrapperPlayServerPlayerListHeaderAndFooter(
                PAPIUtil.runThroughFormat(player, TabListManager.HEADER_TEXT),
                PAPIUtil.runThroughFormat(player, TabListManager.FOOTER_TEXT)
        );

        @Nullable User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user != null) {
            user.sendPacket(packet);
        } else {
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
                User delayedUser = PacketEvents.getAPI().getPlayerManager().getUser(player);
                if (delayedUser != null) {
                    delayedUser.sendPacket(packet);
                }
            }, 1L);
        }
    }

}
